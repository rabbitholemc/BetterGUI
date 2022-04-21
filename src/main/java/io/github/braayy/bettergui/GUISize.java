package io.github.braayy.bettergui;

public enum GUISize {
    @Deprecated ONE_LINE(9),
    @Deprecated TWO_LINES(18),
    @Deprecated THREE_LINES(27),
    @Deprecated FOUR_LINES(36),
    @Deprecated FIVE_LINES(45),
    @Deprecated SIX_LINES(54),

    ONE_ROW(9),
    TWO_ROWS(18),
    THREE_ROWS(27),
    FOUR_ROWS(36),
    FIVE_ROWS(45),
    SIX_ROWS(54);

    public final int value;

    GUISize(int value) {
        this.value = value;
    }
}
