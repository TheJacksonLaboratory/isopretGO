package org.jax.isopret.command;

import org.jax.isopret.prosite.PrositeParser;
import org.jax.isopret.prosite.PrositePattern;
import picocli.CommandLine;

import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "stats", aliases = {"S"},
        mixinStandardHelpOptions = true,
        description = "Show descriptive statistics about data")
public class DumpStatsCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-p", "--prosite"}, description = "prosite.dat file")
    private String prositeDataFile = "data/prosite.dat";

    private List<PrositePattern> patternList;

    public DumpStatsCommand() {
    }


    @Override
    public Integer call() throws Exception {
        PrositeParser proparser = new PrositeParser(this.prositeDataFile);
        patternList = proparser.getPatternList();
        System.out.printf("[INFO] %s.\n", this.prositeDataFile);
        for (PrositePattern pat : patternList) {
            System.out.printf("[INFO] %s\n", pat);
        }
        return 0;
    }
}
