package intregrated.backend.dtos.saleItems;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SaleItemBaseDto {
    private Integer id;
    private Integer sellerId;
    private String sellerName;
    private String model;
    private String brandName;
    private Integer price;
    private Integer ramGb;
    private Integer storageGb;
    private String color;
}