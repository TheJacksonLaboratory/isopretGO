package org.jax.isopret.hbadeals;

import org.jax.isopret.hgnc.HgncItem;
import org.jax.isopret.hgnc.HgncParser;
import org.junit.jupiter.api.Test;

import java.util.Map;

/**
 * Conveniece to run tests, not an actual unit test at this time.
 */
public class HbaDealsThresholderTest {

    private final static HgncParser hgncParser = new HgncParser();
    private final static Map<String, HgncItem> hgncMap = hgncParser.refseqMap();

    private double getDgeThreshold(String name) {
        String hbadealsFile = "/home/peter/GIT/covidASmanuscript/data/HBA-DEALS-files/" + name;
        HbaDealsParser hbaParser = new HbaDealsParser(hbadealsFile, hgncMap);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();
        HbaDealsThresholder thresholder = new HbaDealsThresholder(hbaDealsResults);
        double dgeThreshold = thresholder.getExpressionThreshold();
        double dasThreshold = thresholder.getSplicingThreshold();
        double prob = thresholder.getFdrThreshold();
        System.out.printf("%s: DGE: %f // DAS: %f prob= %f\n", name, dgeThreshold, dasThreshold, prob);
        return dgeThreshold;
    }

/*
mason_latest.txt dge 0.25 das 0.13
mason_latest.txt: DGE: 0.249023 // DAS: 0.137695 prob= 0.050000

SRP040070_3.txt dge 0.25 das 0.16
SRP040070_3.txt: DGE: 0.249023 // DAS: 0.166016 prob= 0.050000

SRP040070_7.txt dge 0.25 das 0.18
SRP040070_7.txt: DGE: 0.249023 // DAS: 0.185547 prob= 0.050000

SRP040070_9.txt dge 0.25 das 0.25
SRP040070_9.txt: DGE: 0.249023 // DAS: 0.249023 prob= 0.050000

SRP078309_53.txt dge 0.25 das 0.16
SRP078309_53.txt: DGE: 0.249023 // DAS: 0.167969 prob= 0.050000

SRP178454_50.txt dge 0.25 das 0.12
SRP178454_50.txt: DGE: 0.249023 // DAS: 0.127930 prob= 0.050000

SRP186406_51.txt dge 0.25 das 0.17
SRP186406_51.txt: DGE: 0.249023 // DAS: 0.175781 prob= 0.050000

SRP216763_55.txt dge 0.25 das 0.15
SRP216763_55.txt: DGE: 0.249023 // DAS: 0.150391 prob= 0.050000

SRP222569_54.txt dge 0.25 das 0.12
SRP222569_54.txt: DGE: 0.249023 // DAS: 0.123047 prob= 0.050000

SRP226819_57.txt dge NA das NA
SRP226819_57.txt: DGE: 0.249023 // DAS: 0.249023 prob= 0.050000

SRP227272_38.txt dge 0.25 das 0.19
SRP227272_38.txt: DGE: 0.249023 // DAS: 0.190430 prob= 0.050000

SRP251704_52.txt dge 0.23 das 0.16
SRP251704_52.txt: DGE: 0.232422 // DAS: 0.164063 prob= 0.050000

SRP273785_56.txt dge 0.18 das 0.11
SRP273785_56.txt: DGE: 0.180664 // DAS: 0.113281 prob= 0.050000


SRP278618_58.txt dge 0.25 das 0.15
SRP278618_58.txt: DGE: 0.249023 // DAS: 0.157227 prob= 0.050000

SRP279203_72.txt dge 0.25 das 0.17
SRP279203_72.txt: DGE: 0.249023 // DAS: 0.175781 prob= 0.050000

SRP284977_76.txt dge 0.25 das 0.18
SRP284977_76.txt: DGE: 0.249023 // DAS: 0.182617 prob= 0.050000


SRP284977_77.txt dge 0.25 das 0.16
SRP284977_77.txt: DGE: 0.249023 // DAS: 0.165039 prob= 0.050000

SRP294125_74.txt dge 0.25 das 0.15
SRP294125_74.txt: DGE: 0.249023 // DAS: 0.157227 prob= 0.050000

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
        getDgeThreshold("mason_latest.txt");
        getDgeThreshold("SRP284977_77.txt");
    }
}
