package org.jax.isopret.core.analysis;

import org.jax.isopret.core.impl.rnaseqdata.RnaSeqAnalysisMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private final static Logger LOGGER = LoggerFactory.getLogger(IsopretStats.class);

   private final Map<String, String> data;
   /** errors and warnings encountered during the analysis */
   private final Map<String, String> errors;
    private final Map<String, String> warnings;
    private final Map<String, String> info;

    public IsopretStats(Map<String,String> data, Map<String, String> errs,
                        Map<String, String> warn, Map<String, String> info) {
        this.data = data;
        this.errors = errs;
        this.warnings = warn;
        this.info = info;
    }
    /** Output to shell */
    public void display() {
        try (Writer stdout =  new BufferedWriter(new OutputStreamWriter(System.out))) {
            write(stdout);
        } catch (IOException e) {
            LOGGER.error("Could not display statistics: {}", e.getMessage());
        }
    }
    /** Output to TSV file. */
    public void writeToFile(String filename) {
        File file = new File(filename);
        try (Writer bw =  new BufferedWriter(new FileWriter(file))) {
            write(bw);
        } catch (IOException e) {
            LOGGER.error("Could not write statistics: {}", e.getMessage());
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

    public Map<String, String> getErrors() {
        return errors;
    }

    public Map<String, String> getWarnings() {
        return warnings;
    }

    public Map<String, String> getInfo() {
        return info;
    }

    public Map<String,String> getAllEntries() {
        Map<String, String> all = new LinkedHashMap<>();
        all.putAll(data);
        all.putAll(errors);
        all.putAll(warnings);
        all.putAll(info);
        return all;
    }

    public void addInfo(String inputFile, String basename) {
        this.info.put(inputFile, basename);
    }

    /**
     * Convenient way of collecting data about the current run.
     */
    public static class Builder {

        private final Map<String, String> map;
        private final Map<String, String> errors;
        private final Map<String, String> warnings;
        private final Map<String, String> info;

        public Builder() {
            map = new LinkedHashMap<>();
            errors = new LinkedHashMap<>();
            warnings = new LinkedHashMap<>();
            info = new LinkedHashMap<>();
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

        public Builder rnaSeqMethod(RnaSeqAnalysisMethod method) {
            map.put("RNA-Seq analysis method", method.name());
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

        public Builder annotatedGeneCount(int n) {
            map.put("Number of annotated genes", String.valueOf(n));
            return this;
        }

        public Builder annotatingGoTermCountGenes(int n) {
            map.put("Number of GO terms used to annotate genes", String.valueOf(n));
            return this;
        }

        public Builder annotatedTranscripts(int n) {
            map.put("Number of annotated transcripts", String.valueOf(n));
            return this;
        }

        public Builder annotatingGoTermCountTranscripts(int n) {
            map.put("Number of GO terms used to annotate transcripts", String.valueOf(n));
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
            map.put("Number of genes with annotated transcripts", String.valueOf(n));
            return this;
        }

        public Builder transcriptsCount(int n) {
            map.put("Number of of annotated transcripts", String.valueOf(n));
            return this;
        }

        public Builder dasIsoformCount(int n) {
            map.put("Number of significantly differential isoforms", String.valueOf(n));
            return this;
        }

        public Builder dgeGeneCount(int n) {
            map.put("Number of significantly differential genes", String.valueOf(n));
            return this;
        }

        public Builder dasPopulation(int n) {
            map.put("DAS population size", String.valueOf(n));
            return this;
        }

        public Builder dasStudy(int n) {
            map.put("DAS study size", String.valueOf(n));
            return this;
        }

        public Builder dgePopulation(int n) {
            map.put("DGE population size", String.valueOf(n));
            return this;
        }

        public Builder dgeStudy(int n) {
            map.put("DGE study size", String.valueOf(n));
            return this;
        }

        public Builder fdrThreshold(double x) {
            map.put("Chosen FDR threshold", String.valueOf(x));
            return this;
        }

        public Builder expressionPthreshold(double x) {
            map.put("Probability threshold (expression)", String.valueOf(x));
            return this;
        }

        public Builder splicingPthreshold(double x) {
            map.put("Probability threshold (splicing)", String.valueOf(x));
            return this;
        }

        public Builder dgeSigGoTermCount(int n) {
            map.put("Significant GO terms (expression)", String.valueOf(n));
            return this;
        }

        public Builder dasSigGoTermCount(int n) {
            map.put("Significant GO terms (splicing)", String.valueOf(n));
            return this;
        }




        public Builder error(String err) {
            errors.put("[ERROR]", err);
            return this;
        }

        public Builder warning(String w) {
            warnings.put("[WARNING]", w);
            return this;
        }

        public Builder info(String key, String value) {
            info.put(String.format("[INFO] %s", key), value);
            return this;
        }



        public IsopretStats build() {
            return new IsopretStats(map, errors, warnings, info);
        }


    }




}
