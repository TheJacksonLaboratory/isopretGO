package org.jax.prositometry;

import org.jax.prositometry.prosite.PrositeParser;
import org.jax.prositometry.prosite.PrositePattern;

import java.util.List;

public class Prositometry {
    private final String prositePath;

    private List<PrositePattern> patternList;
    public Prositometry(String prositePath) {
        this.prositePath = prositePath;
        PrositeParser proparser = new PrositeParser(prositePath);
        patternList = proparser.getPatternList();

    }


    public void dumpStats() {
        System.out.printf("[INFO] %s.\n", this.prositePath);
        for (PrositePattern pat : patternList) {
            System.out.printf("[INFO] %s\n", pat);
        }
    }

}
