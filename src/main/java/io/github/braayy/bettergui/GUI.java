package io.github.braayy.bettergui;

import io.github.braayy.bettergui.input.InputResult;
import io.github.braayy.bettergui.handler.GUISlotCleanClickHandler;
import io.github.braayy.bettergui.handler.GUISlotPlaceableCleanClickHandler;
import io.github.braayy.bettergui.handler.GUISlotPlaceableClickHandler;
import io.github.braayy.bettergui.input.InputRequest;
import io.github.braayy.bettergui.input.InputType;
import io.github.braayy.bettergui.slot.*;
import io.github.braayy.bettergui.handler.GUISlotClickHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class GUI implements InventoryHolder {
    public static final String INPUT_KEY = "BetterGUI:Input";

    final Map<Integer, GUISlot> slotMap = new HashMap<>();

    private String title;
    private GUISize size;
    private GUI parent;
    protected ExecutorService executorService;

    protected Player player;

    private Inventory openInventory;
    boolean updating, backing, waitingInput;

    public GUI() {}

    public GUI(ExecutorService executorService) {
        this.executorService = executorService;
    }

    protected abstract void setup();

    protected void onClose() {}

    protected void postSetup() {}

    protected void setTitle(@NotNull String title) {
        Objects.requireNonNull(title, "title cannot be null");

        this.title = title;
    }

    @Nullable
    protected String getTitle() {
        return this.title;
    }

    protected void setSize(@NotNull GUISize size) {
        Objects.requireNonNull(size, "size cannot be null");

        this.size = size;
    }

    protected GUISize getSize() {
        return this.size;
    }

    @Nullable
    protected GUI getParent() {
        return parent;
    }

    @Nullable
    protected GUISlot getSlot(int index) {
        return this.slotMap.get(index);
    }

    protected void becomeSync() {
        this.executorService = null;
    }

    // region Slots
    protected void addSlot(int slotNumber, GUISlot slot) {
        this.slotMap.put(slotNumber, slot);
    }

    protected void addButton(int slotNumber, @Nullable ItemStack icon, @NotNull GUISlotClickHandler handler) {
        Objects.requireNonNull(handler, "handler should not be null");
        addSlot(slotNumber, new GUISlotButton(icon, handler));
    }

    protected void addButton(int slotNumber, @Nullable ItemStack icon, @NotNull GUISlotCleanClickHandler handler) {
        Objects.requireNonNull(handler, "handler should not be null");
        addSlot(slotNumber, new GUISlotButton(icon, handler));
    }

    protected void addCapturable(int slotNumber, @NotNull ItemStack icon, @Nullable GUISlotClickHandler handler) {
        Objects.requireNonNull(icon, "icon should not be null");
        addSlot(slotNumber, new GUISlotCapturable(icon, handler));
    }

    protected void addCapturable(int slotNumber, @NotNull ItemStack icon, @Nullable GUISlotCleanClickHandler handler) {
        Objects.requireNonNull(icon, "icon should not be null");
        addSlot(slotNumber, new GUISlotCapturable(icon, handler));
    }

    protected void addCapturable(int slotNumber, @NotNull ItemStack icon) {
        Objects.requireNonNull(icon, "icon should not be null");
        addSlot(slotNumber, new GUISlotCapturable(icon, null));
    }

    protected void addDisplay(int slotNumber, @Nullable ItemStack item) {
        addSlot(slotNumber, new GUISlotDisplay(item));
    }

    protected void addPlaceable(int slotNumber, @Nullable GUISlotPlaceableClickHandler handler) {
        addSlot(slotNumber, new GUISlotPlaceable(handler));
    }

    protected void addPlaceable(int slotNumber, @Nullable GUISlotPlaceableCleanClickHandler handler) {
        addSlot(slotNumber, new GUISlotPlaceable(handler));
    }

    protected void addPlaceable(int slotNumber, @Nullable GUISlotPlaceableClickHandler handler, @Nullable Predicate<ItemStack> filter) {
        addSlot(slotNumber, new GUISlotPlaceable(handler, filter));
    }

    protected void addPlaceable(int slotNumber, @Nullable GUISlotPlaceableCleanClickHandler handler, @Nullable Predicate<ItemStack> filter) {
        addSlot(slotNumber, new GUISlotPlaceable(handler, filter));
    }

    protected void addPlaceable(int slotNumber) {
        addSlot(slotNumber, new GUISlotPlaceable(null));
    }
    // endregion

    // region Controls
    public void open(@NotNull Player player) {
        open(player, null);
    }

    public void open(@NotNull Player player, @Nullable GUI parent) {
        if (this.player != null)
            throw new IllegalStateException("Opening inventory for more players!");

        Objects.requireNonNull(player, "player cannot be null");

        if (parent != null) {
            this.parent = parent;
            this.parent.player = null;
            this.parent.updating = true;
        }

        this.player = player;

        if (this.executorService == null) {
            setup();
            if (this instanceof PaginatedGUI)
                ((PaginatedGUI) this).pageSetup();
            if (this instanceof StateGUI<?, ?>)
                ((StateGUI<?, ?>) this).stateSetup();
            postSetup();

            this.updating = true;
            Inventory inventory = getInventory();
            this.updating = false;
            this.player.openInventory(inventory);

            return;
        }

        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(GUI.class);
        CompletableFuture.runAsync(this::setup, this.executorService).thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
            if (this instanceof PaginatedGUI)
                ((PaginatedGUI) this).pageSetup();
            if (this instanceof StateGUI<?, ?>)
                ((StateGUI<?, ?>) this).stateSetup();
            postSetup();

            this.updating = true;
            Inventory inventory = getInventory();
            this.updating = false;
            this.player.openInventory(inventory);
        }));
    }

    public void simpleUpdate() {
        update(true, false);
    }

    public void fullUpdate() {
        update(false, false);
    }

    private void update(boolean simple, boolean internal) {
        Objects.requireNonNull(this.player, "GUI not open for anyone to update");

        this.updating = true;

        if (simple)
            this.openInventory.clear();
        else
            this.openInventory = null;

        this.slotMap.clear();

        if (this.executorService == null) {
            setup();
            if (this instanceof PaginatedGUI)
                ((PaginatedGUI) this).pageSetup();
            if (this instanceof StateGUI<?, ?>)
                ((StateGUI<?, ?>) this).stateSetup();
            postSetup();

            Inventory inventory = getInventory();
            if (!simple) {
                this.player.openInventory(inventory);

                if (internal) {
                    this.updating = false;
                }
            } else {
                this.player.updateInventory();
                this.updating = false;
            }

            return;
        }

        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(GUI.class);
        CompletableFuture.runAsync(this::setup, this.executorService).thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
            if (this instanceof PaginatedGUI)
                ((PaginatedGUI) this).pageSetup();
            if (this instanceof StateGUI<?, ?>)
                ((StateGUI<?, ?>) this).stateSetup();
            postSetup();

            Inventory inventory = getInventory();
            if (!simple) {
                this.player.openInventory(inventory);

                if (internal) {
                    this.updating = false;
                }
            } else {
                this.player.updateInventory();
                this.updating = false;
            }
        }));
    }

    public void back() {
        Objects.requireNonNull(this.parent, "GUI have no parent");
        Objects.requireNonNull(this.player, "GUI not open for anyone to back");

        this.backing = true;
        this.parent.open(player);
    }

    public void close() {
        Objects.requireNonNull(this.player, "GUI not open for anyone to close");

        this.player.closeInventory();
        this.player = null;
    }

    protected void requestInput(InputType inputType, Function<String, InputResult> handler, Duration timeout) {
        Objects.requireNonNull(this.player, "GUI not open for anyone to get input from");

        JavaPlugin plugin = JavaPlugin.getProvidingPlugin(GUI.class);
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (this.waitingInput) {
                this.waitingInput = false;
                this.player.removeMetadata(INPUT_KEY, plugin);
                this.update(false, true);
            }
        }, timeout.toSeconds() * 20L);

        Consumer<String> internalCallback = (input) -> {
            InputResult result = handler.apply(input);

            if (result == InputResult.VALID) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    this.waitingInput = false;
                    task.cancel();
                    this.player.removeMetadata(INPUT_KEY, plugin);
                    this.update(false, true);
                });
            } else if (result == InputResult.DO_NOT_OPEN_BACK) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    this.waitingInput = false;
                    task.cancel();
                    this.player.removeMetadata(INPUT_KEY, plugin);
                    this.close();
                });
            }
        };

        this.waitingInput = true;
        this.player.setMetadata(INPUT_KEY, new InputRequest(inputType, internalCallback, plugin));
        this.player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
    }
    // endregion

    @NotNull
    @Override
    public Inventory getInventory() {
        if (this.openInventory == null) {
            Objects.requireNonNull(this.title, "title cannot be null");

            this.openInventory = Bukkit.createInventory(this, this.size.value, title);
        }

        if (this.updating) {
            for (Map.Entry<Integer, GUISlot> entry : this.slotMap.entrySet()) {
                this.openInventory.setItem(entry.getKey(), entry.getValue().getIcon());
            }
        }

        return this.openInventory;
    }
}
