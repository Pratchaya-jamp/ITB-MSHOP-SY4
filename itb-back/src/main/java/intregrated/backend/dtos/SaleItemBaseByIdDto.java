package intregrated.backend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder(
        { "id", "model", "brandName", "description", "price", "ramGb", "screenSizeInch", "quantity", "storageGb", "color" }
)
public class SaleItemBaseByIdDto {
    private Integer id;
    private String model;
    @JsonIgnore
    private String name;

    @JsonProperty("brandName")
    public String getBrandName() {
        return name;
    }

    private String description;
    private Integer price;
    private Integer ramGb;
    private Double screenSizeInch;
    private Integer quantity;
    private Integer storageGb;
    private String color;
}
