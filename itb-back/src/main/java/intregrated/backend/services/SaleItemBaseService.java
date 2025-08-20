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
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;

import static java.lang.module.Configuration.resolve;

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

    private SaleItemBaseByIdDto buildSaleItemDto(SaleItemBase saleItem) {
        // Get current images for this sale item
        List<SaleItemPicture> currentPictures = saleItemPictureRepo.findBySale_IdOrderByPictureOrderAsc(saleItem.getId());

        // Convert to DTOs
        List<SaleItemImageDto> imageDtos = currentPictures.stream()
                .map(pic -> new SaleItemImageDto(pic.getNewPictureName(), pic.getPictureOrder()))
                .collect(Collectors.toList());

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
                .saleItemImages(imageDtos)
                .createdOn(saleItem.getCreatedOn())
                .updatedOn(saleItem.getUpdatedOn())
                .build();
    }

    private void updateSaleItemFields(SaleItemBase existing, NewSaleItemDto newSaleItem) {
        // Update basic fields
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

        // Update brand
        if (newSaleItem.getBrand() != null && newSaleItem.getBrand().getId() != null) {
            BrandBase brand = brandBaseRepo.findById(newSaleItem.getBrand().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Brand with id " + newSaleItem.getBrand().getId() + " not found"));
            existing.setBrand(brand);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand id must be provided.");
        }
    }

    private void validateDuplicateOrders(List<SaleItemImageRequest> imageInfos) {
        if (imageInfos == null || imageInfos.isEmpty()) {
            return;
        }

        Set<Integer> seenOrders = new HashSet<>();
        List<Integer> duplicateOrders = new ArrayList<>();

        for (SaleItemImageRequest img : imageInfos) {
            Integer order = img.getOrder();
            if (order != null) {
                if (seenOrders.contains(order)) {
                    duplicateOrders.add(order);
                } else {
                    seenOrders.add(order);
                }
            }
        }

        if (!duplicateOrders.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Duplicate picture orders found: " + duplicateOrders.toString());
        }
    }

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

        // --- Update fields ---
        updateSaleItemFields(existing, newSaleItem);
        SaleItemBase savedSaleItem = saleItemBaseRepo.saveAndFlush(existing);

        // --- Handle empty imageInfos case (delete all images) ---
        if (imageInfos == null || imageInfos.isEmpty()) {
            System.out.println("=== DELETING ALL IMAGES ===");
            cleanupOldFiles(savedSaleItem.getId(), new HashSet<>());
            return buildSaleItemDto(savedSaleItem);
        }

        // --- Existing pictures ---
        List<SaleItemPicture> existingPictures =
                saleItemPictureRepo.findBySale_IdOrderByPictureOrderAsc(savedSaleItem.getId());

        Map<String, SaleItemPicture> existingByFileName = existingPictures.stream()
                .collect(Collectors.toMap(SaleItemPicture::getNewPictureName, Function.identity()));

        // --- Validate duplicate orders ---
        validateDuplicateOrders(imageInfos);

        Set<String> keepFileNames = new HashSet<>();
        Instant now = Instant.now();

        // --- Handle image actions ---
        for (SaleItemImageRequest img : imageInfos) {
            String status = img.getStatus() == null ? "" : img.getStatus().trim().toLowerCase();

            switch (status) {
                case "keep" -> handleKeep(img, existingByFileName, now, keepFileNames);
                case "move" -> handleMove(img, existingByFileName, savedSaleItem, now, keepFileNames);
                case "new"  -> handleNew(img, existingPictures, savedSaleItem, now, keepFileNames);
                default -> { /* ignore or throw error */ }
            }

            // อัปเดต existingByFileName map หลังจากการดำเนินการแต่ละครั้ง
            existingPictures = saleItemPictureRepo.findBySale_IdOrderByPictureOrderAsc(savedSaleItem.getId());
            existingByFileName = existingPictures.stream()
                    .collect(Collectors.toMap(SaleItemPicture::getNewPictureName, Function.identity()));
        }

        // --- Cleanup ---
        cleanupOldFiles(savedSaleItem.getId(), keepFileNames);

        // --- Return DTO ---
        return buildSaleItemDto(savedSaleItem);
    }

    private void handleKeep(SaleItemImageRequest img, Map<String, SaleItemPicture> existingByFileName,
                            Instant now, Set<String> keepFileNames) {

        String fileName = img.getFileName();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File name is required for keep operation");
        }

        SaleItemPicture existingPic = existingByFileName.get(fileName);
        if (existingPic == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Picture with fileName '" + fileName + "' not found for keep operation");
        }

        // ตรวจสอบว่าไฟล์จริงยังอยู่หรือไม่
        Path currentFilePath = Paths.get(fileStorageProperties.getUploadDir()).resolve(fileName);
        if (!Files.exists(currentFilePath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Physical file '" + fileName + "' not found in storage");
        }

        // เก็บไฟล์ไว้ - ไม่ต้องทำอะไร แค่ add เข้า keepFileNames
        keepFileNames.add(fileName);

        // อัปเดตเวลา updatedOn
        existingPic.setUpdatedOn(now);
        saleItemPictureRepo.save(existingPic);
    }

    private void handleNew(SaleItemImageRequest img, List<SaleItemPicture> existingPictures,
                           SaleItemBase savedSaleItem, Instant now, Set<String> keepFileNames) {

        MultipartFile file = img.getImageFile();
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Image file is required for new operation");
        }

        Integer order = img.getOrder();
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Order is required for new operation");
        }

        String originalName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalName).toLowerCase();

        // ตรวจสอบนามสกุลไฟล์
        if (!Arrays.asList(fileStorageProperties.getAllowFileTypes()).contains(extension.toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File type not allowed: " + extension);
        }

        // สร้างชื่อไฟล์ใหม่
        String newFileName = savedSaleItem.getId() + "." + order + "." + extension;
        Path targetPath = Paths.get(fileStorageProperties.getUploadDir()).resolve(newFileName);

        try {
            // บันทึกไฟล์ใหม่ (ทับอันเก่าถ้ามี)
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // หา existing picture ที่มี order เดียวกัน (ถ้ามี)
            SaleItemPicture existingPicWithSameOrder = existingPictures.stream()
                    .filter(pic -> order.equals(pic.getPictureOrder()))
                    .findFirst()
                    .orElse(null);

            if (existingPicWithSameOrder != null) {
                // อัปเดต record เดิม
                existingPicWithSameOrder.setOldPictureName(originalName);
                existingPicWithSameOrder.setNewPictureName(newFileName);
                existingPicWithSameOrder.setFileSizeBytes((int) file.getSize());
                existingPicWithSameOrder.setUpdatedOn(now);
                saleItemPictureRepo.save(existingPicWithSameOrder);
            } else {
                // สร้าง record ใหม่
                SaleItemPicture newPicture = new SaleItemPicture();
                newPicture.setSale(savedSaleItem);
                newPicture.setOldPictureName(originalName);
                newPicture.setNewPictureName(newFileName);
                newPicture.setFileSizeBytes((int) file.getSize());
                newPicture.setPictureOrder(order);
                newPicture.setCreatedOn(now);
                newPicture.setUpdatedOn(now);
                saleItemPictureRepo.save(newPicture);
            }

            // เพิ่มชื่อไฟล์ใหม่เข้า keepFileNames
            keepFileNames.add(newFileName);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not save new file: " + newFileName);
        }
    }

    private void handleMove(SaleItemImageRequest img, Map<String, SaleItemPicture> existingByFileName,
                            SaleItemBase savedSaleItem, Instant now, Set<String> keepFileNames) {

        String fileName = img.getFileName();
        Integer newOrder = img.getOrder();

        System.out.println("=== HANDLE MOVE DEBUG ===");
        System.out.println("Original fileName: " + fileName);
        System.out.println("New order: " + newOrder);
        System.out.println("SaleItem ID: " + savedSaleItem.getId());

        if (fileName == null || fileName.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "File name is required for move operation");
        }

        SaleItemPicture existingPic = existingByFileName.get(fileName);
        if (existingPic == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Picture with fileName '" + fileName + "' not found for move operation");
        }

        if (newOrder == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "New order is required for move operation");
        }

        System.out.println("Current order: " + existingPic.getPictureOrder());
        System.out.println("Requested new order: " + newOrder);

        // ถ้า order ใหม่เหมือนเดิม ก็แค่เก็บไฟล์ไว้
        if (newOrder.equals(existingPic.getPictureOrder())) {
            System.out.println("Order unchanged, keeping file: " + fileName);
            keepFileNames.add(fileName);
            existingPic.setUpdatedOn(now);
            saleItemPictureRepo.save(existingPic);
            return;
        }

        Path uploadDir = Paths.get(fileStorageProperties.getUploadDir());
        Path currentFilePath = uploadDir.resolve(fileName);

        System.out.println("Current file path: " + currentFilePath);
        System.out.println("File exists before move: " + Files.exists(currentFilePath));

        if (!Files.exists(currentFilePath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Physical file '" + fileName + "' not found in storage");
        }

        try {
            // หา picture record ที่อยู่ใน order ปลายทางที่เราต้องการย้ายไป
            SaleItemPicture targetPicture = existingByFileName.values().stream()
                    .filter(pic -> newOrder.equals(pic.getPictureOrder()) && !pic.equals(existingPic))
                    .findFirst()
                    .orElse(null);

            System.out.println("Target picture found: " + (targetPicture != null));
            if (targetPicture != null) {
                System.out.println("Target picture details - fileName: " + targetPicture.getNewPictureName() + ", order: " + targetPicture.getPictureOrder());
            }

            // ถ้ามี record อื่นอยู่ใน order ปลายทางแล้ว ต้องทำ swap
            if (targetPicture != null) {
                System.out.println("Target order occupied, performing swap operation");
                System.out.println("This will swap orders: " + fileName + " (order " + existingPic.getPictureOrder() + ") ↔ " + targetPicture.getNewPictureName() + " (order " + targetPicture.getPictureOrder() + ")");

                // เก็บค่าเดิมก่อนเปลี่ยน
                Integer sourceOldOrder = existingPic.getPictureOrder();
                Integer targetOldOrder = targetPicture.getPictureOrder();
                String sourceFileName = existingPic.getNewPictureName();
                String targetFileName = targetPicture.getNewPictureName();

                // สร้างชื่อไฟล์ใหม่สำหรับทั้งคู่
                String extension1 = FilenameUtils.getExtension(sourceFileName);
                String extension2 = FilenameUtils.getExtension(targetFileName);
                String newSourceFileName = savedSaleItem.getId() + "." + targetOldOrder + "." + extension1;
                String newTargetFileName = savedSaleItem.getId() + "." + sourceOldOrder + "." + extension2;

                Path sourceFilePath = uploadDir.resolve(sourceFileName);
                Path targetFilePath = uploadDir.resolve(targetFileName);
                Path newSourceFilePath = uploadDir.resolve(newSourceFileName);
                Path newTargetFilePath = uploadDir.resolve(newTargetFileName);

                System.out.println("File operations:");
                System.out.println("  " + sourceFileName + " -> " + newSourceFileName);
                System.out.println("  " + targetFileName + " -> " + newTargetFileName);

                // ใช้ temp files เพื่อป้องกัน collision
                String tempFileName1 = "temp_swap1_" + System.nanoTime() + "_" + System.currentTimeMillis();
                String tempFileName2 = "temp_swap2_" + System.nanoTime() + "_" + System.currentTimeMillis();
                Path tempFilePath1 = uploadDir.resolve(tempFileName1);
                Path tempFilePath2 = uploadDir.resolve(tempFileName2);

                // Step 1: Move both files to temp locations
                System.out.println("Step 1a: Move " + sourceFileName + " to temp: " + tempFileName1);
                Files.move(sourceFilePath, tempFilePath1);

                System.out.println("Step 1b: Move " + targetFileName + " to temp: " + tempFileName2);
                Files.move(targetFilePath, tempFilePath2);

                // Step 2: Move temp files to final destinations
                System.out.println("Step 2a: Move temp to final: " + tempFileName1 + " -> " + newSourceFileName);
                Files.move(tempFilePath1, newSourceFilePath);

                System.out.println("Step 2b: Move temp to final: " + tempFileName2 + " -> " + newTargetFileName);
                Files.move(tempFilePath2, newTargetFilePath);

                // Verify files exist after move
                if (!Files.exists(newSourceFilePath)) {
                    throw new IOException("Source file move failed - " + newSourceFileName + " does not exist");
                }
                if (!Files.exists(newTargetFilePath)) {
                    throw new IOException("Target file move failed - " + newTargetFileName + " does not exist");
                }

                // อัปเดต database records
                System.out.println("Updating both database records for swap");

                existingPic.setNewPictureName(newSourceFileName);
                existingPic.setPictureOrder(targetOldOrder);
                existingPic.setUpdatedOn(now);
                saleItemPictureRepo.save(existingPic);

                targetPicture.setNewPictureName(newTargetFileName);
                targetPicture.setPictureOrder(sourceOldOrder);
                targetPicture.setUpdatedOn(now);
                saleItemPictureRepo.save(targetPicture);

                // เพิ่มทั้งสองไฟล์เข้า keepFileNames
                keepFileNames.add(newSourceFileName);
                keepFileNames.add(newTargetFileName);

                System.out.println("Swap completed successfully - both files and DB records updated");
                System.out.println("Final files: " + newSourceFileName + ", " + newTargetFileName);

            } else {
                // ไม่มี record อื่นใน order ปลายทาง ทำ rename ธรรมดา
                String extension = FilenameUtils.getExtension(fileName);
                String newFileName = savedSaleItem.getId() + "." + newOrder + "." + extension;
                Path newFilePath = uploadDir.resolve(newFileName);

                System.out.println("Simple rename from " + currentFilePath + " to " + newFilePath);
                Files.move(currentFilePath, newFilePath);

                // Verify file exists after move
                if (!Files.exists(newFilePath)) {
                    throw new IOException("File move failed - " + newFileName + " does not exist");
                }

                // อัปเดตข้อมูลในฐานข้อมูล
                System.out.println("Updating database record");
                existingPic.setNewPictureName(newFileName);
                existingPic.setPictureOrder(newOrder);
                existingPic.setUpdatedOn(now);
                saleItemPictureRepo.save(existingPic);

                // เพิ่มชื่อไฟล์ใหม่เข้า keepFileNames
                keepFileNames.add(newFileName);
                System.out.println("Simple move completed - file: " + newFileName);
            }

            System.out.println("File operation completed successfully");
            System.out.println("Current keepFileNames: " + keepFileNames);
            System.out.println("=== MOVE COMPLETED SUCCESSFULLY ===");

        } catch (IOException e) {
            System.out.println("ERROR during file move: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not move file from '" + fileName + "': " + e.getMessage());
        }
    }

    private void cleanupOldFiles(Integer saleItemId, Set<String> keepFileNames) {
        System.out.println("=== CLEANUP DEBUG ===");
        System.out.println("SaleItem ID: " + saleItemId);
        System.out.println("Files to keep: " + keepFileNames);

        try {
            // หา records ทั้งหมดของ saleItem นี้
            List<SaleItemPicture> allPictures = saleItemPictureRepo.findBySale_IdOrderByPictureOrderAsc(saleItemId);
            System.out.println("Total pictures in DB: " + allPictures.size());

            // หาไฟล์ที่ต้องลบ
            List<SaleItemPicture> picturesToDelete = allPictures.stream()
                    .filter(pic -> !keepFileNames.contains(pic.getNewPictureName()))
                    .collect(Collectors.toList());

            System.out.println("Pictures to delete: " + picturesToDelete.size());
            picturesToDelete.forEach(pic -> System.out.println("  - " + pic.getNewPictureName()));

            // ลบไฟล์จริงก่อน
            Path uploadDir = Paths.get(fileStorageProperties.getUploadDir());
            for (SaleItemPicture pic : picturesToDelete) {
                Path filePath = uploadDir.resolve(pic.getNewPictureName());
                System.out.println("Checking file for deletion: " + pic.getNewPictureName());
                System.out.println("File exists: " + Files.exists(filePath));

                if (Files.exists(filePath)) {
                    try {
                        Files.delete(filePath);
                        System.out.println("Deleted file: " + pic.getNewPictureName());
                    } catch (IOException e) {
                        System.err.println("WARNING: Could not delete file: " + pic.getNewPictureName());
                        e.printStackTrace();
                    }
                }
            }

            // ลบ records ในฐานข้อมูล
            if (!picturesToDelete.isEmpty()) {
                saleItemPictureRepo.deleteAll(picturesToDelete);
                System.out.println("Deleted " + picturesToDelete.size() + " records from database");
            }

            // ตรวจสอบไฟล์ที่เหลืออยู่
            System.out.println("=== FILES AFTER CLEANUP ===");
            try {
                String filePrefix = saleItemId + ".";
                Files.list(uploadDir)
                        .filter(path -> path.getFileName().toString().startsWith(filePrefix))
                        .forEach(path -> System.out.println("Remaining file: " + path.getFileName()));
            } catch (IOException e) {
                System.out.println("Could not list remaining files");
            }

            System.out.println("=== CLEANUP COMPLETED ===");

        } catch (Exception e) {
            System.out.println("ERROR during cleanup: " + e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error during file cleanup: " + e.getMessage());
        }
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
