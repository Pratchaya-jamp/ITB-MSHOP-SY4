package intregrated.backend.repositories;

import intregrated.backend.entities.UserRememberToken;
import intregrated.backend.entities.accounts.UsersAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;

import jakarta.transaction.Transactional;

public interface UserRememberTokenRepo extends JpaRepository<UserRememberToken, Integer> {
    Optional<UserRememberToken> findByRememberToken(String token);

    @Query("SELECT u FROM UserRememberToken u WHERE u.user = :user AND u.isValid = true ORDER BY u.rememberUntil DESC")
    Optional<UserRememberToken> findValidByUserOrderByRememberUntilDesc(UsersAccount user);

    @Modifying
    @Transactional
    @Query("UPDATE UserRememberToken u SET u.isValid = false WHERE u.user = :user")
    void invalidateAllForUser(UsersAccount user);
}
