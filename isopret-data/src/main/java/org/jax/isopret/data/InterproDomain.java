package org.jax.isopret.data;

/**
 * POJO representing an Interpro domain annotation.
 */
public record InterproDomain (int enst,
                              int ensg,
                              int interpro,
                              int start,
                              int end) {
}
