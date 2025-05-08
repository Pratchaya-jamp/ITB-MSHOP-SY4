package intregrated.backend.controllers;

import intregrated.backend.dtos.NewSaleItemDto;
import intregrated.backend.dtos.NewSaleItemResponseDto;
import intregrated.backend.dtos.SaleItemBaseByIdDto;
import intregrated.backend.dtos.SaleItemBaseDto;

import intregrated.backend.entities.SaleItemBase;
import intregrated.backend.services.SaleItemBaseService;
import intregrated.backend.utils.ListMapper;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/sale-items")
public class SaleItemBaseController {
    @Autowired
    SaleItemBaseService saleItemBaseService;

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
        SaleItemBase saleItemBase = saleItemBaseService.getSaleItemBaseRepoById(id);

        return ResponseEntity.ok(modelMapper.map(saleItemBase, SaleItemBaseByIdDto.class));
    }

    @PostMapping("")
    public ResponseEntity<NewSaleItemResponseDto> createCustomer(@RequestBody @Valid NewSaleItemDto newSaleItem) {
        NewSaleItemResponseDto created = saleItemBaseService.createSaleItem(newSaleItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSaleItem(@PathVariable Integer id) {
        saleItemBaseService.deleteSaleItem(id);
    }
}
