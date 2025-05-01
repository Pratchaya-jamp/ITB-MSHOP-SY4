package intregrated.backend.services;

import intregrated.backend.entities.SaleItemBase;
import intregrated.backend.repositories.SaleItemBaseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SaleItemBaseService {
    @Autowired
    private SaleItemBaseRepo saleItemBaseRepo;

    public List<SaleItemBase> getAllSaleItemBase() {
        return saleItemBaseRepo.findAll();
    }

    public SaleItemBase getSaleItemBaseRepoById(Integer id) {
        return saleItemBaseRepo.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,"BrandBase with id" + id + "not found"
                )
        );
    }
}

