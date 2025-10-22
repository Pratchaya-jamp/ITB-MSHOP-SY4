package intregrated.backend.repositories.accounts;

import intregrated.backend.entities.accounts.BuyerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerAccountRepo extends JpaRepository<BuyerAccount, Integer> {
}