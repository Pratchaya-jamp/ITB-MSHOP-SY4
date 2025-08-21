// FileService.java (Updated)
package intregrated.backend.services;

import intregrated.backend.FileStorageProperties;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Getter
public class FileService {
    private final Path fileStorageLocation;

    @Autowired
    public FileService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();
        try {
            if (!Files.exists(this.fileStorageLocation)) {
                Files.createDirectories(this.fileStorageLocation);
            }
        } catch (IOException ex) {
            throw new RuntimeException(
                    "Can't create the directory where the uploaded files will be stored.", ex);
        }
    }

    public Resource loadFileAsResource(String fileName) throws IOException {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new IOException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new IOException("File not found " + fileName, ex);
        }
    }

    /** save/upload ไฟล์ใหม่ */
    public String saveFile(MultipartFile file, String targetFileName) {
        try {
            Path targetLocation = this.fileStorageLocation.resolve(targetFileName).normalize();
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return targetFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file " + targetFileName, e);
        }
    }

    /** rename/move ไฟล์ (ใช้ตอน reorder/move รูป) */
    public void renameFile(String oldName, String newName) {
        try {
            Path oldPath = this.fileStorageLocation.resolve(oldName).normalize();
            Path newPath = this.fileStorageLocation.resolve(newName).normalize();

            if (!Files.exists(oldPath)) {
                throw new RuntimeException("File not found: " + oldName);
            }

            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to rename file: " + oldName + " -> " + newName, e);
        }
    }

    /** delete ไฟล์ */
    public void deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file " + fileName, e);
        }
    }

    /** อ่านขนาดไฟล์ */
    public long sizeOf(String fileName) throws IOException {
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        if (Files.exists(targetLocation)) {
            return Files.size(targetLocation);
        }
        return 0L;
    }
}