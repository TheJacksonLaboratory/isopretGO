package org.jax.isopret.core.hgnc;

import org.jax.isopret.core.except.IsopretRuntimeException;
import org.jax.isopret.core.model.AccessionNumber;
import org.jax.isopret.core.model.GeneSymbolAccession;
import org.jax.isopret.core.model.GeneModel;
import org.jax.isopret.core.model.Transcript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the structure of the HGMC file
 * [0] hgnc_id
 * [1] symbol
 * [2] name
 * [3] locus_group
 * [4] locus_type
 * [5] status
 * [6] location
 * [7] location_sortable
 * [8] alias_symbol
 * [9] alias_name
 * [10] prev_symbol
 * [11] prev_name
 * [12]gene_family
 * [13]gene_family_id
 * [14]date_approved_reserved
 * [15]date_symbol_changed
 * [16]date_name_changed
 * [17]date_modified
 * [18]entrez_id
 * [19]ensembl_gene_id
 * [20] vega_id
 * [21] ucsc_id
 * [22] ena
 * [23] refseq_accession
 * [24] ccds_id
 * [25] uniprot_ids
 * [26] pubmed_id
 * [27] mgd_id
 * [28] rgd_id
 * [29] lsdb
 * [30] cosmic
 * [31] omim_id
 * [32] mirbase
 * [33] homeodb
 * [34] snornabase
 * [35] bioparadigms_slc
 * [36] orphanet
 * [37] pseudogene.org
 * [38] horde_id	merops
 * [39] imgt	iuphar	k
 * [40] znf_gene_catalog
 * [41] mamit-trnadb
 * [42] cd
 * [43] lncrnadb
 * [44] enzyme_id
 * [45] intermediate_filament_db
 * [46] rna_central_ids
 * [47] lncipedia
 * [48] gtrnadb
 * [49] agr
 *
 * The logic of this class is that the user may give us ids from one of several different sources. It would be
 * wasteful to create a Map for each of the sources. Therefore, we parse all of this information into a map of
 * {@link GeneModel} objects. There are then accessor functions for each of the sources that create a map on the fly.
 * @author Peter N Robinson
 */
public class HgncParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HgncParser.class);
    private final static int GENE_SYMBOL = 1;
    private final static int GENE_NAME = 2;
    private final static int ENTREZ_ID = 18;
    private final static int ENSEMBL_GENE_ID = 19;
    private final static int REFSEQ_ACCESSION = 23;



    private final Map<AccessionNumber, GeneModel> ensemblMap;
    /**
     * Parse the HGNC file
     * @param hgncFile path to the hgnc_complete_set.txt file
     * @param geneSymbolAccessionListMap
     */
    public HgncParser(File hgncFile, Map<GeneSymbolAccession, List<Transcript>> geneSymbolAccessionListMap) {
        ensemblMap = initHgncItems(hgncFile, geneSymbolAccessionListMap);
    }

    private Map<AccessionNumber, GeneModel>  initHgncItems(File hgncFile,
                                          Map<GeneSymbolAccession, List<Transcript>>  geneSymbolAccessionListMap) {
        Map<AccessionNumber, GeneModel> ensemblMap = new HashMap<>();
        int less_than_24_fields = 0;
        int well_formed_lines = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(hgncFile))) {
            String line = br.readLine();
            if (! line.startsWith("hgnc_id")) {
                throw new IsopretRuntimeException("Malformed HGNC header line: " + line);
            }
            while ((line=br.readLine()) != null) {
                String [] fields = line.split("\t");
                if (fields.length < 24) {
                    less_than_24_fields++;
                    continue;
                } else {
                    well_formed_lines++;
                }
                AccessionNumber ensemblGeneAcc = AccessionNumber.ensemblGene(fields[ENSEMBL_GENE_ID]);
                GeneSymbolAccession gsa = new GeneSymbolAccession(fields[GENE_SYMBOL], ensemblGeneAcc);
                if (geneSymbolAccessionListMap.containsKey(gsa)) {
                    List<Transcript> transcriptList = geneSymbolAccessionListMap.get(gsa);
                    GeneModel item = new GeneModel(fields[GENE_SYMBOL],
                            fields[GENE_NAME],
                            fields[ENTREZ_ID],
                            gsa.accession(),
                            fields[REFSEQ_ACCESSION],
                            transcriptList);
                    ensemblMap.put(gsa.accession(), item);
                } else {
                    LOGGER.error("Could not find Jannovar transcript data for {}", gsa);
                }
            }
        } catch (IOException e) {
            throw new IsopretRuntimeException("Could not parse the HGNC file: " + e.getMessage());
        }
        LOGGER.trace("{} HGNC lines with less than 24 fields skipped.", less_than_24_fields);
        LOGGER.trace("{} valid HGNC lines successsfully parsed.", well_formed_lines);
        return Map.copyOf(ensemblMap); // immutable copy
    }

    public int itemCount() {
        return this.ensemblMap.size();
    }

    public Map<AccessionNumber, GeneModel> ensemblMap() {
        return this.ensemblMap;
    }


}
