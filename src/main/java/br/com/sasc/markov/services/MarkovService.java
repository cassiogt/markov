package br.com.sasc.markov.services;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import lombok.Getter;
import org.springframework.util.Assert;

/**
 * Markov converter methods.
 *
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
    private double max;

    /* Steps logging */
    @Getter
    private List<double[][]> matrixList;

    /* Results array */
    @Getter
    private List<State> results;

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
    private double[][] buildIdentityMatrix() {
        double[][] identityMatrix = new double[dimension][dimension];
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
    private double[][] buildProbabilityMatrix(double[][] identityMatrix) {
        double[][] probabilityMatrix = new double[dimension][dimension];
        for (int r = 0; r < dimension; r++) {
            for (int c = 0; c < dimension; c++) {
                probabilityMatrix[r][c] = identityMatrix[r][c] - (matrixList.get(0)[r][c] / max);
            }
        }
        matrixList.add(probabilityMatrix);
        return probabilityMatrix;
    }

    /**
     * Converts a matrix to String and put this on the list of matrixList {@link MarkovService#matrixList}.
     *
     * @param matrix is the matrix to be converted.
     */
    private void saveSteps(double[][] matrix) {

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
                        .replaceAll("\\t+", " ")
                        .replaceAll("\\r+", " ")
                        .replaceAll(" +", " ")
                        .split(" ");

                if (cols.length == 1) {
                    if ("".equals(cols[0])) {
                        continue;
                    }
                    Assert.isTrue(cols[0].matches("[0-9]+"), "Entrada informada na matriz não é numérico.");
                    dimension = Integer.parseInt(cols[0]);
                    matrixList.add(new double[dimension][dimension]);
                } else {
                    if (Objects.isNull(dimension)) {
                        dimension = cols.length;
                        matrixList.add(new double[dimension][dimension]);
                    }
                    Assert.isTrue(cols.length == dimension, "A matriz não corresponde à dimensão informada.");
                    for (int c = 0; c < dimension; c++) {
                        Assert.isTrue(cols[c].matches("[-]?[0-9]*\\.?[0-9]+"), "Entrada informada na matriz não é numérica.");
                        matrixList.get(0)[row][c] = Double.parseDouble(cols[c]);
                    }
                    row++;
                }
            }
        }

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
            if (sum != 0) {
                System.out.println();
            }
            Assert.isTrue(sum == 0, "A soma das colunas não é igual a 0.");
        }

        try {
            System.out.println(toJson());
        } catch (JsonProcessingException ex) {

        }

        return this;
    }

    public MarkovService resolveDTMC(int maxSteps) {

        double[][] cc = buildProbabilityMatrix(buildIdentityMatrix());

        int totalSum = 0;
        counter = new int[dimension];

        int statePos = random.nextInt(dimension);

        double randomValue;
        int lineSum;
        int column = 0;
        Map<Integer, State> tmpResults = new HashMap<>(dimension);

        for (int i = 0; i < dimension; i++) {
            tmpResults.put(i, new State(i));
        }

        while (totalSum < maxSteps) {
            lineSum = 0;
            randomValue = random.nextDouble();
            statePos = calc(cc, statePos, lineSum, column, randomValue);
            State state = tmpResults.get(statePos);
            state.setValue(state.getValue() + 1);
            tmpResults.put(statePos, state);
            totalSum++;
        }

        for (statePos = 0; statePos < dimension; statePos++) {
            State state = tmpResults.get(statePos);
            state.setPercent(state.getValue() * 100 / (double) totalSum);
            tmpResults.put(statePos, state);
        }

        results = tmpResults.values().stream()
                .sorted((e1, e2)
                        -> e2.getPercent().compareTo(e1.getPercent()))
                .collect(Collectors.toList());

        return this;

    }

    private int calc(final double[][] matrix, final int state, double lineSum, int column, final double randomValue) {

        lineSum += matrix[state][column];
        if (randomValue < lineSum) {
            return column;
        }
        return calc(matrix, state, lineSum, ++column, randomValue);
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
