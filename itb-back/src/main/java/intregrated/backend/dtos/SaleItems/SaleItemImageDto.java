package intregrated.backend.dtos.SaleItems;

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

