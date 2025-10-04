package intregrated.backend.controllers;

import intregrated.backend.dtos.orders.OrderRequestDto;
import intregrated.backend.dtos.orders.OrderResponseDto;
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

    @GetMapping("/users/{userId}/orders")
    public ResponseEntity<PageResponseDto<OrderResponseDto>> getAllOrders(
            @RequestHeader("Authorization") String token,
            @PathVariable Integer userId,
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

        Page<OrderResponseDto> orderPage =
                orderService.getAllOrders(userId, page, size, sortField, sortDirection);

        PageResponseDto<OrderResponseDto> response = PageResponseDto.<OrderResponseDto>builder()
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
    public ResponseEntity<OrderResponseDto> getOrderById(
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
        Integer buyerId = claims.get("buyer_id", Integer.class);
        Integer sellerId = claims.get("seller_id", Integer.class);

        OrderResponseDto orderResponseDto = orderService.getOrderById(buyerId, sellerId, orderId);
        return ResponseEntity.ok(orderResponseDto);
    }

    @PostMapping("/orders")
    public ResponseEntity<List<OrderResponseDto>> placeOrder(
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

        List<OrderResponseDto> orderResponseDto = orderService.placeOrder(orderRequestDto, userId);
        return ResponseEntity.ok(orderResponseDto);
    }
}
