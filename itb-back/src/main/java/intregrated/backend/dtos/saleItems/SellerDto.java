package intregrated.backend.dtos.saleItems;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerDto {
    private Integer id;
    private String username;
}
