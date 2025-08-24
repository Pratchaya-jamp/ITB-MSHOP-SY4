package intregrated.backend.repositories;

import intregrated.backend.entities.SaleItemPicture;
import intregrated.backend.entities.SellerPicture;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SellerPictureRepository extends JpaRepository<SellerPicture, Integer> {
    // ดึงรูปทั้งหมดของ Seller ตาม sellerId (เรียงตาม order)
    List<SellerPicture> findBySeller_IdOrderByPictureOrderAsc(Integer sellerId);

    // ลบรูปทั้งหมดของ Seller ตาม sellerId
    @Transactional
    @Modifying
    @Query("DELETE FROM SellerPicture p WHERE p.seller.id = :sellerId")
    void deleteBySellerId(@Param("sellerId") Integer sellerId);

    // ดึงรูปที่ไม่อยู่ใน keepFileNames (สำหรับ cleanup)
    @Query("SELECT p FROM SellerPicture p WHERE p.seller.id = :sellerId AND p.newPictureName NOT IN :keepFileNames")
    List<SellerPicture> findBySeller_IdAndNewPictureNameNotIn(@Param("sellerId") Integer sellerId, @Param("keepFileNames") Set<String> keepFileNames);

    // ลบรูปที่ไม่อยู่ใน keepFileNames
    @Transactional
    @Modifying
    @Query("DELETE FROM SellerPicture p WHERE p.seller.id = :sellerId AND p.newPictureName NOT IN :keepFileNames")
    void deleteBySeller_IdAndNewPictureNameNotIn(@Param("sellerId") Integer sellerId, @Param("keepFileNames") Set<String> keepFileNames);
}