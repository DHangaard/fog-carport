package app.enums;

public enum MaterialType
{
    POST("Stolpe"),
    RAFTER("Spær"),
    BEAM("Rem"),
    ROOF("Tag"),
    BOARD("Bræt"),
    FASTENER("Skrue"),
    FITTING("Beslag");

    private final String displayCategory;

    MaterialType(String displayCategory) {
        this.displayCategory = displayCategory;
    }

    public String getDisplayCategory() {
        return displayCategory;
    }
}
