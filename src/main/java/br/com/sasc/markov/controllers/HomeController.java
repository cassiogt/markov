/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.sasc.markov.controllers;

import br.com.sasc.markov.services.MarkovService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author ctatsch
 */
@RestController
public class HomeController {

    @RequestMapping("/")
    public ResponseEntity<?> execute(@RequestParam(value = "input", defaultValue = "Cassio") String input) {
        try {
            MarkovService ms = new MarkovService()
                    .from(input)
                    .enableSteps()
                    .addLabels("A", "B", "C")
                    .execute();
            return new ResponseEntity<>(ms.toJson(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.PRECONDITION_FAILED);
        }
    }
}