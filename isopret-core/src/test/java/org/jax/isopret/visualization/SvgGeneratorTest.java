package test.java.org.jax.isopret.visualization;

import org.jax.core.visualization.AbstractSvgGenerator;
import org.jax.core.visualization.ProteinSvgGenerator;
import org.jax.core.visualization.TranscriptSvgGenerator;
import test.java.org.jax.isopret.TestBase;
import org.jax.core.transcript.AnnotatedGene;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SvgGeneratorTest extends TestBase {

    private static final AnnotatedGene adarAnnotated = getAdarAnnotatedTranscript();

    @Test
    public void testWriteSvg() {
        AbstractSvgGenerator gen = TranscriptSvgGenerator.factory(adarAnnotated);
        String svg = gen.getSvg();
        assertNotNull(svg);
        System.out.println(svg);
        try {
            String path = "target/adar.svg";
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(svg);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testWriteProteinSvg() {
        AbstractSvgGenerator gen = ProteinSvgGenerator.factory(adarAnnotated);
        String svg = gen.getSvg();
        assertNotNull(svg);
        System.out.println(svg);
        try {
            String path = "target/adar-protein.svg";
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write(svg);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
