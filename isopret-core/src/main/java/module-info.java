module org.jax.isopret.core {
    requires org.slf4j;
    requires org.monarchinitiative.phenol.core;
    requires org.monarchitiative.svart;
    requires org.monarchinitiative.phenol.analysis;
    requires guava;
    requires jannovar.core;
    requires org.apache.commons.net;
    requires org.monarchinitiative.phenol.io;

    exports org.jax.isopret.core.analysis;
    exports org.jax.isopret.core.impl.go;
    exports org.jax.isopret.model;
    exports org.jax.isopret.core.except;
    exports org.jax.isopret.core.impl.hbadeals;
    exports org.jax.isopret.visualization;
    exports org.jax.isopret.core;

}