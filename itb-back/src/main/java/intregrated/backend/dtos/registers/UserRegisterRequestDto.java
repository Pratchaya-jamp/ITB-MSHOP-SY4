package intregrated.backend.dtos.registers;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRegisterRequestDto {
    @NotBlank(message = "Nickname is required")
    private String nickname;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Size(max = 50, message = "email cannot exceed 50 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 14, message = "Password must be at least 8 characters with lower, upper, number, and special character")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).+$",
            message = "Password must contain uppercase, lowercase, number, and special character"
    )
    private String password;

    @NotBlank
    @Size(min = 4, max = 40)
    private String fullname;

    private String mobile;

    // เฉพาะ Seller เท่านั้น
    private String bankNumber;

    private String bankName;

    private String nationalId;

    @NotBlank(message = "User type is required")
    private String userType;
}
