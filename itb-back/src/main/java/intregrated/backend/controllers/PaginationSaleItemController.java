package intregrated.backend.controllers;

import intregrated.backend.dtos.PageResponseDto;
import intregrated.backend.dtos.SaleItemBaseByIdDto;
import intregrated.backend.services.SaleItemBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/sale-items")
@CrossOrigin(origins = "http://intproj24.sit.kmutt.ac.th")
public class PaginationSaleItemController {
    @Autowired
    SaleItemBaseService saleItemBaseService;

    @GetMapping("")
    public ResponseEntity<PageResponseDto<SaleItemBaseByIdDto>> getAllV2SaleItems(
            @RequestParam(required = false) List<String> filterBrands,
            @RequestParam Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection
    ) {
        if (filterBrands == null) {
            filterBrands = List.of();
        }

        Page<SaleItemBaseByIdDto> saleItems = saleItemBaseService.getPagedSaleItems(
                filterBrands, page, size, sortField, sortDirection
        );

        PageResponseDto<SaleItemBaseByIdDto> response = PageResponseDto.<SaleItemBaseByIdDto>builder()
                .content(saleItems.getContent())
                .page(saleItems.getNumber())
                .size(saleItems.getSize())
                .totalPages(saleItems.getTotalPages())
                .totalElements(saleItems.getTotalElements())
                .first(saleItems.isFirst())
                .last(saleItems.isLast())
                .sort(sortField + ": " + sortDirection.toUpperCase())
                .build();

        return ResponseEntity.ok(response);
    }
}
