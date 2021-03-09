package org.jax.isopret.interpro;

import org.checkerframework.checker.units.qual.A;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterproMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproMapper.class);
    private final Map<Integer, InterproEntry> interproDescription;
    private final Map<Integer, List<InterproAnnotation>> interproAnnotation;


    public InterproMapper(String interproDescriptionFile, String interproDomainsFile) {
        this.interproDescription = InterproDomainDescParser.getInterproDescriptionMap(interproDescriptionFile);
        this.interproAnnotation = InterproDomainParser.getInterproAnnotationMap(interproDomainsFile);
    }


    public Map<Integer, List<DisplayInterproAnnotation>> transcriptToInterproHitMap(int geneAccession) {
        if (! this.interproAnnotation.containsKey(geneAccession)) {
            return Map.of(); // no hits
        }
        List<InterproAnnotation> hits = this.interproAnnotation.get(geneAccession);
        Map<Integer, List<DisplayInterproAnnotation>> hitmap = new HashMap<>();
        for (InterproAnnotation annot : hits) {
            if (! this.interproDescription.containsKey(annot.getInterpro())) {
                LOGGER.error("Could not find interpro Description for {}", annot);
                continue;
            }
            DisplayInterproAnnotation display = new DisplayInterproAnnotation(annot, this.interproDescription.get(annot.getInterpro()));
            hitmap.putIfAbsent(annot.getEnst(), new ArrayList<>());
            hitmap.get(annot.getEnst()).add(display);
        }
        return hitmap;
    }
}
