package org.jax.isopret.go;

import org.monarchinitiative.phenol.analysis.GoAssociationContainer;
import org.monarchinitiative.phenol.io.OntologyLoader;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class GoParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoParser.class);

    private final Ontology ontology;
    private final GoAssociationContainer associationContainer;

    public GoParser(String obo, String gaf) {
        LOGGER.trace("Gene Ontology path: " + obo);
        LOGGER.trace("Gene Ontology annotation path: " + gaf);
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
