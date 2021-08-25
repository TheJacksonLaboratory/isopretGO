package org.jax.isopret.command;


import org.jax.isopret.interpro.*;
import org.jax.isopret.transcript.AccessionNumber;
import picocli.CommandLine;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

@CommandLine.Command(name = "interpro", aliases = {"I"},
        mixinStandardHelpOptions = true,
        description = "Parse/summarize Interpro files")
public class InterproSummaryCommand implements Callable<Integer>  {


    @CommandLine.Option(names={"--desc"}, description ="prosite.dat file", required = true)
    private String prositeDataFile;

    @CommandLine.Option(names={"--domains"}, description ="interpro_domains.txt", required = true)
    private String interproDomainsFile;

    public InterproSummaryCommand() {}


    @Override
    public Integer call() {
        Map<Integer, InterproEntry> interproDescriptionMap = InterproDomainDescParser.getInterproDescriptionMap(this.prositeDataFile);
        summarizeDomains(interproDescriptionMap);
        Map<AccessionNumber, List<InterproAnnotation>> transcriptIdToInterproAnnotationMap = InterproDomainParser.getInterproAnnotationMap(this.interproDomainsFile);
        summarizeAnnotations(transcriptIdToInterproAnnotationMap);
        return 0;
    }

    private void summarizeDomains(Map<Integer, InterproEntry> interproDescriptionMap) {
        Map<InterproEntryType, Long> counts = interproDescriptionMap
                .values()
                .stream()
                .map(InterproEntry::getEntryType)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        for (var entry : counts.entrySet()) {
            System.out.printf("%s: %d.\n", entry.getKey().name(), entry.getValue());
        }
    }

    private void summarizeAnnotations(Map<AccessionNumber, List<InterproAnnotation>> transcriptIdToInterproAnnotationMap) {
        System.out.printf("[INFO] Total annotated transcripts: %d\n", transcriptIdToInterproAnnotationMap.size());
        long totalAnnotations =
                transcriptIdToInterproAnnotationMap
                .values()
                .stream()
                .mapToInt(List::size)
                .sum();
        System.out.printf("[INFO] Total annotations: %d\n", totalAnnotations);
    }


}