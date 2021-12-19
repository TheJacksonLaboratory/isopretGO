package org.jax.isopret.core.go;

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
            case "term-for-term":
            case "term for term":
            case "tft":
                return TFT;
            case "parent-child-union":
            case "pc-union":
            case "pcu":
                return PCunion;
            case "parent-child-intersection":
            case "parent-child-intersect":
            case "pc-intersection":
            case "pc-intersect":
            case "pci":
                return PCintersect;
            case "mgsa":
                return MGSA;
            default:
                System.err.printf("[ERROR] Did not recognize calculation (%s), using default (%s) instead.\n",
                        calculation, TFT);
                return TFT;
        }
    }
}
