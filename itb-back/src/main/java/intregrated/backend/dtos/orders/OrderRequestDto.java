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
    @NotNull(message = "buyerId is required")
    private Integer buyerId;

    @NotNull(message = "sellerId is required")
    private Integer sellerId;

    @NotNull(message = "orderDate is required")
    private Instant orderDate;

    @NotBlank(message = "shippingAddress is required")
    private String shippingAddress;

    private String orderNote;

    @NotNull(message = "orderItems is required")
    @NotEmpty(message = "orderItems is required")
    private List<OrderItemDto> orderItems;

    @NotNull(message = "orderStatus is required")
    private OrderStatus orderStatus;
}
