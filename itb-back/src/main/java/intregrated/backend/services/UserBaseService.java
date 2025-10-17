package intregrated.backend.services;

import intregrated.backend.dtos.registers.UserRegisterResponseDto;
import intregrated.backend.dtos.users.BuyerResponseDto;
import intregrated.backend.dtos.users.EditUserRequestDto;
import intregrated.backend.dtos.users.SellerResponseDto;
import intregrated.backend.dtos.users.UserResponseDto;
import intregrated.backend.entities.accounts.BuyerAccount;
import intregrated.backend.entities.accounts.SellerAccount;
import intregrated.backend.entities.accounts.UsersAccount;
import intregrated.backend.repositories.UsersAccountRepo;
import intregrated.backend.utils.JwtTokenUtil;
import intregrated.backend.utils.UserTypeResolver;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserBaseService {

    @Autowired
    private UsersAccountRepo userRepo;

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
                        .userType(UserTypeResolver.resolveUserType(user))
                        .build()
                ).toList();
    }

    @Transactional
    public UserResponseDto getUserById(Integer id, String token) {

        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        Claims claims = jwtTokenUtil.getClaims(token);
        Integer tokenUserId = claims.get("id", Integer.class);

        if (!id.equals(tokenUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User ID mismatch between token and request");
        }

        UsersAccount user = userRepo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with id " + id + " not found")
        );

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }

        String userType = UserTypeResolver.resolveUserType(user);

        if ("BUYER".equals(userType)) {
            return BuyerResponseDto.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .fullname(user.getFullname())
                    .isActive(user.getIsActive())
                    .userType(userType)
                    .build();

        } else if ("SELLER".equals(userType)) {
            return SellerResponseDto.builder()
                    .id(user.getId())
                    .nickname(user.getNickname())
                    .email(user.getEmail())
                    .fullname(user.getFullname())
                    .mobile(user.getSeller().getMobile())
                    .bankName(user.getSeller().getBankName())
                    .bankNumber(user.getSeller().getBankNumber())
                    .isActive(user.getIsActive())
                    .userType(userType)
                    .build();
        }

        // Default case
        return UserResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .fullname(user.getFullname())
                .isActive(user.getIsActive())
                .userType(userType)
                .build();
    }


    @Transactional
    public UserResponseDto editUser(Integer id, EditUserRequestDto request, String token) {

        // ตรวจสอบ token
        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        // ดึง claims จาก token
        Claims claims = jwtTokenUtil.getClaims(token);
        Integer tokenUserId = claims.get("id", Integer.class);

        // ถ้า id ใน path ไม่ตรงกับ id ใน token → 403 Forbidden
        if (!id.equals(tokenUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request userId not matched with token userId");
        }

        UsersAccount existing = userRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + id));

        if (existing == null) {
            throw new  ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        if (!Boolean.TRUE.equals(existing.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not active");
        }

        existing.setNickname(request.getNickname().trim());
        existing.setFullname(request.getFullname().trim());

        // update buyer info if exists
        if (existing.getBuyer() != null) {
            BuyerAccount buyer = existing.getBuyer();
            buyer.setNickname(request.getNickname().trim());
            buyer.setFullname(request.getFullname().trim());
        }

        // update seller info if exists
        if (existing.getSeller() != null) {
            SellerAccount seller = existing.getSeller();
            seller.setNickname(request.getNickname().trim());
            seller.setFullname(request.getFullname().trim());
        }

        UsersAccount saved = userRepo.save(existing);

        return UserResponseDto.builder()
                .id(saved.getId())
                .fullname(saved.getFullname())
                .nickname(saved.getNickname())
                .email(saved.getEmail())
                .userType(UserTypeResolver.resolveUserType(saved))
                .isActive(saved.getIsActive())
                .build();
    }
}
