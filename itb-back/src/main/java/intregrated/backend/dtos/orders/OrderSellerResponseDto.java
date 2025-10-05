package intregrated.backend.dtos.orders;

import intregrated.backend.entities.orders.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderSellerResponseDto {
    private Integer id;
    private OrderBuyerDto buyer;
    private Integer sellerId;
    private Instant orderDate;
    private Instant paymentDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItemDto> orderItems;
    private OrderStatus orderStatus;
}
