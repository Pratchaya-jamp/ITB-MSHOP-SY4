package intregrated.backend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import intregrated.backend.dtos.NewSaleItemDto;
import intregrated.backend.dtos.SaleItemBaseByIdDto;
import intregrated.backend.dtos.SaleItemBaseDto;
import intregrated.backend.dtos.SaleItemWithImageInfo;
import intregrated.backend.entities.SaleItemBase;
import intregrated.backend.services.SaleItemBaseService;
import intregrated.backend.utils.ListMapper;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v1/sale-items")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
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
        SaleItemBaseByIdDto dto = saleItemBaseService.getSaleItemBaseRepoById(id); // เรียก method ใหม่
        return ResponseEntity.ok(dto);
    }

    @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SaleItemBaseByIdDto> createSaleItem(
            @RequestPart("saleItem") String saleItemJson,
            @RequestPart(value = "pictures", required = false) MultipartFile[] pictures
    ) throws JsonProcessingException {

        NewSaleItemDto newSaleItem = new ObjectMapper().readValue(saleItemJson, NewSaleItemDto.class);

        SaleItemBaseByIdDto created = saleItemBaseService.createSaleItem(newSaleItem, pictures);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }



    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SaleItemBaseByIdDto> editSaleItem(
            @PathVariable Integer id,
            @ModelAttribute SaleItemWithImageInfo request) {

        SaleItemBaseByIdDto updated = saleItemBaseService
                .editSaleItem(id, request.getSaleItem(), request.getImageInfos());

        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSaleItem(@PathVariable Integer id) {
        saleItemBaseService.deleteSaleItem(id);
    }
}
