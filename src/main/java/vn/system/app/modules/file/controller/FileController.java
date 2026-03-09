package vn.system.app.modules.file.controller;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import vn.system.app.common.util.annotation.ApiMessage;
import vn.system.app.common.util.error.StorageException;
import vn.system.app.modules.file.domain.response.ResUploadFileDTO;
import vn.system.app.modules.file.service.FileService;

@RestController
@RequestMapping("/api/v1")
public class FileController {

        private final FileService fileService;

        public FileController(FileService fileService) {
                this.fileService = fileService;
        }

        // =========================
        // UPLOAD FILE
        // =========================
        @PostMapping("/files")
        @ApiMessage("Upload single file")
        public ResponseEntity<ResUploadFileDTO> upload(
                        @RequestParam("file") MultipartFile file,
                        @RequestParam("folder") String folder) throws IOException, StorageException {

                if (file == null || file.isEmpty()) {
                        throw new StorageException("File is empty. Please upload a file.");
                }

                String originalName = file.getOriginalFilename();
                if (originalName == null) {
                        throw new StorageException("Invalid file name.");
                }

                List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");

                boolean isValid = allowedExtensions.stream()
                                .anyMatch(ext -> originalName.toLowerCase().endsWith("." + ext));

                if (!isValid) {
                        throw new StorageException(
                                        "Invalid file extension. Only allows: " + allowedExtensions);
                }

                // tạo folder + lưu file
                fileService.createDirectory(folder);
                String storedFileName = fileService.store(file, folder);

                ResUploadFileDTO res = new ResUploadFileDTO(
                                storedFileName,
                                Instant.now());

                return ResponseEntity.ok(res);
        }

        // =========================
        // DOWNLOAD FILE
        // =========================
        @GetMapping("/files")
        @ApiMessage("Download a file")
        public ResponseEntity<Resource> download(
                        @RequestParam("fileName") String fileName,
                        @RequestParam("folder") String folder) throws StorageException, FileNotFoundException {

                long fileLength = fileService.getFileLength(fileName, folder);
                if (fileLength == 0) {
                        throw new StorageException(
                                        "File with name = " + fileName + " not found.");
                }

                InputStreamResource resource = fileService.getResource(fileName, folder);

                return ResponseEntity.ok()
                                .header(
                                                HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"" + fileName + "\"")
                                .contentLength(fileLength)
                                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                                .body(resource);
        }
}
