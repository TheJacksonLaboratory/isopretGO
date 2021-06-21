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
import org.monarchinitiative.svart.GenomicAssembly;
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
        Map<AccessionNumber, HgncItem> hgncMap;
        HgncParser hgncParser = new HgncParser();
        if (this.namespace.equalsIgnoreCase("ensembl")) {
            hgncMap = hgncParser.ensemblMap();
//        } else if (this.namespace.equalsIgnoreCase("ucsc")) {
//            hgncMap = hgncParser.ucscMap();
//        } else if (this.namespace.equalsIgnoreCase("refseq")) {
//            hgncMap = hgncParser.refseqMap();
        } else {
            throw new IsopretRuntimeException("Name space was " + namespace + " but must be one of ensembl, UCSC, refseq");
        }
        Map<String, AccessionNumber> geneSymbolToAccessionMap =
                hgncMap.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getValue().getGeneSymbol(), Map.Entry::getKey));
        LOGGER.trace("SVG command");
        LOGGER.trace("jannovar path: {}", jannovarPath);
        LOGGER.trace("interpro: {}", interproDomainsFile);
        LOGGER.trace("interpro Desc: {}", interproDescriptionFile);
        GenomicAssembly hg38 =  GenomicAssemblies.GRCh38p13();
        JannovarReader jreader = new JannovarReader(this.jannovarPath, hg38);
        Map<String, List<Transcript>> geneSymbolToTranscriptMap = jreader.getSymbolToTranscriptListMap();
        InterproMapper interproMapper = new InterproMapper(this.interproDescriptionFile, this.interproDomainsFile);
        HbaDealsParser hbaParser = new HbaDealsParser(hbadealsFile, hgncMap);
        Map<String, HbaDealsResult> hbaDealsResults = hbaParser.getHbaDealsResultMap();
        if (! hbaDealsResults.containsKey(this.geneSymbol)) {
            System.out.printf("[ERROR] Could not find HBA-DEALS result for %s\n", this.geneSymbol);
            return 1;
        }
        LOGGER.trace("Get HBA-DEALS result for: {}", this.geneSymbol);
        HbaDealsResult result = hbaDealsResults.get(this.geneSymbol);
        List<Transcript> transcripts = geneSymbolToTranscriptMap.get(geneSymbol);
        // Get AccessionNumber object corresponding to gene symbol
        AccessionNumber geneAccession = geneSymbolToAccessionMap.get(this.geneSymbol);
        LOGGER.trace("Accession {} found for gene symbol {}", geneAccession.getAccessionString(), this.geneSymbol);
        Map<AccessionNumber, List<DisplayInterproAnnotation>> annotList = interproMapper.transcriptToInterproHitMap(geneAccession);
        LOGGER.trace("Got {} transcripts with annotations for gene accession {} found for gene symbol {}",
                annotList.size(), geneAccession.getAccessionString(), this.geneSymbol);
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
