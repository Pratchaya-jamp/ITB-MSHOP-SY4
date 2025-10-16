package intregrated.backend.services;

import intregrated.backend.dtos.saleItems.*;
import intregrated.backend.entities.accounts.SellerAccount;
import intregrated.backend.fileproperties.ProductFileProperties;
import intregrated.backend.entities.brands.BrandBase;
import intregrated.backend.entities.saleitems.SaleItemBase;
import intregrated.backend.entities.saleitems.SaleItemPicture;
import intregrated.backend.repositories.BrandBaseRepo;
import intregrated.backend.repositories.SaleItemBaseRepo;
import intregrated.backend.repositories.SaleItemPictureRepo;
//import jakarta.transaction.Transactional;
import intregrated.backend.repositories.SellerAccountRepo;
import intregrated.backend.utils.ListMapper;
import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private SellerAccountRepo sellerAccountRepo;
    @Autowired
    private ProductFileProperties fileStorageProperties;

    @Autowired
    @Qualifier("productFileService")
    private FileService productFileService;


    public List<SaleItemBaseDto> getAllSaleItemBase() {
        List<SaleItemBase> saleItemsBases = saleItemBaseRepo.findAll();

        return saleItemsBases.stream().map(s -> {
            SaleItemBaseDto dto = new SaleItemBaseDto();
            dto.setId(s.getId());
            dto.setSellerId(s.getSeller() != null ? s.getSeller().getId() : null);
            dto.setSellerName(s.getSeller() != null ? s.getSeller().getNickname() : null);
            dto.setModel(s.getModel());
            dto.setBrandName(s.getBrand() != null ? s.getBrand().getName() : null);
            dto.setPrice(s.getPrice());
            dto.setRamGb(s.getRamGb());
            dto.setStorageGb(s.getStorageGb());
            dto.setColor(s.getColor());
            return dto;
        }).toList();
    }

    @Transactional
    public SaleItemBaseByIdDto getSaleItemBaseById(Integer id) {
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
                .sellerId(saleItem.getSeller() != null ? saleItem.getSeller().getId() : null)
                .sellerName(saleItem.getSeller() != null ? saleItem.getSeller().getNickname() : null)
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
    public SaleItemBaseByIdDto createSaleItem(NewSaleItemDto newSaleItem, MultipartFile[] pictures, Integer sellerId) {
        // --- ตรวจสอบ brand ---
        BrandBase brand;
        if (newSaleItem.getBrand() != null) {
            if (newSaleItem.getBrand().getId() != null) {
                brand = brandBaseRepo.findById(newSaleItem.getBrand().getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Brand with id " + newSaleItem.getBrand().getId() + " not found"));
            } else if (newSaleItem.getBrand().getBrandName() != null
                    && !newSaleItem.getBrand().getBrandName().trim().isEmpty()) {
                String trimmedBrandName = newSaleItem.getBrand().getBrandName().trim();
                brand = brandBaseRepo.findByNameIgnoreCase(trimmedBrandName)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Brand with name \"" + trimmedBrandName + "\" not found"));
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Brand must contain either valid ID or name.");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Brand is required.");
        }

        SellerAccount seller = sellerAccountRepo.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Seller not found"));

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
        saleItem.setSeller(seller);

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
                    List<String> allowedTypes = Arrays.stream(fileStorageProperties.getAllowFileTypes())
                            .map(String::toLowerCase)
                            .toList();
                    if (!allowedTypes.contains(extension)) {
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
                .saleItemImages(imageDtos) // ส่ง list ของรูป
                .createdOn(savedSaleItem.getCreatedOn())
                .updatedOn(savedSaleItem.getUpdatedOn())
                .build();
    }

    private static final int MAX_ORDER = 4;

    @Transactional
    public SaleItemBaseByIdDto editSaleItem(Integer id, SaleItemWithImageInfo request) {
        // 1) Load entity
        SaleItemBase sale = saleItemBaseRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale item not found: " + id));

        // 2) Update sale attributes
        applySaleAttributes(sale, request.getSaleItem());
        sale.setUpdatedOn(Instant.now());
        saleItemBaseRepo.saveAndFlush(sale);

        // 3) Load old pictures (DB)
        List<SaleItemPicture> existing = saleItemPictureRepo.findBySale_IdOrderByPictureOrderAsc(id);
        Map<String, SaleItemPicture> byNewName = existing.stream()
                .collect(Collectors.toMap(SaleItemPicture::getNewPictureName, p -> p, (a, b) -> a));
        Set<String> existingNames = new HashSet<>(byNewName.keySet());

        // กรณีไม่ส่ง imageInfos → ลบทั้งหมด
        if (request.getImageInfos() == null || request.getImageInfos().isEmpty()) {
            // delete files
            for (SaleItemPicture p : existing) {
                productFileService.deleteFile(p.getNewPictureName());
            }
            // delete DB
            saleItemPictureRepo.deleteBySaleItemId(id);
            return mapToDto(sale);
        }

        // 4) Validate + Normalize input
        List<SaleItemImageRequest> imageInfos = request.getImageInfos();
        // กรองรายการที่ไม่มีสถานะ/ออเดอร์
        imageInfos = imageInfos.stream()
                .filter(Objects::nonNull)
                .filter(i -> i.getOrder() != null && i.getOrder() >= 1 && i.getOrder() <= MAX_ORDER)
                .filter(i -> i.getStatus() != null && !i.getStatus().isBlank())
                .toList();

        // Check duplicate order (สุดท้าย 1 order มี 1 รูป)
        Map<Integer, Long> dupOrderCheck = imageInfos.stream()
                .collect(Collectors.groupingBy(SaleItemImageRequest::getOrder, Collectors.counting()));
        dupOrderCheck.forEach((ord, cnt) -> {
            if (cnt > 1) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Duplicate entries for order " + ord + " in imageInfos");
            }
        });

        // 5) เตรียมแผนการเปลี่ยนแปลง
        // - keepTargets: รายชื่อไฟล์สุดท้ายที่จะต้องอยู่หลังจบกระบวนการ (ไว้ลบส่วนที่เหลือ)
        // - moves: รายการที่จะย้าย/เปลี่ยนชื่อจากไฟล์เดิม
        // - creates: รายการไฟล์ใหม่ (new)
        Set<String> keepTargets = new HashSet<>();
        List<MovePlan> moves = new ArrayList<>();
        List<CreatePlan> creates = new ArrayList<>();

        // สร้าง map: order -> targetFinalName
        Map<Integer, String> targetNameByOrder = new HashMap<>();

        for (SaleItemImageRequest req : imageInfos) {
            final int order = req.getOrder();
            final String status = req.getStatus().trim().toLowerCase();
            final String ext = resolveExtension(req.getFileName(), req.getImageFile()); // .jpg/.png ...
            final String finalName = buildFinalName(id, order, ext);
            targetNameByOrder.put(order, finalName);

            switch (status) {
                case "keep" -> {
                    // ต้องมี fileName อ้างอิง
                    String srcName = req.getFileName();
                    if (srcName == null || !existingNames.contains(srcName)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "KEEP requires an existing fileName for order " + order);
                    }
                    if (srcName.equals(finalName)) {
                        // ไม่ต้องย้าย
                        keepTargets.add(finalName);
                    } else {
                        moves.add(new MovePlan(srcName, finalName));
                        keepTargets.add(finalName);
                    }
                }
                case "move" -> {
                    String srcName = req.getFileName();
                    if (srcName == null || !existingNames.contains(srcName)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "MOVE requires an existing fileName for order " + order);
                    }
                    if (!srcName.equals(finalName)) {
                        moves.add(new MovePlan(srcName, finalName));
                    }
                    keepTargets.add(finalName);
                }
                case "new" -> {
                    MultipartFile file = req.getImageFile();
                    if (file == null || file.isEmpty()) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "NEW requires imageFile for order " + order);
                    }
                    creates.add(new CreatePlan(file, finalName, req.getFileName())); // fileName (ถ้ามี) จะถือว่าเป็นของเก่าที่ถูกแทนที่
                    keepTargets.add(finalName);
                }
                case "keepall", "skip" -> {
                    // เผื่อมี client ส่งมาแบบนี้ ข้ามไปเฉยๆ
                }
                default -> {
                    if (!Set.of("keep", "move", "new").contains(status)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "Unsupported status: " + status);
                    }
                }
            }
        }

        // 6) สองเฟสสำหรับย้ายไฟล์เดิม (กันชนกันเอง)
        // 6.1 เปลี่ยนชื่อไฟล์ที่ "ต้องย้าย" เป็น temp ก่อน (ถ้าต้นทาง = ปลายทาง ไม่ทำ)
        Map<String, String> tempMapping = new HashMap<>(); // src -> temp
        for (MovePlan mv : moves) {
            if (mv.src().equals(mv.dest())) continue;
            String temp = mv.src() + ".reOrder-" + UUID.randomUUID();
            productFileService.renameFile(mv.src(), temp);
            tempMapping.put(mv.src(), temp);
        }

        // 6.2 ย้ายจาก temp → final
        for (MovePlan mv : moves) {
            if (mv.src().equals(mv.dest())) continue;
            String actualSrc = tempMapping.getOrDefault(mv.src(), mv.src());
            productFileService.renameFile(actualSrc, mv.dest());
        }

        // 7) จัดการ NEW (บันทึกไฟล์ใหม่ลงชื่อปลายทาง REPLACE_EXISTING)
        for (CreatePlan cp : creates) {
            productFileService.saveFile(cp.file(), cp.finalName());
        }

        // 8) อัปเดต DB
        // 8.1 เตรียม map newName -> SaleItemPicture (ของเดิม)
        Map<String, SaleItemPicture> currentByName = saleItemPictureRepo.findBySale_IdOrderByPictureOrderAsc(id)
                .stream().collect(Collectors.toMap(SaleItemPicture::getNewPictureName, p -> p, (a,b)->a));

        // 8.2 อัปเดต/เพิ่มรูปตาม targetNameByOrder
        List<SaleItemPicture> toUpsert = new ArrayList<>();

        for (int ord = 1; ord <= MAX_ORDER; ord++) {
            String finalName = targetNameByOrder.get(ord);
            Double randomNumber = Math.random()*10;
            String newImageName = Double.toString(randomNumber);
            if (finalName == null) continue; // ไม่มีคำสั่งสำหรับ order นี้

            SaleItemPicture pic = currentByName.get(finalName);
            if (pic == null) {
                // อาจเป็นรูปที่เพิ่ง MOVE มา (ชื่อใหม่) หรือ NEW
                pic = new SaleItemPicture();
                pic.setSale(sale);
                pic.setOldPictureName(newImageName);
                pic.setNewPictureName(finalName);
                pic.setPictureOrder(ord);
                pic.setCreatedOn(Instant.now());
            } else {
                // ของเดิม → แค่อัปเดต order ให้ตรง (กรณี keep แต่เปลี่ยนลำดับ)
                pic.setPictureOrder(ord);
            }

            // คำนวณขนาดไฟล์จริง
            try {
                long sz = productFileService.sizeOf(finalName);
                pic.setFileSizeBytes((int) Math.min(sz, Integer.MAX_VALUE));
            } catch (IOException e) {
                // ถ้าหาไม่ได้ ให้ใช้ของเดิมถ้ามี
                if (pic.getFileSizeBytes() == null) pic.setFileSizeBytes(0);
            }

            pic.setUpdatedOn(Instant.now());
            toUpsert.add(pic);
        }

        if (!toUpsert.isEmpty()) {
            saleItemPictureRepo.saveAll(toUpsert);
        }

        // 9) ลบรูปที่ไม่อยู่ใน keepTargets (ทั้งไฟล์ + DB)
        List<SaleItemPicture> toDelete = saleItemPictureRepo.findBySale_IdAndNewPictureNameNotIn(id, keepTargets);
        for (SaleItemPicture p : toDelete) {
            productFileService.deleteFile(p.getNewPictureName());
        }
        if (!toDelete.isEmpty()) {
            saleItemPictureRepo.deleteBySale_IdAndNewPictureNameNotIn(id, keepTargets);
        }

        List<SaleItemPicture> currentPictures = saleItemPictureRepo.findBySale_IdOrderByPictureOrderAsc(sale.getId());

        List<SaleItemImageDto> imageDtos = currentPictures.stream()
                .map(pic -> new SaleItemImageDto(pic.getNewPictureName(), pic.getPictureOrder()))
                .collect(Collectors.toList());

        return SaleItemBaseByIdDto.builder()
                .id(sale.getId())
                .model(sale.getModel())
                .brandName(sale.getBrand() != null ? sale.getBrand().getName() : null)
                .description(sale.getDescription())
                .price(sale.getPrice())
                .ramGb(sale.getRamGb())
                .screenSizeInch(sale.getScreenSizeInch() != null
                        ? sale.getScreenSizeInch().doubleValue()
                        : null)
                .quantity(sale.getQuantity())
                .storageGb(sale.getStorageGb())
                .color(sale.getColor())
                .createdOn(sale.getCreatedOn())
                .updatedOn(sale.getUpdatedOn())
                .saleItemImages(imageDtos)
                .build();
    }

    // ---------- Helpers ----------

    private void applySaleAttributes(SaleItemBase s, NewSaleItemDto dto) {
        s.setModel(dto.getModel() != null ? dto.getModel().trim() : null);
        s.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        s.setPrice(dto.getPrice());
        s.setRamGb(dto.getRamGb());
        s.setStorageGb(dto.getStorageGb());
        s.setScreenSizeInch(dto.getScreenSizeInch() != null
                ? BigDecimal.valueOf(dto.getScreenSizeInch())
                : null);
        s.setQuantity((dto.getQuantity() == null || dto.getQuantity() < 0)
                ? 1
                : dto.getQuantity());

        s.setColor((dto.getColor() == null || dto.getColor().trim().isEmpty())
                ? null
                : dto.getColor().trim());
        // Update brand
        if (dto.getBrand() != null && dto.getBrand().getId() != null) {
            BrandBase brand = brandBaseRepo.findById(dto.getBrand().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Brand with id " + dto.getBrand().getId() + " not found"));
            s.setBrand(brand);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand id must be provided.");
        }
    }

    private String buildFinalName(Integer saleId, Integer order, String ext) {
        String safeExt = (ext == null || ext.isBlank()) ? ".jpg" : (ext.startsWith(".") ? ext : "." + ext);
        return saleId + "." + order + safeExt.toLowerCase();
    }

    private String resolveExtension(String fileName, MultipartFile file) {
        // ถ้ามีไฟล์ใหม่ให้ใช้จาก original filename
        if (file != null && file.getOriginalFilename() != null) {
            String ext = extOf(file.getOriginalFilename());
            if (ext != null) return ext;
        }
        // ถ้ามีชื่อไฟล์เดิมให้ยืม extension เดิม
        if (fileName != null) {
            String ext = extOf(fileName);
            if (ext != null) return ext;
        }
        return ".jpg";
    }

    private String extOf(String name) {
        if (name == null) return null;
        int i = name.lastIndexOf('.');
        if (i == -1 || i == name.length() - 1) return null;
        return name.substring(i); // รวมจุด เช่น ".jpg"
    }

    // --- inner plan records ---
    private record MovePlan(String src, String dest) {}
    private record CreatePlan(MultipartFile file, String finalName, String oldNameToReplace) {}

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
                try {
                    productFileService.deleteFile(pic.getNewPictureName());
                } catch (Exception e) {
                    System.err.println("Failed to delete file: " + pic.getNewPictureName());
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
            String searchKeyword,
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

        String keyword = (searchKeyword == null || searchKeyword.isBlank()) ? null : searchKeyword.toLowerCase();

        Page<SaleItemBase> result = saleItemBaseRepo.findWithFilters(
                lowerBrands,
                filterStorages,
                filterStorageIsNull,
                lower,
                upper,
                keyword,
                pageable
        );

        return result.map(this::mapToDto);
    }

    private SaleItemBaseByIdDto mapToDto(SaleItemBase s) {
        // ดึงรูปแรก
        String firstImageName = saleItemPictureRepo
                .findFirstBySale_IdOrderByPictureOrderAsc(s.getId())
                .map(SaleItemPicture::getNewPictureName)
                .orElse(null);

        return SaleItemBaseByIdDto.builder()
                .id(s.getId())
                .sellerId(s.getSeller() != null ? s.getSeller().getId() : null)
                .sellerName(s.getSeller() != null ? s.getSeller().getNickname() : null)
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
                .firstImageName(firstImageName)
                .build();
    }

    public Page<SellerWithSaleItemsDto> getPagedSaleItemsBySeller(
            Integer sellerId,
            Integer page,
            Integer size,
            String sortField,
            String sortDirection
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SaleItemBase> result = saleItemBaseRepo.findBySellerId(sellerId, pageable);

        return result.map(this::mapToSellerDto);
    }

    private SellerWithSaleItemsDto mapToSellerDto(SaleItemBase s) {
        return SellerWithSaleItemsDto.builder()
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
                .seller(SellerDto.builder()
                        .id(s.getSeller().getId())
                        .username(s.getSeller().getNickname())
                        .build())
                .build();
    }
}
