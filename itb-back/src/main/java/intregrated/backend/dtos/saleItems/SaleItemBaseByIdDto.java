package intregrated.backend.dtos.saleItems;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemBaseByIdDto {
    private Integer id;
    private Integer sellerId;
    private String sellerName;
    private String model;
    private String brandName;
    private String description;
    private Integer price;
    private Integer ramGb;
    private Double screenSizeInch;
    private Integer quantity;
    private Integer storageGb;
    private String color;
    private Instant createdOn;
    private Instant updatedOn;

    private String firstImageName;
    private List<SaleItemImageDto> saleItemImages;
}
