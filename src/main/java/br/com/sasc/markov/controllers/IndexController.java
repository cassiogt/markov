/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.sasc.markov.controllers;

import br.com.sasc.markov.services.MarkovService;
import br.com.sasc.markov.services.storage.StorageService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Main controller.
 *
 * @author Cássio Tatsch (tatschcassio@gmail.com)
 */
@Controller
public class IndexController {

    private final StorageService storageService;

    @Autowired
    public IndexController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Home page request
     *
     * @return the home page.
     */
    @RequestMapping(value = {"", "/"})
    public String index() {
        return "index.html";
    }

    /**
     * CTMC to DTMC request
     *
     * @param steps     is the maximum number of steps to be used to test the calculated probabilities.
     * @param stepsType {@code 1} if the maximum number is the sum of at least one entry, or {@code 2} if the maximum number is the sum of all entries.
     * @param text      is the matrix sent through text area.
     * @return a JSON message with all information or an error message if it occurs.
     */
    @RequestMapping(value = "/convert")
    public ResponseEntity<String> execute(@RequestParam(value = "steps", defaultValue = "1000") Integer steps,
                                          @RequestParam(value = "stepsType", defaultValue = "2") Integer stepsType,
                                          @RequestParam(value = "text") String text) {
        try {

            List<String> lines = Collections.emptyList();
            if (!text.isEmpty()) {
                lines = Arrays.asList(text.split("\n"));
            } else {
                Path arquivo = storageService.loadAll().reduce((first, second) -> second).orElse(null);
                if (arquivo != null) {
                    arquivo = storageService.getRootLocation().resolve(arquivo);
                    lines = Files.readAllLines(arquivo);
                }
            }

            MarkovService ms = new MarkovService()
                    .fromListOfLines(lines)
                    .saveAllSteps()
                    .resolveDTMC()
                    .calculateSteps(stepsType, steps);

            return new ResponseEntity<String>(ms.toJson(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }
}
