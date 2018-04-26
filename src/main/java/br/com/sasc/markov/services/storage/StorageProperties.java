package br.com.sasc.markov.services.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Storage service properties class.
 *
 */
@ConfigurationProperties("storage")
@Data
public class StorageProperties {

    /**
     * Folder location for storing files.
     */
    private String location = String.format("%s/sasc", System.getProperty("java.io.tmpdir"));

    /**
     * Flag that indicates to clean directory before upload files.
     */
    private Boolean cleanDirectoryBeforeUpload = Boolean.TRUE;

}
