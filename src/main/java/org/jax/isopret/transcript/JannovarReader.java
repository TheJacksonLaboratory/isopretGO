package org.jax.isopret.transcript;

import com.google.common.collect.ImmutableMultimap;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.jax.isopret.except.IsopretRuntimeException;
import org.monarchinitiative.variant.api.GenomicAssembly;

import java.io.File;
import java.util.*;

/**
 * Ingest jannovar transcripts and transform them to variant-api-compliant objects.
 */
public class JannovarReader {

    private final File jannovarSerFile;

    GenomicAssembly assembly;

    Map<String, List<Transcript>> symbolToTranscriptListMap;

    public JannovarReader(String path, GenomicAssembly assembly) {
        this(new File(path), assembly);
    }
    public JannovarReader(File file, GenomicAssembly assembly) {
        jannovarSerFile = file;
        this.assembly = assembly;
        JannovarTxMapper jmapper = new JannovarTxMapper(assembly);
        symbolToTranscriptListMap = new HashMap<>();
        try {
            JannovarData jannovarData = new JannovarDataSerializer(jannovarSerFile.toString()).load();
            ImmutableMultimap<String, TranscriptModel> mp = jannovarData.getTmByGeneSymbol();
            for (String symbol : mp.keys()) {
                symbolToTranscriptListMap.put(symbol, new ArrayList<>());
                for (TranscriptModel tmod : mp.get(symbol)) {
                    Optional<Transcript> opt = jmapper.remap(tmod);
                    if (opt.isPresent()) {
                        symbolToTranscriptListMap.get(symbol).add(opt.get());
                    }
                }
            }
        } catch (SerializationException e) {
            throw new IsopretRuntimeException(e.getMessage());
        }
    }

    public Map<String, List<Transcript>> getSymbolToTranscriptListMap() {
        return symbolToTranscriptListMap;
    }
}
