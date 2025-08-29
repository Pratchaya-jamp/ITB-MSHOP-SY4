package intregrated.backend.services;

import intregrated.backend.dtos.brands.BrandBaseByIdDto;
import intregrated.backend.dtos.brands.NewBrandBaseDto;
import intregrated.backend.entities.BrandBase;
import intregrated.backend.repositories.BrandBaseRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class BrandBaseService {
    @Autowired
    private BrandBaseRepo brandBaseRepo;

    public List<BrandBase> getAllBrandBase() {
        return brandBaseRepo.findAll();
    }

    public BrandBaseByIdDto getBrandBaseById(Integer id) {
        BrandBase brandBase = brandBaseRepo.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"BrandBase with id " + id + " not found"
                )
        );

        return BrandBaseByIdDto.builder()
                .id(brandBase.getId())
                .name(brandBase.getName())
                .websiteUrl(brandBase.getWebsiteUrl())
                .countryOfOrigin(brandBase.getCountryOfOrigin())
                .isActive(brandBase.getIsActive())
                .noOfSaleItems(brandBase.getSaleItemBases() != null ? brandBase.getSaleItemBases().size() : 0)
                .build();
    }

    @Transactional
    public void deleteBrandBaseById(Integer id) {
        BrandBase brandBase = brandBaseRepo.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Brand with id " + id + " not found"
                )
        );

        if (brandBase.getSaleItemBases() != null && !brandBase.getSaleItemBases().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "BrandBase with id " + id + " has sale items");
        } else {
            brandBaseRepo.deleteById(id);
        }
    }

    public BrandBaseByIdDto createBrandBase(NewBrandBaseDto newBrandBase) {
        String trimmedName = newBrandBase.getName() != null ? newBrandBase.getName().trim() : null;

        BrandBase brandBase = new BrandBase();

        if (brandBaseRepo.findByNameIgnoreCase(trimmedName).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate brand name");
        }

        if (trimmedName == null || trimmedName.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand name cannot be null or empty");
        }

        if (newBrandBase.getWebsiteUrl() == null || newBrandBase.getWebsiteUrl().isEmpty()) {
            brandBase.setWebsiteUrl(null);
        } else {
            brandBase.setWebsiteUrl(newBrandBase.getWebsiteUrl());
        }

        if (newBrandBase.getCountryOfOrigin() == null || newBrandBase.getCountryOfOrigin().isEmpty()) {
            brandBase.setCountryOfOrigin(null);
        } else {
            brandBase.setCountryOfOrigin(newBrandBase.getCountryOfOrigin());
        }

        brandBase.setName(trimmedName);
        brandBase.setIsActive(newBrandBase.getIsActive() != null ? newBrandBase.getIsActive() : true);
        brandBase.setCreatedOn(Instant.now());
        brandBase.setUpdatedOn(Instant.now());

        BrandBase saved = brandBaseRepo.saveAndFlush(brandBase);

        return BrandBaseByIdDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .websiteUrl(saved.getWebsiteUrl())
                .countryOfOrigin(saved.getCountryOfOrigin())
                .isActive(saved.getIsActive() != null ? saved.getIsActive() : true)
                .noOfSaleItems(brandBase.getSaleItemBases() != null ? brandBase.getSaleItemBases().size() : 0)
                .build();
    }

    public BrandBaseByIdDto editBrandBase(Integer id, NewBrandBaseDto updatedBrandBase) {
        String trimmedName = updatedBrandBase.getName() != null ? updatedBrandBase.getName().trim() : null;

        BrandBase existing = brandBaseRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Brand with id " + id + " not found"
                ));

        Optional<BrandBase> duplicate = brandBaseRepo.findByNameIgnoreCase(updatedBrandBase.getName().trim());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate brand name");
        }

        if (trimmedName == null || trimmedName.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Brand name cannot be null or empty");
        }

        if (updatedBrandBase.getWebsiteUrl() == null || updatedBrandBase.getWebsiteUrl().isEmpty()) {
            existing.setWebsiteUrl(null);
        } else {
            existing.setWebsiteUrl(updatedBrandBase.getWebsiteUrl());
        }

        if (updatedBrandBase.getCountryOfOrigin() == null || updatedBrandBase.getCountryOfOrigin().isEmpty()) {
            existing.setCountryOfOrigin(null);
        } else {
            existing.setCountryOfOrigin(updatedBrandBase.getCountryOfOrigin());
        }

        existing.setName(trimmedName);
        existing.setIsActive(updatedBrandBase.getIsActive() != null ? updatedBrandBase.getIsActive() : true);
        existing.setUpdatedOn(Instant.now());

        BrandBase saved = brandBaseRepo.saveAndFlush(existing);

        return BrandBaseByIdDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .websiteUrl(saved.getWebsiteUrl())
                .countryOfOrigin(saved.getCountryOfOrigin())
                .isActive(saved.getIsActive())
                .noOfSaleItems(existing.getSaleItemBases() != null ? existing.getSaleItemBases().size() : 0)
                .build();
    }
}
