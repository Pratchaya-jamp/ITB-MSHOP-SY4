package intregrated.backend.dtos.saleItems;

import intregrated.backend.dtos.brands.BrandBaseDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class NewSaleItemDto {
    @NotNull(message = "Model is required")
    private String model;
    @NotNull(message = "Brand is required")
    private BrandBaseDto brand;
    @NotBlank(message = "Description is required")
    private String description;
    @NotNull(message = "Price is required")
    private Integer price;
    private Integer ramGb;
    private Double screenSizeInch;
    private Integer quantity;
    private Integer storageGb;
    private String color;
}
