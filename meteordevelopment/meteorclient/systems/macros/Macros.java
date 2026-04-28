/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  org.jetbrains.annotations.NotNull
 */
package meteordevelopment.meteorclient.systems.macros;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.meteor.KeyEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import meteordevelopment.meteorclient.systems.macros.Macro;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.jetbrains.annotations.NotNull;

public class Macros
extends System<Macros>
implements Iterable<Macro> {
    private List<Macro> macros = new ArrayList<Macro>();

    public Macros() {
        super("macros");
    }

    public static Macros get() {
        return Systems.get(Macros.class);
    }

    public void add(Macro macro) {
        this.macros.add(macro);
        MeteorClient.EVENT_BUS.subscribe(macro);
        this.save();
    }

    public Macro get(String name) {
        for (Macro macro : this.macros) {
            if (!macro.name.get().equalsIgnoreCase(name)) continue;
            return macro;
        }
        return null;
    }

    public List<Macro> getAll() {
        return this.macros;
    }

    public void remove(Macro macro) {
        if (this.macros.remove(macro)) {
            MeteorClient.EVENT_BUS.unsubscribe(macro);
            this.save();
        }
    }

    @EventHandler(priority=100)
    private void onKey(KeyEvent event) {
        if (event.action == KeyAction.Release) {
            return;
        }
        for (Macro macro : this.macros) {
            if (!macro.onAction(true, event.key, event.modifiers)) continue;
            return;
        }
    }

    @EventHandler(priority=100)
    private void onButton(MouseButtonEvent event) {
        if (event.action == KeyAction.Release) {
            return;
        }
        for (Macro macro : this.macros) {
            if (!macro.onAction(false, event.button, 0)) continue;
            return;
        }
    }

    public boolean isEmpty() {
        return this.macros.isEmpty();
    }

    @Override
    @NotNull
    public Iterator<Macro> iterator() {
        return this.macros.iterator();
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.put("macros", (NbtElement)NbtUtils.listToTag(this.macros));
        return tag;
    }

    @Override
    public Macros fromTag(NbtCompound tag) {
        for (Macro macro : this.macros) {
            MeteorClient.EVENT_BUS.unsubscribe(macro);
        }
        this.macros = NbtUtils.listFromTag(tag.getList("macros", 10), Macro::new);
        for (Macro macro : this.macros) {
            MeteorClient.EVENT_BUS.subscribe(macro);
        }
        return this;
    }
}

