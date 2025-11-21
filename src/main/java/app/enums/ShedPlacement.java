package app.enums;

public enum ShedPlacement
{
    FULL_WIDTH("Fuld bredde"),
    LEFT("Venstre"),
    RIGHT("Højre");

    private final String displayName;

    ShedPlacement(String displayName) {
        this.displayName = displayName;
    }
}
