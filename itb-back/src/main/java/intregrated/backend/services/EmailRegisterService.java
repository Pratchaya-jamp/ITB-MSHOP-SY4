package intregrated.backend.services;

import intregrated.backend.dtos.*;
import intregrated.backend.entities.*;
import intregrated.backend.fileproperties.SellerFileProperties;
import intregrated.backend.repositories.BuyerAccountRepository;
import intregrated.backend.repositories.SellerAccountRepository;
import intregrated.backend.repositories.SellerPictureRepository;
import intregrated.backend.repositories.UsersAccountRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;

@Service
public class EmailRegisterService {
    @Autowired
    private BuyerAccountRepository buyerRepo;

    @Autowired
    private UsersAccountRepository userRepo;

    @Autowired
    private SellerAccountRepository sellerRepo;

    @Autowired
    private SellerPictureRepository sellerPictureRepo;

    @Autowired
    @Qualifier("sellerFileService")
    private FileService sellerFileService;

    @Autowired
    private SellerFileProperties sellerFileProperties;

    @Transactional
    public List<UsersAccount> getAllUsers() {
        return userRepo.findAll();
    }

    @Transactional
    public UserRegisterResponseDto registerBuyer(UserRegisterRequestDto req) {

        // check email duplicate
        if (userRepo.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, // 409
                    "Email already exists"
            );
        }

        BuyerAccount buyer = new BuyerAccount();
        buyer.setCreatedOn(Instant.now());
        buyer.setUpdatedOn(Instant.now());
        buyerRepo.save(buyer);

        UsersAccount user = new UsersAccount();
        user.setNickname(req.getNickname());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());
        user.setFullname(req.getFullname());
        user.setBuyer(buyer);
        user.setCreatedOn(Instant.now());
        user.setUpdatedOn(Instant.now());

        user = userRepo.save(user);

        return UserRegisterResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .isActive(false)
                .userType("BUYER")
                .build();
    }

    @Transactional
    public UserRegisterResponseDto registerSeller(UserRegisterRequestDto seller, MultipartFile idCardFront, MultipartFile idCardBack) {

        // check email duplicate
        if (userRepo.existsByEmail(seller.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, // 409
                    "Email already exists"
            );
        }

        UserRegisterResponseDto user = registerBuyer(seller);

        SellerAccount sellerAccount = new SellerAccount();
        sellerAccount.setMobile(seller.getMobile());
        sellerAccount.setBankName(seller.getBankName());
        sellerAccount.setBankNumber(seller.getBankNumber());
        sellerAccount.setNationalId(seller.getNationalId());
        sellerAccount.setCreatedOn(Instant.now());
        sellerAccount.setUpdatedOn(Instant.now());

        sellerRepo.saveAndFlush(sellerAccount);

        // 3) save รูป
        List<SaleItemImageDto> pics = new ArrayList<>();
        MultipartFile[] idCardImages = {idCardFront, idCardBack};
        int order = 1;
            for (MultipartFile file : idCardImages) {
                if (file == null || file.isEmpty()) continue;

                String originalName = file.getOriginalFilename();
                String extension = FilenameUtils.getExtension(originalName).toLowerCase();
                System.out.println("Uploading file: " + originalName);
                System.out.println("File extension: " + extension);

                // ตรวจสอบนามสกุลไฟล์
                if (!Arrays.asList(sellerFileProperties.getAllowFileTypes()).contains(extension.toUpperCase())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File type not allowed: " + extension);
                }

                // สร้างชื่อไฟล์ใหม่ เช่น 86.1.jpg
                String newFileName = sellerAccount.getId() + "." + order + "." + extension;
                Path targetPath = Paths.get(sellerFileProperties.getUploadDir()).resolve(newFileName);
                System.out.println("Target path: " + targetPath.toAbsolutePath());

                try {
                    Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not save file");
                }

                SellerPicture pic = new SellerPicture();
                pic.setOldPictureName(originalName);
                pic.setNewPictureName(newFileName);
                pic.setFileSizeBytes((int) file.getSize());
                pic.setPictureOrder(order);
                pic.setCreatedOn(Instant.now());
                pic.setUpdatedOn(Instant.now());
                pic.setSeller(sellerAccount);

                sellerPictureRepo.saveAndFlush(pic);
                System.out.println("Saved picture record into DB: " + newFileName);

                pics.add(new SaleItemImageDto(newFileName, order));

                order++;
            }

        UsersAccount userEntity = userRepo.findById(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        userEntity.setSeller(sellerAccount);
        sellerRepo.saveAndFlush(sellerAccount);
        userRepo.saveAndFlush(userEntity);

        sellerRepo.saveAndFlush(sellerAccount);

        // 5) build response
        UserRegisterResponseDto resp = UserRegisterResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .mobile(sellerAccount.getMobile())
                .isActive(false)
                .userType("SELLER")
                .build();

        return resp;
    }

    @Transactional
    public void deleteUser(Integer uid) {
        // หา user ก่อน (เพราะ UsersAccount เป็นเจ้าของ FK)
        UsersAccount usersAccount = userRepo.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "user with id " + uid + " not found"
                ));

        SellerAccount sellerAccount = usersAccount.getSeller(); // ดึง seller ผ่าน user
        if (sellerAccount == null) {
            // user ไม่มี seller → แค่ลบ user
            userRepo.delete(usersAccount);
            return;
        }

        // โหลดรูป (บังคับ fetch)
        List<SellerPicture> pictures = sellerAccount.getPictures();
        pictures.size();

        // ลบไฟล์จริงใน disk
        for (SellerPicture pic : pictures) {
            if (pic.getNewPictureName() != null && !pic.getNewPictureName().isBlank()) {
                try {
                    sellerFileService.deleteFile(pic.getNewPictureName());
                } catch (Exception e) {
                    System.err.println("Failed to delete file: " + pic.getNewPictureName());
                    e.printStackTrace();
                }
            }
        }

        // ตัดความสัมพันธ์ออก เพื่อให้ Hibernate จัดการ orphan ได้
        usersAccount.setSeller(null);

        // ลบ user ก่อน (เพราะมันถือ FK ไปยัง seller)
        userRepo.delete(usersAccount);

        // ลบ seller (Hibernate จะ cascade ลบ SellerPicture ให้เอง)
        sellerRepo.delete(sellerAccount);
    }
}
