package intregrated.backend.services;

import intregrated.backend.dtos.NewSaleItemDto;
import intregrated.backend.dtos.NewSaleItemResponseDto;
import intregrated.backend.dtos.SaleItemBaseDto;
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Duplicate saleItem ID: " + newSaleItem.getId());
        }

        BrandBase brand = brandBaseRepo.findByNameIgnoreCase(newSaleItem.getBrand().getBrandName())
                .orElseGet(() -> {
                    BrandBase newBrand = new BrandBase();
                    newBrand.setName(newSaleItem.getBrand().getBrandName());
                    newBrand.setIsActive(true);
                    newBrand.setCreatedOn(Instant.now());
                    newBrand.setUpdatedOn(Instant.now());
                    return brandBaseRepo.saveAndFlush(newBrand);
                });

        SaleItemBase saleItem = new SaleItemBase();
        saleItem.setModel(newSaleItem.getModel().trim());
        saleItem.setDescription(newSaleItem.getDescription().trim());
        saleItem.setPrice(newSaleItem.getPrice());
        saleItem.setRamGb(newSaleItem.getRamGb() != null ? newSaleItem.getRamGb() : null);
        saleItem.setScreenSizeInch(newSaleItem.getScreenSizeInch() != null ? BigDecimal.valueOf(newSaleItem.getScreenSizeInch()) : null);
        saleItem.setQuantity(newSaleItem.getQuantity() != null ? newSaleItem.getQuantity() : 1);
        saleItem.setStorageGb(newSaleItem.getStorageGb() != null ? newSaleItem.getStorageGb() : null);
        saleItem.setColor(newSaleItem.getColor() != null ? newSaleItem.getColor().trim() : null);
        saleItem.setCreatedOn(Instant.now());
        saleItem.setUpdatedOn(Instant.now());
        saleItem.setBrand(brand);

        SaleItemBase saved = saleItemBaseRepo.saveAndFlush(saleItem);

        return NewSaleItemResponseDto.builder()
                .id(saved.getId())
                .model(saved.getModel().trim())
                .brandName(saved.getBrand().getName())
                .description(saved.getDescription().trim())
                .price(saved.getPrice())
                .ramGb(saved.getRamGb())
                .screenSizeInch(saved.getScreenSizeInch() != null ? saved.getScreenSizeInch().doubleValue() : null)
                .quantity(saved.getQuantity())
                .storageGb(saved.getStorageGb())
                .color(saved.getColor())
                .createdOn(saved.getCreatedOn())
                .updatedOn(saved.getUpdatedOn())
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

    public NewSaleItemResponseDto editSaleItem(NewSaleItemDto newSaleItem) {
        SaleItemBase existing = saleItemBaseRepo.findById(newSaleItem.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "SaleItem with id " + newSaleItem.getId() + " not found"
                ));

        existing.setModel(newSaleItem.getModel().trim());
        existing.setDescription(newSaleItem.getDescription().trim());
        existing.setPrice(newSaleItem.getPrice());
        existing.setRamGb(newSaleItem.getRamGb() != null ? newSaleItem.getRamGb() : null);
        existing.setScreenSizeInch(newSaleItem.getScreenSizeInch() != null ? BigDecimal.valueOf(newSaleItem.getScreenSizeInch()) : null);
        existing.setQuantity(newSaleItem.getQuantity() != null ? newSaleItem.getQuantity() : 1);
        existing.setStorageGb(newSaleItem.getStorageGb() != null ? newSaleItem.getStorageGb() : null);
        existing.setColor(newSaleItem.getColor() != null ? newSaleItem.getColor().trim() : null);
        existing.setUpdatedOn(Instant.now());

        if (newSaleItem.getBrand() != null && newSaleItem.getBrand().getBrandName() != null) {
            BrandBase brand = brandBaseRepo.findByNameIgnoreCase(newSaleItem.getBrand().getBrandName())
                    .orElseGet(() -> {
                        BrandBase newBrand = new BrandBase();
                        newBrand.setName(newSaleItem.getBrand().getBrandName());
                        newBrand.setIsActive(true);
                        newBrand.setCreatedOn(Instant.now());
                        newBrand.setUpdatedOn(Instant.now());
                        return brandBaseRepo.saveAndFlush(newBrand);
                    });
            existing.setBrand(brand);
        }

        SaleItemBase saved = saleItemBaseRepo.saveAndFlush(existing);

        return NewSaleItemResponseDto.builder()
                .id(saved.getId())
                .model(saved.getModel().trim())
                .brandName(saved.getBrand().getName())
                .description(saved.getDescription().trim())
                .price(saved.getPrice())
                .ramGb(saved.getRamGb())
                .screenSizeInch(saved.getScreenSizeInch() != null ? saved.getScreenSizeInch().doubleValue() : null)
                .quantity(saved.getQuantity())
                .storageGb(saved.getStorageGb())
                .color(saved.getColor())
                .createdOn(saved.getCreatedOn())
                .updatedOn(saved.getUpdatedOn())
                .build();
    }
}


