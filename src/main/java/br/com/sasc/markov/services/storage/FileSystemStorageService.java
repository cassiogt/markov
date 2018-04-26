package br.com.sasc.markov.services.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import lombok.Getter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * StorageService implementation.
 */
@Service
class FileSystemStorageService implements StorageService {

    @Getter
    private final Path rootLocation;
    private final boolean cleanDirectoryBeforeUpload;

    /**
     * Constructor
     *
     * @param properties the storage properties.
     */
    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
        this.cleanDirectoryBeforeUpload = properties.getCleanDirectoryBeforeUpload();
    }

    /**
     * Stores a file on disk.
     *
     * @param file the file to be stored.
     */
    @Override
    public void store(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // Security check
                throw new StorageException("Cannot store file with relative path outside current directory " + filename);
            }
            if (cleanDirectoryBeforeUpload) {
                deleteAll();
                init();
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    /**
     * Load all stored files.
     *
     * @return a list of file path.
     */
    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                    .sorted((o1, o2) -> {
                        try {
                            return Files.getLastModifiedTime(o1).compareTo(Files.getLastModifiedTime(o2));
                        } catch (IOException ex) {
                            return 0;
                        }
                    })
                    .filter(path -> !path.equals(this.rootLocation))
                    .filter(file -> Files.isRegularFile(file))
                    .map(this.rootLocation::relativize);

        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    /**
     * Load a file.
     *
     * @param filename the file name to be loaded.
     * @return the file path.
     */
    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    /**
     * Loads a file as resource.
     *
     * @param filename the file name to be loaded.
     * @return the resource file.
     */
    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    /**
     * Deletes all stored files.
     */
    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    /**
     * Initializes storage service, creating a directory where files will be stored.
     */
    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
