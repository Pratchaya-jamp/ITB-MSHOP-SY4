package intregrated.backend.repositories;

import intregrated.backend.entities.accounts.UsersAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersAccountRepo extends JpaRepository<UsersAccount, Integer> {
    Optional<UsersAccount> findByEmail(String email);
    Optional<UsersAccount> findBySellerId(Integer sellerId);
    Optional<UsersAccount> findByBuyerId(Integer buyerId);
}