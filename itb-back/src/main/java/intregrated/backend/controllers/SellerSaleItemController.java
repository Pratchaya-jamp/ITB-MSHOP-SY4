package intregrated.backend.controllers;

import intregrated.backend.dtos.paginations.PageResponseDto;
import intregrated.backend.dtos.saleItems.SellerWithSaleItemsDto;
import intregrated.backend.services.SaleItemBaseService;
import intregrated.backend.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/v2/sellers")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class SellerSaleItemController {
    @Autowired
    private SaleItemBaseService saleItemBaseService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @GetMapping("/{sellerId}/sale-items")
    public PageResponseDto<SellerWithSaleItemsDto> getSellerSaleItems(
            @PathVariable Integer sellerId,
            @RequestParam Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "ASC") String sortDirection,
            @RequestHeader("Authorization") String token
    ) {
        // ตรวจสอบสิทธิ์
        if (token == null || !token.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        token = token.substring(7); // ตัด "Bearer "

        if (!jwtTokenUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Token");
        }

        Integer sellerIdFromToken = jwtTokenUtil.getClaims(token).get("seller_id", Integer.class);
        String role = jwtTokenUtil.getClaims(token).get("role", String.class);

        if (!"SELLER".equals(role)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Only seller can access this resource");
        }

        if (!sellerIdFromToken.equals(sellerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Request seller id not matched with id in access token");
        }

        // ดึงข้อมูล saleItems ของ seller
        Page<SellerWithSaleItemsDto> pagedResult =
                saleItemBaseService.getPagedSaleItemsBySeller(sellerId, page, size, sortField, sortDirection);

        return PageResponseDto.<SellerWithSaleItemsDto>builder()
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
}
