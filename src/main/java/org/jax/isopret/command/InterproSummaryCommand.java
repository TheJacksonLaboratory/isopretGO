package org.jax.isopret.command;


import org.jax.isopret.interpro.InterproDomainDescParser;
import org.jax.isopret.interpro.InterproDomainParser;
import picocli.CommandLine;

import java.util.concurrent.Callable;

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
        InterproDomainParser domainParser = new InterproDomainParser(this.interproDomainsFile, interproDomainDescParser.getInterproDescriptionMap());

        return 0;
    }


}
