package com.banjaraghar.backend.storage;

import com.banjaraghar.backend.config.AppProperties;
import com.banjaraghar.backend.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService implements FileStorageService {

    private static final String URL_PREFIX = "/uploads/";
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    private final Path root;

    public LocalFileStorageService(AppProperties appProperties) {
        this.root = Path.of(appProperties.getStorage().getUploadDir()).toAbsolutePath().normalize();
    }

    @Override
    public String storeImage(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose an image to upload");
        }
        String extension = EXTENSIONS.get(file.getContentType());
        if (extension == null) {
            throw new BadRequestException("Only JPG, PNG or WebP images are allowed");
        }

        // The name is generated here; the customer's file name is never used
        String fileName = UUID.randomUUID() + "." + extension;
        Path target = root.resolve(folder).resolve(fileName).normalize();
        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            throw new IllegalStateException("Could not store the uploaded file", e);
        }
        return URL_PREFIX + folder + "/" + fileName;
    }

    @Override
    public void delete(String url) {
        if (url == null || !url.startsWith(URL_PREFIX)) {
            return;
        }
        Path target = root.resolve(url.substring(URL_PREFIX.length())).normalize();
        if (!target.startsWith(root)) {
            return;   // never delete outside the upload folder
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Could not delete file {}", target, e);
        }
    }
}