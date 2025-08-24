package intregrated.backend.services;

import lombok.Getter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Getter
public class FileService {
    private final Path fileStorageLocation;
    private final String[] allowFileTypes;

    public FileService(String uploadDir, String[] allowFileTypes) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.allowFileTypes = allowFileTypes;
        try {
            if (!Files.exists(this.fileStorageLocation)) {
                Files.createDirectories(this.fileStorageLocation);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Can't create directory " + uploadDir, ex);
        }
    }

    public boolean isFileTypeAllowed(String ext) {
        for (String type : allowFileTypes) {
            if (type.equalsIgnoreCase(ext)) {
                return true;
            }
        }
        return false;
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

    public String saveFile(MultipartFile file, String targetFileName) {
        try {
            Path targetLocation = this.fileStorageLocation.resolve(targetFileName).normalize();
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return targetFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file " + targetFileName, e);
        }
    }

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

    public long sizeOf(String fileName) throws IOException {
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        if (Files.exists(targetLocation)) {
            return Files.size(targetLocation);
        }
        return 0L;
    }
}