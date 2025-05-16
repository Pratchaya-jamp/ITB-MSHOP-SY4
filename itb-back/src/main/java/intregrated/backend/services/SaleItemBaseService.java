package intregrated.backend.services;

import intregrated.backend.dtos.NewSaleItemDto;
import intregrated.backend.dtos.SaleItemBaseByIdDto;
import intregrated.backend.entities.BrandBase;
import intregrated.backend.entities.SaleItemBase;
import intregrated.backend.repositories.BrandBaseRepo;
import intregrated.backend.repositories.SaleItemBaseRepo;
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

    public SaleItemBaseByIdDto createSaleItem(NewSaleItemDto newSaleItem) {
        BrandBase brand;

        if (newSaleItem.getBrand() != null) {
            if (newSaleItem.getBrand().getId() != null) {
                brand = brandBaseRepo.findById(newSaleItem.getBrand().getId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Brand with id " + newSaleItem.getBrand().getId() + " not found"
                        ));
            } else if (newSaleItem.getBrand().getBrandName() != null && !newSaleItem.getBrand().getBrandName().trim().isEmpty()) {
                String trimmedBrandName = newSaleItem.getBrand().getBrandName().trim();
                brand = brandBaseRepo.findByNameIgnoreCase(trimmedBrandName)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Brand with name \"" + trimmedBrandName + "\" not found"
                        ));
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand must contain either valid ID or name.");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand is required.");
        }

        SaleItemBase saleItem = new SaleItemBase();
        saleItem.setModel(newSaleItem.getModel().trim());
        saleItem.setDescription(newSaleItem.getDescription().trim());
        saleItem.setPrice(newSaleItem.getPrice());
        saleItem.setRamGb(newSaleItem.getRamGb());
        saleItem.setScreenSizeInch(newSaleItem.getScreenSizeInch() != null ? BigDecimal.valueOf(newSaleItem.getScreenSizeInch()) : null);
        if (newSaleItem.getQuantity() == null || newSaleItem.getQuantity() < 0) {
            saleItem.setQuantity(1);
        } else {
            saleItem.setQuantity(newSaleItem.getQuantity());
        }
        saleItem.setStorageGb(newSaleItem.getStorageGb() != null ? newSaleItem.getStorageGb() : null);
        if (newSaleItem.getColor() == null || newSaleItem.getColor().trim().isEmpty()) {
            saleItem.setColor(null);
        } else {
            saleItem.setColor(newSaleItem.getColor().trim());
        }
        saleItem.setCreatedOn(Instant.now());
        saleItem.setUpdatedOn(Instant.now());
        saleItem.setBrand(brand);

        SaleItemBase saved = saleItemBaseRepo.saveAndFlush(saleItem);

        return SaleItemBaseByIdDto.builder()
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
                .createdOn(saved.getCreatedOn())
                .updatedOn(saved.getUpdatedOn())
                .build();
    }

    public SaleItemBaseByIdDto editSaleItem(Integer id, NewSaleItemDto newSaleItem) {
        SaleItemBase existing = saleItemBaseRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "SaleItem with id " + id + " not found"
                ));

        existing.setModel(newSaleItem.getModel().trim());
        existing.setDescription(newSaleItem.getDescription().trim());
        existing.setPrice(newSaleItem.getPrice());
        existing.setRamGb(newSaleItem.getRamGb() != null ? newSaleItem.getRamGb() : null);
        existing.setStorageGb(newSaleItem.getStorageGb() != null ? newSaleItem.getStorageGb() : null);
        existing.setScreenSizeInch(newSaleItem.getScreenSizeInch() != null ? BigDecimal.valueOf(newSaleItem.getScreenSizeInch()) : null);
        if (newSaleItem.getQuantity() == null || newSaleItem.getQuantity() < 0) {
            existing.setQuantity(1);
        } else {
            existing.setQuantity(newSaleItem.getQuantity());
        }
        if (newSaleItem.getColor() == null || newSaleItem.getColor().trim().isEmpty()) {
            existing.setColor(null);
        } else {
            existing.setColor(newSaleItem.getColor().trim());
        }
        existing.setUpdatedOn(Instant.now());

        if (newSaleItem.getBrand() != null && newSaleItem.getBrand().getId() != null) {
            BrandBase brand = brandBaseRepo.findById(newSaleItem.getBrand().getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Brand with id " + newSaleItem.getBrand().getId() + " not found"));
            existing.setBrand(brand);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Brand id must be provided.");
        }

        SaleItemBase saved = saleItemBaseRepo.saveAndFlush(existing);

        return SaleItemBaseByIdDto.builder()
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
}


