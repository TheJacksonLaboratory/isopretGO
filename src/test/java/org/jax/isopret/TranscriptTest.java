package org.jax.isopret;

import org.jax.isopret.transcripts.GenomicAssemblyProvider;
import org.jax.isopret.transcripts.JannovarReader;
import org.jax.isopret.transcripts.Transcript;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.variant.api.GenomicAssembly;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TranscriptTest {
    private static final Path JANNOVAR_ADAR_PATH = Paths.get("src/test/resources/jannovar/hg38_ensembl_ADAR.ser");
    private static final Path ASSEMBLY_REPORT_PATH = Paths.get("src/test/resources/GCA_000001405.28_GRCh38.p13_assembly_report.txt");
    private static GenomicAssembly assembly;
    private static Map<String, List<Transcript>> symbolToTranscriptMap;

    @BeforeAll
    public static void init()  throws IOException {
        assembly = GenomicAssemblyProvider.fromAssemblyReport(ASSEMBLY_REPORT_PATH);
        JannovarReader reader = new JannovarReader(JANNOVAR_ADAR_PATH.toAbsolutePath().toString(), assembly);
        symbolToTranscriptMap = reader.getSymbolToTranscriptListMap();
    }

    @Test
    public void if_ADAR_transcripts_found_then_ok() {
        assertTrue(symbolToTranscriptMap.containsKey("ADAR"));
    }

    @Test
    public void if_10_transcripts_retrieved_then_ok() {
        List<Transcript> adarTranscripts = symbolToTranscriptMap.get("ADAR");
        assertEquals(10, adarTranscripts.size());
        for (var t : adarTranscripts) {
            System.out.println(t);
        }
    }
}
