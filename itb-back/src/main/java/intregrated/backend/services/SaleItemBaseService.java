package intregrated.backend.services;

import intregrated.backend.dtos.NewSaleItemDto;
import intregrated.backend.dtos.NewSaleItemResponseDto;
import intregrated.backend.entities.BrandBase;
import intregrated.backend.entities.SaleItemBase;
import intregrated.backend.repositories.BrandBaseRepo;
import intregrated.backend.repositories.SaleItemBaseRepo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class SaleItemBaseService {
    @Autowired
    private SaleItemBaseRepo saleItemBaseRepo;

    @Autowired
    private BrandBaseRepo brandBaseRepo;

    public List<SaleItemBase> getAllSaleItemBase() {
        return saleItemBaseRepo.findAll();
    }

    public SaleItemBase getSaleItemBaseRepoById(Integer id) {
        return saleItemBaseRepo.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "BrandBase with id: " + id + " is not found"
                )
        );
    }

    public NewSaleItemResponseDto createSaleItem(@Valid NewSaleItemDto newSaleItem) {
        if (saleItemBaseRepo.existsById(newSaleItem.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST
                    , "Duplicate SaleItem id " + newSaleItem.getId());
        }

        BrandBase brand = new BrandBase();
        brand.setName(newSaleItem.getBrand().getName());
        brand.setIsActive(true);
        brand.setCreatedOn(Instant.now());
        brand.setUpdatedOn(Instant.now());
        brand = brandBaseRepo.saveAndFlush(brand);

        SaleItemBase saleItem = new SaleItemBase();
        saleItem.setId(newSaleItem.getId());
        saleItem.setModel(newSaleItem.getModel());
        saleItem.setDescription(newSaleItem.getDescription());
        saleItem.setPrice(newSaleItem.getPrice());
        saleItem.setRamGb(newSaleItem.getRamGb() != null ? newSaleItem.getRamGb() : 0);
        saleItem.setScreenSizeInch(newSaleItem.getScreenSizeInch() != null ? BigDecimal.valueOf(newSaleItem.getScreenSizeInch()) : null);
        saleItem.setQuantity(newSaleItem.getQuantity() != null ? newSaleItem.getQuantity() : 1);
        saleItem.setStorageGb(newSaleItem.getStorageGb() != null ? newSaleItem.getStorageGb() : 0);
        saleItem.setColor(newSaleItem.getColor() != null ? newSaleItem.getColor() : null);
        saleItem.setCreatedOn(Instant.now());
        saleItem.setUpdatedOn(Instant.now());
        saleItem.setBrand(brand);

        SaleItemBase saved = saleItemBaseRepo.saveAndFlush(saleItem);

        return NewSaleItemResponseDto.builder()
                .id(saved.getId())
                .model(saved.getModel())
                .brandName(saved.getBrand().getName())
                .description(saved.getDescription())
                .price(saved.getPrice())
                .ramGb(saved.getRamGb())
                .screenSizeInch(saved.getScreenSizeInch() != null ? saved.getScreenSizeInch().doubleValue() : null)
                .quantity(saved.getQuantity())
                .storageGb(saved.getStorageGb())
                .color(saved.getColor())
                .build();
    }

    public void deleteSaleItem(Integer id) {
        if (! saleItemBaseRepo.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "SaleItem with id " + id + " not found"
            );
        }
        saleItemBaseRepo.deleteById(id);
    }
}


