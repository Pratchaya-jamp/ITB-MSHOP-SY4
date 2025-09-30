package intregrated.backend.dtos.carts;

import lombok.Data;

import java.util.List;

@Data
public class CartRequestDto {
    private Integer buyerId;
    private List<CartItemDto> items;

    @Data
    public static class CartItemDto {
        private Integer saleItemId;
        private Integer quantity;
    }
}
