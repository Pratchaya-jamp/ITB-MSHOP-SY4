package intregrated.backend.controllers;

import intregrated.backend.dtos.paginations.PageResponseDto;
import intregrated.backend.dtos.saleItems.SaleItemBaseByIdDto;
import intregrated.backend.services.SaleItemBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v2/sale-items")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class PaginationSaleItemController {

    @Autowired
    private SaleItemBaseService saleItemBaseService;

    @GetMapping
    public PageResponseDto<SaleItemBaseByIdDto> getPagedSaleItemsV2(
            @RequestParam(required = false) List<String> filterBrands,
            @RequestParam(required = false) List<String> filterStorages,
            @RequestParam(required = false) Integer filterPriceLower,
            @RequestParam(required = false) Integer filterPriceUpper,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String sortField,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        validatePaginationParams(page, size, sortDirection, sortField);

        boolean storageIsNullFlag = false;
        List<Integer> storages = null;

        if (filterStorages != null && !filterStorages.isEmpty()) {
            storages = new ArrayList<>();
            for (String storage : filterStorages) {
                if ("null".equalsIgnoreCase(storage)) {
                    storageIsNullFlag = true;
                } else {
                    try {
                        storages.add(Integer.valueOf(storage));
                    } catch (NumberFormatException e) {
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid storage value: " + storage
                        );
                    }
                }
            }
            if (storages.isEmpty()) {
                storages = null;
            }
        }

        Page<SaleItemBaseByIdDto> pagedResult = saleItemBaseService.getPagedSaleItems(
                filterBrands,
                storages,
                storageIsNullFlag,
                filterPriceLower,
                filterPriceUpper,
                searchKeyword,
                page,
                size,
                sortField,
                sortDirection
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
                "id", "model", "price", "ramGb", "screenSizeInch", "storageGb", "createdOn", "updatedOn", "brand.name"
        );
        if (!validSortFields.contains(sortField)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Sort field '" + sortField + "' is invalid. Must be one of: " + validSortFields
            );
        }
    }
}
