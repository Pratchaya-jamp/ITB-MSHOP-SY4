package intregrated.backend.services;

import intregrated.backend.dtos.orders.OrderItemDto;
import intregrated.backend.dtos.orders.OrderRequestDto;
import intregrated.backend.dtos.orders.OrderResponseDto;
import intregrated.backend.dtos.orders.OrderSellerDto;
import intregrated.backend.entities.accounts.SellerAccount;
import intregrated.backend.entities.accounts.UsersAccount;
import intregrated.backend.entities.orders.Order;
import intregrated.backend.entities.orders.OrderItem;
import intregrated.backend.entities.orders.OrderStatus;
import intregrated.backend.entities.saleitems.SaleItemBase;
import intregrated.backend.repositories.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class OrderService {
    @Autowired
    private UsersAccountRepo usersRepo;
    @Autowired
    private SellerAccountRepo sellerRepo;
    @Autowired
    private SaleItemBaseRepo saleItemRepo;
    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private OrderItemRepo orderItemRepo;

    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto request, Integer userId) {

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token does not contain userId");
        }

        UsersAccount user = usersRepo.findById(request.getBuyerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found"));

        UsersAccount sellerUser = usersRepo.findBySellerId(request.getSellerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller user not found"));

        SellerAccount seller = sellerRepo.findById(request.getSellerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));

        Order order = new Order();
        order.setBuyer(user);
        order.setSeller(seller);
        order.setOrderDate(Instant.now());
        order.setShippingAddress(request.getShippingAddress());
        order.setOrderNote(request.getOrderNote());
        order.setCreatedOn(Instant.now());
        order.setUpdatedOn(Instant.now());
        order.setOrderStatus(OrderStatus.PENDING);

        orderRepo.save(order);

        for (OrderItemDto dto : request.getOrderItems()) {
            SaleItemBase saleItem = saleItemRepo.findById(dto.getSaleItemId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SaleItem not found"));

            if (saleItem.getQuantity() < dto.getQuantity()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough items in stock");
            }

            saleItem.setQuantity(saleItem.getQuantity() - dto.getQuantity());
            saleItemRepo.save(saleItem);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setSaleItem(saleItem);
            orderItem.setPrice(dto.getPrice());
            orderItem.setQuantity(dto.getQuantity());
            orderItem.setDescription(dto.getDescription());
            orderItem.setCreatedOn(Instant.now());
            orderItem.setUpdatedOn(Instant.now());

            orderItemRepo.save(orderItem);
            order.getOrderItems().add(orderItem);
        }

        return OrderResponseDto.builder()
                .id(order.getId())
                .buyerId(order.getBuyer().getId())
                .seller(OrderSellerDto.builder()
                        .id(order.getSeller().getId())
                        .email(sellerUser.getEmail())
                        .fullName(order.getSeller().getFullname())
                        .userType(resolveUserType(sellerUser))
                        .nickname(order.getSeller().getNickname())
                        .build())
                .orderDate(order.getOrderDate())
                .shippingAddress(order.getShippingAddress())
                .orderNote(order.getOrderNote())
                .orderItems(order.getOrderItems().stream()
                        .map(oi -> OrderItemDto.builder()
                                .no(oi.getId())
                                .saleItemId(oi.getSaleItem().getId())
                                .price(oi.getPrice())
                                .quantity(oi.getQuantity())
                                .description(oi.getDescription())
                                .build())
                        .toList())
                .orderStatus(order.getOrderStatus())
                .build();
    }

    public String resolveUserType(UsersAccount user) {
        boolean isBuyer = user.getBuyer() != null;
        boolean isSeller = user.getSeller() != null;

        if (isBuyer && isSeller) {
            return "USER, SELLER";
        } else if (isSeller) {
            return "SELLER";
        } else if (isBuyer) {
            return "BUYER";
        } else {
            return "USER";
        }
    }
}
