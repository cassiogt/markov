package br.com.sasc.markov;

import com.fasterxml.jackson.core.JsonProcessingException;

import br.com.sasc.markov.services.storage.StorageProperties;
import br.com.sasc.markov.services.storage.StorageService;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Application Main Class
 *
 * @author Cássio Tatsch (tatschcassio@gmail.com)
 */
@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class MarkovApplication {

    public static void main(String[] args) throws JsonProcessingException {
        SpringApplication application = new SpringApplication(MarkovApplication.class);
        application.setLogStartupInfo(false);
        application.run(args);
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI("http://localhost:9000"));
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }
}
