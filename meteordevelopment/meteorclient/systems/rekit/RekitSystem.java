/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 *  net.minecraft.nbt.NbtList
 *  net.minecraft.registry.Registries
 */
package meteordevelopment.meteorclient.systems.rekit;

import java.util.HashMap;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.System;
import meteordevelopment.meteorclient.systems.Systems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;

public class RekitSystem
extends System<RekitSystem> {
    private final Map<Integer, Kit> kits = new HashMap<Integer, Kit>();

    public RekitSystem() {
        super("rekit-system");
    }

    public static RekitSystem get() {
        return Systems.get(RekitSystem.class);
    }

    public void saveKit(int id) {
        if (MeteorClient.mc.player == null) {
            return;
        }
        Kit k = new Kit();
        for (int slot = 0; slot < 36; ++slot) {
            String base;
            ItemStack stack = MeteorClient.mc.player.getInventory().getStack(slot);
            if (stack.isEmpty()) continue;
            SavedStack s = new SavedStack();
            Item item = stack.getItem();
            s.itemId = Registries.ITEM.getId((Object)item).toString();
            String display = stack.getName().getString();
            s.customName = !display.equals(base = stack.getItem().getName().getString()) ? display : "";
            s.count = stack.getCount();
            k.slots.put(slot, s);
        }
        this.kits.put(id, k);
        Systems.save();
    }

    public Kit getKit(int id) {
        return this.kits.get(id);
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        NbtList list = new NbtList();
        for (Map.Entry<Integer, Kit> e : this.kits.entrySet()) {
            NbtCompound k = new NbtCompound();
            k.putInt("id", e.getKey().intValue());
            k.put("kit", (NbtElement)e.getValue().toTag());
            list.add((Object)k);
        }
        tag.put("kits", (NbtElement)list);
        return tag;
    }

    @Override
    public RekitSystem fromTag(NbtCompound tag) {
        this.kits.clear();
        if (tag.contains("kits")) {
            NbtList list = tag.getList("kits", 10);
            for (NbtElement el : list) {
                NbtCompound ce = (NbtCompound)el;
                int id = ce.getInt("id");
                Kit k = Kit.fromTag(ce.getCompound("kit"));
                this.kits.put(id, k);
            }
        }
        return this;
    }

    public static class Kit {
        public final Map<Integer, SavedStack> slots = new HashMap<Integer, SavedStack>();

        public NbtCompound toTag() {
            NbtCompound k = new NbtCompound();
            NbtList list = new NbtList();
            for (Map.Entry<Integer, SavedStack> e : this.slots.entrySet()) {
                NbtCompound entry = new NbtCompound();
                entry.putInt("slot", e.getKey().intValue());
                entry.put("stack", (NbtElement)e.getValue().toTag());
                list.add((Object)entry);
            }
            k.put("slots", (NbtElement)list);
            return k;
        }

        public static Kit fromTag(NbtCompound n) {
            Kit k = new Kit();
            NbtList list = n.getList("slots", 10);
            for (NbtElement el : list) {
                NbtCompound ce = (NbtCompound)el;
                int slot = ce.getInt("slot");
                SavedStack s = SavedStack.fromTag(ce.getCompound("stack"));
                k.slots.put(slot, s);
            }
            return k;
        }
    }

    public static class SavedStack {
        public String itemId;
        public String customName;
        public int count;

        public NbtCompound toTag() {
            NbtCompound n = new NbtCompound();
            n.putString("item", this.itemId);
            if (this.customName != null && !this.customName.isEmpty()) {
                n.putString("name", this.customName);
            }
            n.putInt("count", this.count);
            return n;
        }

        public static SavedStack fromTag(NbtCompound n) {
            SavedStack s = new SavedStack();
            s.itemId = n.getString("item");
            s.customName = n.contains("name") ? n.getString("name") : "";
            s.count = n.getInt("count");
            return s;
        }
    }
}

