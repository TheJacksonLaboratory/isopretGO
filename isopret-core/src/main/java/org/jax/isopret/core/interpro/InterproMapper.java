package org.jax.isopret.core.interpro;

import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.model.AccessionNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class coordinates the parsing of two interpro files from biomaRt that are available
 * on the isopret GitHub site, {@code interpro_domains.txt} and
 * {@code interpro_domain_desc.txt}, provides access to the maps representing these files
 * and a function {@link #transcriptToInterproHitMap(AccessionNumber)} that gets all interpro
 * hits that are mapped to a specific transcript.
 * @author Peter N Robinson
 */
public class InterproMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproMapper.class);
    private final Map<Integer, InterproEntry> interproDescription;
    private final Map<AccessionNumber, List<InterproAnnotation>> interproAnnotation;


    public InterproMapper(File interproDescriptionFile, File interproDomainsFile) {
        this.interproDescription = InterproDomainDescParser.getInterproDescriptionMap(interproDescriptionFile);
        this.interproAnnotation = InterproDomainParser.getInterproAnnotationMap(interproDomainsFile);
    }

    public Map<Integer, InterproEntry> getInterproDescription() {
        return interproDescription;
    }

    public Map<AccessionNumber, List<InterproAnnotation>> getInterproAnnotation() {
        return interproAnnotation;
    }

    public int getInterproDescriptionCount() {
        return interproDescription.size();
    }

    public int getInterproAnnotationCount() {
        return interproAnnotation.size();
    }

    public Map<AccessionNumber, List<DisplayInterproAnnotation>> transcriptToInterproHitMap(AccessionNumber geneAccession) {
       int notfound = 0;
        if (! geneAccession.isGene()) {
           throw new IsopretRuntimeException("transcriptToInterproHitMap can only be called with gene ids but we got " + geneAccession);
       }

        if (! this.interproAnnotation.containsKey(geneAccession)) {
            return Map.of(); // no hits
        }

        List<InterproAnnotation> hits = this.interproAnnotation.get(geneAccession);
        Map<AccessionNumber, List<DisplayInterproAnnotation>> hitmap = new HashMap<>();
        for (InterproAnnotation annot : hits) {
            if (! this.interproDescription.containsKey(annot.getInterpro())) {
                notfound++;
                continue;
            }
            DisplayInterproAnnotation display = new DisplayInterproAnnotation(annot, this.interproDescription.get(annot.getInterpro()));
            hitmap.putIfAbsent(annot.getEnst(), new ArrayList<>());
            hitmap.get(annot.getEnst()).add(display);
        }
        if (notfound > 0) {
            LOGGER.error("Could not find interpro Description for {} items", notfound);
        }
        return hitmap;
    }
}
