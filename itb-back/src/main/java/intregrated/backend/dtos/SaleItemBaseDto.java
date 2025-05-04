package intregrated.backend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.criteria.CriteriaBuilder;
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
    @JsonIgnore
    private String name;

    @JsonProperty("brandName")
    public String getBrandName() {
        return name;
    }

    private Integer price;
    private Integer ramGb;
    private Integer storageGb;
    private String color;
}