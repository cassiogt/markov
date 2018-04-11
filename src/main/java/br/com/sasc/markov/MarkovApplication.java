package br.com.sasc.markov;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MarkovApplication {

    public static void main(String[] args) throws JsonProcessingException {
        SpringApplication.run(MarkovApplication.class, args);
    }
}
