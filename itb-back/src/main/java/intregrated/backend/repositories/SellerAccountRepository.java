package intregrated.backend.repositories;

import intregrated.backend.entities.SellerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerAccountRepository extends JpaRepository<SellerAccount, Integer> {
}