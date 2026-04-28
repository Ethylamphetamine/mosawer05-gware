/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 */
package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class FastUse
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<List<Item>> items;
    private final Setting<Boolean> blocks;
    private final Setting<Integer> cooldown;

    public FastUse() {
        super(Categories.Player, "fast-use", "Allows you to use items at very high speeds.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Which items to fast use.")).defaultValue(Mode.All)).build());
        this.items = this.sgGeneral.add(((ItemListSetting.Builder)((ItemListSetting.Builder)((ItemListSetting.Builder)new ItemListSetting.Builder().name("items")).description("Which items should fast place work on in \"Some\" mode.")).visible(() -> this.mode.get() == Mode.Some)).build());
        this.blocks = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("blocks")).description("Fast-places blocks if the mode is \"Some\" mode.")).visible(() -> this.mode.get() == Mode.Some)).defaultValue(false)).build());
        this.cooldown = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("cooldown")).description("Fast-use cooldown in ticks.")).defaultValue(0)).min(0).sliderMax(4).build());
    }

    public int getItemUseCooldown(ItemStack itemStack) {
        if (this.mode.get() == Mode.All || this.shouldWorkSome(itemStack)) {
            return this.cooldown.get();
        }
        return 4;
    }

    private boolean shouldWorkSome(ItemStack itemStack) {
        return this.blocks.get() != false && itemStack.getItem() instanceof BlockItem || this.items.get().contains(itemStack.getItem());
    }

    public static enum Mode {
        All,
        Some;

    }
}

