package intregrated.backend.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import intregrated.backend.dtos.BuyerRegisterRequest;
import intregrated.backend.dtos.NewSaleItemDto;
import intregrated.backend.dtos.SellerRegisterRequest;
import intregrated.backend.dtos.SellerRegisterResponse;
import intregrated.backend.entities.UsersAccount;
import intregrated.backend.services.EmailRegisterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/v2/registers")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class EmailRegisterController {
    @Autowired
    private EmailRegisterService emailRegisterService;

    @GetMapping("")
    private ResponseEntity<List<UsersAccount>> getAllUsersAccounts() {
        List<UsersAccount> accounts = emailRegisterService.getAllUsers();
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @PostMapping("/buyer")
    public ResponseEntity<?> registerBuyer(@Valid @RequestBody BuyerRegisterRequest request) {
        UsersAccount account = emailRegisterService.registerBuyer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PostMapping(value = "/seller", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerSeller(
            @RequestPart("seller") String sellerJson,
            @RequestPart(value = "pictures", required = false) MultipartFile[] pictures
    ) throws JsonProcessingException {
        SellerRegisterRequest seller = new ObjectMapper().readValue(sellerJson, SellerRegisterRequest.class);

        SellerRegisterResponse newSeller = emailRegisterService.registerSeller(seller, pictures);
        return ResponseEntity.status(HttpStatus.CREATED).body(newSeller);
    }

    @DeleteMapping("/{uid}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Integer uid) {
        emailRegisterService.deleteUser(uid);
    }
}
