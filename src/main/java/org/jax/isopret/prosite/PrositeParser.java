package org.jax.isopret.prosite;

import org.jax.isopret.except.PrositometryRuntimeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PrositeParser {

    private final static String UNINITIALZED = "";
    private List<PrositePattern> patternList;

    public PrositeParser(String prositePath) {
        patternList = new ArrayList<>();
        parse(prositePath);
    }

    private void parse(String prositePath) {
        Path path = Paths.get(prositePath);
        try {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            System.out.printf("[INFO] Read %d lines from %s.\n", lines.size(), prositePath);
            // Note that each entry starts with "//"
            int N = lines.size();
            // find the first new entry
            int i;
            for (i=0;i<N;i++) {
                if (lines.get(i).startsWith("//")) {
                    break;
                }
            }
            // i is now the index of the first line of the first entry
            while (i < N) {
                int j = i+1;
                for (j=i+1;j<N;j++) {
                    if (lines.get(j).startsWith("//")) {
                        break;
                    }
                }
                // j is the first line of the next entry or it is equal to N and were are done
                parseEntry(lines, i, j);
                i = j;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.printf("[INFO] Extracted %d Prosite motifs\n", patternList.size());
    }


    private void parseEntry(List<String> lines, int startIndex, int endIndex) {
        String ID = UNINITIALZED; // e.g., ID   ASN_GLYCOSYLATION; PATTERN.
        String category = UNINITIALZED; // PATTERN or MATRIX
        String AC = UNINITIALZED; // e.g., AC   PS00001;, the PROSITE PATTERN CODE
        String DE = UNINITIALZED; // e.g., DE   N-glycosylation site.
        StringBuffer PA = new StringBuffer(); // e.g., PA   N-{P}-[ST]-{P}.
        // we skip some other elements for now that are not needed for our purposes.
        // Note that the "payload" of each line starts at the sixth character
        // PA can be multi line
        for (int i = startIndex; i < endIndex; i++) {
            String line = lines.get(i);
            if (line.startsWith("ID")) {
                ID = line.substring(5);
                String[] fields = ID.split(";");
                if (fields.length == 2) {
                    ID = fields[0];
                    if (fields[1].contains("PATTERN")) {
                        category = "PATTERN";
                    } else if (fields[1].contains("MATRIX")) {
                        category = "MATRIX";
                    } else {
                        throw new PrositometryRuntimeException("Did not recogize entry category: " + line);
                    }
                }
            } else if (line.startsWith("AC")) {
                AC = line.substring(5);
            } else if (line.startsWith("DE")) {
                DE = line.substring(5);
            } else if (line.startsWith("PA")) {
                PA.append(line.substring(5));
            }
        }
        if (category.equals("PATTERN")) {
            try {
                PrositePattern pat = new PrositePattern(ID, AC, DE, PA.toString());
                patternList.add(pat);
            } catch (Exception e) {
                System.out.println(ID + "; " + AC + "; " + PA);
                System.out.println("Caught exception " + e.getMessage());
                System.exit(1);
            }
        }
    }

    public List<PrositePattern> getPatternList() {
        return patternList;
    }
}
