package org.jax.isopret.interpro;

import org.jax.isopret.except.IsopretRuntimeException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import static org.jax.isopret.interpro.EnsemblStringToInt.geneStringToInt;
import static org.jax.isopret.interpro.EnsemblStringToInt.transcriptStringToInt;

public class InterproDomainParser {

    public InterproDomainParser(String path, Map<Integer, InterproEntry> interproDescriptionMap) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line=br.readLine()) != null) {
                System.out.println(line);
                String [] fields = line.split("\t");
                int enst = transcriptStringToInt(fields[0]);
                int ensg = geneStringToInt(fields[1]);
                int interpro = InterproEntry.integerPart(fields[2]);
                int start = Integer.parseInt(fields[3]);
                int end = Integer.parseInt(fields[4]);

            }
        } catch (IOException e) {
            throw new IsopretRuntimeException(e.getMessage());
        }
    }
}
