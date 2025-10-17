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

        // ดึงข้อมูล saleItems ของ seller
        Page<SellerWithSaleItemsDto> pagedResult =
                saleItemBaseService.getPagedSaleItemsBySeller(token, sellerId, page, size, sortField, sortDirection);

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
