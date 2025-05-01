package intregrated.backend.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleItemBaseDto {
    private Integer id;
    private String model;
//    @JsonIgnore
    private String brand;
//    public String getbrandName() {
//        return brand;
//    }
    private Integer price;
    private Integer ramGb;
    private Integer storageGb;
    private String color;
}
