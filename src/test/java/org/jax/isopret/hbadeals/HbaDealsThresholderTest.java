package org.jax.isopret.hbadeals;

import org.jax.isopret.hgnc.HgncItem;
import org.jax.isopret.hgnc.HgncParser;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * Conveniece to run tests, not an actual unit test at this time.
 */
public class HbaDealsThresholderTest {

    private final static HgncParser hgncParser = new HgncParser();
    private final static Map<String, HgncItem> hgncMap = hgncParser.ensemblMap();

    private double getDgeThreshold(String name) {
        String hbadealsFile = "/home/peter/GIT/covidASmanuscript/data/HBA-DEALS-files/" + name;
        HbaDealsParser hbaParser = new HbaDealsParser(hbadealsFile, hgncMap);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();
        HbaDealsThresholder thresholder = new HbaDealsThresholder(hbaDealsResults);
        double dgeThreshold = thresholder.getExpressionThreshold();
        int n_dge = thresholder.dgeGeneSymbols().size();
        double dasThreshold = thresholder.getSplicingThreshold();
        int n_das = thresholder.dasGeneSymbols().size();
        System.out.printf("%s: DGE: %f (%d genes) // DAS: %f (%d genes)\n", name, dgeThreshold, n_dge, dasThreshold, n_das);
        return dgeThreshold;
    }

/*

mason_latest.txt dge 0.25, n dge=445; das 0.13 n_das=1
SRP040070_3.txt dge 0.25, n dge=1218; das 0.16 n_das=1431
SRP040070_3.txt: DGE: 0.249023 (1218 genes) // DAS: 0.166016 (1461 genes)


SRP040070_7.txt dge 0.25, n dge=2812; das 0.18 n_das=2641
SRP040070_7.txt: DGE: 0.249023 (2811 genes) // DAS: 0.185547 (2682 genes)


SRP040070_9.txt dge 0.25, n dge=7376; das 0.25 n_das=7293
SRP040070_9.txt: DGE: 0.249023 (7374 genes) // DAS: 0.249023 (7393 genes)


SRP078309_53.txt dge 0.25, n dge=2496; das 0.16 n_das=1523
SRP178454_50.txt dge 0.25, n dge=82; das 0.12 n_das=104
SRP178454_50.txt: DGE: 0.249023 (82 genes) // DAS: 0.127930 (110 genes)


SRP186406_51.txt dge 0.25, n dge=35; das 0.17 n_das=113
SRP186406_51.txt: DGE: 0.249023 (35 genes) // DAS: 0.175781 (115 genes)


SRP216763_55.txt dge 0.25, n dge=190; das 0.15 n_das=110
SRP222569_54.txt dge 0.25, n dge=534; das 0.12 n_das=33
SRP222569_54.txt: DGE: 0.249023 (533 genes) // DAS: 0.123047 (34 genes)


SRP226819_57.txt dge NA, n dge=443; das NA n_das=1
SRP226819_57.txt: DGE: 0.249023 (0 genes) // DAS: 0.249023 (0 genes)



SRP227272_38.txt dge 0.25, n dge=6142; das 0.19 n_das=2136
SRP251704_52.txt dge 0.23, n dge=327; das 0.16 n_das=1093
SRP251704_52.txt: DGE: 0.232422 (327 genes) // DAS: 0.164063 (1114 genes)


SRP273785_56.txt dge 0.18, n dge=33; das 0.11 n_das=51
SRP273785_56.txt: DGE: 0.180664 (33 genes) // DAS: 0.113281 (55 genes)


SRP278618_58.txt dge 0.25, n dge=31; das 0.15 n_das=2
SRP279203_72.txt dge 0.25, n dge=96; das 0.17 n_das=174
SRP279203_72.txt: DGE: 0.249023 (96 genes) // DAS: 0.175781 (177 genes)

SRP284977_76.txt dge 0.25, n dge=297; das 0.18 n_das=166
SRP284977_77.txt dge 0.25, n dge=153; das 0.16 n_das=143
SRP294125_74.txt dge 0.25, n dge=545; das 0.15 n_das=765
SRP294125_74.txt: DGE: 0.249023 (544 genes) // DAS: 0.157227 (782 genes)






SRP284977_76.txt: DGE: 0.249023 (295 genes) // DAS: 0.182617 (166 genes)
SRP040070_3.txt: DGE: 0.249023 (1218 genes) // DAS: 0.166016 (1461 genes)
SRP078309_53.txt: DGE: 0.249023 (2493 genes) // DAS: 0.167969 (1563 genes)
SRP216763_55.txt: DGE: 0.249023 (190 genes) // DAS: 0.150391 (110 genes)
SRP227272_38.txt: DGE: 0.249023 (6142 genes) // DAS: 0.190430 (2141 genes)
SRP278618_58.txt: DGE: 0.249023 (31 genes) // DAS: 0.157227 (2 genes)
SRP284977_77.txt: DGE: 0.249023 (153 genes) // DAS: 0.165039 (144 genes)

 */
    @Test
    public void test() {
        getDgeThreshold("SRP040070_3.txt");
        getDgeThreshold("SRP040070_7.txt");
        getDgeThreshold("SRP178454_50.txt");
        getDgeThreshold("SRP222569_54.txt");
        getDgeThreshold("SRP251704_52.txt");
        getDgeThreshold("SRP279203_72.txt");
        getDgeThreshold("SRP294125_74.txt");
        getDgeThreshold("SRP040070_9.txt");
        getDgeThreshold("SRP186406_51.txt");
        getDgeThreshold("SRP226819_57.txt");
        getDgeThreshold("SRP273785_56.txt");
        getDgeThreshold("SRP284977_76.txt");
        getDgeThreshold("SRP040070_3.txt");
        getDgeThreshold("SRP078309_53.txt");
        getDgeThreshold("SRP216763_55.txt");
        getDgeThreshold("SRP227272_38.txt");
        getDgeThreshold("SRP278618_58.txt");
        //getDgeThreshold("mason_latest.txt");
        getDgeThreshold("SRP284977_77.txt");
    }

    @Test
    public void test54() {
        String name = "SRP222569_54.txt";
        String hbadealsFile = "/home/peter/GIT/covidASmanuscript/data/HBA-DEALS-files/" + name;
        HbaDealsParser hbaParser = new HbaDealsParser(hbadealsFile, hgncMap);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();
        HbaDealsThresholder thresholder = new HbaDealsThresholder(hbaDealsResults);
        double dgeThreshold = thresholder.getExpressionThreshold();
        double dasThreshold = thresholder.getSplicingThreshold();
        double prob = thresholder.getFdrThreshold();
        System.out.printf("%s: DGE: %f // DAS: %f prob= %f\n", name, dgeThreshold, dasThreshold, prob);
        Set<String> das = thresholder.dgeGeneSymbols();
        List<String> dasg = new ArrayList<>(das);
        Collections.sort(dasg);
        for (var s : dasg) {
            System.out.println(s);
        }

    }


    /*
    DAS for 54

    ABCA7
ACO1
ATP5F1A
ATP5F1B
BCL2L13
CAPN1
CCT8
CFLAR
CTNND1
CTSB
CTSD
CX3CL1
HNRNPC
HSP90AA1
KRT18
KRT19
KRT5
MED24
MGAT1
NCBP2
OAS2
PIAS3
PLXNB2
PRSS23
RABGAP1L
S100A14
SERPINA3
SERPINB7
SHC1
SLC20A1
SLC25A3
TM4SF1
TMC6

     */
}
