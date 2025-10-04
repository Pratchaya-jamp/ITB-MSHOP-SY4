package intregrated.backend.controllers;

import intregrated.backend.dtos.paginations.PageResponseDto;
import intregrated.backend.dtos.saleItems.NewSaleItemDto;
import intregrated.backend.dtos.saleItems.SaleItemBaseByIdDto;
import intregrated.backend.dtos.saleItems.SaleItemBaseDto;
import intregrated.backend.dtos.saleItems.SaleItemWithImageInfo;
import intregrated.backend.entities.saleitems.SaleItemBase;
import intregrated.backend.services.SaleItemBaseService;
import intregrated.backend.utils.JwtTokenUtil;
import intregrated.backend.utils.ListMapper;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/v2/sale-items")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class SaleItemBaseController {
    @Autowired
    SaleItemBaseService saleItemBaseService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("")
    public ResponseEntity<List<SaleItemBaseDto>> getAllSaleItemBases() {
        List<SaleItemBase> saleItemBases = saleItemBaseService.getAllSaleItemBase();

        return ResponseEntity.ok(
                ListMapper.mapList(saleItemBases, SaleItemBaseDto.class, modelMapper)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleItemBaseByIdDto> getSaleItemBaseById(@PathVariable Integer id) {
        SaleItemBaseByIdDto dto = saleItemBaseService.getSaleItemBaseById(id); // เรียก method ใหม่
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/page-sale-items")
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

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SaleItemBaseByIdDto> createSaleItem(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestPart(value = "saleItem") NewSaleItemDto newSaleItem,
            @RequestPart(value = "pictures", required = false) MultipartFile[] pictures
    ) {
        String token = authHeader.replace("Bearer ", "");
        Integer sellerId = jwtTokenUtil.getClaims(token).get("seller_id", Integer.class);
        String role = jwtTokenUtil.getClaims(token).get("role", String.class);

        if (!"SELLER".equalsIgnoreCase(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only sellers can create sale items");
        }

        SaleItemBaseByIdDto created = saleItemBaseService.createSaleItem(newSaleItem, pictures, sellerId);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SaleItemBaseByIdDto> editSaleItem(
            @PathVariable Integer id,
            @ModelAttribute SaleItemWithImageInfo request) {

        SaleItemBaseByIdDto updated = saleItemBaseService.editSaleItem(id, request);

        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSaleItem(@PathVariable Integer id) {
        saleItemBaseService.deleteSaleItem(id);
    }

}
