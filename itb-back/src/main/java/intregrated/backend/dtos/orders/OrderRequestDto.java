package intregrated.backend.dtos.orders;

import intregrated.backend.entities.orders.OrderStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class OrderRequestDto {
    @NotBlank(message = "Missing request parameters")
    private Integer buyerId;

    @NotBlank(message = "Missing request parameters")
    private Integer sellerId;

    @NotBlank(message = "Missing request parameters")
    private Instant orderDate;

    @NotBlank(message = "Missing request parameters")
    private String shippingAddress;

    @NotBlank(message = "Missing request parameters")
    private String orderNote;

    @NotBlank(message = "Missing request parameters")
    private List<OrderItemDto> orderItems;

    @NotBlank(message = "Missing request parameters")
    private OrderStatus orderStatus;
}
