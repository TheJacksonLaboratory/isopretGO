package org.jax.isopret.webserviceclient;




import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class BiomartClient {

    private static final String BIOMARTVIEW_URL = "https://www.ensembl.org/biomart/martservice";

    public BiomartClient() {

    }

    private static String exampleRequest = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
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


    public void postRequest() throws IOException {
        String request = exampleRequest.replaceAll("\\n", " ");

        URL url = new URL(BIOMARTVIEW_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setUseCaches(true);
        connection.setRequestMethod("POST");
       // connection.setRequestProperty("Accept", "application/xml");
        connection.setRequestProperty("Content-Type", "application/xml;charset=utf-8");
        // Set timeout as per needs
        //connection.setConnectTimeout(20000);
       // connection.setReadTimeout(20000);

        DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
        outputStream.writeBytes(request);
        outputStream.flush();
        outputStream.close();

        InputStream inputStream = connection.getInputStream();
        byte[] res = new byte[2048];
        int i = 0;
        StringBuilder response = new StringBuilder();
        while ((i = inputStream.read(res)) != -1) {
            response.append(new String(res, 0, i));
        }
        inputStream.close();

        System.out.println("Response= " + response.toString());
    }
}
