package org.jax.isopret.interpro;

import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.transcript.AccessionNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterproMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproMapper.class);
    private final Map<Integer, InterproEntry> interproDescription;
    private final Map<AccessionNumber, List<InterproAnnotation>> interproAnnotation;


    public InterproMapper(String interproDescriptionFile, String interproDomainsFile) {
        this.interproDescription = InterproDomainDescParser.getInterproDescriptionMap(interproDescriptionFile);
        this.interproAnnotation = InterproDomainParser.getInterproAnnotationMap(interproDomainsFile);
    }


    public Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap(AccessionNumber geneAccession) {
       if (! geneAccession.isGene()) {
           throw new IsopretRuntimeException("transcriptToInterproHitMap can only be called with gene ids but wew got " + geneAccession);
       }

        if (! this.interproAnnotation.containsKey(geneAccession)) {
            return Map.of(); // no hits
        }

        List<InterproAnnotation> hits = this.interproAnnotation.get(geneAccession);
        Map<AccessionNumber, List<DisplayInterproAnnotation>> hitmap = new HashMap<>();
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
