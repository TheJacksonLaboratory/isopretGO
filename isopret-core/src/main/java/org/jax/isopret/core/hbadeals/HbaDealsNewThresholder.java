package org.jax.isopret.core.hbadeals;

import org.jax.isopret.core.transcript.AccessionNumber;

import java.util.Set;

public class HbaDealsNewThresholder implements DifferentialThresholder<AccessionNumber>  {


    public HbaDealsNewThresholder() {

    }



    @Override
    public double qValueThreshold() {
        return 0;
    }

    @Override
    public double fdrThreshold() {
        return 0;
    }

    @Override
    public int totalItemCount() {
        return 0;
    }

    @Override
    public int differentialItemCount() {
        return 0;
    }

    @Override
    public Set<AccessionNumber> differentialItems() {
        return null;
    }

    @Override
    public Set<AccessionNumber> totalItems() {
        return null;
    }
}
