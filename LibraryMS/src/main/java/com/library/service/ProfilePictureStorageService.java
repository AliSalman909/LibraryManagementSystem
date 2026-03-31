package com.library.service;

import com.library.exception.BusinessRuleException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProfilePictureStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

    private final Path uploadRoot;
    private final long maxFileBytes;
    private final DataSize maxFileSize;

    public ProfilePictureStorageService(
            @Value("${app.upload.dir:${user.home}/.libraryms/uploads/profile-pictures}") String uploadDir,
            @Value("${spring.servlet.multipart.max-file-size}") DataSize maxFileSize) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.maxFileSize = maxFileSize;
        this.maxFileBytes = maxFileSize.toBytes();
    }

    /**
     * Saves the file under the configured project directory and returns the web path
     * (e.g. {@code /uploads/profiles/userId-uuid.jpg}) to store in the database.
     */
    public String store(MultipartFile file, String userId) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        if (file.getSize() > maxFileBytes) {
            throw new BusinessRuleException(
                    "That photo is too large. Please use an image under " + maxFileSize.toMegabytes() + " MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessRuleException(
                    "Please upload a JPG, PNG, GIF, or WebP image. Other file types are not accepted.");
        }
        String ext = resolveExtension(file.getOriginalFilename());
        if (ext == null) {
            ext = extensionFromContentType(contentType);
        }
        if (ext == null) {
            throw new BusinessRuleException(
                    "The file name should end with .jpg, .png, .gif, or .webp so we know it is a supported image.");
        }
        String filename = userId + "-" + UUID.randomUUID() + ext;
        try {
            Files.createDirectories(uploadRoot);
            Path target = uploadRoot.resolve(filename).normalize();
            if (!target.startsWith(uploadRoot)) {
                throw new BusinessRuleException("Invalid file path.");
            }
            file.transferTo(target);
        } catch (IOException e) {
            throw new BusinessRuleException(
                    "We could not save your profile photo. Please try again in a moment or pick a different file.");
        }
        return "/uploads/profiles/" + filename;
    }

    /**
     * Deletes a previously stored profile photo by its web path (returned from {@link #store}).
     *
     * <p>This is used to keep file-system and DB consistent when a DB transaction fails and rolls back.
     */
    public void deleteByWebPath(String webPath) {
        Optional.ofNullable(webPath)
                .filter(p -> !p.isBlank())
                .ifPresent(
                        p -> {
                            String filename = Paths.get(p).getFileName().toString();
                            Path target = uploadRoot.resolve(filename).normalize();
                            try {
                                Files.deleteIfExists(target);
                            } catch (IOException ignored) {
                                // Best-effort cleanup; DB rollback is the primary consistency mechanism.
                            }
                        });
    }

    private String resolveExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return null;
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            return null;
        }
        String ext = originalFilename.substring(dot).toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.contains(ext) ? ext : null;
    }

    private String extensionFromContentType(String contentType) {
        if (contentType == null) {
            return null;
        }
        return switch (contentType.toLowerCase(Locale.ROOT)) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> null;
        };
    }
}
