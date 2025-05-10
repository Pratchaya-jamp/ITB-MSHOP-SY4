package intregrated.backend.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewSaleItemDto {
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
    @NotNull
    private Integer quantity;
    private Integer storageGb;
    private String color;
}
