package intregrated.backend.dtos.carts;

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
    private List<CartItemDto> items;

    @Data
    public static class CartItemDto {
        private Integer saleItemId;
        private Integer quantity;
    }
}
