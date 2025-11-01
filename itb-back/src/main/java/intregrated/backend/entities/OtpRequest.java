package intregrated.backend.entities;

import intregrated.backend.entities.accounts.UsersAccount;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "otp_request")
public class OtpRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersAccount user;

    @Size(max = 10)
    @Column(name = "otp_code", nullable = false, length = 10)
    private String otpCode;

    @Column(name = "is_used", nullable = false)
    @ColumnDefault("0")
    private Boolean isUsed = false;

    @Column(name = "expired_at", nullable = false)
    private Instant expiredAt;

    @Column(name = "last_requested_at", nullable = false)
    private Instant lastRequestedAt;

    @Column(name = "created_on", nullable = false)
    private Instant createdOn;

    @Column(name = "updated_on", nullable = false)
    private Instant updatedOn;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdOn = now;
        this.updatedOn = now;
        if (this.lastRequestedAt == null) this.lastRequestedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = Instant.now();
    }
}
