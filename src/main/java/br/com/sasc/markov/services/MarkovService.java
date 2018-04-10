/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.sasc.markov.services;

import org.springframework.util.Assert;

/**
 *
 * @author Cássio Tatsch (tatschcassio@gmail.com)
 */
public class MarkovService {

    private int dimension;
    private float[][] inputMatrix;
    private float[][] probabilityMatrix;
    private float[][] identityMatrix;
    private float max;
    

    /**
     *
     * @param inputMatrix
     * @return
     * @throws IllegalArgumentException
     */
    private void convertTextToMatrix(String inputText) throws IllegalArgumentException {

        Assert.notNull(inputText, "String not informed");
        Assert.hasText(inputText, "String not informed");

        String[] rows = inputText.replace(",", ".").trim().split("\n");
        dimension = rows.length;
        inputMatrix = new float[dimension][dimension];

        for (int r = 0; r < dimension; r++) {
            String[] cols = rows[r].trim().split(" ");
            Assert.isTrue(cols.length == dimension, "Size os clomuns and rows differs");
            for (int c = 0; c < dimension; c++) {
                Assert.isTrue(cols[c].matches("[-]?[0-9]*\\.?[0-9]+"), "Input is not number");
                float val = Float.parseFloat(cols[c]);
                max = Math.abs(val) > Math.abs(max) ? val : max;
                inputMatrix[r][c] = val;
            }
        }
    }

    private void buildIdentityMatrix() {
        identityMatrix = new float[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            identityMatrix[i][i] = 1;
        }
    }

    private void buildProbabilityMatrix() {
        probabilityMatrix = new float[dimension][dimension];
        for (int r = 0; r < dimension; r++) {
            for (int c = 0; c < dimension; c++) {
                probabilityMatrix[c][r] = identityMatrix[r][c] - (inputMatrix[r][c] / max);
            }
        }
    }

    public MarkovService withInput(String inputMatrix) throws IllegalArgumentException {

        convertTextToMatrix(inputMatrix);
        buildIdentityMatrix();
        buildProbabilityMatrix();

        return this;
    }

}
