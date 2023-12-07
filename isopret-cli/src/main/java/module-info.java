module isopret.cli {
    requires info.picocli;
    requires org.monarchinitiative.phenol.core;
    requires org.slf4j;
    requires org.jax.isopret.core;
    requires org.jax.isopret.io;
    requires org.monarchinitiative.phenol.analysis;
    requires org.jax.isopret.data;
    requires org.jax.isopret.exception;

    opens org.jax.isopret.cli.command to info.picocli;
}