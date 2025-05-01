package intregrated.backend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleItemBaseByIdDto {
    private Integer id;
    private String model;
//    @JsonIgnore
    private String brand;
//    public String getbrandName() {
//        return brand;
//    }
    private String description;
    private Integer price;
    private Integer ramGb;
    private Double screenSizeInch;
    private Integer quantity;
    private Integer storageGb;
    private String color;
}
