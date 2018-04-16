package br.com.sasc.markov.services.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
@Data
public class StorageProperties {

    /**
     * Folder location for storing files.
     */
    private String location = String.format("%s/sasc", System.getProperty("java.io.tmpdir"));

    private Boolean cleanDirectoryBeforeUpload = Boolean.TRUE;

    private Integer steps = 1000;

}
