package org.jax.isopret.webserviceclient;


import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

public class BiomartClient {

    private static final String BIOMARTVIEW_URL = "https://www.ensembl.org/biomart/martview";

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
        String request = exampleRequest;

        URL url = new URL(BIOMARTVIEW_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Set timeout as per needs
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(20000);

        // Set DoOutput to true if you want to use URLConnection for output.
        // Default is false
        connection.setDoOutput(true);

        connection.setUseCaches(true);
        connection.setRequestMethod("POST");

        // Set Headers
        connection.setRequestProperty("Accept", "application/xml");
        connection.setRequestProperty("Content-Type", "application/xml");

        // Write XML
        OutputStream outputStream = connection.getOutputStream();
        byte[] b = request.getBytes("UTF-8");
        outputStream.write(b);
        outputStream.flush();
        outputStream.close();

        // Read XML
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
