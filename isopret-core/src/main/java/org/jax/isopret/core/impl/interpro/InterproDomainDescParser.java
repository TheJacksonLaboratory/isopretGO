package org.jax.isopret.core.impl.interpro;


import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.model.InterproEntry;
import org.jax.isopret.model.InterproEntryType;
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
        String line = null;
        int unrecognized = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            br.readLine(); // header
            while ((line=br.readLine()) != null) {
                String [] fields = line.split("\t");
                if (fields.length != 3) {
                    throw new IsopretRuntimeException("Malformed isopret description line: " + line);
                }
                String id = fields[0];
                InterproEntryType entryType = InterproEntryType.fromString(fields[1]);
                if (entryType.equals(InterproEntryType.UNKNOWN)) {
                    LOGGER.warn("Did not recognize entry type ({}) from {}", entryType, line);
                    continue;
                }
                String description = fields[2];
                InterproEntry entry = new InterproEntry(id, entryType, description);
                interpromap.put(entry.getId(),entry);
            }
        } catch (IOException e) {
            String err = String.format("%s: %s", e.getMessage(), line);
            throw new IsopretRuntimeException(err);
        }
        return interpromap;
    }

    public static Map<Integer, InterproEntry> getInterproDescriptionMap(File file) {
        InterproDomainDescParser parser = new InterproDomainDescParser(file);
        return parser.interproDescriptionMap;
    }
}
