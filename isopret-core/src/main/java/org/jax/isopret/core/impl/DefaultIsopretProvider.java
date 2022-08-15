package org.jax.isopret.core.impl;

import org.jax.isopret.core.IsopretProvider;
import org.jax.isopret.core.configuration.IsopretDataResolver;
import org.jax.isopret.core.impl.go.IsopretContainerFactory;
import org.jax.isopret.core.impl.hgnc.HgncParser;
import org.jax.isopret.core.impl.go.TranscriptFunctionFileParser;
import org.jax.isopret.core.InterproMapper;
import org.jax.isopret.core.impl.jannovar.JannovarReader;
import org.jax.isopret.model.*;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultIsopretProvider implements IsopretProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultIsopretProvider.class);

    private final IsopretDataResolver dataResolver;

    private final GenomicAssembly assembly = GenomicAssemblies.GRCh38p13();

    private Ontology geneOntology = null;

    private Map<TermId, Set<TermId>> transcriptToGoMap = null;

    private Map<TermId, Set<TermId>> geneIdToGoTermsMap = null;


    private Map<TermId, TermId> transcriptToGeneIdMap = null;

    private  AssociationContainer<TermId> transcriptContainer = null;

    private AssociationContainer<TermId> geneContainer = null;

    private InterproMapper interproMapper = null;


    public DefaultIsopretProvider(Path dataDirectory) {
        dataResolver =  IsopretDataResolver.of(dataDirectory);
    }

    @Override
    public Ontology geneOntology() {
        if (geneOntology == null) {
            Path goPath = dataResolver.goJson();
            geneOntology = OntologyLoader.loadOntology(goPath.toFile());
            int n_terms = geneOntology.countNonObsoleteTerms();
            LOGGER.info("Loaded Gene Ontology json file with {} terms.", n_terms);
        }
        return geneOntology;
    }

    /**
     *
     * @return Map with key: A gene symbol/accession object, value - list of corresponding isoforms
     */
    @Override
    public Map<GeneSymbolAccession, List<Transcript>> geneSymbolToTranscriptListMap() {
        // hg38_ensembl.ser
        File jannovarTranscriptFile = dataResolver.hg38Ensembl().toFile();
        JannovarReader jannovarReader = new JannovarReader(jannovarTranscriptFile, assembly);
        var symbolToTranscriptMap = jannovarReader.getGeneToTranscriptListMap();
        LOGGER.info("Loaded JannovarReader with {} symbols",
                jannovarReader.getGeneToTranscriptListMap().size());
        return symbolToTranscriptMap;
    }

    @Override

    public Map<AccessionNumber, GeneModel> ensemblGeneModelMap() {
        File hgncFile = dataResolver.hgncCompleteSet().toFile();
        HgncParser hgncParser = new HgncParser(hgncFile, geneSymbolToTranscriptListMap());
        Map<AccessionNumber, GeneModel> hgncMap  = hgncParser.ensemblMap();
        LOGGER.info("Loaded HGNC Map with {} symbols",
                hgncMap.size());
        return hgncMap;
    }

    @Override
    public Map<TermId, Set<TermId>> transcriptIdToGoTermsMap() {
        if (transcriptToGoMap == null) {
            initFromTranscriptFunctionParser();
        }
        return transcriptToGoMap;
    }

    @Override
    public Map<TermId, TermId> transcriptToGeneIdMap() {
        if (this.transcriptToGeneIdMap == null) {
            Map<TermId, TermId> accessionNumberMap = new HashMap<>();
            for (var entry : geneSymbolToTranscriptListMap().entrySet()) {
                var geneAcc = entry.getKey();
                var geneTermId = geneAcc.accession().toTermId();
                var transcriptList = entry.getValue();
                for (var transcript : transcriptList) {
                    var transcriptAcc = transcript.accessionId();
                    var transcriptTermId = transcriptAcc.toTermId();
                    //System.out.println(transcriptAcc.getAccessionString() +": " + geneAcc.getAccessionString());
                    accessionNumberMap.put(transcriptTermId, geneTermId);
                }
            }
            this.transcriptToGeneIdMap = Map.copyOf(accessionNumberMap); // immutable copy
        }
        return this.transcriptToGeneIdMap;
    }

    @Override
    public AssociationContainer<TermId> transcriptContainer() {
        if (this.transcriptContainer == null) {
            initContainers();
        }
        return this.transcriptContainer;
    }

    @Override
    public AssociationContainer<TermId> geneContainer() {
        if (this.geneContainer == null) {
            initContainers();
        }
        return this.geneContainer;
    }

    private void initContainers() {
        IsopretContainerFactory isoContainerFac = new IsopretContainerFactory(geneOntology,
                transcriptIdToGoTermsMap(), gene2GoMap());
        this.transcriptContainer = isoContainerFac.transcriptContainer();
        LOGGER.info("transcriptContainer terms n={}", transcriptContainer.getAnnotatingTermCount());
        LOGGER.info("transcriptContainer items n={}", transcriptContainer.getAnnotatedDomainItemCount());
        this.geneContainer = isoContainerFac.geneContainer();
        LOGGER.info("geneContainer terms n={}", geneContainer.getAnnotatingTermCount());
        LOGGER.info("geneContainer items n={}", geneContainer.getAnnotatedDomainItemCount());
    }

    @Override
    public Map<TermId, Set<TermId>> gene2GoMap() {
        if (geneIdToGoTermsMap == null) {
            initFromTranscriptFunctionParser();
        }
        return geneIdToGoTermsMap;
    }
    private void initFromTranscriptFunctionParser() {
        File predictionFileMf = dataResolver.isoformFunctionListMf().toFile();
        File predictionFileBp = dataResolver.isoformFunctionListBp().toFile();
        File predictionFileCc = dataResolver.isoformFunctionListCc().toFile();
        TranscriptFunctionFileParser parser = new TranscriptFunctionFileParser(predictionFileMf,
                predictionFileBp,
                predictionFileCc,
                geneOntology);
        transcriptToGoMap = parser.getTranscriptIdToGoTermsMap();
        geneIdToGoTermsMap = parser.getGeneIdToGoTermsMap(transcriptToGeneIdMap());
    }
    @Override
    public InterproMapper interproMapper() {
        if (interproMapper == null) {
            File interproDescriptionFile = dataResolver.interproDomainDesc().toFile();
            File interproDomainsFile = dataResolver.interproDomains().toFile();
            this.interproMapper = new InterproMapper(interproDescriptionFile, interproDomainsFile);
        }
        return this.interproMapper;
    }

    @Override
    public Path goJson() {
        return dataResolver.goJson();
    }

    @Override
    public Path isoformFunctionListBp() {
        return dataResolver.isoformFunctionListBp();
    }

    @Override
    public Path isoformFunctionListCc() {
        return dataResolver.isoformFunctionListCc();
    }

    @Override
    public Path isoformFunctionListMf() {
        return dataResolver.isoformFunctionListMf();
    }

    @Override
    public Path hg38Ensembl() {
        return dataResolver.hg38Ensembl();
    }

    @Override
    public Path hgncCompleteSet() {
        return dataResolver.hgncCompleteSet();
    }

    @Override
    public Path interproDomainDesc() {
        return dataResolver.interproDomainDesc();
    }

    @Override
    public Path interproDomains() {
        return dataResolver.interproDomains();
    }


}
