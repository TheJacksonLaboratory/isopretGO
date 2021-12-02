package org.jax.isopret.core.interpro;

/**
 * POJO representing an Interpro domain annotation.
 */
public record InterproDomain (int enst,
                              int ensg,
                              int interpro,
                              int start,
                              int end) {
}
