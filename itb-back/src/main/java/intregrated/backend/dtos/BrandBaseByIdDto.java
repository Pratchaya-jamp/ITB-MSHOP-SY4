package intregrated.backend.dtos;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandBaseByIdDto {
    private Integer id;
    private String name;
    private String websiteUrl;
    private String countryOfOrigin;
    private Boolean isActive;
    private Integer noOfSaleItems;
}