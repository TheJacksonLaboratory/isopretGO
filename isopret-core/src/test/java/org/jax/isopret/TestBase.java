package org.jax.isopret;

import org.jax.isopret.core.hbadeals.HbaDealsParser;
import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.hgnc.HgncParser;
import org.jax.isopret.core.interpro.*;
import org.jax.isopret.core.transcript.AccessionNumber;
import org.jax.isopret.core.transcript.JannovarReader;
import org.jax.isopret.core.transcript.Transcript;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class TestBase {

    private static final String hgncPath;
    protected static final String INTERPRO_ADAR_DOMAIN_DESC;


    static {
        String cp = System.getProperty("java.class.path");
        String [] resources = cp.split(":");
        for (String r : resources) {
            if (r.contains("isopret"))
                System.out.println("classpath is: " + r);
        }
        final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        final URL url = classloader.getResource("hgnc/hgnc_complete_set_excerpt.txt");
        assert url != null;
        hgncPath = url.getPath();
        final URL url2 = classloader.getResource("interpro/ADAR_interpro_domain_desc.txt");
        INTERPRO_ADAR_DOMAIN_DESC = url2.getPath();
        File f = new File(INTERPRO_ADAR_DOMAIN_DESC);
        System.out.println(INTERPRO_ADAR_DOMAIN_DESC + " :" + f.isFile());
    }


    protected static final Path INTERPRO_ADAR_PATH = Paths.get("src/test/resources/interpro/ADAR_interpro.txt");
    private static final Map<Integer, InterproEntry> interproDomainMap = InterproDomainDescParser.getInterproDescriptionMap(new File(INTERPRO_ADAR_DOMAIN_DESC));
    private static final Map<AccessionNumber, List<InterproAnnotation>> annotationMap = InterproDomainParser.getInterproAnnotationMap(INTERPRO_ADAR_PATH.toFile());
    private static final Path JANNOVAR_ADAR_PATH = Paths.get("src/test/resources/jannovar/hg38_ensembl_ADAR.ser");
    private static final GenomicAssembly assembly = GenomicAssemblies.GRCh38p13();
    private static Map<String, List<Transcript>> symbolToTranscriptMap = null;


    protected static final HgncParser hgncParser = new HgncParser(hgncPath);

    private static final Path HBADEALS_ADAR_PATH = Paths.get("src/test/resources/hbadeals/ADAR_HBADEALS.tsv");
    private static Map<String, HbaDealsResult> hbaDealsResultMap = null;



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



}
