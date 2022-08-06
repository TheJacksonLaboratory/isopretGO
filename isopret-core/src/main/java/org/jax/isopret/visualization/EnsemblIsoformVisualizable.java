package org.jax.isopret.visualization;

import org.jax.isopret.core.impl.hbadeals.HbaDealsTranscriptResult;

public class EnsemblIsoformVisualizable implements IsoformVisualizable {


    private final String transcriptAccession;

    private final String isoformUrlAnchor;

    private final double log2FoldChange;

    private final double isoformP;

    private final boolean isSignificant;

    public EnsemblIsoformVisualizable(HbaDealsTranscriptResult transcriptResult, double splicingPepThreshold){
        this.transcriptAccession = transcriptResult.getTranscript();
        String url = getEnsemblTranscriptUrl(transcriptResult.getTranscript());
        this.isoformUrlAnchor =  String.format("<a href=\"%s\" target=\"__blank\">%s</a>\n", url, transcriptResult.getTranscript());
        this.log2FoldChange = transcriptResult.getLog2FoldChange();
        this.isoformP = transcriptResult.getP();
        this.isSignificant = transcriptResult.isSignificant(splicingPepThreshold);
    }

    private String getEnsemblTranscriptUrl(String accession) {
        //https://useast.ensembl.org/Homo_sapiens/Transcript/Summary?db=core;g=ENSG00000181026;r=15:88626612-88632281;t=ENST00000557927
        return String.format("https://ensembl.org/Homo_sapiens/Gene/Summary?db=core;t=%s", accession);
    }


    @Override
    public String transcriptAccession() {
        return this.transcriptAccession;
    }

    @Override
    public String isoformUrlAnchor() {
        return isoformUrlAnchor;
    }

    @Override
    public String log2Foldchange() {
        return String.format("%.2f", this.log2FoldChange);
    }

    @Override
    public String isoformP() {
        return formatP(this.isoformP);
    }

    private String formatP(double p) {
        if (p > 0.05) {
            return String.format("%.2f", p);
        } else if (p > 0.001) {
            return String.format("%.3f", p);
        } else {
            return String.format("%e", p);
        }
    }

    @Override
    public boolean isSignificant() {
        return this.isSignificant;
    }


}
