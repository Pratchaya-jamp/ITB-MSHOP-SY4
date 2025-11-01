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
@Table(name = "user_remember_token")
public class UserRememberToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersAccount user;

    @Size(max = 255)
    @Column(name = "remember_token", nullable = false, length = 255, unique = true)
    private String rememberToken;

    @Column(name = "remember_until", nullable = false)
    private Instant rememberUntil;

    @Column(name = "is_valid", nullable = false)
    @ColumnDefault("1")
    private Boolean isValid = true;

    @Column(name = "created_on", nullable = false)
    private Instant createdOn;

    @Column(name = "updated_on", nullable = false)
    private Instant updatedOn;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        this.createdOn = now;
        this.updatedOn = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedOn = Instant.now();
    }
}
