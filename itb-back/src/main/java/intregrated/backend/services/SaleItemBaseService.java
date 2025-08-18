package intregrated.backend.services;

import intregrated.backend.FileStorageProperties;
import intregrated.backend.dtos.*;
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
import java.util.stream.Collectors;

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



    @Transactional
    public SaleItemBaseByIdDto getSaleItemBaseRepoById(Integer id) {
        SaleItemBase saleItem = saleItemBaseRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "SaleItemBase with id: " + id + " is not found"));

        List<SaleItemImageDto> imageDtos = saleItemPictureRepo
                .findBySale_IdOrderByPictureOrderAsc(id)
                .stream()
                .map(pic -> new SaleItemImageDto(pic.getNewPictureName(), pic.getPictureOrder()))
                .toList();

        // Map entity → DTO
        return SaleItemBaseByIdDto.builder()
                .id(saleItem.getId())
                .model(saleItem.getModel())
                .brandName(saleItem.getBrand() != null ? saleItem.getBrand().getName() : null)
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
                .saleItemImages(imageDtos)
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




    // @Transactional
    // public SaleItemBaseByIdDto editSaleItem(Integer id, NewSaleItemDto newSaleItem, List<ImageInfoDto> imageInfos) {
    //     SaleItemBase existing = saleItemBaseRepo.findById(id)
    //             .orElseThrow(() -> new ResponseStatusException(
    //                     HttpStatus.NOT_FOUND,
    //                     "SaleItem with id " + id + " not found"));

    //     // --- อัปเดตข้อมูลสินค้า ---
    //     existing.setModel(newSaleItem.getModel() != null ? newSaleItem.getModel().trim() : null);
    //     existing.setDescription(newSaleItem.getDescription() != null ? newSaleItem.getDescription().trim() : null);
    //     existing.setPrice(newSaleItem.getPrice());
    //     existing.setRamGb(newSaleItem.getRamGb());
    //     existing.setStorageGb(newSaleItem.getStorageGb());
    //     existing.setScreenSizeInch(newSaleItem.getScreenSizeInch() != null
    //             ? BigDecimal.valueOf(newSaleItem.getScreenSizeInch())
    //             : null);
    //     existing.setQuantity((newSaleItem.getQuantity() == null || newSaleItem.getQuantity() < 0)
    //             ? 1
    //             : newSaleItem.getQuantity());
    //     existing.setColor((newSaleItem.getColor() == null || newSaleItem.getColor().trim().isEmpty())
    //             ? null
    //             : newSaleItem.getColor().trim());
    //     existing.setUpdatedOn(Instant.now());

    //     // --- อัปเดต brand ---
    //     if (newSaleItem.getBrand() != null && newSaleItem.getBrand().getId() != null) {
    //         BrandBase brand = brandBaseRepo.findById(newSaleItem.getBrand().getId())
    //                 .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
    //                         "Brand with id " + newSaleItem.getBrand().getId() + " not found"));
    //         existing.setBrand(brand);
    //     } else {
    //         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand id must be provided.");
    //     }

    //     SaleItemBase savedSaleItem = saleItemBaseRepo.saveAndFlush(existing);

    //     // --- จัดการรูป ---
    //     List<SaleItemImageDto> imageDtos = new ArrayList<>();

    //     if (imageInfos != null && !imageInfos.isEmpty()) {
    //         // ลบรูปเก่าก่อน
    //         saleItemPictureRepo.deleteBySaleItemId(savedSaleItem.getId());

    //         for (ImageInfoDto img : imageInfos) {
    //             MultipartFile file = img.getImageFile();
    //             if (file != null && !file.isEmpty()) {
    //                 String originalName = file.getOriginalFilename();
    //                 String extension = FilenameUtils.getExtension(originalName).toLowerCase();

    //                 // ตรวจสอบนามสกุลไฟล์
    //                 if (!Arrays.asList(fileStorageProperties.getAllowFileTypes()).contains(extension.toUpperCase())) {
    //                     throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
    //                             "File type not allowed: " + extension);
    //                 }

    //                 // ✅ ตั้งชื่อไฟล์ใหม่ เช่น 86.1.jpg
    //                 String newFileName = savedSaleItem.getId() + "." + img.getOrder() + "." + extension;
    //                 Path targetPath = Paths.get(fileStorageProperties.getUploadDir()).resolve(newFileName);

    //                 try {
    //                     Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    //                 } catch (IOException e) {
    //                     throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save file");
    //                 }

    //                 // บันทึกลง DB
    //                 SaleItemPicture picEntity = new SaleItemPicture();
    //                 picEntity.setSale(savedSaleItem);
    //                 picEntity.setOldPictureName(originalName);
    //                 picEntity.setNewPictureName(newFileName);
    //                 picEntity.setFileSizeBytes((int) file.getSize());
    //                 picEntity.setPictureOrder(img.getOrder());
    //                 picEntity.setCreatedOn(Instant.now());
    //                 picEntity.setUpdatedOn(Instant.now());
    //                 saleItemPictureRepo.save(picEntity);

    //                 // เพิ่มเข้า DTO
    //                 imageDtos.add(new SaleItemImageDto(newFileName, img.getOrder()));
    //             }
    //         }
    //     }

    //     // --- Return DTO ---
    //     return SaleItemBaseByIdDto.builder()
    //             .id(savedSaleItem.getId())
    //             .model(savedSaleItem.getModel())
    //             .brandName(savedSaleItem.getBrand().getName())
    //             .description(savedSaleItem.getDescription())
    //             .price(savedSaleItem.getPrice())
    //             .ramGb(savedSaleItem.getRamGb())
    //             .screenSizeInch(savedSaleItem.getScreenSizeInch() != null
    //                     ? savedSaleItem.getScreenSizeInch().doubleValue()
    //                     : null)
    //             .quantity(savedSaleItem.getQuantity())
    //             .storageGb(savedSaleItem.getStorageGb())
    //             .color(savedSaleItem.getColor())
    //             .saleItemImages(imageDtos)
    //             .createdOn(savedSaleItem.getCreatedOn())
    //             .updatedOn(savedSaleItem.getUpdatedOn())
    //             .build();
    // }

    @Transactional
    public SaleItemBaseByIdDto editSaleItem(Integer id, SaleItemWithImageInfo request) {
        // --- Extract incoming data ---
        NewSaleItemDto newSaleItem = request.getSaleItem();
        List<SaleItemImageRequest> imageInfos = request.getImageInfos();

        // --- หา SaleItem ที่จะอัปเดต ---
        SaleItemBase existing = saleItemBaseRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "SaleItem with id " + id + " not found"));

        // --- อัปเดตข้อมูลสินค้า ---
        existing.setModel(newSaleItem.getModel() != null ? newSaleItem.getModel().trim() : null);
        existing.setDescription(newSaleItem.getDescription() != null ? newSaleItem.getDescription().trim() : null);
        existing.setPrice(newSaleItem.getPrice());
        existing.setRamGb(newSaleItem.getRamGb());
        existing.setStorageGb(newSaleItem.getStorageGb());
        existing.setScreenSizeInch(newSaleItem.getScreenSizeInch() != null
                ? BigDecimal.valueOf(newSaleItem.getScreenSizeInch())
                : null);
        existing.setQuantity((newSaleItem.getQuantity() == null || newSaleItem.getQuantity() < 0)
                ? 1
                : newSaleItem.getQuantity());
        existing.setColor((newSaleItem.getColor() == null || newSaleItem.getColor().trim().isEmpty())
                ? null
                : newSaleItem.getColor().trim());
        existing.setUpdatedOn(Instant.now());

        // --- อัปเดต brand ---
        if (newSaleItem.getBrand() != null && newSaleItem.getBrand().getId() != null) {
            BrandBase brand = brandBaseRepo.findById(newSaleItem.getBrand().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Brand with id " + newSaleItem.getBrand().getId() + " not found"));
            existing.setBrand(brand);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand id must be provided.");
        }

        // --- บันทึกสินค้า ---
        SaleItemBase savedSaleItem = saleItemBaseRepo.saveAndFlush(existing);

        // --- จัดการรูป ---
        List<SaleItemImageDto> imageDtos = new ArrayList<>();

        if (imageInfos != null && !imageInfos.isEmpty()) {
            // ลบรูปเก่าออกก่อน
            saleItemPictureRepo.deleteBySaleItemId(savedSaleItem.getId());

            for (SaleItemImageRequest img : imageInfos) {
                MultipartFile file = img.getImageFile();
                if (file != null && !file.isEmpty()) {
                    String originalName = file.getOriginalFilename();
                    String extension = FilenameUtils.getExtension(originalName).toLowerCase();

                    // ตรวจสอบชนิดไฟล์ (normalize ทั้งสองด้าน)
                    boolean allowed = Arrays.stream(fileStorageProperties.getAllowFileTypes())
                            .map(String::toLowerCase)
                            .toList()
                            .contains(extension);

                    if (!allowed) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "File type not allowed: " + extension);
                    }

                    // ตั้งชื่อไฟล์ใหม่ เช่น 86.1.jpg
                    String newFileName = savedSaleItem.getId() + "." + img.getOrder() + "." + extension;
                    Path targetPath = Paths.get(fileStorageProperties.getUploadDir()).resolve(newFileName);

                    try {
                        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save file");
                    }

                    // บันทึกลง DB
                    SaleItemPicture picEntity = new SaleItemPicture();
                    picEntity.setSale(savedSaleItem);
                    picEntity.setOldPictureName(originalName);
                    picEntity.setNewPictureName(newFileName);
                    picEntity.setFileSizeBytes((int) file.getSize());
                    picEntity.setPictureOrder(img.getOrder());
                    picEntity.setCreatedOn(Instant.now());
                    picEntity.setUpdatedOn(Instant.now());
                    saleItemPictureRepo.save(picEntity);

                    // เพิ่มเข้า response DTO
                    imageDtos.add(new SaleItemImageDto(newFileName, img.getOrder()));
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
                .saleItemImages(imageDtos) // use response DTO not request DTO
                .createdOn(savedSaleItem.getCreatedOn())
                .updatedOn(savedSaleItem.getUpdatedOn())
                .build();
    }




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
