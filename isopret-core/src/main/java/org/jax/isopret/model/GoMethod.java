package org.jax.isopret.model;

public enum GoMethod {
    TFT("Term-for-Term"), PCunion("Parent-Child-Union"), PCintersect("Parent-Child-Intersection"), MGSA("MGSA");
    private final String name;

    GoMethod(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return this.name;
    }

    public static GoMethod fromString(String calculation) {
        switch (calculation.toLowerCase()) {
            case "term-for-term", "term for term", "tft" -> {
                return TFT;
            }
            case "parent-child-union", "parent-child union", "pc-union", "pcu" -> {
                return PCunion;
            }
            case "parent-child-intersection", "parent-child-intersect", "parent-child intersect", "pc-intersection", "pc-intersect", "pci" -> {
                return PCintersect;
            }
            case "mgsa" -> {
                return MGSA;
            }
            default -> {
                System.err.printf("[ERROR] Did not recognize calculation (%s), using default (%s) instead.\n",
                        calculation, TFT);
                return TFT;
            }
        }
    }
}
