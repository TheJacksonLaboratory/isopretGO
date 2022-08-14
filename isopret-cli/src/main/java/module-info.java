module isopret.cli {
    requires info.picocli;
    requires org.monarchinitiative.phenol.core;
    requires org.slf4j;
    requires org.jax.isopret.core;
    requires org.monarchinitiative.phenol.analysis;

    opens org.jax.isopret.cli.command to info.picocli;
}