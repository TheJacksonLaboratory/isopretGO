package org.jax.prositometry;

import org.jax.prositometry.prosite.PrositeParser;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;


@Command(name = "prositometry", mixinStandardHelpOptions = true, version = "prositometry 0.0.1",
        description = "Prosite tool.")
public class Main implements Callable<Integer> {

    @CommandLine.Option(names = {"-p","--prosite"}, description = "path to prosite.dat")
    String prositePath;

    public static void main(String[] args) {
        CommandLine cline = new CommandLine(new Main());
        cline.setToggleBooleanFlags(false);
        int exitCode = cline.execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        Prositometry pro = new Prositometry(prositePath);
        pro.dumpStats();
        return 0;
    }

}
