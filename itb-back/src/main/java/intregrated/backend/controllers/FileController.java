package intregrated.backend.controllers;

import intregrated.backend.fileproperties.ProductFileProperties;
import intregrated.backend.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.net.URLConnection;

@RestController
@RequestMapping("/v2/sale-items")
@CrossOrigin(origins = "${app.cors.allowedOrigins}")
public class FileController {
    @Autowired
    ProductFileProperties fileStorageProperties;

    @Autowired
    @Qualifier("productFileService")
    private FileService productFileService;

    @GetMapping("/test")
    public ResponseEntity<Object> testPropertiesMapping() {
        return ResponseEntity.ok(productFileService.getFileStorageLocation() + " has been created !!!");
    }

    @GetMapping("/allow-file-type")
    public ResponseEntity<Object> testPropertiesMappingArray() {
        return ResponseEntity.ok(fileStorageProperties.getAllowFileTypes());
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Resource resource = productFileService.loadFileAsResource(filename);

            String contentType = URLConnection.guessContentTypeFromName(resource.getFilename());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}