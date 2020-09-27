package org.jax.prositometry.prosite;

import org.jax.prositometry.ensembl.EnsemblGene;
import org.jax.prositometry.ensembl.EnsemblTranscript;

import java.util.List;

public class PrositeComparator {

    private final List<PrositePattern> prositeList;

    public PrositeComparator(String path) {
        PrositeParser pparser = new PrositeParser(path);
        this.prositeList = pparser.getPatternList();
    }

    public void annotateEnsemblGene(EnsemblGene egene) {
        for (EnsemblTranscript etrns : egene.getTranscriptMap().values()) {
            String aaSeq = etrns.getLongestAaSequence();
            for (PrositePattern pattern : prositeList) {
                List<Integer> positionList = pattern.getPositions(aaSeq);
                if (! positionList.isEmpty()) {
                    etrns.addMotif(pattern.getPrositeId(), positionList);
                }
            }
        }
    }



}
