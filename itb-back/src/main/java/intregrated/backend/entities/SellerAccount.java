package intregrated.backend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "seller_account")
public class SellerAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sellerid", nullable = false)
    private Integer id;

    @NotBlank
    @NotNull
    @Column(name = "nickname", nullable = false)
    private String nickname;

    @NotBlank
    @NotNull
    @Column(name = "fullname", nullable = false)
    private String fullname;

    @Size(max = 255)
    @NotNull
    @Column(name = "mobile", nullable = false)
    private String mobile;

    @Size(max = 255)
    @NotNull
    @Column(name = "bankNumber", nullable = false)
    private String bankNumber;

    @Size(max = 255)
    @NotNull
    @Column(name = "bankName", nullable = false)
    private String bankName;

    @Size(max = 255)
    @NotNull
    @Column(name = "nationalId", nullable = false)
    private String nationalId;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "createdOn", nullable = false)
    private Instant createdOn;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updatedOn", nullable = false)
    private Instant updatedOn;

    @OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SellerPicture> pictures = new ArrayList<>();

}