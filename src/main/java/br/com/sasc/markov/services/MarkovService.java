package br.com.sasc.markov.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import org.springframework.util.Assert;

/**
 *
 * @author Cássio Tatsch (tatschcassio@gmail.com)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarkovService {

    private final static ObjectMapper MAPPER = new ObjectMapper();

    /* Matrix dimension */
    @Getter
    private int dimension;

    /* Original Matrix */
    @Getter
    private float[][] inputMatrix;

    /* Results array */
    @Getter
    private List<Float> results;

    /* Results label array */
    @Getter
    private List<String> resultLabels;

    /* Absolute max value */
    @Getter
    private float max;

    /* Steps logging */
    @Getter
    private List<float[][]> steps;

    public MarkovService() {
        this.steps = null;
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
        addStep(identityMatrix);
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
                probabilityMatrix[r][c] = identityMatrix[r][c] - (inputMatrix[r][c] / max);
            }
        }
        addStep(probabilityMatrix);
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

        for (int i = 0; i < dimension; i++) {
            for (int j = 0; j < dimension; j++) {
                for (int k = 0; k < dimension; k++) {
                    tmp[i][j] = tmp[i][j] + (input[i][k] * input[k][j]);
                }
            }
        }

        if (!finished(tmp)) {
            tmp = multiply(tmp);
        }

        addStep(tmp);
        return tmp;
    }

    /**
     * Validates matrix returning {@code true} if reached its convergence or
     * {@code false}, otherwise.
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
     * Converts a matrix to String and put this on the list of steps
     * {@link MarkovService#steps}.
     *
     * @param matrix is the matrix to be converted.
     */
    private void addStep(float[][] matrix) {

        if (Objects.isNull(steps)) {
            return;
        }

        steps.add(matrix);

    }

    /**
     * Enable steps tracing.
     *
     * @return this {@link MarkovService}.
     */
    public MarkovService enableSteps() {
        this.steps = new ArrayList<>();
        return this;
    }

    /**
     * Transforms the text passed by parameter to an float number matrix and
     * stores it on {@link MarkovService#inputMatrix}. Also verifies input data
     * and return IllegalArgumetException if an error occurred.
     *
     * @param inputText the text representing a matrix.
     * @return this {@link MarkovService}.
     */
    public MarkovService from(String inputText) {

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

        addStep(inputMatrix);

        return this;
    }

    /**
     * Executes matrix calculation to transform CTMC to DTMC.
     *
     * @return this {@link MarkovService}.
     */
    public MarkovService execute() {

        float[][] cc = multiply(buildProbabilityMatrix(buildIdentityMatrix()));
        results = new ArrayList<>();
        for (int x = 0; x < dimension; x++) {
            results.add(cc[0][x]);
        }

        return this;

    }

    /**
     * Creates a list of labels, used to nicely print the results;
     *
     * @param labels the labels to be used.
     * @return this {@link MarkovService}.
     */
    public MarkovService addLabels(String... labels) {
        resultLabels = new ArrayList<>();
        resultLabels.addAll(Arrays.asList(labels));
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
