package intregrated.backend.dtos.authentications;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccessTokenResponseDto {
    private String access_token;
}
