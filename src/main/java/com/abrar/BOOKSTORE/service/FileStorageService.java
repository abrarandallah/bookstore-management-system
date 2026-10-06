package com.abrar.BOOKSTORE.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    // Deliberately narrow: only real image types, checked against the
    // browser-supplied content type. Not foolproof (a client can lie about
    // content type), but it's a first filter against someone uploading an
    // .html/.jsp/.exe disguised as an image. Combined with saving under a
    // random UUID name (not the original filename) and never executing
    // anything from the upload directory.
    private static final Set<String> ALLOWED_TYPES = Set.of("image/png", "image/jpeg", "image/webp", "image/gif");
    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5MB

    @Value("${app.upload-dir}")
    private String uploadDir;

    /**
     * @return the public path (under /uploads/) the file was saved to.
     * @throws IllegalArgumentException if the file is missing, too large, or
     *                                  not a recognized image type. The message
     *                                  is a translation key (upload.error.*) so
     *                                  the page can show it in the reader's language.
     */
    public String store(MultipartFile file, String subfolder) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("upload.error.none");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new IllegalArgumentException("upload.error.size");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("upload.error.type");
        }

        try {
            Path targetDir = Paths.get(uploadDir, subfolder);
            Files.createDirectories(targetDir);

            String extension = switch (contentType) {
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                case "image/gif" -> ".gif";
                default -> ".jpg";
            };
            String filename = UUID.randomUUID() + extension;
            Path target = targetDir.resolve(filename);
            file.transferTo(target);

            return "/uploads/" + subfolder + "/" + filename;
        } catch (IOException e) {
            throw new IllegalArgumentException("upload.error.save", e);
        }
    }
}