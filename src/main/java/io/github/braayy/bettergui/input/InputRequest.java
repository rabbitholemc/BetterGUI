package io.github.braayy.bettergui.input;

import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record InputRequest(InputType inputType, Consumer<String> callback, Plugin plugin) implements MetadataValue {

    @Override
    public @Nullable Object value() {
        return this.callback;
    }

    @Override
    public int asInt() {
        return 0;
    }

    @Override
    public float asFloat() {
        return 0;
    }

    @Override
    public double asDouble() {
        return 0;
    }

    @Override
    public long asLong() {
        return 0;
    }

    @Override
    public short asShort() {
        return 0;
    }

    @Override
    public byte asByte() {
        return 0;
    }

    @Override
    public boolean asBoolean() {
        return false;
    }

    @Override
    public @NotNull String asString() {
        return this.inputType.getClass().getSimpleName();
    }

    @Override
    public @Nullable Plugin getOwningPlugin() {
        return this.plugin;
    }

    @Override
    public void invalidate() {}
}
