package intregrated.backend.controllers;

import intregrated.backend.dtos.SaleItemBaseByIdDto;
import intregrated.backend.dtos.SaleItemBaseDto;

import intregrated.backend.entities.SaleItemBase;
import intregrated.backend.services.SaleItemBaseService;
import intregrated.backend.utils.ListMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
