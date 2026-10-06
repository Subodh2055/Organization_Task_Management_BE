package com.organization.taskmanagement.storage;

import com.organization.taskmanagement.common.exception.ApiException;
import com.organization.taskmanagement.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/** Stores uploaded files on disk under random names; the original name is kept in the database. */
@Slf4j
@Service
public class FileStorageService {

    private final Path root;

    public FileStorageService(AppProperties properties) {
        this.root = Path.of(properties.storage().location()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new UncheckedIOException("Cannot create storage directory " + root, ex);
        }
        log.info("Attachments are stored in {}", root);
    }

    /** Saves the file and returns the generated name to store in the database. */
    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw ApiException.badRequest("The file is empty");
        }
        String storedName = UUID.randomUUID().toString();
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException("Could not store file", ex);
        }
        return storedName;
    }

    public Resource load(String storedName) {
        Path path = resolve(storedName);
        if (!Files.exists(path)) {
            throw ApiException.notFound("The file is no longer available");
        }
        return new PathResource(path);
    }

    public void delete(String storedName) {
        try {
            Files.deleteIfExists(resolve(storedName));
        } catch (IOException ex) {
            log.warn("Could not delete stored file {}: {}", storedName, ex.getMessage());
        }
    }

    private Path resolve(String storedName) {
        Path path = root.resolve(storedName).normalize();
        if (!path.startsWith(root)) {
            throw ApiException.badRequest("Invalid file name");
        }
        return path;
    }
}
