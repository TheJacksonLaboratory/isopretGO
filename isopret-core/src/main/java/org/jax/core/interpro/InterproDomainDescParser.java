package org.jax.core.interpro;


import org.jax.core.except.IsopretRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parse the Interpro domain description file
 */
public class InterproDomainDescParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproDomainDescParser.class);
    final Map<Integer, InterproEntry> interproDescriptionMap;

    private InterproDomainDescParser(File file) {
        interproDescriptionMap = getDescriptions(file);
        LOGGER.trace("Got {} interpro descriptions (interproDescriptionMap)", interproDescriptionMap.size());
    }

    private  Map<Integer, InterproEntry> getDescriptions(File file) {
        Map<Integer, InterproEntry> interpromap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine(); // header
            while ((line=br.readLine()) != null) {
                String [] fields = line.split("\t");
                if (fields.length != 3) {
                    throw new IsopretRuntimeException("Malformed isopret description line: " + line);
                }
                String id = fields[0];
                InterproEntryType entryType = InterproEntryType.fromString(fields[1]);
                String description = fields[2];
                InterproEntry entry = new InterproEntry(id, entryType, description);
                interpromap.put(entry.getId(),entry);
            }
        } catch (IOException e) {
            throw new IsopretRuntimeException(e.getMessage());
        }
        return interpromap;
    }

    public static Map<Integer, InterproEntry> getInterproDescriptionMap(File file) {
        InterproDomainDescParser parser = new InterproDomainDescParser(file);
        return parser.interproDescriptionMap;
    }
}
