package intregrated.backend.dtos.saleItems;

import intregrated.backend.dtos.brands.BrandBaseDto;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    private String description;
    @NotNull
    private Integer price;
    private Integer ramGb;
    private Double screenSizeInch;
    private Integer quantity;
    private Integer storageGb;
    private String color;
}
