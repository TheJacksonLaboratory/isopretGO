package org.jax.isopret.interpro;

import org.jax.isopret.except.IsopretRuntimeException;

public enum InterproEntryType {
    ACTIVE_SITE("Active_site"),
    BINDING_SITE("Binding_site"),
    CONSERVED_SITE("Conserved_site"),
    DOMAIN("Domain"),
    FAMILY("Family"),
    HOMOLOGOUS_SUPERFAMILY("Homologous_superfamily"),
    PTM("PTM"),
    REPEAT("Repeat");

    private final String name;

    InterproEntryType(String n) {
        this.name = n;
    }


    public static InterproEntryType fromString(String s) {
        switch (s.toUpperCase()) {
            case "ACTIVE_SITE":
                return ACTIVE_SITE;
            case "BINDING_SITE":
                return BINDING_SITE;
            case "CONSERVED_SITE":
                return CONSERVED_SITE;
            case "DOMAIN":
                return DOMAIN;
            case "FAMILY":
                return FAMILY;
            case "HOMOLOGOUS_SUPERFAMILY":
                return HOMOLOGOUS_SUPERFAMILY;
            case "PTM":
                return PTM;
            case "REPEAT":
                return REPEAT;
            default:
                throw new IsopretRuntimeException("Did not recognize InterproEntryType:" + s);
        }
    }




    @Override
    public String toString() {
        return this.name;
    }





}
