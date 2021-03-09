package org.jax.isopret.interpro;


import org.jax.isopret.except.IsopretRuntimeException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parse the Interpro domain description file
 */
public class InterproDomainDescParser {

    final Map<Integer, InterproEntry> interproDescriptionMap;

    private InterproDomainDescParser(String path) {
        interproDescriptionMap = getDescriptions(path);
        System.out.println("Got " + interproDescriptionMap.size());
    }

    private  Map<Integer, InterproEntry> getDescriptions(String path) {
        Map<Integer, InterproEntry> interpromap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            br.readLine(); // header
            while ((line=br.readLine()) != null) {
                System.out.println(line);
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

    public static Map<Integer, InterproEntry> getInterproDescriptionMap(String path) {
        InterproDomainDescParser parser = new InterproDomainDescParser(path);
        return parser.interproDescriptionMap;
    }
}
