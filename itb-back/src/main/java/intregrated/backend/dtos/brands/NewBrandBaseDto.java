package intregrated.backend.dtos.brands;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewBrandBaseDto {
    @NotNull
    @NotEmpty
    private String name;
    @NotEmpty
    private String websiteUrl;
    @NotEmpty
    private String countryOfOrigin;
    private Boolean isActive;
}
