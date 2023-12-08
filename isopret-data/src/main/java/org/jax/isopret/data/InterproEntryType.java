package org.jax.isopret.data;

/**
 * Interpro entry types that appear in the interpro domain description file.
 * Active_site
 * Binding_site
 * Conserved_site
 * Domain
 * DUF4510

 * ERM_C_dom
 * Family
 * FGFR_TM
 * HMMR_N
 * Homologous_superfamily
 * Marf1_cons_dom
 * MIF4-like_sf
 * Munc13_subgr_dom-2
 * NHL_repeat_subgr
 * PTM
 * Repeat
 * RNAP_RPB6_omega
 * Rpb5-like
 * RpoH/RPB5
 * SMIM11A
 * SMIM11B
 * WTAP/Mum2
 */
public enum InterproEntryType {
    ACTIVE_SITE("Active_site"),
    BINDING_SITE("Binding_site"),
    CONSERVED_SITE("Conserved_site"),
    DOMAIN("Domain"),
    DUF4510("DUF4510"),
    ERM_C_DOM("ERM_C_dom"),
    FAMILY("Family"),
    FGFR_TM("FGFR_TM"),
    HMMR_N("HMMR_N"),
    HOMOLOGOUS_SUPERFAMILY("Homologous_superfamily"),
    Marf1_cons_dom("Marf1_cons_dom"),
    Munc13_subgr_dom_2("Munc13_subgr_dom-2"),
    MIF4_like_sf("MIF4-like_sf"),
    NHL_repeat_subgr("NHL_repeat_subgr"),
    PTM("PTM"),
    REPEAT("Repeat"),
    RNAP_RPB6_omega("RNAP_RPB6_omega"),
    Rpb5_like("Rpb5-like"),
    RpoH_RPB5("RpoH/RPB5"),
    SMIM11A("SMIM11A"),
    SMIM11B("SMIM11B"),
    WTAP_Mum2("WTAP/Mum2"),
    UNKNOWN("Unknown");

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
            case "DUF4510" -> DUF4510;
            case "ERM_C_DOM" -> ERM_C_DOM;
            case "FAMILY" -> FAMILY;
            case "FGFR_TM" -> FGFR_TM;
            case "HMMR_N" -> HMMR_N;
            case "HOMOLOGOUS_SUPERFAMILY" -> HOMOLOGOUS_SUPERFAMILY;
            case "MARF1_CONS_DOM" -> Marf1_cons_dom;
            case "MIF4-LIKE_SF" -> MIF4_like_sf;
            case "MUNC13_SUBGR_DOM-2" -> Munc13_subgr_dom_2;
            case "NHL_REPEAT_SUBGR" -> NHL_repeat_subgr;
            case "PTM" -> PTM;
            case "REPEAT" -> REPEAT;
            case "RPB5-LIKE" -> Rpb5_like;
            case "RNAP_RPB6_OMEGA" -> RNAP_RPB6_omega;
            case "RPOH/RPB5" -> RpoH_RPB5;
            case "SMIM11A" -> SMIM11A;
            case "SMIM11B" -> SMIM11B;
            case "WTAP/MUM2" -> WTAP_Mum2;
            default -> UNKNOWN;
        };
    }




    @Override
    public String toString() {
        return this.name;
    }





}
