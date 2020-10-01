package org.jax.prositometry.go;

import org.monarchinitiative.phenol.annotations.formats.go.GoGaf21Annotation;
import org.monarchinitiative.phenol.annotations.obo.go.GoGeneAnnotationParser;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoParser {

    private final Ontology ontology;
    private final Map<String, List<TermId>> annotationMap;

    public GoParser(String obo, String gaf) {
        System.out.println("go.obo: " + obo);
        this.ontology = OntologyLoader.loadOntology(new File(obo));
        Map<String, List<TermId>> annotmap = new HashMap<>();
        List<GoGaf21Annotation> annotations = GoGeneAnnotationParser.loadAnnotations(gaf);
        for (GoGaf21Annotation a : annotations) {
            String symbol = a.getDbObjectSymbol();
            annotmap.putIfAbsent(symbol, new ArrayList<>());
            annotmap.get(symbol).add(a.getGoId());
        }
        annotationMap = Map.copyOf(annotmap);
    }

    public Ontology getOntology() {
        return ontology;
    }

    public Map<String, List<TermId>> getAnnotationMap() {
        return annotationMap;
    }
}
