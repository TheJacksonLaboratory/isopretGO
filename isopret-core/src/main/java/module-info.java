module org.jax.isopret.core {
    requires org.slf4j;
    requires org.monarchinitiative.phenol.core;
    requires org.monarchitiative.svart;
    requires org.monarchinitiative.phenol.analysis;
    requires guava;
    requires jannovar.core;
    requires org.apache.commons.net;

    exports org.jax.isopret.core.analysis;
    exports org.jax.isopret.core.go;
    exports org.jax.isopret.model;
    exports org.jax.isopret.core.except;
    exports org.jax.isopret.core.hbadeals;
    exports org.jax.isopret.core.hgnc;
    exports org.jax.isopret.core.interpro;
    //exports org.jax.isopret.core.io;
    exports org.jax.isopret.core.visualization;

}