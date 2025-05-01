package intregrated.backend.services;

import intregrated.backend.entities.BrandBase;
import intregrated.backend.repositories.BrandBaseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
}
