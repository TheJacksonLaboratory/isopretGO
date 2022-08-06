package org.jax.isopret.visualization;

/**
 * A convenience class to store values of the posterior error probability
 * and whether they were determined to be significant given the current
 * dataset and FDR threshold.
 */
public record DoublePepValue(double pep, boolean isSignificant) {


}