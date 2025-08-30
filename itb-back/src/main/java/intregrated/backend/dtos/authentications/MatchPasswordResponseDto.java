package intregrated.backend.dtos.authentications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchPasswordResponseDto {
    private String access_token;
    private String refresh_token;
}
