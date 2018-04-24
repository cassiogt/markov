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
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author ctatsch
 */
@Controller
public class IndexController {

    private final StorageService storageService;

    @Autowired
    public IndexController(StorageService storageService) {
        this.storageService = storageService;
    }

    @RequestMapping(value = {"", "/"})
    public String index() {
        return "index.html";
    }

    @RequestMapping(value = "/convert")
    public ResponseEntity<String> execute(@RequestParam(value = "steps", defaultValue = "1000") Integer steps, @RequestParam(value = "text") String text) {
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
                    .calculateStepsUntil(steps);

            return new ResponseEntity<String>(ms.toJson(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<String>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
        }
    }
}
