package org.jax.isopret;

import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.hbadeals.HbaDealsResult;
import org.jax.isopret.core.hgnc.HgncParser;
import org.jax.isopret.core.interpro.*;
import org.jax.isopret.core.model.*;
import org.monarchinitiative.svart.assembly.GenomicAssemblies;
import org.monarchinitiative.svart.assembly.GenomicAssembly;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class TestBase {

    protected static final String hgncPath;
    private static final String INTERPRO_ADAR_DOMAIN_DESC;
    protected static final AccessionNumber adarAccession = AccessionNumber.ensemblGene("ENSG00000160710");
    protected static final Path JANNOVAR_ADAR_PATH = Paths.get("src/test/resources/jannovar/hg38_ensembl_ADAR.ser");
    protected static final GenomicAssembly assembly = GenomicAssemblies.GRCh38p13();
    protected static final Map<AccessionNumber, GeneModel> ensemblMap;
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
        INTERPRO_ADAR_DOMAIN_DESC = Objects.requireNonNull(url2).getPath();
        File f = new File(INTERPRO_ADAR_DOMAIN_DESC);
        System.out.println(INTERPRO_ADAR_DOMAIN_DESC + " :" + f.isFile());
        JannovarReader reader = new JannovarReader(JANNOVAR_ADAR_PATH.toAbsolutePath().toString(), assembly);
        Map<GeneSymbolAccession, List<Transcript>> transcriptListMap = reader.getGeneToTranscriptListMap();
        HgncParser hgncParser = new HgncParser(new File(hgncPath), transcriptListMap);
        ensemblMap = hgncParser.ensemblMap();
    }


    protected static final Path INTERPRO_ADAR_PATH = Paths.get("src/test/resources/interpro/ADAR_interpro.txt");
    private static final Map<Integer, InterproEntry> interproDomainMap = InterproDomainDescParser.getInterproDescriptionMap(new File(INTERPRO_ADAR_DOMAIN_DESC));
    private static final Map<AccessionNumber, List<InterproAnnotation>> annotationMap = InterproDomainParser.getInterproAnnotationMap(INTERPRO_ADAR_PATH.toFile());
    private static final Path HBADEALS_ADAR_PATH = Paths.get("src/test/resources/hbadeals/ADAR_HBADEALS.tsv");
    private static Map<String, HbaDealsResult> hbaDealsResultMap = null;



    public static GenomicAssembly getHg38() {
        return assembly;
    }

    public static Map<String, List<Transcript>> getADARToTranscriptMap() {

        Map<String, List<Transcript>> wrapper = new HashMap<>();
        if (! ensemblMap.containsKey(adarAccession)) {
            // should really never happen!
            throw new IsopretRuntimeException("Could not find gene model for ADAR in test");
        }
        GeneModel model = ensemblMap.get(adarAccession);
        wrapper.put(model.geneSymbol(), model.transcriptList());
        return wrapper;
    }



}
