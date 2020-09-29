package org.jax.prositometry.prosite;

import org.jax.prositometry.ensembl.EnsemblGene;
import org.jax.prositometry.ensembl.EnsemblTranscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        List<Map<String,List<Integer>>> mmapList = new ArrayList<>();
        for (EnsemblTranscript etrns : egene.getTranscriptMap().values()) {
            mmapList.add(etrns.getMotifMap());
        }
        boolean different = false;
        if (mmapList.size() < 2) {
            return;
        }
        for (int i=0;i<mmapList.size();i++) {
            for (int j=i+1;j<mmapList.size();j++) {
                Map<String,List<Integer>> map1 = mmapList.get(i);
                Map<String,List<Integer>> map2 = mmapList.get(j);
                if (! map1.equals(map2)) {
                    different = true;
                    break;
                }
            }
        }
        egene.setMapDifference(different);

    }



}
