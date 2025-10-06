package intregrated.backend.dtos.carts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponseDto {
    private Integer saleItemId;
    private String saleItemName;
    private Integer saleItemPrice;
    private Integer quantity;
    private Integer sellerId;
    private String sellerName;
    private String description;
}
