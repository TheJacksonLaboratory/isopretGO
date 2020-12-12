package org.jax.isopret.webserviceclient;

/**
 * Use a system process to run curl to get data from biomart
 */
public class BiomartViaCurl {

    private static final String exampleRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<!DOCTYPE Query>\n" +
            "<Query  virtualSchemaName = \"default\" formatter = \"TSV\" header = \"0\" uniqueRows = \"0\" count = \"\" datasetConfigVersion = \"0.6\" >\n" +
            "\t\t\t\n" +
            "\t<Dataset name = \"hsapiens_gene_ensembl\" interface = \"default\" >\n" +
            "\t\t<Attribute name = \"ensembl_gene_id\" />\n" +
            "\t\t<Attribute name = \"ensembl_gene_id_version\" />\n" +
            "\t\t<Attribute name = \"ensembl_transcript_id\" />\n" +
            "\t\t<Attribute name = \"ensembl_transcript_id_version\" />\n" +
            "\t\t<Attribute name = \"ensembl_peptide_id\" />\n" +
            "\t\t<Attribute name = \"ensembl_peptide_id_version\" />\n" +
            "\t\t<Attribute name = \"transcript_start\" />\n" +
            "\t\t<Attribute name = \"transcript_end\" />\n" +
            "\t\t<Attribute name = \"transcript_biotype\" />\n" +
            "\t\t<Attribute name = \"scanprosite\" />\n" +
            "\t\t<Attribute name = \"scanprosite_start\" />\n" +
            "\t\t<Attribute name = \"scanprosite_end\" />\n" +
            "\t\t<Attribute name = \"pfscan\" />\n" +
            "\t\t<Attribute name = \"pfscan_start\" />\n" +
            "\t\t<Attribute name = \"pfscan_end\" />\n" +
            "\t\t<Attribute name = \"ncoils_start\" />\n" +
            "\t\t<Attribute name = \"ncoils_end\" />\n" +
            "\t\t<Attribute name = \"seg\" />\n" +
            "\t\t<Attribute name = \"seg_start\" />\n" +
            "\t\t<Attribute name = \"seg_end\" />\n" +
            "\t</Dataset>\n" +
            "</Query>";



    public void curlQuery() {
        String command = "curl -X POST https://postman-echo.com/post --data foo1=bar1&foo2=bar2";
        //Process process = Runtime.getRuntime().exec(command);
    }



}
