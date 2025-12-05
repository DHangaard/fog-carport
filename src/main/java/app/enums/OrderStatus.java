package app.enums;

public enum OrderStatus
{
    PENDING("Afventer godkendelse"),
    READY("Klar"),
    ACCEPTED("Accepteret"),
    REJECTED("Afvist"),
    EXPIRED("Udløbet"),
    PAID("BETALT"), // Intentionally capitalized
    CANCELLED("ANNULLERET"); // Intentionally capitalized


    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
