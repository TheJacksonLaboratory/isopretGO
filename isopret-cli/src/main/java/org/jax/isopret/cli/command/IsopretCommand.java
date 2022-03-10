package org.jax.isopret.cli.command;

import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.hbadeals.HbaDealsParser;
import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.hbadeals.HbaDealsThresholder;
import org.jax.isopret.core.hgnc.HgncItem;
import org.jax.isopret.core.hgnc.HgncParser;
import org.jax.isopret.core.interpro.InterproMapper;
import org.jax.isopret.core.io.TranscriptFunctionFileParser;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.JannovarReader;
import org.jax.isopret.core.transcript.Transcript;
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

public abstract class IsopretCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsopretCommand.class);
    /** isopret only supports hg38. */
    private final GenomicAssembly assembly = GenomicAssemblies.GRCh38p13();

    private Ontology geneOntology = null;

    private GoAssociationContainer associationContainer = null;

    private JannovarReader jannovarReader = null;

    private  Map<AccessionNumber, HgncItem> hgncMap = null;
    /** Key ensembl transcript id; values: annotating go terms .*/
    private Map<TermId, Set<TermId>> transcriptToGoMap = null;


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


    protected Map<String, List<Transcript>> loadJannovarSymbolToTranscriptMap() {
        if (jannovarReader == null) {
            jannovarReader = new JannovarReader(jannovarTranscriptFile(), assembly);
            LOGGER.info("Loaded JannovarReader with {} symbols",
                    jannovarReader.getSymbolToTranscriptListMap().size());
        }
        return jannovarReader.getSymbolToTranscriptListMap();
    }

    protected Map<AccessionNumber, List<Transcript>> loadJannovarGeneIdToTranscriptMap() {
        if (jannovarReader == null) {
            jannovarReader = new JannovarReader(jannovarTranscriptFile(), assembly);
            LOGGER.info("Loaded JannovarReader with {} genes",
                    jannovarReader.getGeneIdToTranscriptMap().size());
        }
        return jannovarReader.getGeneIdToTranscriptMap();
    }

    /**
     * @return map with key: Ensembl Gene ID and value: the corresponding {@link HgncItem} object
     */
    protected Map<AccessionNumber, HgncItem> loadHgncMap() {
        if (hgncMap == null) {
            File hgncFile = new File(downloadDirectory + File.separator + "hgnc_complete_set.txt");
            HgncParser hgncParser = new HgncParser(hgncFile);
            hgncMap = hgncParser.ensemblMap();
            LOGGER.info("Loaded Ensembl HGNC map with {} genes", hgncMap.size());
        }
        return hgncMap;
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


    private void runTranscriptFunctionFileParser() {
        if (geneOntology == null) {
            loadGeneOntology();
        }
        TranscriptFunctionFileParser parser = new TranscriptFunctionFileParser(new File(downloadDirectory), geneOntology);
        transcriptToGoMap = parser.getTranscriptIdToGoTermsMap();
    }

    protected Map<TermId, Set<TermId>> loadTranscriptIdToGoTermsMap() {
        if (transcriptToGoMap == null)
            runTranscriptFunctionFileParser();
        return transcriptToGoMap;
    }

    protected HbaDealsThresholder initializeHbaDealsThresholder(Map<AccessionNumber, HgncItem> hgncMap, String hbadealsPath) {
        HbaDealsParser hbaParser = new HbaDealsParser(hbadealsPath, hgncMap);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();
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
