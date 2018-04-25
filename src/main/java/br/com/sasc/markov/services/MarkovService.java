package br.com.sasc.markov.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Getter;
import org.springframework.util.Assert;

/**
 * Markov converter methods.
 *
 * @author Cássio Tatsch (tatschcassio@gmail.com)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarkovService {

    private final Random random = new Random();

    // <editor-fold defaultstate="collapsed" desc="Private members and methods.">
    private final static ObjectMapper MAPPER = new ObjectMapper();

    /* Matrix dimension */
    private boolean shouldSaveAllSteps;

    /* Matrix dimension */
    @Getter
    private Integer dimension;

    @Getter
    private float max;

    /* Steps logging */
    @Getter
    private List<float[][]> matrixList;

    /* Results array */
    @Getter
    private float[] results;

    @Getter
    private int[] counter;

    /* Absolute max value */
    public MarkovService() {
        this.matrixList = new ArrayList<>();
    }

    /**
     * Builds and returns a identity matrix.
     *
     * @return a identity matrix.
     */
    private float[][] buildIdentityMatrix() {
        float[][] identityMatrix = new float[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            identityMatrix[i][i] = 1;
        }
        matrixList.add(identityMatrix);
        return identityMatrix;
    }

    /**
     * Builds and returns a probability matrix.
     *
     * @param identityMatrix a identity matrix with the same size as rate matrix
     * @return a matrix with probabilities.
     */
    private float[][] buildProbabilityMatrix(float[][] identityMatrix) {
        float[][] probabilityMatrix = new float[dimension][dimension];
        for (int r = 0; r < dimension; r++) {
            for (int c = 0; c < dimension; c++) {
                probabilityMatrix[r][c] = identityMatrix[r][c] - (matrixList.get(0)[r][c] / max);
            }
        }
        matrixList.add(probabilityMatrix);
        return probabilityMatrix;
    }

    /**
     * Multiplies matrix by itself until it converges.
     *
     * @param input the matrix that will be multiplied.
     * @return the result matrix.
     */
    private float[][] multiply(float[][] input) {

        float tmp[][] = new float[dimension][dimension];

        for (int r = 0; r < dimension; r++) {
            for (int c = 0; c < dimension; c++) {
                for (int k = 0; k < dimension; k++) {
                    tmp[r][c] = tmp[r][c] + (input[r][k] * input[k][c]);
                }
            }
        }

        saveSteps(tmp);
        if (!finished(tmp)) {
            tmp = multiply(tmp);
        }

        return tmp;
    }

    /**
     * Validates matrix returning {@code true} if reached its convergence or {@code false}, otherwise.
     *
     * @param matrix the matrix to be validated.
     * @return {@code true} if converged or {@code false}, otherwise.
     */
    private boolean finished(float[][] matrix) {

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

    /**
     * Converts a matrix to String and put this on the list of matrixList {@link MarkovService#matrixList}.
     *
     * @param matrix is the matrix to be converted.
     */
    private void saveSteps(float[][] matrix) {

        if (!shouldSaveAllSteps) {
            return;
        }

        matrixList.add(matrix);

    }
// </editor-fold>

    /**
     * Enable matrixList tracing.
     *
     * @return this {@link MarkovService}.
     */
    public MarkovService saveAllSteps() {
        this.shouldSaveAllSteps = true;
        return this;
    }

    /**
     * Reads a list of lines and converts it to a matrix.
     *
     * @param lines the array of lines.
     * @return this class.
     */
    public MarkovService fromListOfLines(List<String> lines) {

        Assert.notNull(lines, "Matriz não informada.");
        Assert.notEmpty(lines, "Matriz não informada.");

        int row = 0;
        for (String line : lines) {
            if (!line.startsWith("#") && line.length() >= 1) {
                String[] cols = line.trim()
                        .replaceAll("\\t", "")
                        .replaceAll("\\r", "")
                        .split(" ");
                if (dimension == null) {
                    if (cols.length == 1) {
                        Assert.isTrue(cols[0].matches("[0-9]"), "Entrada informada na matriz não é numérico.");
                        dimension = Integer.parseInt(cols[0]);
                        matrixList.add(new float[dimension][dimension]);
                    }
                } else {
                    Assert.isTrue(cols.length == dimension, "A matriz não corresponde à dimensão informada.");
                    for (int c = 0; c < dimension; c++) {
                        Assert.isTrue(cols[c].matches("[-]?[0-9]*\\.?[0-9]+"), "Entrada informada na matriz não é numérica.");
                        matrixList.get(0)[row][c] = Float.parseFloat(cols[c]);
                    }
                    row++;
                }
            }
        }
        Assert.notNull(dimension, "Não foi informada a dimensão da matriz.");

        float sum = 0.0f;
        for (int idx = 0; idx < dimension; idx++) {
            sum += matrixList.get(0)[idx][idx];
        }
        if (sum == 0) {//Se a matriz foi informada com 0 na diagonal principal
            for (int r = 0; r < dimension; r++) {
                sum = 0;
                for (int c = 0; c < dimension; c++) {
                    sum += matrixList.get(0)[r][c];
                }
                matrixList.get(0)[r][r] = 0 - sum;
            }
        }

        for (int r = 0; r < dimension; r++) {
            sum = 0;
            for (int c = 0; c < dimension; c++) {
                sum += matrixList.get(0)[r][c];
                max = Math.abs(matrixList.get(0)[r][c]) > Math.abs(max) ? matrixList.get(0)[r][c] : max;
            }
            Assert.isTrue(sum == 0, "A soma das colunas não é igual a 0.");
        }


        try {
            System.out.println("" + toJson());
        } catch (JsonProcessingException ex) {

        }

        return this;
    }

    /**
     * Executes matrix calculation to transform CTMC to DTMC.
     *
     * @return this {@link MarkovService}.
     */
    public MarkovService resolveDTMC() {

        float[][] cc = multiply(buildProbabilityMatrix(buildIdentityMatrix()));
        if (!shouldSaveAllSteps) {
            matrixList.add(cc);
        }

        results = new float[dimension];
        for (int x = 0; x < dimension; x++) {
            results[x] = cc[0][x];
        }

        return this;

    }

    /**
     * Generates random numbers to test probabilities.
     *
     * @param type     if {@code 1} then the calculation ends if at least
     *                 one entry reaches {@code maxSteps} or, if {@code 2},
     *                 the calculation ends if the sum of all entries
     *                 reaches {@code maxSteps}.
     * @param maxSteps the maximum number of steps.
     * @return this class;
     */
    public MarkovService calculateSteps(int type, int maxSteps) {

        float[] limits = new float[dimension];
        int idx = 0;
        for (; idx < results.length; idx++) {
            limits[idx] = idx > 0 ? results[idx] + limits[idx - 1] : results[idx];
        }
        counter = new int[dimension];
        if (type == 1) {//Generate numbers until one reaches maxSteps

            do {
                float rand = random.nextFloat();
                for (idx = 0; idx < results.length; idx++) {
                    if (rand <= limits[idx]) {
                        counter[idx]++;
                        break;
                    }
                }
            } while (counter[idx] < maxSteps);
        } else {//Generate maxSteps numbers.
            int count2 = 0;
            do {
                float rand = random.nextFloat();
                for (idx = 0; idx < results.length; idx++) {
                    if (rand <= limits[idx]) {
                        counter[idx]++;
                        break;
                    }
                }
            } while (++count2 < maxSteps);
        }
        return this;
    }

    /**
     * Serializes this object to JSON format.
     *
     * @return a String with JSON content.
     * @throws JsonProcessingException if an error occurs.
     */
    public String toJson() throws JsonProcessingException {

        return MAPPER.writeValueAsString(this);

    }
}
