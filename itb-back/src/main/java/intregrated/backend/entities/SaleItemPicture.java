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
@Table(name = "sale_item_picture")
public class SaleItemPicture {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleItemBase sale;

    @Size(max = 255)
    @NotNull
    @Column(name = "old_picture_name", nullable = false)
    private String oldPictureName;

    @Size(max = 255)
    @NotNull
    @Column(name = "new_picture_name", nullable = false)
    private String newPictureName;

    @NotNull
    @Column(name = "file_size_bytes", nullable = false)
    private Integer fileSizeBytes;

    @NotNull
    @Column(name = "picture_order", nullable = false)
    private Integer pictureOrder;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "createdOn", nullable = false)
    private Instant createdOn;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updatedOn", nullable = false)
    private Instant updatedOn;

}