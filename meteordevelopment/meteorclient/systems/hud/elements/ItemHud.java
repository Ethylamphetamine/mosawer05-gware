/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ItemHud
extends HudElement {
    public static final HudElementInfo<ItemHud> INFO = new HudElementInfo<ItemHud>(Hud.GROUP, "item", "Displays the item count.", ItemHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgBackground;
    private final Setting<Item> item;
    private final Setting<NoneMode> noneMode;
    private final Setting<Double> scale;
    private final Setting<Integer> border;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;

    private ItemHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgBackground = this.settings.createGroup("Background");
        this.item = this.sgGeneral.add(((ItemSetting.Builder)((ItemSetting.Builder)((ItemSetting.Builder)new ItemSetting.Builder().name("item")).description("Item to display")).defaultValue(Items.TOTEM_OF_UNDYING)).build());
        this.noneMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("none-mode")).description("How to render the item when you don't have the specified item in your inventory.")).defaultValue(NoneMode.HideCount)).build());
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("Scale of the item.")).defaultValue(2.0).onChanged(aDouble -> this.calculateSize())).min(1.0).sliderRange(1.0, 4.0).build());
        this.border = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("border")).description("How much space to add around the element.")).defaultValue(0)).onChanged(integer -> this.calculateSize())).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.calculateSize();
    }

    @Override
    public void setSize(double width, double height) {
        super.setSize(width + (double)(this.border.get() * 2), height + (double)(this.border.get() * 2));
    }

    private void calculateSize() {
        this.setSize(17.0 * this.scale.get(), 17.0 * this.scale.get());
    }

    @Override
    public void render(HudRenderer renderer) {
        ItemStack itemStack = new ItemStack((ItemConvertible)this.item.get(), InvUtils.find(this.item.get()).count());
        if (this.noneMode.get() == NoneMode.HideItem && itemStack.isEmpty()) {
            if (this.isInEditor()) {
                renderer.line(this.x, this.y, this.x + this.getWidth(), this.y + this.getHeight(), Color.GRAY);
                renderer.line(this.x, this.y + this.getHeight(), this.x + this.getWidth(), this.y, Color.GRAY);
            }
        } else {
            renderer.post(() -> {
                double x = this.x + this.border.get();
                double y = this.y + this.border.get();
                this.render(renderer, itemStack, (int)x, (int)y);
            });
        }
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        }
    }

    private void render(HudRenderer renderer, ItemStack itemStack, int x, int y) {
        if (this.noneMode.get() == NoneMode.HideItem) {
            renderer.item(itemStack, x, y, this.scale.get().floatValue(), true);
            return;
        }
        String countOverride = null;
        boolean resetToZero = false;
        if (itemStack.isEmpty()) {
            if (this.noneMode.get() == NoneMode.ShowCount) {
                countOverride = "0";
            }
            itemStack.setCount(1);
            resetToZero = true;
        }
        renderer.item(itemStack, x, y, this.scale.get().floatValue(), true, countOverride);
        if (resetToZero) {
            itemStack.setCount(0);
        }
    }

    public static enum NoneMode {
        HideItem,
        HideCount,
        ShowCount;


        public String toString() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> "Hide Item";
                case 1 -> "Hide Count";
                case 2 -> "Show Count";
            };
        }
    }
}

