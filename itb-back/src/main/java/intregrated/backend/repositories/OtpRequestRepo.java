package intregrated.backend.repositories;

import intregrated.backend.entities.OtpRequest;
import intregrated.backend.entities.accounts.UsersAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface OtpRequestRepo extends JpaRepository<OtpRequest, Integer> {

    @Query("SELECT o FROM OtpRequest o WHERE o.user = :user ORDER BY o.createdOn DESC")
    Optional<OtpRequest> findTopByUserOrderByCreatedOnDesc(UsersAccount user);

    // find latest unused OTP (optional)
    @Query("SELECT o FROM OtpRequest o WHERE o.user = :user AND o.isUsed = false ORDER BY o.createdOn DESC")
    Optional<OtpRequest> findTopUnusedByUserOrderByCreatedOnDesc(UsersAccount user);
}
