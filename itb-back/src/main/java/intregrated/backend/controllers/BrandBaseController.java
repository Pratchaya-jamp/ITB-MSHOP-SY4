package intregrated.backend.controllers;

import intregrated.backend.dtos.BrandBaseDto;
import intregrated.backend.entities.BrandBase;
import intregrated.backend.services.BrandBaseService;
import intregrated.backend.utils.ListMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/brands")
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
    public ResponseEntity<BrandBase> getBrandBaseById(@PathVariable Integer id) {
        return ResponseEntity.ok(brandBaseService.getBrandBaseById(id));
    }

    @DeleteMapping("/{id}")
    public void deleteBrandBaseById(@PathVariable Integer id) {
        brandBaseService.deleteBrandBaseById(id);
    }
}
