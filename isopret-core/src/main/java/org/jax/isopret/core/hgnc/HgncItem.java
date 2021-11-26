package org.jax.isopret.core.hgnc;

public class HgncItem {
    private final String geneSymbol;
    private final String geneName;
    private final String entrezId;
    private final String ensemblGeneId;
    private final String ucscId;
    private final String refseqAccecssion;

    public HgncItem(String symbol, String name, String entrez, String ensembl, String ucsc, String refseq) {
        this.geneSymbol = symbol;
        this.geneName = name;
        this.entrezId = entrez;
        this.ensemblGeneId = ensembl;
        this.ucscId = ucsc;
        this.refseqAccecssion = refseq;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public String getGeneName() {
        return geneName;
    }

    public String getEntrezId() {
        return entrezId;
    }

    public String getEnsemblGeneId() {
        return ensemblGeneId;
    }

    public String getUcscId() {
        return ucscId;
    }

    public String getRefseqAccecssion() {
        return refseqAccecssion;
    }
}
