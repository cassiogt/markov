package br.com.sasc.markov.controllers;

import java.io.FileNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.sasc.markov.services.storage.StorageService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * File Upload controller.
 *
 * @author Cássio Tatsch (tatschcassio@gmail.com)
 */
@RestController
@RequestMapping("/api")
public class FileUploadController {

    private final StorageService storageService;

    /**
     * Class constructor.
     *
     * @param storageService a {@link StorageService} instance.
     */
    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    /**
     * Stores a uploaded file.
     *
     * @param file               the uploaded file.
     * @param redirectAttributes page redirection attributes.
     * @return a success or error message.
     */
    @PostMapping("/uploadfile")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

        try {
            storageService.store(file);
            return "You successfully uploaded - " + file.getOriginalFilename();
        } catch (Exception e) {
            return "FAIL! Maybe You had uploaded the file before or the file's size > 500KB";
        }
    }

    /**
     * File upload exception handler
     *
     * @param exc the exception
     * @return NOT_FOUND response.
     */
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(FileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
