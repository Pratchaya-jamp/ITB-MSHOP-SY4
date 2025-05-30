package intregrated.backend.controllers;

import intregrated.backend.dtos.PageResponseDto;
import intregrated.backend.dtos.SaleItemBaseByIdDto;
import intregrated.backend.services.SaleItemBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/v2/sale-items")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class PaginationSaleItemController {
    @Autowired
    SaleItemBaseService saleItemBaseService;

    @GetMapping("")
    public ResponseEntity<PageResponseDto<SaleItemBaseByIdDto>> getAllV2SaleItems(
            @RequestParam(required = false) List<String> filterBrands,
            @RequestParam Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection) {

        if (filterBrands == null) {
            filterBrands = List.of();
        }

        validatePaginationParams(page, size, sortDirection, sortField);

        Page<SaleItemBaseByIdDto> saleItems = saleItemBaseService.getPagedSaleItems(
                filterBrands, page, size, sortField, sortDirection);

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

    private void validatePaginationParams(Integer page, Integer size, String sortDirection, String sortField) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page index must not be negative.");
        }

        if (size <= 0 || size > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page size must be between 1 and 100.");
        }

        if (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sort direction must be 'asc' or 'desc'.");
        }

        List<String> validSortFields = List.of(
                "id", "model", "price", "ramGb", "screenSizeInch", "storageGb", "createdOn", "updatedOn", "brand.name");

        if (!validSortFields.contains(sortField)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Sort field '" + sortField + "' is invalid. Must be one of: " + validSortFields);
        }
    }
}
