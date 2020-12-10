package org.jax.isopret.visualization;

import org.jax.isopret.TestBase;
import org.jax.isopret.transcript.AnnotatedTranscript;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SvgGeneratorTest extends TestBase {

    private static final AnnotatedTranscript adarAnnotated = getAdarAnnotatedTranscript();

    @Test
    public void testWriteSvg() {
        SvgGenerator gen = new SvgGenerator(adarAnnotated);
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
}
