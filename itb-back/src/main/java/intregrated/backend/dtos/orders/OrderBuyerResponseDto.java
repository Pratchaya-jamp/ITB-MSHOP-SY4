package intregrated.backend.dtos.orders;

import intregrated.backend.entities.orders.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderBuyerResponseDto {
    private Integer id;
    private Integer buyerId;
    private OrderSellerDto seller;
    private Instant orderDate;
    private Instant paymentDate;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItemDto> orderItems;
    private OrderStatus orderStatus;
}
