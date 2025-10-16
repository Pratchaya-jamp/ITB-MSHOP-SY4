package intregrated.backend.dtos.orders;

import intregrated.backend.entities.orders.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
public class OrderRequestDto {
    @NotNull
    private Integer buyerId;

    @NotNull
    private Integer sellerId;

    @NotNull
    private Instant orderDate;

    @NotBlank
    private String shippingAddress;

    private String orderNote;

    @NotNull
    @NotEmpty
    private List<OrderItemDto> orderItems;

    @NotNull
    private OrderStatus orderStatus;
}
