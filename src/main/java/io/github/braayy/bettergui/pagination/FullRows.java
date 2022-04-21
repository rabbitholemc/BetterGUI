package io.github.braayy.bettergui.pagination;

public enum FullRows implements GUISlotsPerPage {
    ONE_ROW(9),
    TWO_ROWS(18),
    THREE_ROWS(27),
    FOUR_ROWS(36);

    private final int value;

    FullRows(int value) {
        this.value = value;
    }

    @Override
    public int getSlotsPerPage() {
        return this.value;
    }

    @Override
    public int mapSequenceToSlotIndex(int sequence) {
        return 9 + sequence;
    }
}
