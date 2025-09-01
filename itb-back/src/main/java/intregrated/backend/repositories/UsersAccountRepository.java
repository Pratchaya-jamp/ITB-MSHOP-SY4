package intregrated.backend.repositories;

import intregrated.backend.entities.UsersAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersAccountRepository extends JpaRepository<UsersAccount, Integer> {
    Optional<UsersAccount> findByEmail(String email);
}