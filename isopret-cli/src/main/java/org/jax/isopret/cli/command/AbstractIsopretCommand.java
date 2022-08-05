package org.jax.isopret.cli.command;

import org.jax.isopret.core.except.IsopretException;
import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.hbadeals.HbaDealsParser;
import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.hbadeals.HbaDealsThresholder;
import org.jax.isopret.model.GeneModel;
import org.jax.isopret.core.interpro.InterproMapper;
import org.jax.isopret.core.io.TranscriptFunctionFileParser;
import org.jax.isopret.model.AccessionNumber;
import org.jax.isopret.model.GeneSymbolAccession;
import org.jax.isopret.model.JannovarReader;
import org.jax.isopret.model.Transcript;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class AbstractIsopretCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIsopretCommand.class);
    /** isopret only supports hg38. */
    private final GenomicAssembly assembly = GenomicAssemblies.GRCh38p13();

    private Ontology geneOntology = null;

    private GoAssociationContainer associationContainer = null;

    private JannovarReader jannovarReader = null;

    protected   Map<AccessionNumber, GeneModel> hgncMap = null;
    /** Key ensembl transcript id; values: annotating go terms .*/
    protected Map<TermId, Set<TermId>> transcriptToGoMap = null;

    protected Map<GeneSymbolAccession, List<Transcript>>  geneSymbolAccessionListMap = null;

    @CommandLine.Option(names={"-d","--download"},
            scope = CommandLine.ScopeType.INHERIT,
            description = "directory to download HPO data")
    protected String downloadDirectory="data";




    protected Ontology loadGeneOntology() {
        if (geneOntology == null) {
            File goJsonFile = new File(downloadDirectory + File.separator + "go.json");
            if (!goJsonFile.isFile()) {
                throw new IsopretRuntimeException("Could not find Gene Ontology JSON file at " + goJsonFile.getAbsolutePath());
            }
            geneOntology = OntologyLoader.loadOntology(goJsonFile);
        }
        int n_terms = geneOntology.countNonObsoleteTerms();
        LOGGER.info("Loaded Gene Ontology json file with {} terms.", n_terms);
        return geneOntology;
    }


    protected GoAssociationContainer loadGoAssociationContainer() {
        if (this.associationContainer == null) {
            File goGafFile = new File(downloadDirectory + File.separator + "goa_human.gaf");
            if (!goGafFile.isFile()) {
                throw new IsopretRuntimeException("Could not find Gene Ontology goa_human.gaf file at " +
                        goGafFile.getAbsolutePath());
            }
            Ontology go = loadGeneOntology();
            this.associationContainer = GoAssociationContainer.loadGoGafAssociationContainer(goGafFile.toPath(), go);
        }
        LOGGER.info("Loaded GO Association container with {} associations",
                associationContainer.getRawAssociations().size());
        return this.associationContainer;
    }

    /**
     * Hardcoded path to Ensembl transcript definitions.
     * isopret only supports Ensembl.
     * @return path to the downloaded Jannovar file in the download directory
     */
    private File jannovarTranscriptFile() {
        return new File(downloadDirectory + File.separator + "hg38_ensembl.ser");
    }


    protected Map<GeneSymbolAccession, List<Transcript>> loadJannovarSymbolToTranscriptMap() {
        if (jannovarReader == null || geneSymbolAccessionListMap == null) {
            jannovarReader = new JannovarReader(jannovarTranscriptFile(), assembly);
            geneSymbolAccessionListMap = jannovarReader.getGeneToTranscriptListMap();
            LOGGER.info("Loaded JannovarReader with {} symbols",
                    jannovarReader.getGeneToTranscriptListMap().size());
        }
        return geneSymbolAccessionListMap;
    }


    protected InterproMapper loadInterproMapper() {
        File interproDescriptionFile = new File(downloadDirectory + File.separator + "interpro_domain_desc.txt");
        File interproDomainsFile = new File(downloadDirectory + File.separator + "interpro_domains.txt");
        if (! interproDomainsFile.isFile()) {
            throw new IsopretRuntimeException("Could not find interpro_domains.txt at " +
                    interproDomainsFile.getAbsolutePath());
        }
        if (! interproDescriptionFile.isFile()) {
            throw new IsopretRuntimeException("Could not find interpro_domain_desc.txt at " +
                    interproDescriptionFile.getAbsolutePath());
        }
        return new InterproMapper(interproDescriptionFile, interproDomainsFile);
    }


    private void runTranscriptFunctionFileParser() throws IsopretException {
        if (geneOntology == null) {
            loadGeneOntology();
        }
        TranscriptFunctionFileParser parser = new TranscriptFunctionFileParser(new File(downloadDirectory), geneOntology);
        transcriptToGoMap = parser.getTranscriptIdToGoTermsMap();
    }

    protected Map<TermId, Set<TermId>> loadTranscriptIdToGoTermsMap() throws IsopretException {
        if (transcriptToGoMap == null)
            runTranscriptFunctionFileParser();
        return transcriptToGoMap;
    }

    protected HbaDealsThresholder initializeHbaDealsThresholder(Map<AccessionNumber, GeneModel> hgncMap, String hbadealsPath) {
        HbaDealsParser hbaParser = new HbaDealsParser(hbadealsPath, hgncMap);
        Map<AccessionNumber, HbaDealsResult> hbaDealsResults = hbaParser.getEnsgAcc2hbaDealsMap();
        LOGGER.trace("Analyzing {} genes.", hbaDealsResults.size());
        return new HbaDealsThresholder(hbaDealsResults);
    }


    /**
     *
     * @param category either gene-ontology or interpro
     * @param hbaDealsFileName e.g., SRP149366_70.txt
     * @return e.g., gene-ontology-overrep-SRP149366_70.tsv
     */
    protected String getDefaultOutfileName(String category, String hbaDealsFileName) {
        File f = new File(hbaDealsFileName);
        String basename = f.getName();
        String hbaWithoutExtension =  basename.replaceFirst("[.][^.]+$", "");
        return hbaWithoutExtension + "-overrep-" + category + ".tsv";

    }



}
