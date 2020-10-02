package org.jax.prositometry.html;

import org.jax.prositometry.ensembl.EnsemblGene;
import org.jax.prositometry.ensembl.EnsemblTranscript;
import org.jax.prositometry.hbadeals.HbaDealsResult;
import org.jax.prositometry.hbadeals.HbaDealsTranscriptResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class is intended to be a final common pathway to display results about a gene and the isoforms it
 * contains in the HTML output.
 */
public class HtmlGene {

    public final String symbol;
    public final double FC;
    public final double pval;
    public final double pvalCorr;
    public final int n_transcripts;
    private final List<HtmlTranscript> transcriptList;
    private final List<String> goAnnotations;

    public HtmlGene(HbaDealsResult result, EnsemblGene egene, List<String> goAnnots) {
        symbol = result.getSymbol();
        FC = result.getExpressionFoldChange();
        pval = result.getExpressionP();
        pvalCorr = result.getCorrectedPval();
        this.n_transcripts = egene.getTranscriptMap().size();
        this.transcriptList = new ArrayList<>();
        this.goAnnotations = goAnnots;


        for (EnsemblTranscript et : egene.getTranscriptMap().values()) {
            Set<String> m = egene.getDifference(et.getTranscriptId());
            String differenceString = "none";
            if (! m.isEmpty()) {
                differenceString = String.join(";", m);
            }
            String motifString = et.getHtmlMotifString();
            // result.getTranscriptMap()
            double pval = -1.0;
            double correctedpval = -1.0;
            String transcriptId = et.getTranscriptId();
            if (result.getTranscriptMap().containsKey(transcriptId)) {
                HbaDealsTranscriptResult tresult = result.getTranscriptMap().get(transcriptId);
                pval = tresult.getP();
                correctedpval = tresult.getCorrectedP();
//            } else {
//                System.out.println("transcriptID " + transcriptId);
//                for (String id : result.getTranscriptMap().keySet()) {
//                    System.out.println("[\t" + id);
//                }
            }
            HtmlTranscript htranscript = new HtmlTranscript(et.getTranscriptId(),
                    motifString,
                    differenceString,
                    !(differenceString.equals("none")),
                    et.cDNAlen(),
                    et.aaLen(),
                    pval,
                    correctedpval
            );
            transcriptList.add(htranscript);
        }
    }

    public String getSymbol() {
        return symbol;
    }

    public double getFoldchange() {
        return FC;
    }

    public double getPval() {
        return pval;
    }

    public double getPvalcorr() {
        return pvalCorr;
    }

    public int getNtranscripts() {
        return n_transcripts;
    }

    public List<HtmlTranscript> getTranscripts() {
        return transcriptList;
    }

    public List<String> getGoannotations() {
        return goAnnotations;
    }

    public boolean getHasgo() {
        return goAnnotations.size() > 0;
    }
}
