package org.jax.isopret.cli.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "interpro",
        mixinStandardHelpOptions = true,
        description = "Interpro Overrepresentation")
public class InterproOverrepCommand extends IsopretCommand implements Callable<Integer> {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterproOverrepCommand.class);
    @CommandLine.Option(names={"-b","--hbadeals"},
            description ="HBA-DEALS output file" , required = true)
    private String hbadealsFile;
    @CommandLine.Option(names={"--outfile"}, description = "Name of output file to write stats")
    private String outfile = null;

    @Override
    public Integer call() throws Exception {
        return null;
    }
}
