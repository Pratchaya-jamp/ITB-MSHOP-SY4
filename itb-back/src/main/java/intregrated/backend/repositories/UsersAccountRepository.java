package intregrated.backend.repositories;

import intregrated.backend.entities.UsersAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersAccountRepository extends JpaRepository<UsersAccount, Integer> {
    boolean existsByEmail(String email);
}