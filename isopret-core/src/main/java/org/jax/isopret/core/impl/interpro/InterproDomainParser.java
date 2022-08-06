package org.jax.isopret.core.impl.interpro;

import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.model.AccessionNumber;
import org.jax.isopret.model.InterproAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class InterproDomainParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproDomainParser.class);

    private final Map<AccessionNumber, List<InterproAnnotation>> geneIdToAnnotationMap;

    private InterproDomainParser(File file) {
        geneIdToAnnotationMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // header
            int noInterproId = 0; // counts number of lines with biomart error
            while ((line=br.readLine()) != null) {
                //System.out.println(line);
                String [] fields = line.split("\t");
                try {
                    Optional<InterproAnnotation> opt = InterproAnnotation.fromLine(line);
                    if (opt.isEmpty()) {
                        noInterproId++;
                        continue;
                    }
                    InterproAnnotation annotation = opt.get();
                    this.geneIdToAnnotationMap.putIfAbsent(annotation.getEnsg(), new ArrayList<>());
                    this.geneIdToAnnotationMap.get(annotation.getEnsg()).add(annotation);
                } catch (Exception e) {
                    // should never happen
                    throw new IsopretRuntimeException("Malformed interpro domain line: \"" + line + "\": " + e.getMessage());
                }

            }
            LOGGER.trace("Lines with missing interpro id (skipped): {}", noInterproId);
            LOGGER.trace("Number of interpro annotations identified: {}", this.geneIdToAnnotationMap.size());
        } catch (IOException e) {
            throw new IsopretRuntimeException(e.getMessage());
        }
    }




    public static Map<AccessionNumber, List<InterproAnnotation>> getInterproAnnotationMap(File file) {
        InterproDomainParser parser = new InterproDomainParser(file);
        return parser.geneIdToAnnotationMap;
    }
}
