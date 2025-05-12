package intregrated.backend.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@JsonPropertyOrder(
        { "id", "model", "brandName", "description", "price", "ramGb", "screenSizeInch", "quantity", "storageGb", "color", "createOn", "updateOn"}
)
public class SaleItemBaseByIdDto {
    private Integer id;
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
}
