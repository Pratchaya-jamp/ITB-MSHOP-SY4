package intregrated.backend.repositories;

import intregrated.backend.entities.SaleItemBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SaleItemBaseRepo extends JpaRepository<SaleItemBase, Integer> {

    @Query("""
        SELECT s FROM SaleItemBase s
        JOIN s.brand b
        WHERE (:brands IS NULL OR LOWER(b.name) IN :brands)
          AND (:storages IS NULL OR s.storageGb IN :storages)
          AND (
               :priceLower IS NULL OR :priceUpper IS NULL 
               OR s.price BETWEEN :priceLower AND :priceUpper
          )
    """)
    Page<SaleItemBase> findWithFilters(
            @Param("brands") List<String> brands,
            @Param("storages") List<Integer> storages,
            @Param("priceLower") Integer priceLower,
            @Param("priceUpper") Integer priceUpper,
            Pageable pageable
    );
}

