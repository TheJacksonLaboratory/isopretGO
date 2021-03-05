package org.jax.isopret.interpro;

import org.jax.isopret.except.IsopretRuntimeException;

public class EnsemblStringToInt {



    public static int transcriptStringToInt(String ensg) {
        if (! ensg.startsWith("ENST")) {
            throw new IsopretRuntimeException("Malformed ENST string: \"" + ensg + "\"");
        }
        return Integer.parseInt(ensg.substring(4));
    }

    public static String transcriptIntToString(int ensg) {
        return String.format("ENST%011d", ensg);
    }

    public static int geneStringToInt(String ensg) {
        if (! ensg.startsWith("ENSG")) {
            throw new IsopretRuntimeException("Malformed ENSG string: \"" + ensg + "\"");
        }
        return Integer.parseInt(ensg.substring(4));
    }

    public static String geneIntToString(int ensg) {
        return String.format("ENSG%011d", ensg);
    }

}
