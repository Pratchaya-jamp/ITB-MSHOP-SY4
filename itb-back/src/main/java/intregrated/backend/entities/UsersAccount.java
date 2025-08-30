package intregrated.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "users_account")
public class UsersAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid", nullable = false)
    private Integer id;

    @Size(max = 255)
    @NotNull
    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Size(max = 255)
    @NotNull
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 255)
    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @Size(max = 255)
    @NotNull
    @Column(name = "fullname", nullable = false)
    private String fullname;

    // buyer เป็น optional
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "buyerid")
    private BuyerAccount buyer;

    // seller เป็น optional
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "sellerid")
    private SellerAccount seller;

    @Column(nullable = false)
    private Boolean isActive = false;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "createdOn", nullable = false)
    private Instant createdOn;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updatedOn", nullable = false)
    private Instant updatedOn;

    @PreUpdate
    public void setUpdatedOn() {
        this.updatedOn = Instant.now();
    }
}