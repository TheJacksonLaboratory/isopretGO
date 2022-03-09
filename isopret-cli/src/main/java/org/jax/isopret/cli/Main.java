package org.jax.isopret.cli;

import org.jax.isopret.cli.command.*;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.concurrent.Callable;


@Command(name = "isopret", mixinStandardHelpOptions = true, version = "isopret 0.6.4",
        description = "Isoform interpretation tool.")
public class Main implements Callable<Integer> {
    public static void main(String[] args) {
        if (args.length == 0) {
            // if the user doesn't pass any command or option, add -h to show help
            args = new String[]{"-h"};
        }
        CommandLine cline = new CommandLine(new Main())
                .addSubcommand("download", new DownloadCommand())
                .addSubcommand("GO", new GoOverrepCommand())
                .addSubcommand("interpro", new InterproOverrepCommand());
        cline.setToggleBooleanFlags(false);
        int exitCode = cline.execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        // work done in subcommands
        return 0;
    }

}
