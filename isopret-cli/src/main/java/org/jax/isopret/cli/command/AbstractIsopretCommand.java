package org.jax.isopret.cli.command;

import org.jax.isopret.core.impl.rnaseqdata.HbaDealsParser;
import org.jax.isopret.core.impl.rnaseqdata.HbaDealsResult;
import org.jax.isopret.core.impl.rnaseqdata.HbaDealsThresholder;
import org.jax.isopret.model.GeneModel;
import org.jax.isopret.model.AccessionNumber;
import org.jax.isopret.model.GeneSymbolAccession;
import org.jax.isopret.model.Transcript;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class AbstractIsopretCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractIsopretCommand.class);
    /** isopret only supports hg38. */
    private final GenomicAssembly assembly = GenomicAssemblies.GRCh38p13();

    protected Map<GeneSymbolAccession, List<Transcript>>  geneSymbolAccessionListMap = null;

    @CommandLine.Option(names={"-d","--download"},
            scope = CommandLine.ScopeType.INHERIT,
            description = "directory to download HPO data")
    protected String downloadDirectory="data";



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
