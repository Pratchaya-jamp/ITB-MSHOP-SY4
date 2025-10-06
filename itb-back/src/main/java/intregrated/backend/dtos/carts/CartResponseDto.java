package intregrated.backend.dtos.carts;

import intregrated.backend.entities.carts.CartItem;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CartResponseDto {
    private Integer cartId;
    private Integer userId;
    private List<CartItemResponseDto> carts;
}
