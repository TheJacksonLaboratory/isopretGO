package org.jax.isopret.core.model;


import java.util.List;

public record GeneModel(String geneSymbol,
                        String geneName,
                        String entrezId,
                        AccessionNumber ensemblGeneId,
                        String refseqAccecssion,
                        List<Transcript> transcriptList) {


}
