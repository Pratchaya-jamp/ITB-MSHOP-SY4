package intregrated.backend.services;

import intregrated.backend.dtos.BrandBaseByIdDto;
import intregrated.backend.dtos.NewBrandBaseDto;
import intregrated.backend.entities.BrandBase;
import intregrated.backend.repositories.BrandBaseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
public class BrandBaseService {
    @Autowired
    private BrandBaseRepo brandBaseRepo;

    public List<BrandBase> getAllBrandBase() {
        return brandBaseRepo.findAll();
    }

    public BrandBase getBrandBaseById(Integer id) {
        return brandBaseRepo.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"BrandBase with id" + id + "not found"
                )
        );
    }

    public void deleteBrandBaseById(Integer id) {
        if (!brandBaseRepo.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Brand with id " + id + " not found"
            );
        }
        brandBaseRepo.deleteById(id);
    }

    public BrandBaseByIdDto createBrandBase(NewBrandBaseDto newBrandBase) {
        BrandBase brandBase = new BrandBase();

        if (brandBaseRepo.findByNameIgnoreCase(newBrandBase.getName()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate brand name");
        }

        if (newBrandBase.getName() == null || newBrandBase.getName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Brand name cannot be null or empty");
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

        brandBase.setName(newBrandBase.getName());
        brandBase.setIsActive(newBrandBase.getIsActive());
        brandBase.setCreatedOn(Instant.now());
        brandBase.setUpdatedOn(Instant.now());

        BrandBase saved = brandBaseRepo.saveAndFlush(brandBase);

        return BrandBaseByIdDto.builder()
                .id(saved.getId())
                .name(saved.getName())
                .websiteUrl(saved.getWebsiteUrl())
                .countryOfOrigin(saved.getCountryOfOrigin())
                .isActive(saved.getIsActive())
                .build();
    }
}
