package org.jax.isopret.hgnc;

import org.jax.isopret.except.IsopretRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
 * wasteful to create a Map for each of the sources. Therefore, we parse all of this information into a List of
 * {@link HgncItem} objects. There are then accessor functions for each of the sources that create a map on the fly.
 * @author Peter N Robinson
 */
public class HgncParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HgncParser.class);
    private final static int GENE_SYMBOL = 1;
    private final static int GENE_NAME = 2;
    private final static int ENTREZ_ID = 18;
    private final static int ENSEMBL_GENE_ID = 19;
    private final static int USCS_ID = 21;
    private final static int REFSEQ_ACCESSION = 23;

    private final List<HgncItem> itemList;

    public HgncParser() {
        Path path = Paths.get("data", "hgnc_complete_set.txt");
        File f = path.toFile();
        if (! f.isFile()) {
            throw new IsopretRuntimeException("Could not find HGNC file at " + f.getAbsolutePath() + ". Did you run the download command?");
        }
        itemList = initHgncItems(f.getAbsolutePath());
    }

    /**
     * Parse the HGNC file
     * @param hgncPath path to the hgnc_complete_set.txt file
     */
    public HgncParser(String hgncPath) {
        itemList = initHgncItems(hgncPath);
    }

    private List<HgncItem> initHgncItems(String hgncPath) {
        List<HgncItem> items = new ArrayList<>();
        int less_than_24_fields = 0;
        int well_formed_lines = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(hgncPath))) {
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
                HgncItem item = new HgncItem(fields[GENE_SYMBOL], fields[GENE_NAME], fields[ENTREZ_ID], fields[ENSEMBL_GENE_ID], fields[USCS_ID], fields[REFSEQ_ACCESSION]);
                items.add(item);
            }
        } catch (IOException e) {
            throw new IsopretRuntimeException("Could not parse the HGNC file: " + e.getMessage());
        }
        LOGGER.info("{} HGNC lines with less than 24 fields skipped.", less_than_24_fields);
        LOGGER.info("{} valid HGNC lines successsfully parsed.", well_formed_lines);
        return items;
    }

    public int itemCount() {
        return this.itemList.size();
    }

    public Map<String, HgncItem> ensemblMap() {
        Map<String, HgncItem> ensmap = new HashMap<>();
        int notMapped = 0;
        for (HgncItem item : itemList) {
            String ens = item.getEnsemblGeneId();
            if (ens != null && ens.startsWith("ENS")) {
                ensmap.put(ens, item);
            } else {
                notMapped++;
            }
        }
        if (notMapped > 0) {
            System.out.printf("[INFO] Retrieving %d ENSG mappings; could not map %d HGNS entries.\n", ensmap.size(), notMapped);
        }
        return ensmap;
    }

    public Map<String, HgncItem> ucscMap() {
        Map<String, HgncItem> ensmap = new HashMap<>();
        int notMapped = 0;
        for (HgncItem item : itemList) {
            String ucsc = item.getUcscId();
            if (ucsc != null && ucsc.startsWith("uc")) {
                ensmap.put(ucsc, item);
            } else {
                notMapped++;
            }
        }
        if (notMapped > 0) {
            System.out.printf("[INFO] Retrieving %d UCSC mappings; could not map %d HGNS entries.\n", ensmap.size(), notMapped);
        }
        return ensmap;
    }

    public Map<String, HgncItem> refseqMap() {
        Map<String, HgncItem> ensmap = new HashMap<>();
        int notMapped = 0;
        for (HgncItem item : itemList) {
            String refseq = item.getRefseqAccecssion();
            if (refseq != null && refseq.startsWith("NM_")) {
                ensmap.put(refseq, item);
            } else {
                notMapped++;
            }
        }
        if (notMapped > 0) {
            System.out.printf("[INFO] Retrieving %d RefSeq mappings; could not map %d HGNS entries.\n", ensmap.size(), notMapped);
        }
        return ensmap;
    }




}
