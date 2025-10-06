package intregrated.backend.dtos.carts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CartRequestDto {
    @NotNull
    private Integer buyerId;
    @NotNull
    @NotEmpty
    private List<CartItemDto> cart_items;

    @Data
    public static class CartItemDto {
        @NotNull
        private Integer saleItemId;
        @NotNull
        private Integer quantity;
    }
}
