package org.jax.isopret.core;

import java.util.Set;

/**
 * The purpose of this interface is to select the items (Genes or Isoforms) that are differential according
 * to HBA Deals
 * TODO -- FIGURE OUT INTERFACE SO WE CAN IMPLEMENT FOR edgeR
 */
public interface DifferentialThresholder<T> {

    double qValueThreshold();
    double fdrThreshold();
    int totalItemCount();
    int differentialItemCount();
    Set<T> differentialItems();
    Set<T> totalItems(); // TODO do we really need this?
}
