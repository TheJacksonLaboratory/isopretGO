package org.jax.isopret.core.visualization;

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
    private final Logger LOGGER = LoggerFactory.getLogger(GoAnnotationMatrix.class);

    private final String accession;
    private final List<String> transcripts;
    private final List<GoAnnotationRow> annotationRows;

    public String getAccession() {
        return accession;
    }

    public List<String> getTranscripts() {
        return transcripts;
    }

    /**
     *
     * @param ontology reference to Gene Ontology
     * @param geneIdToTranscriptMap map from gene ids to corresponding transcript ids
     * @param transcript2GoMap GO annotations for transcripts
     * @param dgeGoTerms signficant DGE GO terms (assumed to be all significnat terms)
     * @param accessionNumber accession of gene we are looking at.
     */
    public GoAnnotationMatrix(Ontology ontology,
                              Map<AccessionNumber, List<Transcript>> geneIdToTranscriptMap,
                              Map<TermId, Set<TermId>> transcript2GoMap,
                              List<GoTerm2PValAndCounts> dgeGoTerms,
                              AccessionNumber accessionNumber){
        List<GoAnnotationRow> rows = new ArrayList<>();
        // The termId (key) of transcriptToGoMap corresponds to an accession number
        TermId accessionId = accessionNumber.toTermId();
        this.accession = accessionNumber.getAccessionString();
        if (geneIdToTranscriptMap.containsKey(accessionNumber) && transcript2GoMap != null) {
            // collect all DGE Go terms (over entire experiment)
            Set<TermId> significantGoSet = dgeGoTerms.stream()
                    .map(GoTerm2PValAndCounts::getGoTermId)
                    .collect(Collectors.toSet());
            // if this is the case then we have isoforms all is Good
            List<TermId> transcriptIds =
                    geneIdToTranscriptMap.get(accessionNumber)
                            .stream()
                            .map(Transcript::accessionId)
                            .map(AccessionNumber::toTermId)
                            .collect(Collectors.toList());
            this.transcripts = transcriptIds.stream().map(TermId::getValue).collect(Collectors.toList());
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
                    if (transcript2GoMap.containsKey(transcriptId)) {
                        transcriptAnnotated.add(transcript2GoMap.get(transcriptId).contains(goId));
                    } else {
                        transcriptAnnotated.add(false);
                    }
                }
                GoAnnotationRow row = new GoAnnotationRow(goId, label, significant, transcriptAnnotated);
                rows.add(row);
            }
        } else {
            transcripts = List.of();
        }
        Collections.sort(rows);
        annotationRows = List.copyOf(rows);
    }

    public List<GoAnnotationRow> getAnnotationRows() {
        return annotationRows;
    }
}
