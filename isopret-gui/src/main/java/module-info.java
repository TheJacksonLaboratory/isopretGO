module org.jax.isopret.gui {
    requires org.slf4j;
    requires org.monarchinitiative.phenol.core;
    requires org.monarchinitiative.svart;
    requires org.monarchinitiative.phenol.analysis;
    requires jannovar.core;
    requires org.apache.commons.net;
    requires org.monarchinitiative.phenol.io;
    requires guava;



    requires org.jax.isopret.core;
    requires org.jax.isopret.io;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;

    requires org.apache.commons.io;


    exports org.jax.isopret.gui;

}