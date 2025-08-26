package intregrated.backend.controllers;

import intregrated.backend.dtos.Brands.BrandBaseByIdDto;
import intregrated.backend.dtos.Brands.BrandBaseDto;
import intregrated.backend.dtos.Brands.NewBrandBaseDto;
import intregrated.backend.entities.BrandBase;
import intregrated.backend.services.BrandBaseService;
import intregrated.backend.utils.ListMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/brands")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class BrandBaseController {
    @Autowired
    BrandBaseService brandBaseService;

    @Autowired
    private ModelMapper modelMapper;


    @GetMapping("")
    public ResponseEntity<List<BrandBaseDto>> getAllBrandBases() {
        List<BrandBase> brandBases = brandBaseService.getAllBrandBase();
        return ResponseEntity.ok(ListMapper.mapList(brandBases, BrandBaseDto.class, modelMapper));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandBaseByIdDto> getBrandBaseById(@PathVariable Integer id) {
        BrandBaseByIdDto brandBase = brandBaseService.getBrandBaseById(id);
        return ResponseEntity.ok(modelMapper.map(brandBase, BrandBaseByIdDto.class));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBrandBaseById(@PathVariable Integer id) {
        brandBaseService.deleteBrandBaseById(id);
    }

    @PostMapping("")
    public ResponseEntity<BrandBaseByIdDto> createBrandBase(@RequestBody NewBrandBaseDto newBrandBase) {
        BrandBaseByIdDto created = brandBaseService.createBrandBase(newBrandBase);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BrandBaseByIdDto> editBrandBaseById(@PathVariable Integer id, @RequestBody NewBrandBaseDto updatedBrandBase) {
        BrandBaseByIdDto editUpdated = brandBaseService.editBrandBase(id, updatedBrandBase);
        return ResponseEntity.ok(editUpdated);
    }
}
