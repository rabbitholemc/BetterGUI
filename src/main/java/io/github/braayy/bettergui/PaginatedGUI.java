package io.github.braayy.bettergui;

import io.github.braayy.bettergui.pagination.GUISlotsPerPage;
import io.github.braayy.bettergui.slot.GUISlot;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public abstract class PaginatedGUI extends GUI {
    private int currentPage;
    private GUISlotsPerPage slotsPerPage;
    private List<GUISlot> slots;
    private GUISlot pageLeftoverSlot;

    public PaginatedGUI() {}

    public PaginatedGUI(ExecutorService executorService) {
        super(executorService);
    }

    protected void setSlots(List<GUISlot> slots) {
        this.slots = slots;
    }

    protected void setPageLeftoverSlot(@NotNull GUISlot pageLeftoverSlot) {
        Objects.requireNonNull(pageLeftoverSlot, "page leftover slot cannot be null");

        this.pageLeftoverSlot = pageLeftoverSlot;
    }

    protected GUISlot getPageLeftoverSlot() {
        return pageLeftoverSlot;
    }

    protected boolean isSlotsLoaded() {
        return slots != null;
    }

    protected List<GUISlot> getSlots() {
        return slots;
    }

    protected void setSlotsPerPage(@NotNull GUISlotsPerPage slotsPerPage) {
        Objects.requireNonNull(slotsPerPage, "slots per page cannot be null");

        this.slotsPerPage = slotsPerPage;
    }

    protected GUISlotsPerPage getSlotsPerPage() {
        return slotsPerPage;
    }

    protected int getCurrentPage() {
        return currentPage;
    }

    protected int getMaxPage() {
        if (slots == null || slotsPerPage == null) return 0;

        return slots.size() / slotsPerPage.getSlotsPerPage();
    }

    protected void nextPage(boolean updateTitle) {
        if (this.currentPage == getMaxPage()) return;
        this.currentPage += 1;

        if (updateTitle)
            fullUpdate();
        else
            simpleUpdate();
    }

    protected void previousPage(boolean updateTitle) {
        if (this.currentPage == 0) return;
        this.currentPage -= 1;

        if (updateTitle)
            fullUpdate();
        else
            simpleUpdate();
    }

    void pageSetup() {
        Objects.requireNonNull(slots, "Slot list was null at rendering time");

        int startIndex = currentPage * slotsPerPage.getSlotsPerPage();
        int endIndex = Math.min(currentPage * slotsPerPage.getSlotsPerPage() + slotsPerPage.getSlotsPerPage(), slots.size());
        List<GUISlot> slotsView = slots.subList(startIndex, endIndex);

        int sequence = 0;
        for (GUISlot guiSlot : slotsView) {
            addSlot(slotsPerPage.mapSequenceToSlotIndex(sequence++), guiSlot);
        }

        int leftover = slotsPerPage.getSlotsPerPage() - slotsView.size();
        if (pageLeftoverSlot != null && leftover > 0) {
            for (int i = 0; i < leftover; i++)
                addSlot(slotsPerPage.mapSequenceToSlotIndex(sequence++), pageLeftoverSlot);
        }
    }
}
