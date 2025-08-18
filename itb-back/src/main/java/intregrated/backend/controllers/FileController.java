package intregrated.backend.controllers;

import intregrated.backend.FileStorageProperties;
import intregrated.backend.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
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
public class FileController {
    @Autowired
    FileStorageProperties fileStorageProperties;

    @Autowired
    FileService fileService;

    @GetMapping("/test")
    public ResponseEntity<Object> testPropertiesMapping() {
        return ResponseEntity.ok(fileService.getFileStorageLocation() + " has been created !!!");
    }

    @GetMapping("/allow-file-type")
    public ResponseEntity<Object> testPropertiesMappingArray() {
        return ResponseEntity.ok(fileStorageProperties.getAllowFileTypes());
    }

    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Resource resource = fileService.loadFileAsResource(filename);

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