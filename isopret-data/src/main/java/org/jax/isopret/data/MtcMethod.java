package org.jax.isopret.data;

public enum MtcMethod {
    BENJAMINI_HOCHBERG("Benjamini-Hochberg"),
    BENJAMINI_YEKUTIELI("Benjamini-Yekutieli"),
    BONFERRONI("Bonferroni"),
    BONFERRONI_HOLM("Bonferroni-Holm"),
    SIDAK("Sidak"),
    NONE("None");

    private final String name;

    MtcMethod(String name) {
        this.name = name;
    }

    public String display() {
        return name;
    }


    @Override
    public String toString() {
        return this.name;
    }

    public static MtcMethod fromString(String mtc) {
        switch (mtc.toLowerCase()) {
            case "benjamini-hochberg", "bh" -> {
                return BENJAMINI_HOCHBERG;
            }
            case "benjamini-yekutieli", "by" -> {
                return BENJAMINI_YEKUTIELI;
            }
            case "bonferroni" -> {
                return BONFERRONI;
            }
            case "bonferroni-holm", "holm" -> {
                return BONFERRONI_HOLM;
            }
            case "sidak" -> {
                return SIDAK;
            }
            case "none" -> {
                return NONE;
            }
            default -> {
                System.err.printf("[ERROR] Did not recognize MTC (%s), using default (%s) instead.\n",
                        mtc, BONFERRONI);
                return BONFERRONI;
            }
        }
    }
}
