package intregrated.backend.utils;

import intregrated.backend.fileproperties.ProductFileProperties;
import intregrated.backend.fileproperties.SellerFileProperties;
import intregrated.backend.services.FileService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Configuration
@EnableConfigurationProperties({ProductFileProperties.class, SellerFileProperties.class})
public class FileServiceConfig {

    @Bean(name = "productFileService")
    public FileService productFileService(ProductFileProperties props) {
        return new FileService(props.getUploadDir(), props.getAllowFileTypes());
    }

    @Bean(name = "sellerFileService")
    public FileService sellerFileService(SellerFileProperties props) {
        return new FileService(props.getUploadDir(), props.getAllowFileTypes());
    }
}