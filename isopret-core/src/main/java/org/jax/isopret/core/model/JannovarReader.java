package org.jax.isopret.core.model;

import com.google.common.collect.ImmutableMultimap;
import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.jax.isopret.core.except.IsopretRuntimeException;
import org.monarchinitiative.svart.assembly.GenomicAssembly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.*;

/**
 * Ingest jannovar transcripts and transform them to variant-api-compliant objects. We intend to
 * use the resultant {@link #geneToTranscriptListMap} to transmit the information to the HGNC
 * parsed gene information object that is parsed by {@link org.jax.isopret.core.hgnc.HgncParser}.
 * @author Peter Robinson
 */
public class JannovarReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(JannovarReader.class);

    private final File jannovarSerFile;

    private final GenomicAssembly assembly;
    /** Key -- An {@link GeneSymbolAccession} object for a gene; value -- list of corresponding transcripts. */
    private final Map<GeneSymbolAccession, List<Transcript>> geneToTranscriptListMap;

    public JannovarReader(String path, GenomicAssembly assembly) {
        this(new File(path), assembly);
    }
    public JannovarReader(File file, GenomicAssembly assembly) {
        jannovarSerFile = file;
        this.assembly = assembly;
        JannovarTxMapper jmapper = new JannovarTxMapper(assembly);
        geneToTranscriptListMap = new HashMap<>();
        try {
            JannovarData jannovarData = new JannovarDataSerializer(jannovarSerFile.toString()).load();
            ImmutableMultimap<String, TranscriptModel> transcriptByGeneSymbolMap = jannovarData.getTmByGeneSymbol();
            transcriptByGeneSymbolMap.asMap().forEach((symbol, transcriptModelList) -> {
                for (TranscriptModel tmod : transcriptModelList) {
                    Optional<Transcript> opt = jmapper.remap(tmod);
                    if (opt.isPresent()) {
                        Transcript transcript = opt.get();
                        AccessionNumber geneId = AccessionNumber.ensemblGene(tmod.getGeneID());
                        GeneSymbolAccession gsa = new GeneSymbolAccession(symbol, geneId);
                        geneToTranscriptListMap.putIfAbsent(gsa, new ArrayList<>());
                        geneToTranscriptListMap.get(gsa).add(transcript);
                    } else {
                        LOGGER.warn("Could not find Jannovar transcript model for {}.", symbol);
                    }
                }
            });
            LOGGER.info("Parsed geneToTranscriptListMap with {} entries", geneToTranscriptListMap.size());
        } catch (SerializationException e) {
            throw new IsopretRuntimeException(e.getMessage());
        }
    }

    public Map<GeneSymbolAccession, List<Transcript>> getGeneToTranscriptListMap() {
        return geneToTranscriptListMap;
    }

}
