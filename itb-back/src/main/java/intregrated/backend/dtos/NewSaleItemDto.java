package intregrated.backend.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewSaleItemDto {
    private Integer id;
    @NotNull
    private String model;
    @NotNull
    private BrandBaseDto brand;
    @NotNull
    private String description;
    @NotNull
    private Integer price;
    private Integer ramGb;
    private Double screenSizeInch;
    private Integer quantity;
    private Integer storageGb;
    private String color;
}
