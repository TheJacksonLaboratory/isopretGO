package org.jax.isopret.ensembl;

import org.jax.isopret.except.IsopretRuntimeException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * >ENST00000474740.1 cdna chromosome:GRCh38:18:9126627:9134295:1 gene:ENSG00000178127.13 gene_biotype:protein_coding transcript_biotype:processed_transcript gene_symbol:NDUFV2 description:NADH:ubiquinone oxidoreductase core subunit V2 [Source:HGNC Symbol;Acc:HGNC:7717]
 */
public class EnsemblCdnaParser {

    private final String ensemblCdnaPath;
    private final Map<String, EnsemblGene> i2dGeneMap;

    public EnsemblCdnaParser(String path) {
        this.ensemblCdnaPath = path;
        this.i2dGeneMap = new HashMap<>();
        try {
            InputStream fileStream = new FileInputStream(ensemblCdnaPath);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(decoder);
            String line;
            String currentHeader = null;
            StringBuilder currentSequenceBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                if (line.startsWith(">")) {
                    if (currentHeader != null && currentSequenceBuilder.length() > 0) {
                        EnsemblTranscript transcript = new EnsemblTranscript(currentHeader, currentSequenceBuilder.toString());
                        String genId = transcript.getGeneId();
                        this.i2dGeneMap.putIfAbsent(genId, new EnsemblGene(transcript));
                        this.i2dGeneMap.get(genId).addTranscript(transcript);
                        currentSequenceBuilder = new StringBuilder();
                    }
                    currentHeader = line.substring(1); // remove '>' symbol
                } else {
                    currentSequenceBuilder.append(line);
                }
                //System.out.println(line);
            }
        } catch (IOException e) {
            throw new IsopretRuntimeException(e.getLocalizedMessage());
        }
        System.out.printf("[INFO] Parsed a total of %d Ensembl genes\n", i2dGeneMap.size());
    }

    public Map<String, EnsemblGene> getI2dGeneMap() {
        return i2dGeneMap;
    }

    public Map<String, EnsemblGene> getSymbol2GeneMap() {
        Map<String, EnsemblGene> symbolmap = new HashMap<>();
        for (EnsemblGene gene : this.i2dGeneMap.values()) {
            symbolmap.putIfAbsent(gene.getGeneSymbol(), gene);
        }
        return symbolmap;
    }
}
