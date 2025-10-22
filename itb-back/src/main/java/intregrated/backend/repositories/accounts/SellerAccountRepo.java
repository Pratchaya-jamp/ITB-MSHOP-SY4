package intregrated.backend.repositories.accounts;

import intregrated.backend.entities.accounts.SellerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerAccountRepo extends JpaRepository<SellerAccount, Integer> {
}