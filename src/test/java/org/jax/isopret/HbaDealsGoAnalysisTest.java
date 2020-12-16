package org.jax.isopret;

import org.jax.isopret.go.GoParser;
import org.jax.isopret.go.HbaDealsGoAnalysis;
import org.jax.isopret.hbadeals.HbaDealsParser;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.GoTerm2PValAndCounts;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class HbaDealsGoAnalysisTest {

    private static Map<String, HbaDealsResult> hbaDealsResultMap;

    @BeforeAll
    private static void  init() {
        String path = "/home/peter/Downloads/mason_with_ensembl.txt";
        HbaDealsParser parser = new HbaDealsParser(path);
        hbaDealsResultMap = parser.getHbaDealsResultMap();
    }

    @Test
    public void testIt() {
        String obo = "/home/peter/IdeaProjects/isopret/data/go.obo";
        String gaf = "/home/peter/IdeaProjects/isopret/data/goa_human.gaf";
        GoParser goParser = new GoParser(obo, gaf);
        Ontology ontology = goParser.getOntology();
        GoAssociationContainer associationContainer = goParser.getAssociationContainer();
        HbaDealsGoAnalysis goAnalysis = new HbaDealsGoAnalysis(hbaDealsResultMap,ontology, associationContainer);
        List<GoTerm2PValAndCounts> dge = goAnalysis.dgeOverrepresetationAnalysis();
        for (var g : dge) {
            System.out.println(g);
        }
    }

}
