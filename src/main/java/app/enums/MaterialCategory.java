package app.enums;

public enum MaterialCategory
{
    WOOD_AND_ROOFING("Træ & Tagplader"),
    FITTINGS_AND_FASTENERS("Beslag & Skruer");

    private final String displayCategory;

    MaterialCategory(String displayCategory) {
        this.displayCategory = displayCategory;
    }

    public String getDisplayCategory() {
        return displayCategory;
    }
}
