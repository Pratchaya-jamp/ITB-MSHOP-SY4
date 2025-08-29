package intregrated.backend.dtos.authentications;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchPasswordRequestDto {
    @NotBlank(message = "Email must not be empty")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    private String email;

    @NotBlank(message = "Password must not be empty")
    @Size(max = 14, message = "Password cannot exceed 14 characters")
    private String password;
}
