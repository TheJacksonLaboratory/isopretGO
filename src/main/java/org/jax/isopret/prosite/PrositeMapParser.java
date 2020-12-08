package org.jax.isopret.prosite;


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

    private Map<String,String> prositeNameMap;

    public PrositeMapParser(String prositeMapFile, String prositeDatFile) {
        prositeNameMap = getPrositeEntryNames(prositeDatFile);
    }


    private Map<String,String> getPrositeEntryNames(String prositeDatFile) {
        Map<String,String> mp = new HashMap<>(); // key, e.g., PS50141, value name of motif, e.g., A_DEAMIN_EDITASE
        try (BufferedReader br = new BufferedReader(new FileReader(prositeDatFile))) {
            String line;
            while ((line=br.readLine())!=null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mp;
    }


}
