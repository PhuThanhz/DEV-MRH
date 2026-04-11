package vn.system.app.modules.file.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    @Value("${lotusgroup.upload-file.dir}")
    private String rootDir;

    public void createDirectory(String folder) throws IOException {
        Path dirPath = Paths.get(rootDir, folder);
        Files.createDirectories(dirPath);
    }

    public String store(MultipartFile file, String folder) throws IOException {
        String originalName = file.getOriginalFilename();

        // Sanitize tên file — xóa dấu cách và ký tự đặc biệt
        String safeName = originalName
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_");

        String finalName = System.currentTimeMillis() + "-" + safeName;

        Path folderPath = Paths.get(rootDir, folder);
        Files.createDirectories(folderPath);

        Path filePath = folderPath.resolve(finalName);
        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING);

        return finalName;
    }

    public long getFileLength(String fileName, String folder) {
        Path filePath = Paths.get(rootDir, folder, fileName);
        File file = filePath.toFile();

        if (!file.exists() || file.isDirectory()) {
            return 0;
        }
        return file.length();
    }

    public InputStreamResource getResource(String fileName, String folder)
            throws FileNotFoundException {

        Path filePath = Paths.get(rootDir, folder, fileName);
        File file = filePath.toFile();

        return new InputStreamResource(new FileInputStream(file));
    }
}