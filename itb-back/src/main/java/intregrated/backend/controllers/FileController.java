package intregrated.backend.controllers;

import intregrated.backend.FileStorageProperties;
import intregrated.backend.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
public class FileController {
    @Autowired
    FileStorageProperties fileStorageProperties;

    @Autowired
    FileService fileService;

    @GetMapping("/test")
    public ResponseEntity<Object> testPropertiesMapping() {
        return ResponseEntity.ok(fileService.getFileStorageLocation()+ " has been created !!!");
    }
    @GetMapping("/allow-file-type")
    public ResponseEntity<Object> testPropertiesMappingArray() {
        return ResponseEntity.ok(fileStorageProperties.getAllowFileTypes());
    }
}

