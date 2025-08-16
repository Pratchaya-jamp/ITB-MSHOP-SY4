package intregrated.backend.dtos;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ImageInfoDto {
    private Integer order;             // ลำดับรูป
    private MultipartFile imageFile;   // ไฟล์จริง
}


