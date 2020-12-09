package org.jax.isopret;

import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.prosite.PrositeMapParser;
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

    public static PrositeMapParser getPrositeMapParser() {
        if (pmparser == null) {
            pmparser = new PrositeMapParser(PROSITE_MAP_PATH.toString(), PROSITE_DAT_PATH.toString());
        }
        return pmparser;
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

}
