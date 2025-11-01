package intregrated.backend.dtos.authentications;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Instant;

@Data
@AllArgsConstructor
public class RequestOtpResponseDto {
    private String message;
    private Instant expiredAt;
}
