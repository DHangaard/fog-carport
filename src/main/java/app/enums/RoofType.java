package app.enums;

public enum RoofType
{
    FLAT("Fladt tag"),
    TRAPEZ_ROOF("Trapez tag");

    private final String displayName;

    RoofType(String displayName) {
        this.displayName = displayName;
    }
}
