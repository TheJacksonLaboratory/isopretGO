module org.jax.isopret.core {
    requires org.slf4j;
    requires org.monarchinitiative.phenol.core;
    requires org.monarchinitiative.svart;
    requires org.monarchinitiative.phenol.analysis;
    requires jannovar.core;
    requires org.apache.commons.net;
    requires org.monarchinitiative.phenol.io;
    requires guava;

    exports org.jax.isopret.core.analysis;
    exports org.jax.isopret.model;
    exports org.jax.isopret.except;
    exports org.jax.isopret.core.impl.rnaseqdata;
    exports org.jax.isopret.visualization;
    exports org.jax.isopret.core;
    exports org.jax.isopret.core.impl.interpro;
    exports org.jax.isopret.core.impl.hgnc;
    exports org.jax.isopret.core.impl.go;

}