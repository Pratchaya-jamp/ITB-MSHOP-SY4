package intregrated.backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BuyerRegisterResponse {
    @NotBlank
    private String nickname;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 4, max = 40)
    private String fullname;
}
