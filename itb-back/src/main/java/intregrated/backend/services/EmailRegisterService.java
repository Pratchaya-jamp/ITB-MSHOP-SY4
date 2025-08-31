package intregrated.backend.services;

import intregrated.backend.dtos.registers.UserRegisterRequestDto;
import intregrated.backend.dtos.registers.UserRegisterResponseDto;
import intregrated.backend.dtos.saleItems.SaleItemImageDto;
import intregrated.backend.entities.*;
import intregrated.backend.fileproperties.SellerFileProperties;
import intregrated.backend.repositories.BuyerAccountRepository;
import intregrated.backend.repositories.SellerAccountRepository;
import intregrated.backend.repositories.SellerPictureRepository;
import intregrated.backend.repositories.UsersAccountRepository;
import intregrated.backend.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private MailService mailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Transactional
    public List<UserRegisterResponseDto> getAllUsers() {
        List<UsersAccount> accounts = userRepo.findAll();

        return accounts.stream()
                .map(user -> UserRegisterResponseDto.builder()
                        .id(user.getId())
                        .nickname(user.getNickname())
                        .email(user.getEmail())
                        .fullname(user.getFullname())
                        .mobile(user.getSeller() != null ? user.getSeller().getMobile() : null)
                        .isActive(user.getIsActive())
                        .userType(resolveUserType(user))
                        .build()
                ).toList();
    }

    public String resolveUserType(UsersAccount user) {
        boolean isBuyer = user.getBuyer() != null;
        boolean isSeller = user.getSeller() != null;

        if (isBuyer && isSeller) {
            return "USER, SELLER";
        } else if (isSeller) {
            return "SELLER";
        } else if (isBuyer) {
            return "BUYER";
        } else {
            return "USER";
        }
    }

    @Transactional
    public UserRegisterResponseDto getUserById(Integer id) {
        UsersAccount user = userRepo.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"User with id " + id + " not found"
                )
        );

        String mobile = null;
        if (user.getSeller() != null) {
            mobile = user.getSeller().getMobile();
        }

        return UserRegisterResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .mobile(mobile)
                .isActive(user.getIsActive())
                .userType(resolveUserType(user))
                .build();
    }

    @Transactional
    public UserRegisterResponseDto registerBuyer(UserRegisterRequestDto req) {
        UsersAccount user = userRepo.findByEmail(req.getEmail()).orElse(null);

        if (user == null) {
            // user ยังไม่มี → สมัครใหม่
            BuyerAccount buyer = new BuyerAccount();
            buyer.setNickname(req.getNickname());
            buyer.setFullname(req.getFullname());
            buyer.setCreatedOn(Instant.now());
            buyer.setUpdatedOn(Instant.now());
            buyerRepo.save(buyer);

            user = new UsersAccount();
            user.setNickname(req.getNickname());
            user.setEmail(req.getEmail());
            user.setPassword(passwordEncoder.encode(req.getPassword()));
            user.setFullname(req.getFullname());
            user.setBuyer(buyer);
            user.setCreatedOn(Instant.now());
            user.setUpdatedOn(Instant.now());

            user.setIsActive(false);

            user = userRepo.save(user);

            String token = jwtTokenUtil.generateVerificationToken(user);
            sendVerificationEmail(user, token);

        } else if (user.getBuyer() == null) {
            // user มีอยู่แล้ว แต่ยังไม่มี buyer → เพิ่ม buyer ให้
            BuyerAccount buyer = new BuyerAccount();
            buyer.setNickname(req.getNickname());
            buyer.setFullname(req.getFullname());
            buyer.setCreatedOn(Instant.now());
            buyer.setUpdatedOn(Instant.now());
            buyerRepo.save(buyer);

            user.setBuyer(buyer);
            userRepo.saveAndFlush(user);
        } else {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered as BUYER");
        }

        return UserRegisterResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .isActive(false)
                .userType("BUYER")
                .build();
    }

    private void sendVerificationEmail(UsersAccount user, String jwtToken) {
        mailService.sendVerificationEmail(user.getEmail(), jwtToken);
    }

    @Transactional
    public UserRegisterResponseDto verifyEmailToken(String jwtToken) {
        if (!jwtTokenUtil.validateToken(jwtToken)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token");
        }

        Claims claims = jwtTokenUtil.getClaims(jwtToken);
        Integer userId = claims.get("id", Integer.class);
        String email = claims.get("email", String.class);

        UsersAccount user = userRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token"));

        if (!user.getEmail().equals(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification token");
        }

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already verified");
        }

        user.setIsActive(true);
        userRepo.save(user);

        String mobile = null;
        if (user.getSeller() != null) {
            mobile = user.getSeller().getMobile();
        }

        return UserRegisterResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .mobile(mobile)
                .isActive(user.getIsActive())
                .userType(resolveUserType(user))
                .build();
    }

    @Transactional
    public UserRegisterResponseDto registerSeller(UserRegisterRequestDto seller, MultipartFile idCardFront, MultipartFile idCardBack) {

        // หา user เดิมจาก email
        UsersAccount userEntity = userRepo.findByEmail(seller.getEmail()).orElse(null);

        if (userEntity == null) {
            // ถ้าไม่มี user → สมัคร user ใหม่ (สำหรับ seller โดยตรง)
            userEntity = new UsersAccount();
            userEntity.setNickname(seller.getNickname());
            userEntity.setEmail(seller.getEmail());
            userEntity.setPassword(passwordEncoder.encode(seller.getPassword()));
            userEntity.setFullname(seller.getFullname());
            userEntity.setCreatedOn(Instant.now());
            userEntity.setUpdatedOn(Instant.now());
            userEntity = userRepo.saveAndFlush(userEntity);
        }

        // ถ้ามี seller อยู่แล้ว ไม่ต้องสร้างซ้ำ
        if (userEntity.getSeller() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered as SELLER");
        }

        SellerAccount sellerAccount = new SellerAccount();
        sellerAccount.setNickname(seller.getNickname());
        sellerAccount.setFullname(seller.getFullname());
        sellerAccount.setMobile(seller.getMobile());
        sellerAccount.setBankName(seller.getBankName());
        sellerAccount.setBankNumber(seller.getBankNumber());
        sellerAccount.setNationalId(seller.getNationalId());
        sellerAccount.setCreatedOn(Instant.now());
        sellerAccount.setUpdatedOn(Instant.now());

        sellerRepo.saveAndFlush(sellerAccount);

        String token = jwtTokenUtil.generateVerificationToken(userEntity);
        sendVerificationEmail(userEntity, token);

        // 3) save รูป
        List<SaleItemImageDto> pics = new ArrayList<>();
        MultipartFile[] idCardImages = {idCardFront, idCardBack};
        int order = 1;
            for (MultipartFile file : idCardImages) {
                if (file == null || file.isEmpty()) continue;

                String originalName = file.getOriginalFilename();
                String extension = FilenameUtils.getExtension(originalName).toLowerCase();

                // ตรวจสอบนามสกุลไฟล์
                if (!Arrays.asList(sellerFileProperties.getAllowFileTypes()).contains(extension.toUpperCase())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File type not allowed: " + extension);
                }

                // สร้างชื่อไฟล์ใหม่ เช่น 86.1.jpg
                String newFileName = sellerAccount.getId() + "." + order + "." + extension;
                Path targetPath = Paths.get(sellerFileProperties.getUploadDir()).resolve(newFileName);

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

                pics.add(new SaleItemImageDto(newFileName, order));

                order++;
            }

        userEntity.setSeller(sellerAccount);
        sellerRepo.saveAndFlush(sellerAccount);
        userRepo.saveAndFlush(userEntity);

        sellerRepo.saveAndFlush(sellerAccount);

        // 5) build response
        UserRegisterResponseDto resp = UserRegisterResponseDto.builder()
                .id(userEntity.getId())
                .nickname(userEntity.getNickname())
                .email(userEntity.getEmail())
                .fullname(userEntity.getFullname())
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
