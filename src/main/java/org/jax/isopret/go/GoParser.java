package org.jax.isopret.go;

import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.File;

public class GoParser {

    private final Ontology ontology;
    private final GoAssociationContainer associationContainer;

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
