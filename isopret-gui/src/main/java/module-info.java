module org.jax.isopret.gui {

    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;


    requires org.apache.commons.net;
    requires org.apache.commons.io;
    requires org.slf4j;

    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;

    requires org.monarchinitiative.phenol.analysis;
    requires org.monarchinitiative.phenol.core;
    requires org.monarchinitiative.phenol.io;
    requires org.monarchinitiative.svart;
    requires jannovar.core;
    requires org.jax.isopret.core;
    requires org.jax.isopret.data;
    requires org.jax.isopret.exception;
    requires org.jax.isopret.io;

    exports org.jax.isopret.gui;
    exports org.jax.isopret.gui.service;
    exports org.jax.isopret.gui.configuration to spring.beans, spring.context;
    exports org.jax.isopret.gui.service.impl to spring.beans, spring.core;
    exports org.jax.isopret.gui.controller to spring.beans, spring.context;



    opens org.jax.isopret.gui to spring.core;
    opens org.jax.isopret.gui.configuration to spring.core;
    opens org.jax.isopret.gui.controller to spring.core;
    opens org.jax.isopret.gui.service.impl to spring.core;
}