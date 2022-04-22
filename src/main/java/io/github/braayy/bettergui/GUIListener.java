package io.github.braayy.bettergui;

import io.github.braayy.bettergui.handler.GUISlotClickHandler;
import io.github.braayy.bettergui.handler.GUISlotPlaceableClickHandler;
import io.github.braayy.bettergui.slot.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.Predicate;

public class GUIListener implements Listener {

    private static boolean registered = false;

    private GUIListener() {}

    public static void register(JavaPlugin plugin) {
        if (registered) {
            plugin.getLogger().warning("Tried to register GUIListener twice!");
            return;
        }

        Bukkit.getPluginManager().registerEvents(new GUIListener(), plugin);
        registered = true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof GUI gui)) return;

        if (gui.updating || gui.backing) return;

        int slotIndex = event.getSlot();

        boolean clickedBottomInventory = event.getRawSlot() >= event.getView().getTopInventory().getSize();

        if (clickedBottomInventory && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            slotIndex = event.getView().getTopInventory().firstEmpty();
        } else if (clickedBottomInventory) {
            return;
        }

        GUISlot slot = gui.slotMap.get(slotIndex);
        if (slot == null) return;

        if (slot instanceof GUISlotDisplay) {
            event.setCancelled(true);
        } else if (slot instanceof GUISlotButton) {
            event.setCancelled(true);
            ((GUISlotButton) slot).getHandler().handle(event);
        } else if (slot instanceof GUISlotCapturable) {
            GUISlotClickHandler handler = ((GUISlotCapturable) slot).getHandler();
            if (handler != null)
                handler.handle(event);
        } else if (slot instanceof GUISlotPlaceable placeable) {
            ItemStack item = switch (event.getAction()) {
                case PLACE_ONE, PLACE_SOME, PLACE_ALL -> event.getCursor();
                case MOVE_TO_OTHER_INVENTORY -> event.getView().getBottomInventory().getItem(event.getSlot());
                default -> null;
            };

            if (item == null) return;

            Predicate<ItemStack> filter = placeable.getFilter();
            if (filter != null) {
                if (!filter.test(item)) {
                    event.setCancelled(true);
                    return;
                }
            }

            GUISlotPlaceableClickHandler handler = placeable.getHandler();
            if (handler != null)
                handler.handle(item);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof GUI gui)) return;

        if (gui.updating) {
            gui.updating = false;
        } else if (gui.backing) {
            gui.backing = false;
            gui.onClose();
        } else {
            GUI next = gui;

            while (next != null) {
                next.onClose();
                next = next.getParent();
            }
        }
    }

}
