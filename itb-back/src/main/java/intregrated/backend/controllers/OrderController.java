package intregrated.backend.controllers;

import intregrated.backend.dtos.orders.OrderListRequestDto;
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

        Page<OrderBuyerResponseDto> orderPage =
                orderService.getAllBuyerOrders(token, id, page, size, sortField, sortDirection);

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

        Page<OrderSellerResponseDto> orderPage =
                orderService.getAllSellerOrders(token, sid, page, size, sortField, sortDirection);

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

        OrderBuyerResponseDto orderResponseDto = orderService.getOrderById(token, orderId);
        return ResponseEntity.ok(orderResponseDto);
    }

    @PostMapping("/orders")
    public ResponseEntity<List<OrderBuyerResponseDto>> placeOrder(
            @RequestHeader("Authorization") String token,
            @Valid @RequestBody OrderListRequestDto orderRequestDto
    ) {
        // ตรวจสอบสิทธิ์
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        token = token.substring(7); // ตัด "Bearer "

        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        List<OrderBuyerResponseDto> orderResponseDto = orderService.placeOrder(orderRequestDto.getOrders(), token);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderResponseDto);
    }
}
