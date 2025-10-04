package intregrated.backend.services;

import intregrated.backend.dtos.orders.OrderItemDto;
import intregrated.backend.dtos.orders.OrderRequestDto;
import intregrated.backend.dtos.orders.OrderResponseDto;
import intregrated.backend.dtos.orders.OrderSellerDto;
import intregrated.backend.dtos.paginations.PageResponseDto;
import intregrated.backend.entities.accounts.SellerAccount;
import intregrated.backend.entities.accounts.UsersAccount;
import intregrated.backend.entities.orders.Order;
import intregrated.backend.entities.orders.OrderItem;
import intregrated.backend.entities.orders.OrderStatus;
import intregrated.backend.entities.saleitems.SaleItemBase;
import intregrated.backend.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    public List<OrderResponseDto> placeOrder(List<OrderRequestDto> requests, Integer userId) {

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token does not contain userId");
        }

        List<OrderResponseDto> orderResponseDtos = new ArrayList<>();

        for (OrderRequestDto request : requests) {

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
            order.setOrderStatus(OrderStatus.COMPLETED);

            orderRepo.save(order);

            for (OrderItemDto dto : request.getOrderItems()) {
                SaleItemBase saleItem = saleItemRepo.findById(dto.getSaleItemId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale item not found"));

                if (saleItem.getQuantity() < dto.getQuantity()) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Quantity Not Enough");
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

            orderResponseDtos.add(OrderResponseDto.builder()
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
                    .build());
        }
        return orderResponseDtos;
    }

    public OrderResponseDto getOrderById(Integer buyerId, Integer sellerId, Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order id with " + orderId + " does not exist"));

        boolean isOwner = (order.getBuyer() != null && order.getBuyer().getId().equals(buyerId))
                || (order.getSeller() != null && order.getSeller().getId().equals(sellerId));

        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User id not an owner (seller/buyer) of the order");
        }

        UsersAccount sellerUser = usersRepo.findBySellerId(order.getSeller().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller user not found"));

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

    public Page<OrderResponseDto> getAllOrders(Integer userId, Integer page, Integer size, String sortField, String sortDirection) {
        UsersAccount usersAccount = usersRepo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with " + userId + " not found"));

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orders;
        if (usersAccount.getBuyer() != null) {
            orders = orderRepo.findByBuyer_Id(usersAccount.getBuyer().getId(), pageable);
        } else if (usersAccount.getSeller() != null) {
            orders = orderRepo.findBySeller_Id(usersAccount.getSeller().getId(), pageable);
        } else {
            // ไม่มี role ที่ถูกต้อง
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User is neither buyer nor seller");
        }

        return orders.map(order -> {
            UsersAccount sellerUser = usersRepo.findBySellerId(order.getSeller().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Seller user not found"));

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
        });
    }

    public PageResponseDto<OrderResponseDto> getAllOrdersPaged(
            Integer userId, Integer page, Integer size, String sortField, String sortDirection) {

        Page<OrderResponseDto> pagedResult = getAllOrders(userId, page, size, sortField, sortDirection);

        return PageResponseDto.<OrderResponseDto>builder()
                .content(pagedResult.getContent())
                .page(pagedResult.getNumber())
                .size(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .first(pagedResult.isFirst())
                .last(pagedResult.isLast())
                .sort(sortField + ": " + sortDirection)
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
