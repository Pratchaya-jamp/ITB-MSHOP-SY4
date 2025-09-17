package intregrated.backend.dtos.users;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class SellerResponseDto extends UserResponseDto {
    private String mobile;
    private String bankName;
    private String bankNumber;
}
