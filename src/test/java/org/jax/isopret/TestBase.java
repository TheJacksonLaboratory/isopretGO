package org.jax.isopret;

import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.hbadeals.HbaDealsParser;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.prosite.PrositeMapParser;
import org.jax.isopret.prosite.PrositeMapping;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.GenomicAssemblyProvider;
import org.jax.isopret.transcript.JannovarReader;
import org.jax.isopret.transcript.Transcript;
import org.monarchinitiative.variant.api.GenomicAssembly;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class TestBase {
    protected static final Path PROSITE_MAP_PATH = Paths.get("src/test/resources/prosite/ADAR_prosite_profiles.txt");
    protected static final Path PROSITE_DAT_PATH = Paths.get("src/test/resources/prosite/prosite-excerpt.dat");
    private static PrositeMapParser pmparser = null;

    private static final Path JANNOVAR_ADAR_PATH = Paths.get("src/test/resources/jannovar/hg38_ensembl_ADAR.ser");
    private static final Path ASSEMBLY_REPORT_PATH = Paths.get("src/test/resources/GCA_000001405.28_GRCh38.p13_assembly_report.txt");
    private static GenomicAssembly assembly = null;
    private static Map<String, List<Transcript>> symbolToTranscriptMap = null;

    private static final Path HBADEALS_ADAR_PATH = Paths.get("src/test/resources/hbadeals/ADAR_HBADEALS.tsv");
    private static Map<String, HbaDealsResult> hbaDealsResultMap = null;

    public static PrositeMapParser getPrositeMapParser() {
        if (pmparser == null) {
            pmparser = new PrositeMapParser(PROSITE_MAP_PATH.toString(), PROSITE_DAT_PATH.toString());
        }
        return pmparser;
    }

    public static Map<String, HbaDealsResult> getADARHbaDealsResultMap () {
        if (hbaDealsResultMap == null) {
            final HbaDealsParser parser = new HbaDealsParser(HBADEALS_ADAR_PATH.toString());
            hbaDealsResultMap = parser.getHbaDealsResultMap();
        }
        return hbaDealsResultMap;
    }


    public static GenomicAssembly getHg38() {
        try {
            if (assembly == null) {
                assembly = GenomicAssemblyProvider.fromAssemblyReport(ASSEMBLY_REPORT_PATH);
            }
            return assembly;
        } catch (IOException e) {
            throw new IsopretRuntimeException("Could not ingest assembly");
        }
    }

    public static Map<String, List<Transcript>> getADARToTranscriptMap() {
        if (assembly == null) {
            assembly = getHg38();
        }
        if (symbolToTranscriptMap == null) {
            JannovarReader reader = new JannovarReader(JANNOVAR_ADAR_PATH.toAbsolutePath().toString(), assembly);
            symbolToTranscriptMap = reader.getSymbolToTranscriptListMap();
        }
        return symbolToTranscriptMap;
    }

    public static AnnotatedGene getAdarAnnotatedTranscript() {
        String transcriptIDWithVersion = "ENST00000368474.8";
        String transcriptIDWithoutVersion = "ENST00000368474";
        String adarGeneId = "ENSG00000160710";
        Map<String, List<Transcript>> adarSymbolToTranscriptMap = getADARToTranscriptMap();
        List<Transcript> adarTranscripts = adarSymbolToTranscriptMap.get("ADAR");
        PrositeMapParser prositeMapParser = getPrositeMapParser();
        Map<String, PrositeMapping> prositeMappingMap = prositeMapParser.getPrositeMappingMap();
        Map<String,String> prositeIdMap = prositeMapParser.getPrositeNameMap();
        Map<String, HbaDealsResult> hbadealmaps = getADARHbaDealsResultMap();
        HbaDealsResult adarResult = hbadealmaps.get("ADAR"); // expressed genes
        PrositeMapping adarPrositeMapping = prositeMappingMap.get(adarGeneId);
        return new AnnotatedGene(adarTranscripts,  adarPrositeMapping.getTranscriptToPrositeListMap(), adarResult);
    }

}
