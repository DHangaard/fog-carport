package app.enums;

public enum OrderStatus
{
    PAID("Betalt"),
    CANCELLED("Annulleret");

    private final String displayCategory;

    OrderStatus(String displayCategory)
    {
        this.displayCategory = displayCategory;
    }

    public String getDisplayCategory()
    {
        return displayCategory;
    }
}
