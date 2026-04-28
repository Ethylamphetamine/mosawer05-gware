/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 */
package meteordevelopment.meteorclient.settings;

import java.util.function.Consumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.gui.widgets.WKeybind;
import meteordevelopment.meteorclient.settings.IVisible;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public class KeybindSetting
extends Setting<Keybind> {
    private final Runnable action;
    public WKeybind widget;

    public KeybindSetting(String name, String description, Keybind defaultValue, Consumer<Keybind> onChanged, Consumer<Setting<Keybind>> onModuleActivated, IVisible visible, Runnable action) {
        super(name, description, defaultValue, onChanged, onModuleActivated, visible);
        this.action = action;
        MeteorClient.EVENT_BUS.subscribe(this);
    }

    @EventHandler(priority=200)
    private void onKeyBinding(KeyEvent event) {
        if (this.widget == null) {
            return;
        }
        if (event.action == KeyAction.Press && event.key == 256 && this.widget.onClear()) {
            event.cancel();
        } else if (event.action == KeyAction.Release && this.widget.onAction(true, event.key, event.modifiers)) {
            event.cancel();
        }
    }

    @EventHandler(priority=200)
    private void onMouseButtonBinding(MouseButtonEvent event) {
        if (event.action == KeyAction.Press && this.widget != null && this.widget.onAction(false, event.button, 0)) {
            event.cancel();
        }
    }

    @EventHandler(priority=100)
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Release && ((Keybind)this.get()).matches(true, event.key, event.modifiers) && (this.module == null || this.module.isActive()) && this.action != null) {
            this.action.run();
        }
    }

    @EventHandler(priority=100)
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Release && ((Keybind)this.get()).matches(false, event.button, 0) && (this.module == null || this.module.isActive()) && this.action != null) {
            this.action.run();
        }
    }

    @Override
    public void resetImpl() {
        if (this.value == null) {
            this.value = ((Keybind)this.defaultValue).copy();
        } else {
            ((Keybind)this.value).set((Keybind)this.defaultValue);
        }
        if (this.widget != null) {
            this.widget.reset();
        }
    }

    @Override
    protected Keybind parseImpl(String str) {
        try {
            return Keybind.fromKey(Integer.parseInt(str.trim()));
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Override
    protected boolean isValueValid(Keybind value) {
        return true;
    }

    @Override
    public NbtCompound save(NbtCompound tag) {
        tag.put("value", (NbtElement)((Keybind)this.get()).toTag());
        return tag;
    }

    @Override
    public Keybind load(NbtCompound tag) {
        ((Keybind)this.get()).fromTag(tag.getCompound("value"));
        return (Keybind)this.get();
    }

    public static class Builder
    extends Setting.SettingBuilder<Builder, Keybind, KeybindSetting> {
        private Runnable action;

        public Builder() {
            super(Keybind.none());
        }

        public Builder action(Runnable action) {
            this.action = action;
            return this;
        }

        @Override
        public KeybindSetting build() {
            return new KeybindSetting(this.name, this.description, (Keybind)this.defaultValue, this.onChanged, this.onModuleActivated, this.visible, this.action);
        }
    }
}

