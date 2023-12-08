package org.jax.isopret.model;


import org.jax.isopret.data.AccessionNumber;
import org.jax.isopret.data.Transcript;

import java.util.List;

public record GeneModel(String geneSymbol,
                        String geneName,
                        String entrezId,
                        AccessionNumber ensemblGeneId,
                        String refseqAccecssion,
                        List<Transcript> transcriptList) {


}
