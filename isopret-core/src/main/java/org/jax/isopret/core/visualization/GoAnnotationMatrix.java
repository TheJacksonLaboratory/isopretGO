package org.jax.isopret.core.visualization;

import org.jax.isopret.model.AccessionNumber;
import org.jax.isopret.model.GeneModel;
import org.jax.isopret.model.Transcript;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * This class records information about the Gene Ontology annotations for a gene
 * and all of its isoforms that are expressed in the current experiment.
 */
public class GoAnnotationMatrix {
    private final Logger LOGGER = LoggerFactory.getLogger(GoAnnotationMatrix.class);

    private final String accession;

    private final String geneSymbol;
    private final List<TermId> transcriptIds;

    private final List<TermId> expressedCodingTranscriptIds;
    
    private final List<TermId> expressedNoncodingTranscriptIds;
    
    

    /** GO annotation patterns for transcripts expressed incurrent HBADEALS dataset. */
    private final List<GoAnnotationRow> expressedCodingAnnotationRows;

    /**
     *  @param ontology reference to Gene Ontology
     * @param geneIdToTranscriptMap map from gene ids to corresponding transcript ids
     * @param transcript2GoMap GO annotations for transcripts
     * @param significantGoSet signficant GO terms
     * @param accessionNumber accession of gene we are looking at.
     * @param expressedTranscriptSet Transcripts that are expressed in the RNA-seq experiment/HBADEALS result
     */
    public GoAnnotationMatrix(Ontology ontology,
                              Map<AccessionNumber, GeneModel> geneIdToTranscriptMap,
                              Map<TermId, Set<TermId>> transcript2GoMap,
                              Set<TermId> significantGoSet,
                              AccessionNumber accessionNumber,
                              Set<TermId> expressedTranscriptSet){
        // The termId (key) of transcriptToGoMap corresponds to an accession number
        this.accession = accessionNumber.getAccessionString();
        if (geneIdToTranscriptMap.containsKey(accessionNumber) && transcript2GoMap != null) {
            // if this is the case then we have isoforms all is Good
            this.transcriptIds =
                    geneIdToTranscriptMap.get(accessionNumber).transcriptList()
                            .stream()
                            .map(Transcript::accessionId)
                            .map(AccessionNumber::toTermId)
                            .collect(Collectors.toList());
            this.expressedCodingTranscriptIds = geneIdToTranscriptMap.get(accessionNumber).transcriptList()
                    .stream()
                    .filter(Transcript::isCoding)
                    .map(Transcript::accessionId)
                    .map(AccessionNumber::toTermId)
                    .filter(expressedTranscriptSet::contains)
                    .collect(Collectors.toList());
            this.expressedNoncodingTranscriptIds = geneIdToTranscriptMap.get(accessionNumber).transcriptList()
                    .stream()
                    .filter(Predicate.not(Transcript::isCoding))
                    .map(Transcript::accessionId)
                    .map(AccessionNumber::toTermId)
                    .filter(expressedTranscriptSet::contains)
                    .collect(Collectors.toList());
            this.geneSymbol = geneIdToTranscriptMap.get(accessionNumber).geneSymbol();
            LOGGER.trace("Got {} expressed transcript Ids for {}", expressedCodingTranscriptIds.size(), accessionNumber.getAccessionString());
            List<GoAnnotationRow> rows = expressedCodingAnnotationRows(ontology, geneIdToTranscriptMap, transcript2GoMap, significantGoSet);

            Collections.sort(rows);
            expressedCodingAnnotationRows = List.copyOf(rows);
        } else {
            LOGGER.info("Could not get GO data for {}", accessionNumber.getAccessionString());
            this.geneSymbol = "n/a";
            transcriptIds = List.of();
            expressedNoncodingTranscriptIds = List.of();
            expressedCodingTranscriptIds = List.of();
            expressedCodingAnnotationRows = List.of();
        }
    }


    List<GoAnnotationRow> expressedCodingAnnotationRows(Ontology ontology,
                                                        Map<AccessionNumber, GeneModel> geneIdToTranscriptMap,
                                                        Map<TermId, Set<TermId>> transcript2GoMap,
                                                        Set<TermId> significantGoSet) {
        // collect all GO terms that annotate at least one transcript
        Set<TermId> goIdSet = new HashSet<>();
        List<GoAnnotationRow> rows = new ArrayList<>();
        for (TermId transcriptId : expressedCodingTranscriptIds) {
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
            for (TermId transcriptId : expressedCodingTranscriptIds) {
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

    public String getGeneSymbol() {
        return geneSymbol;
    }

    List<GoAnnotationRow> allAnnotationRows(Ontology ontology,
                                            Map<AccessionNumber, GeneModel> geneIdToTranscriptMap,
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

    public List<GoAnnotationRow> getExpressedCodingAnnotationRows() { return expressedCodingAnnotationRows; }

    public List<TermId> getExpressedNoncodingTranscriptIds() { return this.expressedNoncodingTranscriptIds;}


    public List<String> getTranscripts() {
        return  this.transcriptIds.stream().map(TermId::getValue).collect(Collectors.toList());
    }

    public List<String> getExpressedCodingTranscripts() {
        return  this.expressedCodingTranscriptIds.stream().map(TermId::getValue).collect(Collectors.toList());
    }

    /**
     *
     * @return All GO ids that are associated with one or more expressed transcript.
     */
    public Set<TermId> getAllGoIds() {
        return expressedCodingAnnotationRows.stream()
                .map(GoAnnotationRow::getGoId)
                .collect(Collectors.toSet());
    }
}
