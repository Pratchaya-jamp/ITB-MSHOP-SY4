package intregrated.backend.repositories;

import intregrated.backend.entities.accounts.SellerPicture;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface SellerPictureRepo extends JpaRepository<SellerPicture, Integer> {
}