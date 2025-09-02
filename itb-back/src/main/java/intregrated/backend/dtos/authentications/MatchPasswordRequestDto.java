package intregrated.backend.dtos.authentications;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchPasswordRequestDto {
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    private String email;

    @Size(min = 8, max = 14, message = "Password must be between 8 and 14 characters")
    private String password;
}
