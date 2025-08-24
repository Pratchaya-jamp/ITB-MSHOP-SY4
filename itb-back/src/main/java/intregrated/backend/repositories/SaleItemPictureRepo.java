package intregrated.backend.repositories;

import intregrated.backend.entities.SaleItemPicture;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SaleItemPictureRepo extends JpaRepository<SaleItemPicture, Integer> {
    List<SaleItemPicture> findBySale_IdOrderByPictureOrderAsc(Integer saleId);

    @Transactional
    @Modifying
    @Query("DELETE FROM SaleItemPicture p WHERE p.sale.id = :saleId")
    void deleteBySaleItemId(@Param("saleId") Integer saleId);

    // เพิ่ม method สำหรับหา records ที่ไม่อยู่ใน keepFileNames (สำหรับ cleanup)
    @Query("SELECT p FROM SaleItemPicture p WHERE p.sale.id = :saleId AND p.newPictureName NOT IN :keepFileNames")
    List<SaleItemPicture> findBySale_IdAndNewPictureNameNotIn(@Param("saleId") Integer saleId, @Param("keepFileNames") Set<String> keepFileNames);

    // เพิ่ม method สำหรับลบ records ที่ไม่อยู่ใน keepFileNames
    @Transactional
    @Modifying
    @Query("DELETE FROM SaleItemPicture p WHERE p.sale.id = :saleId AND p.newPictureName NOT IN :keepFileNames")
    void deleteBySale_IdAndNewPictureNameNotIn(@Param("saleId") Integer saleId, @Param("keepFileNames") Set<String> keepFileNames);

    Optional<SaleItemPicture> findFirstBySale_IdOrderByPictureOrderAsc(Integer saleId);
}



