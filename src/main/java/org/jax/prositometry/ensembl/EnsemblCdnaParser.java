package org.jax.prositometry.ensembl;

import java.io.*;;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * >ENST00000474740.1 cdna chromosome:GRCh38:18:9126627:9134295:1 gene:ENSG00000178127.13 gene_biotype:protein_coding transcript_biotype:processed_transcript gene_symbol:NDUFV2 description:NADH:ubiquinone oxidoreductase core subunit V2 [Source:HGNC Symbol;Acc:HGNC:7717]
 */
public class EnsemblCdnaParser {

    private final String ensemblCdnaPath;
    private Map<String, EnsemblGene> i2dGeneMap;

    public EnsemblCdnaParser(String path) {
        this.ensemblCdnaPath = path;
        this.i2dGeneMap = new HashMap<>();
        try {
            InputStream fileStream = new FileInputStream(ensemblCdnaPath);
            InputStream gzipStream = new GZIPInputStream(fileStream);
            Reader decoder = new InputStreamReader(gzipStream, "UTF8");
            BufferedReader br = new BufferedReader(decoder);
            String line;
            String currentHeader = null;
            StringBuilder currentSequenceBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                if (line.startsWith(">")) {
                    if (currentHeader != null && currentSequenceBuilder.length() > 0) {
                        EnsemblTranscript transcript = new EnsemblTranscript(currentHeader, currentSequenceBuilder.toString());
                        currentHeader = null;
                        currentSequenceBuilder = new StringBuilder();
                    }
                }
                System.out.println(line);
            }
        } catch (IOException e) {

        }
    }



}
