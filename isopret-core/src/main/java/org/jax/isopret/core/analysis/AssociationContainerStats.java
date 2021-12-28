package org.jax.isopret.core.analysis;

import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.ontology.data.Ontology;

import java.io.*;
import java.util.Set;

public class AssociationContainerStats<T> {
    private final Ontology geneOntology;
    private final AssociationContainer<T> container;
    private final String label;

    public AssociationContainerStats(Ontology geneOntology, AssociationContainer<T> container, String label) {
        this.geneOntology = geneOntology;
        this.container = container;
        System.out.println(label);
        this.label = label;
    }


    public void display() {
        try (Writer stdout =  new BufferedWriter(new OutputStreamWriter(System.out))) {
            write(stdout);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToFile(String filename) {
        File file = new File(filename);
        try (Writer bw =  new BufferedWriter(new FileWriter(file))) {
            write(bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(Writer writer) throws IOException {
        writer.write("### Isopret: Gene Ontology AssociationContainer  ###\n\n");
        writer.write("\t" + label + "\n");
        Set<T> allAnnotated = container.getAllAnnotatedGenes();
        int n_annotated_genes = allAnnotated == null ? 0 : allAnnotated.size();
        writer.write("-- values from GO Gaf file ----- \n");
        int n_termCount = container.getOntologyTermToDomainItemsMap().size();
        writer.write("\t(" + label +") annotated genes: " + n_annotated_genes +"\n");
        writer.write("\t(\" + label +\") GO terms: " + n_termCount +"\n");
        int c = 0;
        if (allAnnotated != null) {
            for (var g : container.getAllAnnotatedGenes()) {
                c++;
                if (c > 10) break;
                writer.write(c + "/" + n_annotated_genes + ") " + g + "\n");
            }
        }
        writer.write("...\n");
//        Multimap<TermId, TermId> mmap = container.getTermToItemMultimap();
//        int n = 0;
//        for (TermId tid : mmap.keys()) {
//            Collection<TermId> collection = mmap.get(tid);
//            n += collection.size();
//        }
//        Map<TermId, List<T>> m =  container.getOntologyTermToDomainItemsMap();
//        int n_annots_from_container = container.getTermToItemMultimap().size();
//        writer.write("n_annots_from_container: " + n_annots_from_container + "\n");
//        for (var a : container.getTermToItemMultimap().values()) {
//            c++;
//            if (c>10) break;
//            writer.write(c +"/"+n_annotated_genes+ ") " + a.toString() + "\n");
//        }
    }


}
