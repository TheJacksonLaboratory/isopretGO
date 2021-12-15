package org.jax.isopret.core.transcript;

import com.google.common.collect.ImmutableMultimap;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.io.TranscriptFunctionFileParser;
import org.monarchinitiative.svart.GenomicAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.swing.tree.TreeNode;
import java.io.File;
import java.util.*;

/**
 * Ingest jannovar transcripts and transform them to variant-api-compliant objects.
 */
public class JannovarReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(JannovarReader.class);

    private final File jannovarSerFile;

    private final GenomicAssembly assembly;
    /** Key -- a gene symbol; value -- list of corresponding transcripts. */
    private final Map<String, List<Transcript>> symbolToTranscriptListMap;
    private final Map<AccessionNumber, List<Transcript>> geneIdToTranscriptMap;

    public JannovarReader(String path, GenomicAssembly assembly) {
        this(new File(path), assembly);
    }
    public JannovarReader(File file, GenomicAssembly assembly) {
        jannovarSerFile = file;
        this.assembly = assembly;
        JannovarTxMapper jmapper = new JannovarTxMapper(assembly);
        symbolToTranscriptListMap = new HashMap<>();
        geneIdToTranscriptMap = new HashMap<>();
        try {
            JannovarData jannovarData = new JannovarDataSerializer(jannovarSerFile.toString()).load();
            ImmutableMultimap<String, TranscriptModel> mp = jannovarData.getTmByGeneSymbol();
            for (String symbol : mp.keys()) {
                symbolToTranscriptListMap.put(symbol, new ArrayList<>());
                for (TranscriptModel tmod : mp.get(symbol)) {
                    Optional<Transcript> opt = jmapper.remap(tmod);
                    if (opt.isPresent()) {
                        Transcript transcript = opt.get();
                        symbolToTranscriptListMap.get(symbol).add(transcript);
                        String geneAccessionString = tmod.getGeneID();
                        AccessionNumber geneId = AccessionNumber.ensemblGene(geneAccessionString);
                        geneIdToTranscriptMap.putIfAbsent(geneId, new ArrayList<>());
                        geneIdToTranscriptMap.get(geneId).add(transcript);
                    } else {
                        LOGGER.warn("Could not find Jannovar transcript model for {}.", symbol);
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

    public Map<AccessionNumber, List<Transcript>> getGeneIdToTranscriptMap() {
        return geneIdToTranscriptMap;
    }
}