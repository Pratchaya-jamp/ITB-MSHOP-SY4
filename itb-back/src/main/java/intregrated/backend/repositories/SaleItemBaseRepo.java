package intregrated.backend.repositories;

import intregrated.backend.entities.SaleItemBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SaleItemBaseRepo extends JpaRepository<SaleItemBase, Integer> {
    Page<SaleItemBase> findByBrand_NameInIgnoreCase(List<String> brandNames, Pageable pageable);

    @Query("SELECT s FROM SaleItemBase s JOIN s.brand b WHERE (:brands IS NULL OR LOWER(b.name) IN :brands)")
    Page<SaleItemBase> findAllWithBrandNameFilter(@Param("brands") List<String> brands, Pageable pageable);

}
