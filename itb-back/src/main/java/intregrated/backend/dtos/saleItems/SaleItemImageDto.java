package intregrated.backend.dtos.saleItems;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaleItemImageDto {
    private String fileName;
    private Integer imageViewOrder;
}

