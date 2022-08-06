package org.jax.isopret.model;

/**
 * POJO representing an Interpro domain annotation.
 */
public record InterproDomain (int enst,
                              int ensg,
                              int interpro,
                              int start,
                              int end) {
}
