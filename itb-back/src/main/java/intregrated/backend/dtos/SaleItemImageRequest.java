package intregrated.backend.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaleItemImageRequest {
    private Integer order;
    private String fileName;
    private String status;
    private MultipartFile imageFile;
}