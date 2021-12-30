package org.jax.isopret.core.io;

import org.jax.isopret.core.transcript.AccessionNumber;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Our inference procedure generates a file called {@code isoform_function_list.txt} that
 * contains a list of isoforms with their inferred functions,
 * ENST00000380173	GO:2001303
 * ENST00000251535	GO:2001304
 * ENST00000609196	GO:2001311
 * (...)
 * This class parses the file and checks the syntax of the identifiers and also checks that the GO terms
 * are present in the ontology object
 * @author Peter N Robinson
 */
public class TranscriptFunctionFileParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TranscriptFunctionFileParser.class);
    /** Key: TermId representing the accession number of a gene or transcript (e.g., ENST:00123), value:
     * set of annotated GO terms
     */
    private final  Map<TermId, Set<TermId>> transcriptIdToGoTermsMap;

    public TranscriptFunctionFileParser(File transcriptFunctionFile, Ontology ontology) {
        Map<TermId, Set<TermId>> annotMap = new HashMap<>();
        Set<String> notFound = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(transcriptFunctionFile))) {
            String line = br.readLine();
            if (! line.startsWith("Ensembl.ID\tGo.Terms")) {
                throw new PhenolRuntimeException("Malformed header of isoform_function_list.txt: " + line);
            }
            while ((line = br.readLine()) != null) {
                String [] fields = line.split("\t");
                if (fields.length != 2) {
                    throw new PhenolRuntimeException("Malformed line in isoform_function_list.txt :" + line +
                            " (number of fields: " + fields.length + ")");
                }
                AccessionNumber transcriptId = AccessionNumber.ensemblTranscript(fields[0]);
                TermId GoId = TermId.of(fields[1]);
                if (! ontology.containsTerm(GoId)) {
                    notFound.add(GoId.getValue());
                    continue;
                }
                TermId transcriptTermId = transcriptId.toTermId();
                annotMap.putIfAbsent(transcriptTermId, new HashSet<>());
                annotMap.get(transcriptTermId).add(GoId);
            }
        } catch (IOException e) {
            throw new PhenolRuntimeException("Could not import isoform_function_list.txt :" + e.getMessage());
        }
        if (notFound.size() > 0) {
            LOGGER.warn("Could not find " + notFound.size() + " terms in Ontology");
            //LOGGER.warn(String.join("; ", notFound));

        }
        transcriptIdToGoTermsMap = Map.copyOf(annotMap); // return immutable map
    }

    public Map<TermId, Set<TermId>> getTranscriptIdToGoTermsMap() {
        return transcriptIdToGoTermsMap;
    }

    /**
     *
     * @param transcript2gene Map with Ensembl transcript id to gene id map
     * @return Map with key: Ensembl gene id and value set of GO Annotations.
     */
    public Map<TermId, Set<TermId>> getGeneIdToGoTermsMap(Map<TermId, TermId> transcript2gene) {
        Map<TermId, Set<TermId>> annotMap = new HashMap<>();
        if (transcriptIdToGoTermsMap.isEmpty()) {
            // should never happen
            throw new PhenolRuntimeException("Attempt to get gene-level annotations without transcript annotations");
        }
        for (var entry : transcriptIdToGoTermsMap.entrySet()) {
            TermId transcriptAcc = entry.getKey();
            if (! transcript2gene.containsKey(transcriptAcc)) {
                LOGGER.error("Could not find gene accession number for {}", transcriptAcc.getValue());
            } else {
                TermId geneAcc = transcript2gene.get(transcriptAcc);
                annotMap.putIfAbsent(geneAcc, new HashSet<>());
                for (TermId goId : entry.getValue()) {
                    annotMap.get(geneAcc).add(goId);
                }
            }
        }
        return Map.copyOf(annotMap); // return immutable map
    }
}
