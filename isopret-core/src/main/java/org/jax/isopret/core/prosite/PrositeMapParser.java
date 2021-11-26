package org.jax.isopret.core.prosite;


import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.transcript.AccessionNumber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This class parses not the oppriginal prosite data but instead
 * maps from ENSEMBL transcript to prosite ids. We additionally ingest the prosite.dat file to
 * get the names of the prosite entries
 */
public class PrositeMapParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrositeMapParser.class);
    private final Map<String,String> prositeNameMap;

    private final  Map<AccessionNumber, PrositeMapping> prositeMappingMap;

    public PrositeMapParser(String prositeMapFile, String prositeDatFile) {
        prositeNameMap = getPrositeEntryNames(prositeDatFile);
        prositeMappingMap = getPrositeMappings(prositeMapFile);
    }

    private boolean isNumber(String field) {
        if (field.length() == 0) return false;
        for (int i=0; i<field.length(); i++) {
            if (! Character.isDigit(field.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param prositeMapFile Our mapping of prosite modules to transcript locations
     * @return map with key=gene ID, value {@link PrositeMapping} object with prosite hits for the transcripts of the gene
     */
    private Map<AccessionNumber, PrositeMapping> getPrositeMappings(String prositeMapFile) {
        Map<AccessionNumber, PrositeMapping> prositeMappingMap = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(prositeMapFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("ensembl_transcript_id")) {
                    continue;
                }
                String [] fields = line.split("\t");
                if (fields.length != 5) {
                    throw new IsopretRuntimeException("Malformed prosite map file line: " + line);
                }
                String transcriptID = fields[0];
                String geneID = fields[1];
                AccessionNumber geneAccession = AccessionNumber.ensemblGene(geneID);
                String prositeAc = fields[2];
               try {
                   int begin = Integer.parseInt(fields[3]);
                   int end = Integer.parseInt(fields[4]);
                   prositeMappingMap.putIfAbsent(geneAccession, new PrositeMapping(transcriptID, geneID));
                   prositeMappingMap.get(geneAccession).addPrositeHit(transcriptID, prositeAc, begin, end);
               } catch (NumberFormatException e) {
                   LOGGER.error("[ERROR] Could not parse line in prosite map ({}): {}.\n", line, e.getMessage());
               }
            }

        } catch (IOException e) {
            // not a recoverable error!
            throw new IsopretRuntimeException(e.getMessage());
        }
        return prositeMappingMap;
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
            LOGGER.error(e.getMessage());
        }
        return Map.copyOf(mp); // make immutable
    }

    public Map<String, String> getPrositeNameMap() {
        return prositeNameMap;
    }

    public Map<AccessionNumber, PrositeMapping> getPrositeMappingMap() {
        return prositeMappingMap;
    }
}
