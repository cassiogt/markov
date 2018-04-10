/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.sasc.markov.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.validation.ValidationException;
import org.springframework.util.Assert;

/**
 *
 * @author Cássio Tatsch (tatschcassio@gmail.com)
 */
public class MarkovService {

    private float[][] matrix;

    /**
     * 
     * @param inputMatrix
     * @return
     * @throws IllegalArgumentException 
     */
    private float[][] convertTextToMatrix(String inputMatrix) throws IllegalArgumentException {

        Assert.notNull(inputMatrix, "String not informed");
        Assert.hasText(inputMatrix, "String not informed");

        String[] rows = inputMatrix.replace(",", ".").trim().split("\n");

        float[][] matrix = new float[rows.length][rows.length];

        //String pattern = String.format("(((\\+|-?)\\d+([\\.]\\d+)?)|\\ ){%d}", lines.length + (lines.length - 1));
        for (int r = 0; r < rows.length; r++) {
            String[] cols = rows[r].trim().split(" ");
            Assert.isTrue(cols.length == rows.length, "Size os clomuns and rows differs");
            for (int c = 0; c < cols.length; c++) {
                Assert.isTrue(cols[c].matches("[-]?[0-9]*\\.?[0-9]+"), "Input is not number");
                matrix[r][c] = Float.parseFloat(cols[c]);
            }
        }

        return matrix;
    }

    public MarkovService withInput(String inputMatrix) throws IllegalArgumentException {

        matrix = convertTextToMatrix(inputMatrix);

        return this;
    }
    
    

}
