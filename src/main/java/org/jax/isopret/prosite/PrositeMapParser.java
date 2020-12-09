package org.jax.isopret.prosite;


import org.jax.isopret.except.IsopretRuntimeException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * This class parses not the oppriginal prosite data but instead
 * maps from ENSEMBL transcript to prosite ids. We additionally ingest the prosite.dat file to
 * get the names of the prosite entries
 */
public class PrositeMapParser {

    private final Map<String,String> prositeNameMap;

    public PrositeMapParser(String prositeMapFile, String prositeDatFile) {
        prositeNameMap = getPrositeEntryNames(prositeDatFile);
        getPrositeMappings(prositeMapFile);
    }

    private void getPrositeMappings(String prositeMapFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(prositeMapFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
            }

        } catch (IOException e) {
            // not a recoverable error!
            throw new IsopretRuntimeException(e.getMessage());
        }
    }


    private String getID(String line) {
        String [] fields = line.split(";");
        return fields[0].trim();
    }

    private String getAC(String line) {
        String [] fields = line.split(";");
        return fields[0].trim();
    }

    private Map<String,String> getPrositeEntryNames(String prositeDatFile) {
        Map<String,String> mp = new HashMap<>(); // key, e.g., PS50141, value name of motif, e.g., A_DEAMIN_EDITASE
        try (BufferedReader br = new BufferedReader(new FileReader(prositeDatFile))) {
            String line;
            while ((line=br.readLine())!=null) {
                if (line.startsWith("ID")) {
                    String currentId = getID(line.substring(5));
                    line = br.readLine();
                    if (! line.startsWith("AC")) {
                        // should never happen
                        throw new IsopretRuntimeException("Malformed AC line in prosite.dat file");
                    }
                    String currentAc = getAC(line.substring(5));
                    mp.put(currentAc, currentId);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Map.copyOf(mp); // make immutable
    }

    public Map<String, String> getPrositeNameMap() {
        return prositeNameMap;
    }
}
