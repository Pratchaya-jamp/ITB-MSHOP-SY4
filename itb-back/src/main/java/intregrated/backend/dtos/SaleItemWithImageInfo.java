package intregrated.backend.dtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class SaleItemWithImageInfo {
    private NewSaleItemDto saleItem;
    private List<SaleItemImageRequest> imageInfos;
}
