package intregrated.backend.dtos.saleItems;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaleItemWithImageInfo {
    @NotNull
    private NewSaleItemDto saleItem;

    private List<SaleItemImageRequest> imageInfos;
}
