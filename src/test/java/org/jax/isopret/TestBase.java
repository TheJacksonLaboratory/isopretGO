package org.jax.isopret;

import org.jax.isopret.hbadeals.HbaDealsParser;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hgnc.HgncParser;
import org.jax.isopret.interpro.*;
import org.jax.isopret.prosite.PrositeMapParser;
import org.jax.isopret.prosite.PrositeMapping;
import org.jax.isopret.transcript.AccessionNumber;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.JannovarReader;
import org.jax.isopret.transcript.Transcript;
import org.monarchinitiative.svart.GenomicAssemblies;
import org.monarchinitiative.svart.GenomicAssembly;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class TestBase {
    private static final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    private static final URL url = classloader.getResource("hgnc/hgnc_complete_set_excerpt.txt");
    private static final String hgncPath;

    static {
        assert url != null;
        hgncPath = url.getPath();
    }

    protected static final Path PROSITE_MAP_PATH = Paths.get("src/test/resources/prosite/ADAR_prosite_profiles.txt");
    protected static final Path PROSITE_DAT_PATH = Paths.get("src/test/resources/prosite/prosite-excerpt.dat");
    private static PrositeMapParser pmparser = null;

    protected static final Path INTERPRO_ADAR_PATH = Paths.get("src/test/resources/interpro/ADAR_interpro.txt");
    protected static final Path INTERPRO_ADAR_DOMAIN_DESC = Paths.get("src/test/resources/interpro/ADAR_interpro_domain_desc.txt");
    private static final Map<Integer, InterproEntry> interproDomainMap = InterproDomainDescParser.getInterproDescriptionMap(INTERPRO_ADAR_DOMAIN_DESC.toAbsolutePath().toString());
    private static final Map<AccessionNumber, List<InterproAnnotation>> annotationMap = InterproDomainParser.getInterproAnnotationMap(INTERPRO_ADAR_PATH.toString());
    private static final Path JANNOVAR_ADAR_PATH = Paths.get("src/test/resources/jannovar/hg38_ensembl_ADAR.ser");
    private static final GenomicAssembly assembly = GenomicAssemblies.GRCh38p13();
    private static Map<String, List<Transcript>> symbolToTranscriptMap = null;


    protected static final HgncParser hgncParser = new HgncParser(hgncPath);

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
            final HbaDealsParser parser = new HbaDealsParser(HBADEALS_ADAR_PATH.toString(), hgncParser.ensemblMap());
            hbaDealsResultMap = parser.getHbaDealsResultMap();
        }
        return hbaDealsResultMap;
    }


    public static GenomicAssembly getHg38() {
        return assembly;
    }

    public static Map<String, List<Transcript>> getADARToTranscriptMap() {
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
        Map<AccessionNumber, PrositeMapping> prositeMappingMap = prositeMapParser.getPrositeMappingMap();
        Map<String,String> prositeIdMap = prositeMapParser.getPrositeNameMap();
        Map<String, HbaDealsResult> hbadealmaps = getADARHbaDealsResultMap();
        HbaDealsResult adarResult = hbadealmaps.get("ADAR"); // expressed genes
        PrositeMapping adarPrositeMapping = prositeMappingMap.get(adarGeneId);
        Map<AccessionNumber, List<DisplayInterproAnnotation>> annotList = Map.of(); // TODO
        return new AnnotatedGene(adarTranscripts, annotList,adarResult);
    }

}
