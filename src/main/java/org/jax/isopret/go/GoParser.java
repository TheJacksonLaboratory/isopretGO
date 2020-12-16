package org.jax.isopret.go;

import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
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
    GoAssociationContainer associationContainer;

    public GoParser(String obo, String gaf) {
        System.out.println("go.obo: " + obo);
        this.ontology = OntologyLoader.loadOntology(new File(obo));
        this.associationContainer = GoAssociationContainer.loadGoGafAssociationContainer(new File(gaf), this.ontology);
    }

    public Ontology getOntology() {
        return ontology;
    }

    public GoAssociationContainer getAssociationContainer() {
        return associationContainer;
    }
}
