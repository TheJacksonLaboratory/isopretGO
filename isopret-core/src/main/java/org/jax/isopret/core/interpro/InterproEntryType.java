package org.jax.isopret.core.interpro;

public enum InterproEntryType {
    ACTIVE_SITE("Active_site"),
    BINDING_SITE("Binding_site"),
    CONSERVED_SITE("Conserved_site"),
    DOMAIN("Domain"),
    FAMILY("Family"),
    HOMOLOGOUS_SUPERFAMILY("Homologous_superfamily"),
    PTM("PTM"),
    REPEAT("Repeat"),
    UNKNOWN("Knknown");

    private final String name;

    InterproEntryType(String n) {
        this.name = n;
    }


    public static InterproEntryType fromString(String s) {
        return switch (s.toUpperCase()) {
            case "ACTIVE_SITE" -> ACTIVE_SITE;
            case "BINDING_SITE" -> BINDING_SITE;
            case "CONSERVED_SITE" -> CONSERVED_SITE;
            case "DOMAIN" -> DOMAIN;
            case "FAMILY" -> FAMILY;
            case "HOMOLOGOUS_SUPERFAMILY" -> HOMOLOGOUS_SUPERFAMILY;
            case "PTM" -> PTM;
            case "REPEAT" -> REPEAT;
            default -> UNKNOWN;
        };
    }




    @Override
    public String toString() {
        return this.name;
    }





}
