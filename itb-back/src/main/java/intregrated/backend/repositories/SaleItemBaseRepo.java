package intregrated.backend.repositories;

import intregrated.backend.entities.SaleItemBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SaleItemBaseRepo extends JpaRepository<SaleItemBase, Integer> {

//    @Query("""
//        SELECT s FROM SaleItemBase s
//        JOIN s.brand b
//        WHERE (:brands IS NULL OR LOWER(b.name) IN :brands)
//          AND (
//               (:storages IS NOT NULL AND s.storageGb IN :storages)
//               OR (:storageIsNull = true AND s.storageGb IS NULL)
//               OR (:storages IS NULL AND :storageIsNull = false)
//          )
//          AND (
//               :priceLower IS NULL OR :priceUpper IS NULL
//               OR s.price BETWEEN :priceLower AND :priceUpper
//          )
//    """)
//    Page<SaleItemBase> findWithFilters(
//            @Param("brands") List<String> brands,
//            @Param("storages") List<Integer> storages,
//            @Param("storageIsNull") boolean storageIsNull,
//            @Param("priceLower") Integer priceLower,
//            @Param("priceUpper") Integer priceUpper,
//            Pageable pageable
//    );

    @Query("""
    SELECT s FROM SaleItemBase s
    JOIN s.brand b
    WHERE (:brands IS NULL OR LOWER(b.name) IN :brands)
      AND (
            (:storages IS NOT NULL AND s.storageGb IN :storages)
           OR (:storageIsNull = true AND s.storageGb IS NULL)
      )
      AND (
           :priceLower IS NULL OR :priceUpper IS NULL
           OR s.price BETWEEN :priceLower AND :priceUpper
      )
    """)
    Page<SaleItemBase> findWithFilters(
            @Param("brands") List<String> brands,
            @Param("storages") List<Integer> storages,
            @Param("storageIsNull") boolean storageIsNull,
            @Param("priceLower") Integer priceLower,
            @Param("priceUpper") Integer priceUpper,
            Pageable pageable
    );
}