package org.jax.isopret.command;

import org.jax.isopret.webserviceclient.BiomartClient;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "biomart", aliases = {"B"},
        mixinStandardHelpOptions = true,
        description = "Fetch data from biomart")
public class BiomartCommand implements Callable<Integer> {

    public BiomartCommand(){

    }

    @Override
    public Integer call() throws Exception {
        BiomartClient client = new BiomartClient();
        client.postRequest();
        return 0;
    }
}
