package intregrated.backend.dtos.orders;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderItemDto {
    private Integer no;
    private Integer saleItemId;
    private Integer price;
    private Integer quantity;
    private String description;
}
