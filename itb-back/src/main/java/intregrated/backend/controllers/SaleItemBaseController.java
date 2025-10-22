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

    @GetMapping("")
    public ResponseEntity<List<SaleItemBaseDto>> getAllSaleItemBases() {
        List<SaleItemBaseDto> saleItemBases = saleItemBaseService.getAllSaleItemBase();
        return ResponseEntity.ok(saleItemBases);
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

        saleItemBaseService.validatePaginationParams(page, size, sortDirection, sortField);

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

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SaleItemBaseByIdDto> createSaleItem(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestPart(value = "saleItem") NewSaleItemDto newSaleItem,
            @RequestPart(value = "pictures", required = false) MultipartFile[] pictures
    ) {
        String token = authHeader.replace("Bearer ", "");

        SaleItemBaseByIdDto created = saleItemBaseService.createSaleItem(newSaleItem, pictures, token);

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
