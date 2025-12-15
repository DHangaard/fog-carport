package app.enums;

public enum MaterialType
{
    POST("Stolpe"),
    RAFTER("Spær"),
    BEAM("Rem"),
    ROOF("Tag"),
    BOARD("Bræt"),
    FASTENER("Skrue"),
    FITTING("Beslag"),
    METAL_STRAP("Hulbånd"),
    WASHER("Spændeskive"),
    UNDER_FASCIA_BOARD("Under stern"),
    OVER_FASCIA_BOARD("Over stern"),
    WATER_BOARD("Vandbrædt");

    private final String displayCategory;

    MaterialType(String displayCategory) {
        this.displayCategory = displayCategory;
    }

    public String getDisplayCategory() {
        return displayCategory;
    }

    public String getDisplayName()
    {
        return displayCategory;
    }
}
