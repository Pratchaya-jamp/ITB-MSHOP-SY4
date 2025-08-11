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

    @GetMapping
    public PageResponseDto<SaleItemBaseByIdDto> getPagedSaleItemsV2(
            @RequestParam(required = false) List<String> filterBrands,
            @RequestParam(required = false) List<Integer> filterStorages,
            @RequestParam(required = false) Integer filterPriceLower,
            @RequestParam(required = false) Integer filterPriceUpper,
            @RequestParam(required = true) Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Page<SaleItemBaseByIdDto> pagedResult = saleItemBaseService.getPagedSaleItems(
                filterBrands, filterStorages, filterPriceLower, filterPriceUpper,
                page, size, sortField, sortDirection
        );

        return PageResponseDto.<SaleItemBaseByIdDto>builder()
                .content(pagedResult.getContent())
                .page(pagedResult.getNumber())
                .size(pagedResult.getSize())
                .totalElements(pagedResult.getTotalElements())
                .totalPages(pagedResult.getTotalPages())
                .first(pagedResult.isFirst())
                .last(pagedResult.isLast())
                .sort(sortField + ": " + sortDirection)
                .page(pagedResult.getNumber())
                .build();
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
