package org.jax.isopret.command;


import org.jax.isopret.interpro.*;
import picocli.CommandLine;

import java.util.HashMap;
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
        InterproDomainDescParser interproDomainDescParser = new InterproDomainDescParser(this.prositeDataFile);
        Map<Integer, InterproEntry> interproDescriptionMap = interproDomainDescParser.getInterproDescriptionMap();
        summarizeDomains(interproDescriptionMap);
        InterproDomainParser domainParser = new InterproDomainParser(this.interproDomainsFile);
        Map<Integer, InterproAnnotation> transcriptIdToInterproAnnotationMap = domainParser.getTranscriptIdToInterproAnnotationMap();

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


}
