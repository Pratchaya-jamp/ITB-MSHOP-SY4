package intregrated.backend.services;

import intregrated.backend.dtos.orders.*;
import intregrated.backend.entities.accounts.SellerAccount;
import intregrated.backend.entities.accounts.UsersAccount;
import intregrated.backend.entities.orders.Order;
import intregrated.backend.entities.orders.OrderItem;
import intregrated.backend.entities.orders.OrderStatus;
import intregrated.backend.entities.saleitems.SaleItemBase;
import intregrated.backend.repositories.accounts.SellerAccountRepo;
import intregrated.backend.repositories.accounts.UsersAccountRepo;
import intregrated.backend.repositories.orders.OrderRepo;
import intregrated.backend.repositories.saleitems.SaleItemBaseRepo;
import intregrated.backend.utils.JwtTokenUtil;
import intregrated.backend.utils.UserTypeResolver;
import io.jsonwebtoken.Claims;
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
    private JwtTokenUtil jwtTokenUtil;

    @Transactional
    public List<OrderBuyerResponseDto> placeOrder(List<OrderRequestDto> requests, String token) {

        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired verification token");
        }

        Integer userId = jwtTokenUtil.getClaims(token).get("id", Integer.class);

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token does not contain userId");
        }

        List<OrderBuyerResponseDto> orderResponseDtos = new ArrayList<>();

        for (OrderRequestDto request : requests) {

            UsersAccount user = usersRepo.findById(request.getBuyerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Buyer not found"));

            UsersAccount sellerUser = usersRepo.findBySellerId(request.getSellerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller user not found"));

            SellerAccount seller = sellerRepo.findById(request.getSellerId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller not found"));

            Order order = new Order();
            order.setUser(user);
            order.setSeller(seller);
            order.setOrderDate(Instant.now());
            order.setPaymentDate(Instant.now());
            order.setShippingAddress(request.getShippingAddress());
            order.setOrderNote(request.getOrderNote() != null ? request.getOrderNote() : null);
            order.setCreatedOn(Instant.now());
            order.setUpdatedOn(Instant.now());
            order.setOrderStatus(OrderStatus.COMPLETED);

            orderRepo.save(order);

            boolean hasInsufficientStock = false;
            List<SaleItemBase> saleItemsToUpdated = new ArrayList<>();

            for (OrderItemDto dto : request.getOrderItems()) {
                SaleItemBase saleItem = saleItemRepo.findById(dto.getSaleItemId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sale item not found"));

                // ห้ามซื้อสินค้าของตัวเอง
                if (saleItem.getSeller() != null && user.getSeller() != null &&
                        saleItem.getSeller().getId().equals(user.getSeller().getId())) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot buy your own product");
                }

                if (saleItem.getQuantity() < dto.getQuantity()) {
                    hasInsufficientStock = true;
                }

                saleItemsToUpdated.add(saleItem);
            }

            // If insufficient stock, cancel the whole order for this seller
            if (hasInsufficientStock) {
                order.setOrderStatus(OrderStatus.CANCELED);
            } else {
                // Deduct stock only if order is valid
                for (int i = 0; i < request.getOrderItems().size(); i++) {
                    OrderItemDto dto = request.getOrderItems().get(i);
                    SaleItemBase saleItem = saleItemsToUpdated.get(i);

                    saleItem.setQuantity(saleItem.getQuantity() - dto.getQuantity());
                    saleItemRepo.save(saleItem);
                }
            }

            for (int i = 0; i < request.getOrderItems().size(); i++) {
                OrderItemDto dto = request.getOrderItems().get(i);
                SaleItemBase saleItem = saleItemsToUpdated.get(i);

                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setSaleItem(saleItem);
                orderItem.setPrice(dto.getPrice());
                orderItem.setPaymentDate(Instant.now());
                orderItem.setQuantity(dto.getQuantity());
                orderItem.setDescription(dto.getDescription());
                orderItem.setCreatedOn(Instant.now());
                orderItem.setUpdatedOn(Instant.now());

                order.getOrderItems().add(orderItem);
            }

            orderRepo.save(order);

            orderResponseDtos.add(OrderBuyerResponseDto.builder()
                    .id(order.getId())
                    .buyerId(order.getUser().getId())
                    .seller(OrderSellerDto.builder()
                            .id(order.getSeller().getId())
                            .email(sellerUser.getEmail())
                            .fullName(order.getSeller().getFullname())
                            .userType(UserTypeResolver.resolveUserType(sellerUser))
                            .nickname(order.getSeller().getNickname())
                            .build())
                    .orderDate(order.getOrderDate())
                    .paymentDate(order.getPaymentDate())
                    .shippingAddress(order.getShippingAddress())
                    .orderNote(order.getOrderNote() != null ? order.getOrderNote() : null)
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

    public OrderBuyerResponseDto getOrderById(String token, Long orderId) {

        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired verification token");
        }

        Claims claims = jwtTokenUtil.getClaims(token);
        String role = claims.get("role", String.class);
        Integer buyerId = claims.get("buyer_id", Integer.class);
        Integer sellerId = claims.get("seller_id", Integer.class);

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order id with " + orderId + " does not exist"));

        boolean isOwner = false;

        if ("SELLER".equalsIgnoreCase(role) && order.getSeller() != null) {
            isOwner = order.getSeller().getId().equals(sellerId);
        } else if ("BUYER".equalsIgnoreCase(role) && order.getUser() != null) {
            isOwner = order.getUser().getId().equals(buyerId);
        }

        if (!isOwner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "User id not an owner (seller/buyer) of the order");
        }

        UsersAccount sellerUser = usersRepo.findBySellerId(order.getSeller().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller user not found"));

        return OrderBuyerResponseDto.builder()
                .id(order.getId())
                .buyerId(order.getUser().getId())
                .seller(OrderSellerDto.builder()
                        .id(order.getSeller().getId())
                        .email(sellerUser.getEmail())
                        .fullName(order.getSeller().getFullname())
                        .userType(UserTypeResolver.resolveUserType(sellerUser))
                        .nickname(order.getSeller().getNickname())
                        .build())
                .orderDate(order.getOrderDate())
                .paymentDate(order.getPaymentDate())
                .shippingAddress(order.getShippingAddress())
                .orderNote(order.getOrderNote() !=  null ? order.getOrderNote() : null)
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

    public Page<OrderBuyerResponseDto> getAllBuyerOrders(String token, Integer id, Integer page, Integer size, String sortField, String sortDirection) {
        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        UsersAccount usersAccount = usersRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with " + id + " not found"));

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orders = orderRepo.findByUser_Id(usersAccount.getId(), pageable);

        return orders.map(order -> {
            UsersAccount sellerUser = usersRepo.findBySellerId(order.getSeller().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Seller user not found"));

            return OrderBuyerResponseDto.builder()
                    .id(order.getId())
                    .buyerId(order.getUser().getId())
                    .seller(OrderSellerDto.builder()
                            .id(order.getSeller().getId())
                            .email(sellerUser.getEmail())
                            .fullName(order.getSeller().getFullname())
                            .userType(UserTypeResolver.resolveUserType(sellerUser))
                            .nickname(order.getSeller().getNickname())
                            .build())
                    .orderDate(order.getOrderDate())
                    .paymentDate(order.getPaymentDate())
                    .shippingAddress(order.getShippingAddress())
                    .orderNote(order.getOrderNote() != null ? order.getOrderNote() : null)
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

    public Page<OrderSellerResponseDto> getAllSellerOrders(String token, Integer sid, Integer page, Integer size, String sortField, String sortDirection) {
        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired verification token");
        }

        Claims claims = jwtTokenUtil.getClaims(token);
        Integer sidFromToken = claims.get("seller_id", Integer.class);

        if (sidFromToken == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a Seller");
        }

        if (!sidFromToken.equals(sid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User id in access token not matched with resource id");
        }

        SellerAccount sellerAccount = sellerRepo.findById(sid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User with " + sid + " not found"));

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Order> orders = orderRepo.findBySeller_Id(sellerAccount.getId(), pageable);

        return orders.map(order ->
                OrderSellerResponseDto.builder()
                        .id(order.getId())
                        .sellerId(order.getSeller().getId())
                        .buyer(OrderBuyerDto.builder()
                                .id(order.getUser().getId())
                                .username(order.getUser().getFullname())
                                .build())
                        .orderDate(order.getOrderDate() != null ? order.getOrderDate() : null)
                        .paymentDate(order.getPaymentDate())
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
                        .build()
        );
    }
}