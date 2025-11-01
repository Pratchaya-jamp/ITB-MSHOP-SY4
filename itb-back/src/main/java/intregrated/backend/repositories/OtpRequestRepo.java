package intregrated.backend.repositories;

import intregrated.backend.entities.OtpRequest;
import intregrated.backend.entities.accounts.UsersAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtpRequestRepo extends JpaRepository<OtpRequest, Integer> {

    @Query("SELECT o FROM OtpRequest o WHERE o.user = :user ORDER BY o.createdOn DESC")
    Optional<OtpRequest> findTopByUserOrderByCreatedOnDesc(@Param("user") UsersAccount user);

    @Query("SELECT o FROM OtpRequest o WHERE o.user = :user AND o.isUsed = false ORDER BY o.createdOn DESC")
    Optional<OtpRequest> findTopUnusedByUserOrderByCreatedOnDesc(@Param("user") UsersAccount user);

    @Transactional
    @Modifying
    @Query("DELETE FROM OtpRequest o WHERE o.user = :user")
    void deleteAllByUser(@Param("user") UsersAccount user);
}
