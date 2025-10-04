package intregrated.backend.dtos.orders;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderSellerDto {
    private Integer id;
    private String email;
    private String fullName;
    private String userType;
    private String nickname;
}
