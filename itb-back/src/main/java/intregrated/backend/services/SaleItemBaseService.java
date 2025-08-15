package intregrated.backend.services;

import intregrated.backend.FileStorageProperties;
import intregrated.backend.dtos.NewSaleItemDto;
import intregrated.backend.dtos.SaleItemBaseByIdDto;
import intregrated.backend.dtos.SaleItemImageDto;
import intregrated.backend.entities.BrandBase;
import intregrated.backend.entities.SaleItemBase;
import intregrated.backend.entities.SaleItemPicture;
import intregrated.backend.repositories.BrandBaseRepo;
import intregrated.backend.repositories.SaleItemBaseRepo;
import intregrated.backend.repositories.SaleItemPictureRepo;
//import jakarta.transaction.Transactional;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleItemBaseService {
    @Autowired
    private SaleItemBaseRepo saleItemBaseRepo;

    @Autowired
    private BrandBaseRepo brandBaseRepo;


    @Autowired
    private SaleItemPictureRepo saleItemPictureRepo;

    @Autowired
    private FileStorageProperties fileStorageProperties;



    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<SaleItemBase> getAllSaleItemBase() {
        return saleItemBaseRepo.findAll();
    }

    @Transactional(readOnly = true)
    public SaleItemBaseByIdDto getSaleItemBaseRepoById(Integer id) {
        SaleItemBase saleItem = saleItemBaseRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "SaleItemBase with id: " + id + " is not found"));

        List<SaleItemImageDto> imageDtos = saleItemPictureRepo
                .findBySale_IdOrderByPictureOrderAsc(id)
                .stream()
                .map(pic -> new SaleItemImageDto(
                        pic.getNewPictureName(),
                        pic.getPictureOrder()
                ))
                .toList();

        return SaleItemBaseByIdDto.builder()
                .id(saleItem.getId())
                .model(saleItem.getModel())
                .brandName(saleItem.getBrand().getName())
                .description(saleItem.getDescription())
                .price(saleItem.getPrice())
                .ramGb(saleItem.getRamGb())
                .screenSizeInch(saleItem.getScreenSizeInch() != null
                        ? saleItem.getScreenSizeInch().doubleValue()
                        : null)
                .quantity(saleItem.getQuantity())
                .storageGb(saleItem.getStorageGb())
                .color(saleItem.getColor())
                .createdOn(saleItem.getCreatedOn())
                .updatedOn(saleItem.getUpdatedOn())
                .saleItemImages(imageDtos) // ✅ ดึงรูปมาใส่
                .build();
    }

    @Transactional
    public SaleItemBaseByIdDto createSaleItem(NewSaleItemDto newSaleItem, MultipartFile[] pictures) {
        // --- ตรวจสอบ brand ---
        BrandBase brand;
        if (newSaleItem.getBrand() != null) {
            if (newSaleItem.getBrand().getId() != null) {
                brand = brandBaseRepo.findById(newSaleItem.getBrand().getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Brand with id " + newSaleItem.getBrand().getId() + " not found"));
            } else if (newSaleItem.getBrand().getBrandName() != null
                    && !newSaleItem.getBrand().getBrandName().trim().isEmpty()) {
                String trimmedBrandName = newSaleItem.getBrand().getBrandName().trim();
                brand = brandBaseRepo.findByNameIgnoreCase(trimmedBrandName)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Brand with name \"" + trimmedBrandName + "\" not found"));
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Brand must contain either valid ID or name.");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand is required.");
        }

        // --- บันทึก SaleItemBase ---
        SaleItemBase saleItem = new SaleItemBase();
        saleItem.setModel(newSaleItem.getModel() != null ? newSaleItem.getModel().trim() : null);
        saleItem.setDescription(newSaleItem.getDescription() != null ? newSaleItem.getDescription().trim() : null);
        saleItem.setPrice(newSaleItem.getPrice());
        saleItem.setRamGb(newSaleItem.getRamGb());
        saleItem.setScreenSizeInch(
                newSaleItem.getScreenSizeInch() != null ? BigDecimal.valueOf(newSaleItem.getScreenSizeInch()) : null);
        saleItem.setQuantity((newSaleItem.getQuantity() == null || newSaleItem.getQuantity() < 0)
                ? 1
                : newSaleItem.getQuantity());
        saleItem.setStorageGb(newSaleItem.getStorageGb());
        saleItem.setColor(
                (newSaleItem.getColor() == null || newSaleItem.getColor().trim().isEmpty())
                        ? null
                        : newSaleItem.getColor().trim());
        saleItem.setCreatedOn(Instant.now());
        saleItem.setUpdatedOn(Instant.now());
        saleItem.setBrand(brand);

        SaleItemBase savedSaleItem = saleItemBaseRepo.saveAndFlush(saleItem);

        // --- เก็บรูป ---
        List<SaleItemImageDto> imageDtos = new ArrayList<>();
        if (pictures != null && pictures.length > 0) {
            int order = 1;
            for (MultipartFile picture : pictures) {
                if (!picture.isEmpty()) {
                    String originalName = picture.getOriginalFilename();
                    String extension = FilenameUtils.getExtension(originalName).toLowerCase();

                    // ตรวจสอบนามสกุลไฟล์
                    if (!Arrays.asList(fileStorageProperties.getAllowFileTypes()).contains(extension.toUpperCase())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File type not allowed: " + extension);
                    }

                    // สร้างชื่อไฟล์ใหม่ เช่น 86.1.jpg
                    String newFileName = savedSaleItem.getId() + "." + order + "." + extension;
                    Path targetPath = Paths.get(fileStorageProperties.getUploadDir()).resolve(newFileName);

                    try {
                        Files.copy(picture.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save file");
                    }

                    // บันทึกลง DB
                    SaleItemPicture picEntity = new SaleItemPicture();
                    picEntity.setSale(savedSaleItem);
                    picEntity.setOldPictureName(originalName);
                    picEntity.setNewPictureName(newFileName);
                    picEntity.setFileSizeBytes((int) picture.getSize());
                    picEntity.setPictureOrder(order);
                    picEntity.setCreatedOn(Instant.now());
                    picEntity.setUpdatedOn(Instant.now());
                    saleItemPictureRepo.save(picEntity);

                    // เพิ่มเข้า DTO
                    imageDtos.add(new SaleItemImageDto(newFileName, order));

                    order++;
                }
            }
        }

        // --- Return DTO ---
        return SaleItemBaseByIdDto.builder()
                .id(savedSaleItem.getId())
                .model(savedSaleItem.getModel())
                .brandName(savedSaleItem.getBrand().getName())
                .description(savedSaleItem.getDescription())
                .price(savedSaleItem.getPrice())
                .ramGb(savedSaleItem.getRamGb())
                .screenSizeInch(savedSaleItem.getScreenSizeInch() != null
                        ? savedSaleItem.getScreenSizeInch().doubleValue()
                        : null)
                .quantity(savedSaleItem.getQuantity())
                .storageGb(savedSaleItem.getStorageGb())
                .color(savedSaleItem.getColor())
                .saleItemImages(imageDtos) // ✅ ส่ง list ของรูป
                .createdOn(savedSaleItem.getCreatedOn())
                .updatedOn(savedSaleItem.getUpdatedOn())
                .build();
    }




    public SaleItemBaseByIdDto editSaleItem(Integer id, NewSaleItemDto newSaleItem) {
        SaleItemBase existing = saleItemBaseRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "SaleItem with id " + id + " not found"));

        existing.setModel(newSaleItem.getModel().trim());
        existing.setDescription(newSaleItem.getDescription().trim());
        existing.setPrice(newSaleItem.getPrice());
        existing.setRamGb(newSaleItem.getRamGb() != null ? newSaleItem.getRamGb() : null);
        existing.setStorageGb(newSaleItem.getStorageGb() != null ? newSaleItem.getStorageGb() : null);
        existing.setScreenSizeInch(
                newSaleItem.getScreenSizeInch() != null ? BigDecimal.valueOf(newSaleItem.getScreenSizeInch()) : null);
        if (newSaleItem.getQuantity() == null || newSaleItem.getQuantity() < 0) {
            existing.setQuantity(1);
        } else {
            existing.setQuantity(newSaleItem.getQuantity());
        }
        if (newSaleItem.getColor() == null || newSaleItem.getColor().trim().isEmpty()) {
            existing.setColor(null);
        } else {
            existing.setColor(newSaleItem.getColor().trim());
        }
        existing.setUpdatedOn(Instant.now());

        if (newSaleItem.getBrand() != null && newSaleItem.getBrand().getId() != null) {
            BrandBase brand = brandBaseRepo.findById(newSaleItem.getBrand().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Brand with id " + newSaleItem.getBrand().getId() + " not found"));
            existing.setBrand(brand);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Brand id must be provided.");
        }

        SaleItemBase saved = saleItemBaseRepo.saveAndFlush(existing);

        return SaleItemBaseByIdDto.builder()
                .id(saved.getId())
                .model(saved.getModel().trim())
                .brandName(saved.getBrand().getName())
                .description(saved.getDescription().trim())
                .price(saved.getPrice())
                .ramGb(saved.getRamGb())
                .screenSizeInch(saved.getScreenSizeInch() != null ? saved.getScreenSizeInch().doubleValue() : null)
                .quantity(saved.getQuantity())
                .storageGb(saved.getStorageGb())
                .color(saved.getColor())
                .createdOn(saved.getCreatedOn())
                .updatedOn(saved.getUpdatedOn())
                .build();
    }

//    public SaleItemBaseByIdDto editSaleItem(Integer id, NewSaleItemDto newSaleItem, MultipartFile[] imageInfos) {
//        SaleItemBase existing = saleItemBaseRepo.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(
//                        HttpStatus.NOT_FOUND,
//                        "SaleItem with id " + id + " not found"));
//
//        // อัปเดตข้อมูลสินค้าเหมือนเดิม
//        existing.setModel(newSaleItem.getModel().trim());
//        existing.setDescription(newSaleItem.getDescription().trim());
//        existing.setPrice(newSaleItem.getPrice());
//        existing.setRamGb(newSaleItem.getRamGb());
//        existing.setStorageGb(newSaleItem.getStorageGb());
//        existing.setScreenSizeInch(newSaleItem.getScreenSizeInch() != null ? BigDecimal.valueOf(newSaleItem.getScreenSizeInch()) : null);
//        existing.setQuantity(newSaleItem.getQuantity() == null || newSaleItem.getQuantity() < 0 ? 1 : newSaleItem.getQuantity());
//        existing.setColor((newSaleItem.getColor() == null || newSaleItem.getColor().trim().isEmpty()) ? null : newSaleItem.getColor().trim());
//        existing.setUpdatedOn(Instant.now());
//
//        // อัปเดต brand
//        if (newSaleItem.getBrand() != null && newSaleItem.getBrand().getId() != null) {
//            BrandBase brand = brandBaseRepo.findById(newSaleItem.getBrand().getId())
//                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
//                            "Brand with id " + newSaleItem.getBrand().getId() + " not found"));
//            existing.setBrand(brand);
//        } else {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand id must be provided.");
//        }
//
//        if (imageInfos != null && imageInfos.length > 0) {
//            // ลบรูปเก่าของสินค้านี้ก่อน
//            saleItemPictureRepo.deleteBySaleItemId(existing.getId());
//
//            // เพิ่มรูปใหม่
//            for (int i = 0; i < imageInfos.length; i++) {
//                MultipartFile file = imageInfos[i];
//
//                SaleItemPicture picture = new SaleItemPicture();
//                picture.setSale(existing); // setSale() เพราะ field ชื่อ sale
//
//                // เก็บชื่อไฟล์เก่า (original name)
//                picture.setOldPictureName(file.getOriginalFilename());
//
//                // สร้างชื่อไฟล์ใหม่กันซ้ำ เช่น ใช้ timestamp นำหน้า
//                picture.setNewPictureName(System.currentTimeMillis() + "_" + file.getOriginalFilename());
//
//                // เก็บขนาดไฟล์ (เป็น byte)
//                picture.setFileSizeBytes((int) file.getSize());
//
//                // กำหนดลำดับของรูป (เริ่มจาก 1)
//                picture.setPictureOrder(i + 1);
//
//                // วันที่สร้างและแก้ไข
//                picture.setCreatedOn(Instant.now());
//                picture.setUpdatedOn(Instant.now());
//
//                saleItemPictureRepo.save(picture);
//
//                // ✅ ถ้าต้องเก็บไฟล์จริงในโฟลเดอร์
//                // Path path = Paths.get("/path/to/images/" + picture.getNewPictureName());
//                // Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
//            }
//        }
//
//
//        SaleItemBase saved = saleItemBaseRepo.saveAndFlush(existing);
//
//        return SaleItemBaseByIdDto.builder()
//                .id(saved.getId())
//                .model(saved.getModel().trim())
//                .brandName(saved.getBrand().getName())
//                .description(saved.getDescription().trim())
//                .price(saved.getPrice())
//                .ramGb(saved.getRamGb())
//                .screenSizeInch(saved.getScreenSizeInch() != null ? saved.getScreenSizeInch().doubleValue() : null)
//                .quantity(saved.getQuantity())
//                .storageGb(saved.getStorageGb())
//                .color(saved.getColor())
//                .createdOn(saved.getCreatedOn())
//                .updatedOn(saved.getUpdatedOn())
//                .build();
//    }


//    public void deleteSaleItem(Integer id) {
//        if (!saleItemBaseRepo.existsById(id)) {
//            throw new ResponseStatusException(
//                    HttpStatus.NOT_FOUND,
//                    "SaleItem with id " + id + " not found");
//        }
//        saleItemBaseRepo.deleteById(id);
//    }

    @Transactional
    public void deleteSaleItem(Integer id) {
        SaleItemBase saleItem = saleItemBaseRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "SaleItem with id " + id + " not found"
                ));

        // โหลดรูปถ้า lazy
        List<SaleItemPicture> pictures = saleItem.getPictures();
        pictures.size(); // บังคับโหลด

        // ลบไฟล์รูป
        for (SaleItemPicture pic : pictures) {
            if (pic.getNewPictureName() != null && !pic.getNewPictureName().isBlank()) {
                Path filePath = Paths.get(uploadDir, pic.getNewPictureName());
                try {
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    System.err.println("Failed to delete file: " + filePath);
                    e.printStackTrace();
                }
            }
        }

        // ลบ SaleItemBase → cascade ลบ SaleItemPicture
        saleItemBaseRepo.delete(saleItem);
    }




public Page<SaleItemBaseByIdDto> getPagedSaleItems(
        List<String> filterBrands,
        List<Integer> filterStorages,
        boolean filterStorageIsNull,
        Integer filterPriceLower,
        Integer filterPriceUpper,
        Integer page,
        Integer size,
        String sortField,
        String sortDirection) {

    Sort sort = "brand.name".equals(sortField)
            ? Sort.by(Sort.Direction.fromString(sortDirection), "brand.name")
            : Sort.by(Sort.Direction.fromString(sortDirection), sortField);

    Pageable pageable = PageRequest.of(page, size, sort);

    List<String> lowerBrands = (filterBrands == null || filterBrands.isEmpty())
            ? null
            : filterBrands.stream().map(String::toLowerCase).toList();

    Integer lower = (filterPriceLower != null && filterPriceUpper != null) ? filterPriceLower : null;
    Integer upper = (filterPriceLower != null && filterPriceUpper != null) ? filterPriceUpper : null;

    Page<SaleItemBase> result = saleItemBaseRepo.findWithFilters(
            lowerBrands, filterStorages, filterStorageIsNull, lower, upper, pageable
    );

    return result.map(this::mapToDto);
}

    private SaleItemBaseByIdDto mapToDto(SaleItemBase s) {
        return SaleItemBaseByIdDto.builder()
                .id(s.getId())
                .model(s.getModel())
                .brandName(s.getBrand() != null ? s.getBrand().getName() : null)
                .description(s.getDescription())
                .price(s.getPrice())
                .ramGb(s.getRamGb())
                .screenSizeInch(s.getScreenSizeInch() != null ? s.getScreenSizeInch().doubleValue() : null)
                .quantity(s.getQuantity())
                .storageGb(s.getStorageGb())
                .color(s.getColor())
                .createdOn(s.getCreatedOn())
                .updatedOn(s.getUpdatedOn())
                .build();
    }

}
