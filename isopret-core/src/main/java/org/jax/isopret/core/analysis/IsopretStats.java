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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class to collect descriptive statistics about the current run in a Map that
 * can be used to output a TSV file or to show a ListView in JavaFX
 * <pre>
 * {@code
 * Ontology geneOntology = loadGeneOntology();
 * GoAssociationContainer container = loadGoAssociationContainer();
 * Set<TermId> allAnnotated = container.getAllAnnotatedGenes();
 * Map<AccessionNumber, HgncItem> hgncMap = loadHgncMap();
 * Map<String, List<Transcript>> geneSymbolToTranscriptMap = loadJannovarTranscriptMap();
 * int n_transcripts = geneSymbolToTranscriptMap.values()
 *       .stream()
 *       .map(List::size)
 *       .reduce(0, Integer::sum);
 *  InterproMapper mapper = loadInterproMapper();
 *  String goVersion = geneOntology.getMetaInfo().getOrDefault("data-version", "n/a/");
 *  IsopretStats.Builder builder = new IsopretStats.Builder();
 *  builder.geneOntologyVersion(goVersion)
 *                 .hgncCount(hgncMap.size())
 *                 .goAssociations(container.getRawAssociations().size())
 *                 .gannotatedGeneCount(container.getTotalNumberOfAnnotatedItems())
 *                 .annotatingGoTermCount(container.getAnnotatingTermCount())
 *                 .interproAnnotationCount(mapper.getInterproAnnotationCount())
 *                 .interproDescriptionCount(mapper.getInterproDescriptionCount())
 *                 .geneSymbolCount(geneSymbolToTranscriptMap.size())
 *                 .transcriptsCount(n_transcripts);
 *
 *  IsopretStats stats = builder.build();
 *  stats.display();
 * }
 * </pre>
 * @author Peter N Robinson
 */
public class IsopretStats {

   private final Map<String, String> data;

    public IsopretStats(Map<String,String> data) {
        this.data = data;
    }
    /** Output to shell */
    public void display() {
        try (Writer stdout =  new BufferedWriter(new OutputStreamWriter(System.out))) {
            write(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /** Output to TSV file. */
    public void writeToFile(String filename) {
        File file = new File(filename);
        try (Writer bw =  new BufferedWriter(new FileWriter(file))) {
            write(bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(Writer writer) throws IOException {
        for (var entry : this.data.entrySet()) {
            writer.write(String.format("%s\t%s\n", entry.getKey(), entry.getValue()));
        }
    }

    public Map<String, String> getData() {
        return data;
    }

    /**
     * Convenient way of collecting data about the current run.
     */
    public static class Builder {

        private final Map<String, String> map;

        public Builder() {
            map = new LinkedHashMap<>();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
            // Quoted "Z" to indicate UTC, no timezone offset
            String nowAsISO = df.format(new Date());
            map.put("Analysis performed on", nowAsISO);
        }

        public Builder version(String isopretVersion) {
            map.put("Isopret version", isopretVersion);
            return this;
        }

        public Builder geneOntologyVersion(String v) {
            map.put("Gene Ontology version", v);
            return this;
        }

        public Builder hgncCount(int n) {
            map.put("Number of HGNC gene entries", String.valueOf(n));
            return this;
        }

        public Builder goAssociationsGenes(int n) {
            map.put("Number of GO associations",String.valueOf(n));
            return this;
        }

        public Builder gannotatedGeneCount(int n) {
            map.put("Number of annotated genes", String.valueOf(n));
            return this;
        }

        public Builder annotatingGoTermCountGenes(int n) {
            map.put("Number of GO terms used to annotate genes", String.valueOf(n));
            return this;
        }

        public Builder interproAnnotationCount(int n) {
            map.put("Number of interpro annotations", String.valueOf(n));
            return this;
        }

        public Builder interproDescriptionCount(int n) {
            map.put("Number of of interpro descriptions", String.valueOf(n));
            return this;
        }

        public Builder geneSymbolCount(int n) {
            map.put("Number of of genes with annotated transcripts", String.valueOf(n));
            return this;
        }

        public Builder transcriptsCount(int n) {
            map.put("Number of of annotated transcripts", String.valueOf(n));
            return this;
        }






        public IsopretStats build() {
            return new IsopretStats(map);
        }


    }




}
