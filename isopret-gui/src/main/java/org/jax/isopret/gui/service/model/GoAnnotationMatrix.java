package org.jax.isopret.gui.service.model;

import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.Transcript;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class records information about the Gene Ontology annotations for a gene
 * and all of its isoforms.
 */
public class GoAnnotationMatrix {
    final Logger LOGGER = LoggerFactory.getLogger(GoAnnotationMatrix.class);

    private final List<GoAnnotationRow> annotationRows;

    public GoAnnotationMatrix(Ontology ontology,
                              Map<AccessionNumber, List<Transcript>> geneIdToTranscriptMap,
                                Map<TermId, Set<TermId>> transcript2GoMap,
                              List<GoTerm2PValAndCounts> dgeGoTerms,
                              List<GoTerm2PValAndCounts> dasGoTerms,
                              AccessionNumber accessionNumber){
        List<GoAnnotationRow> rows = new ArrayList<>();
        // The termId (key) of transcriptToGoMap corresponds to an accession number
        TermId accessionId = accessionNumber.toTermId();
        if (geneIdToTranscriptMap.containsKey(accessionNumber)) {
            // collect all DGE Go terms
            Set<TermId> significantGoSet = dgeGoTerms.stream()
                    .map(GoTerm2PValAndCounts::getGoTermId)
                    .collect(Collectors.toSet());
            // if this is the case then we have isoforms all is Good
            List<TermId> transcriptIds =
                    geneIdToTranscriptMap.get(accessionId)
                            .stream()
                            .map(Transcript::accessionId)
                            .map(AccessionNumber::toTermId)
                            .collect(Collectors.toList());
            // collect all GO terms that annotate at least one transcript
            Set<TermId> goIdSet = new HashSet<>();
            for (TermId transcriptId : transcriptIds) {
                if (transcript2GoMap.containsKey(transcriptId)) {
                    goIdSet.addAll(transcript2GoMap.get(transcriptId));
                }
            }
            // when we get here, we have all GO ids for this gene
            for (TermId goId : goIdSet) {
                Optional<String> opt = ontology.getTermLabel(goId);
                if (opt.isEmpty()) {
                    LOGGER.error("Could not find label for GO {} (should never happen).", goId.getValue());
                    continue;
                }
                String label = opt.get();
                boolean significant = significantGoSet.contains(goId);
                List<Boolean> transcriptAnnotated = new ArrayList<>();
                for (TermId transcriptId : transcriptIds) {
                    boolean annot = transcript2GoMap.get(transcriptId).contains(goId);
                    transcriptAnnotated.add(annot);
                }
                GoAnnotationRow row = new GoAnnotationRow(goId, label, significant, transcriptAnnotated);
                rows.add(row);
            }
        }
        Collections.sort(rows);
        annotationRows = List.copyOf(rows);
    }

}
