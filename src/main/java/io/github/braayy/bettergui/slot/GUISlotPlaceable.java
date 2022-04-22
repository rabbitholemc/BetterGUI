package io.github.braayy.bettergui.slot;

import io.github.braayy.bettergui.handler.GUISlotPlaceableClickHandler;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class GUISlotPlaceable implements GUISlot {

    @Nullable private final GUISlotPlaceableClickHandler handler;
    @Nullable private final Predicate<ItemStack> filter;

    public GUISlotPlaceable(@Nullable GUISlotPlaceableClickHandler handler) {
        this.handler = handler;
        this.filter = null;
    }

    public GUISlotPlaceable(@Nullable GUISlotPlaceableClickHandler handler, @Nullable Predicate<ItemStack> filter) {
        this.handler = handler;
        this.filter = filter;
    }

    @Nullable
    @Override
    public ItemStack getIcon() {
        return null;
    }

    @Nullable
    public GUISlotPlaceableClickHandler getHandler() {
        return this.handler;
    }

    @Nullable
    public Predicate<ItemStack> getFilter() {
        return filter;
    }
}
