package intregrated.backend.repositories;

import intregrated.backend.entities.SaleItemPicture;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SaleItemPictureRepo extends JpaRepository<SaleItemPicture, Integer> {
    List<SaleItemPicture> findBySale_IdOrderByPictureOrderAsc(Integer saleId);

    @Transactional
    @Modifying
    @Query("DELETE FROM SaleItemPicture p WHERE p.sale.id = :saleId")
    void deleteBySaleItemId(@Param("saleId") Integer saleId);
}


