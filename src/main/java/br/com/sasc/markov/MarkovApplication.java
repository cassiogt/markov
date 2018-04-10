package br.com.sasc.markov;

import br.com.sasc.markov.services.MarkovService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication
public class MarkovApplication {

    public static void main(String[] args) {
        //SpringApplication.run(MarkovApplication.class, args);

        String text = "-15 10 5\n"
                + "9 -21 12\n"
                + "1 4 -5\n";

        try {
            MarkovService ms = new MarkovService();
            ms.withInput(text);
        } catch (IllegalArgumentException nfe) {
            System.out.println("" + nfe.getMessage());
        }

    }
}
