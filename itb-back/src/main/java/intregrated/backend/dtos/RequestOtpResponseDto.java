package intregrated.backend.dtos.authentications;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;

@Data
@AllArgsConstructor
public class RequestOtpResponseDto {
    private String message;
    private String otp; // in production you may omit this and only send via email/sms
    private Instant expiredAt;
}
