package app.enums;

public enum OfferStatus
{
    PENDING("Afventer"),
    ACCEPTED("Accepteret"),
    REJECTED("Afvist"),
    EXPIRED("Udløbet");

    private final String displayName;

    OfferStatus(String displayName) {
        this.displayName = displayName;
    }
}
