package intregrated.backend.repositories;

import intregrated.backend.entities.BuyerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuyerAccountRepository extends JpaRepository<BuyerAccount, Integer> {
}