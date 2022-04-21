package io.github.braayy.bettergui.pagination;

public class FixedSlotIndexes implements GUISlotsPerPage {

    private final int[] slotIndexes;

    public FixedSlotIndexes(int... slotIndexes) {
        this.slotIndexes = slotIndexes;
    }

    @Override
    public int getSlotsPerPage() {
        return this.slotIndexes.length;
    }

    @Override
    public int mapSequenceToSlotIndex(int sequence) {
        if (sequence >= this.slotIndexes.length)
            throw new IllegalArgumentException(sequence + " is not a valid slot sequence number");

        return this.slotIndexes[sequence];
    }
}
