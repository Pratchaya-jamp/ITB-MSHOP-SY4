package intregrated.backend.repositories;

import intregrated.backend.entities.SaleItemBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleItemBaseRepo extends JpaRepository<SaleItemBase, Integer> {
    Page<SaleItemBase> findByBrand_NameInIgnoreCase(List<String> brandNames, Pageable pageable);
}
