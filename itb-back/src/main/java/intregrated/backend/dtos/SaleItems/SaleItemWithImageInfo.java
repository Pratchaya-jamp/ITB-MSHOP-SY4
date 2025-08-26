package intregrated.backend.dtos.SaleItems;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaleItemWithImageInfo {
    private NewSaleItemDto saleItem;
    private List<SaleItemImageRequest> imageInfos;
}
