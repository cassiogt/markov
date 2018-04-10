/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.sasc.markov.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
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
    private float[][] results;
    private float max;
    private int loop;

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

    private void print(int loop, float[][] matrix) {
        System.out.println("M" + loop + ":");
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix.length; c++) {
                System.out.print(String.format(" %.4f ", matrix[r][c]));
            }
            System.out.println("\n");
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
                probabilityMatrix[r][c] = identityMatrix[r][c] - (inputMatrix[r][c] / max);
            }
        }
    }

    private boolean validate(float[][] matrix) {
        for (int c = 0; c < dimension; c++) {
            for (int r = 0; r < dimension - 1; r++) {
                if (!new BigDecimal(matrix[r][c]).setScale(4, BigDecimal.ROUND_HALF_UP).equals(
                        new BigDecimal(matrix[r + 1][c]).setScale(4, BigDecimal.ROUND_HALF_UP))) {
                    return false;
                }
            }
        }
        return true;
    }

    public float[][] multiply(float[][] input) {
        float c[][] = new float[dimension][dimension];

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                c[i][j] = 0;
            }
        }

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                for (int k = 0; k < dimension; k++) {
                    c[i][j] = c[i][j] + (input[i][k] * input[k][j]);
                }
            }
        }

        print(loop++, c);
        if (!validate(c)) {
            multiply(c);
        }

        return c;
    }

    public MarkovService withInput(String inputMatrix) throws IllegalArgumentException {
        LocalDateTime dt1 = LocalDateTime.now();

        convertTextToMatrix(inputMatrix);
        buildIdentityMatrix();
        buildProbabilityMatrix();
        loop = 2;
        float[][] cc = multiply(probabilityMatrix);
        System.out.println("A: " + cc[0][0]);
        System.out.println("B: " + cc[1][0]);
        System.out.println("C: " + cc[2][0]);
        LocalDateTime dt2 = LocalDateTime.now();
        
        System.out.println("DIFF: " + dt1.until(dt2, ChronoUnit.MILLIS));
        return this;
    }

}
