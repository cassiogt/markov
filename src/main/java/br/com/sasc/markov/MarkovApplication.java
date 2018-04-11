package br.com.sasc.markov;

import br.com.sasc.markov.services.MarkovService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MarkovApplication {

    public static void main(String[] args) throws JsonProcessingException {
        SpringApplication.run(MarkovApplication.class, args);

//        String text = "-15 10 5\n9 -21 12\n1 4 -5\n";
//
//        try {
//            MarkovService ms = new MarkovService()
//                    .from(text)
//                    .enableSteps()
//                    .addLabels("A", "B", "C")
//                    .execute();
//            System.out.println(ms.toJson());
//        } catch (IllegalArgumentException nfe) {
//            System.out.println("" + nfe.getMessage());
//        }

    }
}
