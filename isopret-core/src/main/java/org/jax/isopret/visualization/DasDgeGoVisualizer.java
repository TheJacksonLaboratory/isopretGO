package org.jax.isopret.visualization;

import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DasDgeGoVisualizer {

    private final List<DasDgeGoVisualizable> dasDgeVisList;

    private final static String [] headers = {
            "go.id",
            "go.label",
            "das.study.isoforms",
            "das.pop.isoforms",
            "das.p",
            "das.adj.p",
            "dge.study.isoforms",
            "dge.pop.isoforms",
            "dge.p",
            "dge.adj.p"};


    public DasDgeGoVisualizer(Ontology ontology, List<GoTerm2PValAndCounts> das, List<GoTerm2PValAndCounts> dge) {
        Map<TermId, GoTerm2PValAndCounts> dasMap = das.stream()
                .collect(Collectors.toMap(GoTerm2PValAndCounts::getGoTermId, Function.identity()));
        Map<TermId, GoTerm2PValAndCounts> dgeMap = dge.stream()
                .collect(Collectors.toMap(GoTerm2PValAndCounts::getGoTermId, Function.identity()));
        Set<TermId> allTermIds = new HashSet<>(dasMap.keySet());
        allTermIds.addAll(dgeMap.keySet());
        List<DasDgeGoVisualizable> visList = new ArrayList<>();
        for (TermId tid: allTermIds) {
            Optional<String> opt = ontology.getTermLabel(tid);
            String label = opt.orElse("n/a");
            GoTerm2PValAndCounts dasCounts = dasMap.get(tid);
            GoTerm2PValAndCounts dgeCounts = dgeMap.get(tid);
            // by contruction, at least dasCounts or DgeCounts must be nonnull
            if (dasCounts == null) {
                DasDgeGoVisualizable vis = DasDgeGoVisualizable.fromDge(tid,label, dgeCounts);
                visList.add(vis);
            } else if (dgeCounts == null) {
                DasDgeGoVisualizable vis = DasDgeGoVisualizable.fromDas(tid,label,dasCounts);
                visList.add(vis);
            } else {
                DasDgeGoVisualizable vis = DasDgeGoVisualizable.fromDasDge(tid,label,dasCounts, dgeCounts);
                visList.add(vis);
            }
        }
        Collections.sort(visList);
        this.dasDgeVisList = List.copyOf(visList);
    }


    public String getTsv() {
        String header = String.join("\t", headers);
        StringBuilder sb = new StringBuilder();
        sb.append(header).append("\n");
        for (DasDgeGoVisualizable vis : dasDgeVisList) {
            sb.append(getRow(vis));
        }
        return sb.toString();
    }


    private String getCountString(int annot, int total) {
        return String.format("%d/%d (%.1f%%", annot, total, 100.0*annot/total);
    }

    private String getRow(DasDgeGoVisualizable vis) {
        String studyIso = getCountString(vis.getDasStudyAnnotated(), vis.getDasStudyTotal());
        String popIso = getCountString(vis.getDasPopAnnotated(), vis.getDasPopTotal());
        String studyGene = getCountString(vis.getDgeStudyAnnotated(), vis.getDgeStudyTotal());
        String popGene = getCountString(vis.getDgePopAnnotated(), vis.getDgePopTotal());
        return String.format("%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
                vis.getGoId(),
                vis.getGoLabel(),
                studyIso,
                popIso,
                vis.getDasP(),
                vis.getDasAdjP(),
                studyGene,
                popGene,
                vis.getDgeP(),
                vis.getDgeAdjP()
        );
    }



}
