package intregrated.backend.fileproperties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@Getter
@Setter
@ConfigurationProperties(prefix = "file.product")
public class ProductFileProperties {
    private String uploadDir;
    private String[] allowFileTypes;
}