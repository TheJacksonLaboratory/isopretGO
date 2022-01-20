package org.jax.isopret.core.html;

import org.jax.isopret.core.go.HbaDealsGoAnalysis;
import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.hbadeals.HbaDealsThresholder;
import org.jax.isopret.core.interpro.InterproMapper;
import org.jax.isopret.core.transcript.Transcript;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.analysis.stats.GoTerm2PValAndCounts;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Deprecated
public abstract class AbstractWriter {
    protected static final Logger LOGGER = LoggerFactory.getLogger(TsvWriter.class);
    protected final String outprefix;
    protected final HbaDealsThresholder thresholder;
    protected final List<GoTerm2PValAndCounts> dasGoTerms;
    protected final List<GoTerm2PValAndCounts> dgeGoTerms;
    protected final Map<String, HbaDealsResult> hbaDealsResults;
    protected final String ontologizerCalculation;
    protected final String mtc;
    protected final Ontology geneOntology;


    AbstractWriter(String prefix,
                      HbaDealsThresholder thresholder,
                      List<GoTerm2PValAndCounts> das,
                      List<GoTerm2PValAndCounts> dge,
                      String calc,
                      String mtc,
                      Ontology ontology) {
        this.outprefix = prefix;
        this.thresholder = thresholder;
        this.dasGoTerms = das;
        this.dgeGoTerms = dge;
        this.hbaDealsResults = thresholder.getRawResults();
        this.ontologizerCalculation = calc;
        this.mtc = mtc;
        this.geneOntology = ontology;
    }


    public abstract void write();




    public static class Builder {

        private String outprefix = "ISOPRET";
        private HbaDealsThresholder thresholder;
        private List<GoTerm2PValAndCounts> dasGoTerms;
        private List<GoTerm2PValAndCounts> dgeGoTerms;
        private String ontologizerCalculation;
        private String mtc;
        private Ontology ontology;
        // the following just needed for HTML
        private GoAssociationContainer container;
        private HbaDealsGoAnalysis hbaDealsGoAnalysis;
        private Map<String, List<Transcript>> geneSymbolToTranscriptMap;
        private Integer chunkSize;
        private InterproMapper interproMapper;
        private File hbadealsFile;

        public Builder prefix(String p) {
            this.outprefix = p;
            return this;
        }

        public Builder thresholder(HbaDealsThresholder t) {
            this.thresholder = t;
            return this;
        }

        public Builder dasGoTerms(List<GoTerm2PValAndCounts> das) {
            this.dasGoTerms = das;
            return this;
        }

        public Builder dgeGoTerms(List<GoTerm2PValAndCounts> dge) {
            this.dgeGoTerms = dge;
            return this;
        }


        public Builder ontologizerCalculation(String calc) {
            this.ontologizerCalculation = calc;
            return this;
        }

        public Builder mtc(String m) {
            this.mtc = m;
            return this;
        }

        public Builder ontology(Ontology ontology) {
            this.ontology = ontology;
            return this;
        }

        // the following just needed for HTML
        public Builder  goAssociationContainer(GoAssociationContainer container) {
            this.container = container;
            return this;
        }

        public Builder hbago(HbaDealsGoAnalysis hbaDealsGoAnalysis) {
            this.hbaDealsGoAnalysis = hbaDealsGoAnalysis;
            return this;
        }

        public Builder genesymbolToTranscriptMap(Map<String, List<Transcript>> geneSymbolToTranscriptMap) {
            this.geneSymbolToTranscriptMap = geneSymbolToTranscriptMap;
            return this;
        }

        public Builder chunkSize(int s) {
            this.chunkSize = s;
            return this;
        }

        public Builder  interproMapper(InterproMapper interproMapper) {
            this.interproMapper = interproMapper;
            return this;
        }

        public Builder hbadealsFile(File f) {

            this.hbadealsFile = f;
            return this;
        }

        public TsvWriter buildTsvWriter() {
            Objects.requireNonNull(outprefix);
            Objects.requireNonNull(thresholder);
            Objects.requireNonNull(dasGoTerms);
            Objects.requireNonNull(dgeGoTerms);
            Objects.requireNonNull(ontologizerCalculation);
            Objects.requireNonNull(mtc);
            return new TsvWriter(outprefix, thresholder, dasGoTerms, dgeGoTerms, ontologizerCalculation, mtc, ontology);
        }

        public HtmlWriter buildHtmlWriter() {
            Objects.requireNonNull(outprefix);
            Objects.requireNonNull(thresholder);
            Objects.requireNonNull(dasGoTerms);
            Objects.requireNonNull(dgeGoTerms);
            Objects.requireNonNull(ontologizerCalculation);
            Objects.requireNonNull(mtc);
            Objects.requireNonNull(container);
            Objects.requireNonNull(hbaDealsGoAnalysis);
            Objects.requireNonNull(geneSymbolToTranscriptMap);
            Objects.requireNonNull(chunkSize);
            Objects.requireNonNull(interproMapper);
            Objects.requireNonNull(hbadealsFile);
            return new HtmlWriter(outprefix, thresholder, dasGoTerms, dgeGoTerms, ontologizerCalculation, mtc, ontology,
                    container, hbaDealsGoAnalysis, geneSymbolToTranscriptMap,chunkSize, interproMapper, hbadealsFile);
        }
    }

}
