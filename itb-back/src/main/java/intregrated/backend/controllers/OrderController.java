package intregrated.backend.controllers;

import intregrated.backend.dtos.orders.OrderRequestDto;
import intregrated.backend.dtos.orders.OrderResponseDto;
import intregrated.backend.services.OrderService;
import intregrated.backend.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v2/orders")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @PostMapping("")
    public ResponseEntity<OrderResponseDto> placeOrder(
            @RequestHeader("Authorization") String token,
            @RequestBody OrderRequestDto orderRequestDto
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

        OrderResponseDto orderResponseDto = orderService.placeOrder(orderRequestDto, userId);
        return ResponseEntity.ok(orderResponseDto);
    }
}
