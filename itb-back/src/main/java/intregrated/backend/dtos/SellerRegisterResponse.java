package intregrated.backend.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SellerRegisterResponse {
    @NotBlank
    private String nickname;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 4, max = 40)
    private String fullname;

    @NotBlank
    private String mobile;

    @NotBlank
    private String bankNumber;

    @NotBlank
    private String bankName;

    @NotBlank
    private String nationalId;

    @NotBlank
    private List<SaleItemImageDto> sellerImages;
}
