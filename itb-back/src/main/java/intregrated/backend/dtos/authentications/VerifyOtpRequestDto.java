package intregrated.backend.dtos.authentications;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerifyOtpRequestDto {
    private String email;
    private String otp;
    private Boolean rememberMe = false;
}
