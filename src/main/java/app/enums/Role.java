package app.enums;

public enum Role
{
    CUSTOMER("Kunde"),
    SALESREP("Sælger");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }
}
