package intregrated.backend.controllers;

import intregrated.backend.dtos.carts.CartRequestDto;
import intregrated.backend.dtos.carts.CartResponseDto;
import intregrated.backend.services.CartService;
import intregrated.backend.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v2/carts")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class CartController {
    @Autowired
    CartService cartService;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @PostMapping("")
    public ResponseEntity<CartResponseDto> addToCart(
            @RequestHeader("Authorization") String token,
            @RequestBody CartRequestDto request
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

        CartResponseDto cart = cartService.addToCart(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }
}
