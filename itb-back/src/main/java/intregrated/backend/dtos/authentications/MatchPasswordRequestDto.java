package intregrated.backend.dtos.authentications;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MatchPasswordRequestDto {

    private String email;

    private String password;
}
