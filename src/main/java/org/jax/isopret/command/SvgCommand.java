package org.jax.isopret.command;

import org.apache.commons.io.IOUtils;
import org.jax.isopret.except.IsopretRuntimeException;
import org.jax.isopret.hbadeals.HbaDealsParser;
import org.jax.isopret.hbadeals.HbaDealsResult;
import org.jax.isopret.hgnc.HgncItem;
import org.jax.isopret.hgnc.HgncParser;
import org.jax.isopret.interpro.DisplayInterproAnnotation;
import org.jax.isopret.interpro.InterproMapper;
import org.jax.isopret.transcript.AccessionNumber;
import org.jax.isopret.transcript.AnnotatedGene;
import org.jax.isopret.transcript.JannovarReader;
import org.jax.isopret.transcript.Transcript;
import org.jax.isopret.visualization.AbstractSvgGenerator;
import org.jax.isopret.visualization.ProteinSvgGenerator;
import org.jax.isopret.visualization.TranscriptSvgGenerator;
import org.monarchinitiative.svart.GenomicAssemblies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(name = "svg", aliases = {"V"},
        mixinStandardHelpOptions = true,
        description = "Create SVG/PDF files for a specific gene")
public class SvgCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SvgCommand.class);

    @CommandLine.Option(names={"-b","--hbadeals"}, description ="HBA-DEALS output file" , required = true)
    private String hbadealsFile;
    @CommandLine.Option(names = {"-j", "--jannovar"}, description = "Path to Jannovar transcript file")
    private String jannovarPath = "data/hg38_ensembl.ser";
    @CommandLine.Option(names={"-n", "--namespace"}, description = "Namespace of gene identifiers (ENSG, ucsc, RefSeq)")
    private String namespace = "ensembl";
    @CommandLine.Option(names={"-g","--gene"}, required = true, description = "Gene symbol")
    private String geneSymbol;
    @CommandLine.Option(names={"--desc"}, description ="interpro_domain_desc.txt file", required = true)
    private String interproDescriptionFile;
    @CommandLine.Option(names={"--domains"}, description ="interpro_domains.txt", required = true)
    private String interproDomainsFile;

    public SvgCommand() {

    }

    @Override
    public Integer call() {
        JannovarReader jreader = new JannovarReader(this.jannovarPath, GenomicAssemblies.GRCh38p13());
        Map<String, List<Transcript>> geneSymbolToTranscriptMap = jreader.getSymbolToTranscriptListMap();
        Map<AccessionNumber, HgncItem> hgncMap;
        HgncParser hgncParser = new HgncParser();
        if (this.namespace.equalsIgnoreCase("ensembl")) {
            hgncMap = hgncParser.ensemblMap();
        } else {
            throw new IsopretRuntimeException("Only ensembl supported but you entered " + this.namespace);
        }
        Map<AccessionNumber, List<DisplayInterproAnnotation>> annotList = gettranscriptToInterproHitMap(hgncMap);

        // Get HBADEALS result for one gene (this.geneSymbol)
        HbaDealsResult result = getHbaDealsResultForGene(hgncMap);
        List<Transcript> transcripts = geneSymbolToTranscriptMap.get(this.geneSymbol);
        AnnotatedGene agene = new AnnotatedGene(transcripts, annotList,result);
        AbstractSvgGenerator svggen = TranscriptSvgGenerator.factory(agene);
        String isoformSvg = svggen.getSvg();
        svggen = ProteinSvgGenerator.factory(agene);
        String proteinSvg = svggen.getSvg();
        String isoformFilename = String.format("%s-isoforms.svg", geneSymbol);
        String proteinFilename = String.format("%s-protein.svg", geneSymbol);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(isoformFilename))) {
            writer.write(isoformSvg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(proteinFilename))) {
            writer.write(proteinSvg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        convertToPdf(isoformFilename);
        convertToPdf(proteinFilename);

        return 0;
    }

    /**
     * This command extracts a result for a single gene (this.geneSymbol) from the HBADEALS file in the input.
     * If it cannot find the gene, it throws a runtime exception
     * @param hgncMap Map between ensembl accession numbers and {@link HgncItem} objects.
     * @return {@link HbaDealsResult} object corresponding to {@link #geneSymbol}
     */
    private HbaDealsResult getHbaDealsResultForGene(Map<AccessionNumber, HgncItem> hgncMap) {
        LOGGER.trace("SVG-jannovar path: {}, interpro: {}, nterpro Desc: {}", jannovarPath, interproDomainsFile, interproDescriptionFile);
        HbaDealsParser hbaParser = new HbaDealsParser(hbadealsFile, hgncMap);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();
        if (! hbaDealsResults.containsKey(this.geneSymbol)) {
            throw new IsopretRuntimeException(String.format("[ERROR] Could not find HBA-DEALS result for %s\n", this.geneSymbol));
        }
        LOGGER.trace("Get HBA-DEALS result for: {}", this.geneSymbol);
        HbaDealsResult result = hbaDealsResults.get(this.geneSymbol);
        return result;
    }

    /**
     * Parse the two interpro files and extract all entries that relate to {@link #geneSymbol}
     * @param hgncMap map of {@link HgncItem} objects
     * @return a map with key: Ensembl transcript, value: list of interpro annotations for display.
     */
    private Map<AccessionNumber, List<DisplayInterproAnnotation>> gettranscriptToInterproHitMap(Map<AccessionNumber, HgncItem> hgncMap) {
        InterproMapper interproMapper = new InterproMapper(this.interproDescriptionFile, this.interproDomainsFile);
        Map<String, AccessionNumber> geneSymbolToAccessionMap =
                hgncMap.entrySet()
                        .stream()
                        .collect(Collectors.toMap(e -> e.getValue().getGeneSymbol(), Map.Entry::getKey));
        // Get AccessionNumber object corresponding to gene symbol
        AccessionNumber geneAccession = geneSymbolToAccessionMap.get(this.geneSymbol);
        LOGGER.trace("Accession {} found for gene symbol {}", geneAccession.getAccessionString(), this.geneSymbol);
        Map<AccessionNumber, List<DisplayInterproAnnotation>> annotList = interproMapper.transcriptToInterproHitMap(geneAccession);
        LOGGER.trace("Got {} transcripts with annotations for gene accession {} found for gene symbol {}",
                annotList.size(), geneAccession.getAccessionString(), this.geneSymbol);
        return annotList;
    }


    /**
     * This is a convenience function that runs {@code rsvg-convert} to convert the SVG files it writes into corresponding
     * PDF graphic files.
     * @param svgFileName name of the input SVG file (which is created by this program in a first step).
     */
    private void convertToPdf(String svgFileName) {
        /*
         -f pdf -o mygraph.pdf mygraph.svg
         */
        String pdfFileName = svgFileName.replace(".svg", ".pdf");
        Runtime rt = Runtime.getRuntime();

        String[] commands = {"rsvg-convert", "-f" , "pdf", "-o", pdfFileName, svgFileName };
        try {
            Process proc = rt.exec(commands);
            String stderr = IOUtils.toString(proc.getErrorStream(), Charset.defaultCharset());
            String stdout = IOUtils.toString(proc.getInputStream(), Charset.defaultCharset());
            if (stderr != null && ! stderr.isEmpty()) {
                System.err.println(stderr);
            }
            if (stdout != null && ! stdout.isEmpty()) {
                System.out.println(stdout);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
