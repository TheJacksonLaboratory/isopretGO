package org.jax.isopret.core.analysis;

import org.jax.isopret.core.hgnc.HgncItem;
import org.jax.isopret.core.interpro.InterproAnnotation;
import org.jax.isopret.core.interpro.InterproEntry;
import org.jax.isopret.core.interpro.InterproEntryType;
import org.jax.isopret.core.interpro.InterproMapper;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.Transcript;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class IsopretStats {


    private final Ontology geneOntology;
    private final GoAssociationContainer container;
    private final Map<AccessionNumber, HgncItem> hgncMap;
    private final Map<String, List<Transcript>> geneSymbolToTranscriptMap;
    private final InterproMapper interproMapper;

    public IsopretStats(Ontology go,
                        GoAssociationContainer container,
                        Map<AccessionNumber, HgncItem> hgncMap,
                        Map<String, List<Transcript>> geneSymbolToTranscriptMap, InterproMapper mapper) {
        this.geneOntology = go;
        this.container = container;
        this.hgncMap = hgncMap;
        this.geneSymbolToTranscriptMap = geneSymbolToTranscriptMap;
        this.interproMapper = mapper;
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
        writer.write("### Isopret: Input data  ###\n\n");
        writer.write("### Gene Ontology:\n");
        String goVersion = geneOntology.getMetaInfo().getOrDefault("data-version", "n/a");
        int n_terms = geneOntology.countNonObsoleteTerms();
        writer.write("\tVersion: " + goVersion + "\n");
        writer.write("\tTerms n=" + n_terms + "\n");
        writer.write("### GO associations:\n");
        int n_association_counts = container.getRawAssociations().size();
        int n_annotating_go_terms = container.getOntologyTermCount();
        int n_annotated_gene_count = container.getTotalNumberOfAnnotatedItems();
        writer.write("\tAnnotations n=" + n_association_counts + "\n");
        writer.write("\tNumber of GO terms used for annotations n=" + n_annotating_go_terms + "\n");
        writer.write("\tNumber of annotated genes n=" + n_annotated_gene_count + "\n");
        writer.write("### Human Gene Nomenclature Committee:\n");
        writer.write("\tHGNC Map n=" + hgncMap.size() + "\n");
        writer.write("### Jannovar Transcript Map:\n");
        int n_genes = geneSymbolToTranscriptMap.size();
        int n_isoforms = (int) geneSymbolToTranscriptMap.values().stream().mapToLong(Collection::size).sum();
        double n_isoforms_per_gene = (double)n_isoforms/n_genes;
        writer.write("\tGenes n=" + n_genes + "\n");
        writer.write("\tIsoforms n=" + n_isoforms + "\n");
        writer.write(String.format("\tMean isoforms per gene %.1f\n", n_isoforms_per_gene));
        writer.write("### Interpro Annotations:\n");
        summarizeDomains(writer);
        summarizeAnnotations(writer);
        writer.write("\n\n");
    }



    private void summarizeDomains(Writer writer) throws IOException {
        Map<Integer, InterproEntry> interproDescriptionMap = interproMapper.getInterproDescription();
        Map<InterproEntryType, Long> counts = interproDescriptionMap
                .values()
                .stream()
                .map(InterproEntry::getEntryType)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        writer.write("----- Domains ------------\n");
        for (var entry : counts.entrySet()) {
            writer.write(String.format("\t%s: %d.\n", entry.getKey().name(), entry.getValue()));
        }
    }

    private void summarizeAnnotations(Writer writer) throws IOException {
        Map<AccessionNumber, List<InterproAnnotation>> transcriptIdToInterproAnnotationMap = interproMapper.getInterproAnnotation();
        writer.write("----- Annotations ------------\n");
        writer.write(String.format("\tTotal annotated transcripts: %d\n", transcriptIdToInterproAnnotationMap.size()));
        long totalAnnotations =
                transcriptIdToInterproAnnotationMap
                        .values()
                        .stream()
                        .mapToInt(List::size)
                        .sum();
        writer.write(String.format("\tTotal annotations: %d\n", totalAnnotations));
    }




}
