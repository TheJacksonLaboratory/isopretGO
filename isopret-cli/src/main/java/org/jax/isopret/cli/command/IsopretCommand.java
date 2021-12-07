package org.jax.isopret.cli.command;

import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.go.GoMethod;
import org.jax.isopret.core.go.HbaDealsGoAnalysis;
import org.jax.isopret.core.go.MtcMethod;
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
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.svart.GenomicAssemblies;
import org.monarchinitiative.svart.GenomicAssembly;
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
            this.associationContainer = GoAssociationContainer.loadGoGafAssociationContainer(goGafFile, go);
        }
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


    protected Map<String, List<Transcript>> loadJannovarTranscriptMap() {
        if (jannovarReader == null) {
            jannovarReader = new JannovarReader(jannovarTranscriptFile(), assembly);
        }
        return jannovarReader.getSymbolToTranscriptListMap();
    }

    protected Map<AccessionNumber, List<Transcript>> loadJannovarGeneIdToTranscriptMap() {
        if (jannovarReader == null) {
            jannovarReader = new JannovarReader(jannovarTranscriptFile(), assembly);
        }
        return jannovarReader.getGeneIdToTranscriptMap();
    }


    protected Map<AccessionNumber, HgncItem> loadHgncMap() {
        if (hgncMap == null) {
            HgncParser hgncParser = new HgncParser();
            hgncMap = hgncParser.ensemblMap();
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
        File predictionFile = new File(downloadDirectory + File.separator + "isoform_function_list.txt");
        if (!predictionFile.isFile()) {
            throw new IsopretRuntimeException("Could not find isoform_function_list.txt at " +
                    predictionFile.getAbsolutePath());
        }
        if (geneOntology == null) {
            loadGeneOntology();
        }
        TranscriptFunctionFileParser parser = new TranscriptFunctionFileParser(predictionFile, geneOntology);
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

    protected HbaDealsGoAnalysis getHbaDealsGoAnalysis(GoMethod goMethod,
                                                     HbaDealsThresholder thresholder,
                                                     Ontology ontology,
                                                     GoAssociationContainer goAssociationContainer,
                                                     MtcMethod mtc) {
        if (goMethod == GoMethod.PCunion) {
            return HbaDealsGoAnalysis.parentChildUnion(thresholder, ontology, goAssociationContainer, mtc);
        } else if (goMethod == GoMethod.PCintersect) {
            return HbaDealsGoAnalysis.parentChildIntersect(thresholder, ontology, goAssociationContainer, mtc);
        } else {
            return HbaDealsGoAnalysis.termForTerm(thresholder, ontology, goAssociationContainer, mtc);
        }
    }


}
