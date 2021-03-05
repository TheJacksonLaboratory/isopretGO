package org.jax.isopret.interpro;

import org.jax.isopret.except.IsopretRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.jax.isopret.interpro.EnsemblStringToInt.geneStringToInt;
import static org.jax.isopret.interpro.EnsemblStringToInt.transcriptStringToInt;

public class InterproDomainParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproDomainParser.class);

    private final Map<Integer, InterproAnnotation> transcriptIdToInterproAnnotationMap;

    public InterproDomainParser(String path) {
        transcriptIdToInterproAnnotationMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            br.readLine(); // header
            int noInterproId = 0; // counts number of lines with biomart error
            while ((line=br.readLine()) != null) {
                //System.out.println(line);
                String [] fields = line.split("\t");
                try {
                    int enst = transcriptStringToInt(fields[0]);
                    int ensg = geneStringToInt(fields[1]);
                    String interproId = fields[2];
                    if (interproId == null || interproId.isEmpty() ) {
                        noInterproId++;
                        continue;
                    }
                    int interpro = InterproEntry.integerPart(interproId);
                    int start = Integer.parseInt(fields[3]);
                    int end = Integer.parseInt(fields[4]);
                    InterproAnnotation annotation = new InterproAnnotation(enst, ensg, interpro, start, end);
                    this.transcriptIdToInterproAnnotationMap.put(annotation.getEnst(), annotation);
                } catch (Exception e) {
                    // should never happen
                    throw new IsopretRuntimeException("Malformed interpro domain line: \"" + line + "\"");
                }

            }
            LOGGER.trace("Lines with missing interpro id (skipped): {}", noInterproId);
            LOGGER.trace("Number of interpro annotations identified: {}", this.transcriptIdToInterproAnnotationMap.size());
        } catch (IOException e) {
            throw new IsopretRuntimeException(e.getMessage());
        }
    }

    public Map<Integer, InterproAnnotation> getTranscriptIdToInterproAnnotationMap() {
        return transcriptIdToInterproAnnotationMap;
    }
}
