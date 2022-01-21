package org.jax.isopret.core.visualization;

import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.Transcript;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
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
    private final List<TermId> transcriptIds;

    private final List<TermId> expressedTranscriptIds;
    /** GO annotation patterns for all transcripts */
    private final List<GoAnnotationRow> annotationRows;
    /** GO annotation patterns for transcripts expressed incurrent HBADEALS dataset. */
    private final List<GoAnnotationRow> expressedAnnotationRows;

    /**
     *
     * @param ontology reference to Gene Ontology
     * @param geneIdToTranscriptMap map from gene ids to corresponding transcript ids
     * @param transcript2GoMap GO annotations for transcripts
     * @param significantGoSet signficant GO terms
     * @param accessionNumber accession of gene we are looking at.
     * @param expressedTranscriptSet Transcripts that are expressed in the RNA-seq experiment/HBADEALS result
     */
    public GoAnnotationMatrix(Ontology ontology,
                              Map<AccessionNumber, List<Transcript>> geneIdToTranscriptMap,
                              Map<TermId, Set<TermId>> transcript2GoMap,
                              Set<TermId> significantGoSet,
                              AccessionNumber accessionNumber,
                              Set<TermId> expressedTranscriptSet){
        // The termId (key) of transcriptToGoMap corresponds to an accession number
       // TermId accessionId = accessionNumber.toTermId();
        this.accession = accessionNumber.getAccessionString();
        if (geneIdToTranscriptMap.containsKey(accessionNumber) && transcript2GoMap != null) {
            // if this is the case then we have isoforms all is Good
            this. transcriptIds =
                    geneIdToTranscriptMap.get(accessionNumber)
                            .stream()
                            .map(Transcript::accessionId)
                            .map(AccessionNumber::toTermId)
                            .collect(Collectors.toList());
            this.expressedTranscriptIds = transcriptIds.stream()
                    .filter(expressedTranscriptSet::contains)
                    .collect(Collectors.toList());
            LOGGER.trace("Got {} transcript Ids for {}", transcriptIds.size(), accessionNumber.getAccessionString());
            LOGGER.trace("Gene: {}", accessionNumber.getAccessionString());
            List<GoAnnotationRow> rows = allAnnotationRows(ontology, geneIdToTranscriptMap, transcript2GoMap, significantGoSet);
            Collections.sort(rows);
            annotationRows = List.copyOf(rows);
            rows = expressedAnnotationRows(ontology, geneIdToTranscriptMap, transcript2GoMap, significantGoSet);
            Collections.sort(rows);
            expressedAnnotationRows = List.copyOf(rows);
        } else {
            LOGGER.info("Could not get GO data for {}", accessionNumber.getAccessionString());
            transcriptIds = List.of();
            expressedTranscriptIds = List.of();
            annotationRows = List.of();
            expressedAnnotationRows = List.of();
        }
    }


    List<GoAnnotationRow> expressedAnnotationRows(Ontology ontology,
                                                  Map<AccessionNumber, List<Transcript>> geneIdToTranscriptMap,
                                                  Map<TermId, Set<TermId>> transcript2GoMap,
                                                  Set<TermId> significantGoSet) {
        // collect all GO terms that annotate at least one transcript
        Set<TermId> goIdSet = new HashSet<>();
        List<GoAnnotationRow> rows = new ArrayList<>();
        for (TermId transcriptId : expressedTranscriptIds) {
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
            for (TermId transcriptId : expressedTranscriptIds) {
                if (transcript2GoMap.containsKey(transcriptId)) {
                    transcriptAnnotated.add(transcript2GoMap.get(transcriptId).contains(goId));
                } else {
                    transcriptAnnotated.add(false);
                }
            }
            GoAnnotationRow row = new GoAnnotationRow(goId, label, significant, transcriptAnnotated);
            rows.add(row);
        }
        return rows;
    }

    List<GoAnnotationRow> allAnnotationRows(Ontology ontology,
                                            Map<AccessionNumber, List<Transcript>> geneIdToTranscriptMap,
                                            Map<TermId, Set<TermId>> transcript2GoMap,
                                            Set<TermId> significantGoSet) {
        // collect all GO terms that annotate at least one transcript
        Set<TermId> goIdSet = new HashSet<>();
        List<GoAnnotationRow> rows = new ArrayList<>();
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
        return rows;
    }







    public String getAccession() {
        return accession;
    }
    public List<GoAnnotationRow> getAnnotationRows() {
        return annotationRows;
    }

    public List<GoAnnotationRow> getExpressedAnnotationRows() { return expressedAnnotationRows; }

    public List<String> getTranscripts() {
        return  this.transcriptIds.stream().map(TermId::getValue).collect(Collectors.toList());
    }

    public List<String> getExpressedTranscripts() {
        return  this.expressedTranscriptIds.stream().map(TermId::getValue).collect(Collectors.toList());
    }

    public Set<TermId> getAllGoIds() {
        return annotationRows.stream()
                .map(GoAnnotationRow::getGoId)
                .collect(Collectors.toSet());
    }
}
