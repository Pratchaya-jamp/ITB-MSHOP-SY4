package intregrated.backend.controllers;

import intregrated.backend.dtos.orders.OrderRequestDto;
import intregrated.backend.dtos.orders.OrderBuyerResponseDto;
import intregrated.backend.dtos.orders.OrderSellerResponseDto;
import intregrated.backend.dtos.paginations.PageResponseDto;
import intregrated.backend.services.OrderService;
import intregrated.backend.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/v2")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/users/{id}/orders")
    public ResponseEntity<PageResponseDto<OrderBuyerResponseDto>> getAllBuyerOrders(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        // ตรวจสอบสิทธิ์
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        token = token.substring(7); // ตัด "Bearer "

        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        Page<OrderBuyerResponseDto> orderPage =
                orderService.getAllBuyerOrders(id, page, size, sortField, sortDirection);

        PageResponseDto<OrderBuyerResponseDto> response = PageResponseDto.<OrderBuyerResponseDto>builder()
                .content(orderPage.getContent())
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalPages(orderPage.getTotalPages())
                .totalElements(orderPage.getTotalElements())
                .first(orderPage.isFirst())
                .last(orderPage.isLast())
                .sort(sortField + ": " + sortDirection)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sellers/{sid}/orders")
    public ResponseEntity<PageResponseDto<OrderSellerResponseDto>> getAllSellerOrders(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer sid,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "ASC") String sortDirection
    ) {
        // ตรวจสอบสิทธิ์
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        token = token.substring(7); // ตัด "Bearer "

        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        // ดึง claims
        Claims claims = jwtTokenUtil.getClaims(token);
        Integer sellerId = claims.get("seller_id", Integer.class);

        if (sellerId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a Seller");
        }

        if (!sellerId.equals(sid)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User id in access token not matched with resource id");
        }

        Page<OrderSellerResponseDto> orderPage =
                orderService.getAllSellerOrders(sid, page, size, sortField, sortDirection);

        PageResponseDto<OrderSellerResponseDto> response = PageResponseDto.<OrderSellerResponseDto>builder()
                .content(orderPage.getContent())
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .totalPages(orderPage.getTotalPages())
                .totalElements(orderPage.getTotalElements())
                .first(orderPage.isFirst())
                .last(orderPage.isLast())
                .sort(sortField + ": " + sortDirection)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderBuyerResponseDto> getOrderById(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId
    ) {
        // ตรวจสอบสิทธิ์
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        token = token.substring(7); // ตัด "Bearer "

        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        // ดึง claims
        Claims claims = jwtTokenUtil.getClaims(token);
        String role = claims.get("role", String.class);
        Integer buyerId = claims.get("buyer_id", Integer.class);
        Integer sellerId = claims.get("seller_id", Integer.class);

        OrderBuyerResponseDto orderResponseDto = orderService.getOrderById(role, buyerId, sellerId, orderId);
        return ResponseEntity.ok(orderResponseDto);
    }

    @PostMapping("/orders")
    public ResponseEntity<List<OrderBuyerResponseDto>> placeOrder(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody List<@Valid OrderRequestDto> orderRequestDto
    ) {
        // ตรวจสอบสิทธิ์
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        token = token.substring(7); // ตัด "Bearer "

        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        Integer userId = jwtTokenUtil.getClaims(token).get("id", Integer.class);

        List<OrderBuyerResponseDto> orderResponseDto = orderService.placeOrder(orderRequestDto, userId);
        return ResponseEntity.ok(orderResponseDto);
    }
}
