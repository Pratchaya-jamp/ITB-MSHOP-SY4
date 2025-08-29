package intregrated.backend.dtos.saleItems;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder(
        { "id", "model", "brandName", "price", "ramGb", "storageGb", "color" }
)
public class SaleItemBaseDto {
    private Integer id;
    private String model;
    private String brandName;
    private Integer price;
    private Integer ramGb;
    private Integer storageGb;
    private String color;
}