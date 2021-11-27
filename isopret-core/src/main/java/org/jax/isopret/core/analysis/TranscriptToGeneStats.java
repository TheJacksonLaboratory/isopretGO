package org.jax.isopret.core.analysis;

import org.jax.isopret.core.transcript.AccessionNumber;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class TranscriptToGeneStats {
    Logger LOGGER = LoggerFactory.getLogger(TranscriptToGeneStats.class);

    private final Ontology geneOntology;
    private final Map<AccessionNumber, Set<TermId>> transcriptIdToGoTermsMap;
    private final Map<AccessionNumber, AccessionNumber> transcriptToGeneIdMap;

    public TranscriptToGeneStats(Ontology geneOntology, Map<AccessionNumber, Set<TermId>> transcriptIdToGoTermsMap, Map<AccessionNumber, AccessionNumber> transcriptToGeneIdMap) {
        this.geneOntology = geneOntology;
        this.transcriptIdToGoTermsMap = transcriptIdToGoTermsMap;
        this.transcriptToGeneIdMap = transcriptToGeneIdMap;
    }

    public void display() {
        try (Writer stdout =  new BufferedWriter(new OutputStreamWriter(System.out))) {
            write(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(String filename) {
        File file = new File(filename);
        try (Writer bw =  new BufferedWriter(new FileWriter(file))) {
            write(bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(Writer writer) throws IOException {
        writer.write("### Isopret: Transcript to Gene Annotations data  ###\n\n");
        Set<AccessionNumber> geneIds = new HashSet<>(this.transcriptToGeneIdMap.values());
        int n_number_of_genes = geneIds.size();
        int n_number_of_transcripts = this.transcriptIdToGoTermsMap.size();
        writer.write("\tgenes: " + n_number_of_genes +"\n");
        writer.write("\ttranscripts: " + n_number_of_transcripts +"\n");
        Set<TermId> goSet = transcriptIdToGoTermsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        int n_goterms = goSet.size();
        int n_valid = 0;
        for (TermId id : goSet) {
            if (geneOntology.containsTerm(id) && geneOntology.getPrimaryTermId(id).equals(id)) {
                n_valid++;
            }
        }
        writer.write("\tGO terms: " + n_goterms + " (of which " + n_valid + " were valid)\n");
    }


}
