package org.jax.isopret.command;

import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.hgnc.HgncItem;
import org.jax.isopret.hgnc.HgncParser;
import org.jax.isopret.interpro.InterproMapper;
import org.jax.isopret.transcript.AccessionNumber;
import org.jax.isopret.transcript.JannovarReader;
import org.jax.isopret.transcript.Transcript;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.svart.GenomicAssemblies;
import org.monarchinitiative.svart.GenomicAssembly;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class IsopretCommand {
    /** isopret only supports hg38. */
    private final GenomicAssembly assembly = GenomicAssemblies.GRCh38p13();

    private Ontology geneOntology = null;

    private GoAssociationContainer associationContainer = null;

    private JannovarReader jannovarReader = null;

    private  Map<AccessionNumber, HgncItem> hgncMap = null;


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


}
