package intregrated.backend.dtos.orders;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderBuyerDto {
    private Integer id;
    private String username;
}
