/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class ArmorHud
extends HudElement {
    public static final HudElementInfo<ArmorHud> INFO = new HudElementInfo<ArmorHud>(Hud.GROUP, "armor", "Displays your armor.", ArmorHud::new);
    private final SettingGroup sgGeneral;
    private final SettingGroup sgDurability;
    private final SettingGroup sgBackground;
    private final Setting<Orientation> orientation;
    private final Setting<Boolean> flipOrder;
    private final Setting<Double> scale;
    private final Setting<Integer> border;
    private final Setting<Durability> durability;
    private final Setting<SettingColor> durabilityColor;
    private final Setting<Boolean> durabilityShadow;
    private final Setting<Boolean> background;
    private final Setting<SettingColor> backgroundColor;

    public ArmorHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgDurability = this.settings.createGroup("Durability");
        this.sgBackground = this.settings.createGroup("Background");
        this.orientation = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("orientation")).description("How to display armor.")).defaultValue(Orientation.Horizontal)).onChanged(val -> this.calculateSize())).build());
        this.flipOrder = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("flip-order")).description("Flips the order of armor items.")).defaultValue(true)).build());
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale.")).defaultValue(2.0).onChanged(aDouble -> this.calculateSize())).min(1.0).sliderRange(1.0, 5.0).build());
        this.border = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("border")).description("How much space to add around the element.")).defaultValue(0)).onChanged(integer -> this.calculateSize())).build());
        this.durability = this.sgDurability.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("durability")).description("How to display armor durability.")).defaultValue(Durability.Bar)).onChanged(durability1 -> this.calculateSize())).build());
        this.durabilityColor = this.sgDurability.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("durability-color")).description("Color of the text.")).visible(() -> this.durability.get() == Durability.Total || this.durability.get() == Durability.Percentage)).defaultValue(new SettingColor()).build());
        this.durabilityShadow = this.sgDurability.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("durability-shadow")).description("Text shadow.")).visible(() -> this.durability.get() == Durability.Total || this.durability.get() == Durability.Percentage)).defaultValue(true)).build());
        this.background = this.sgBackground.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("background")).description("Displays background.")).defaultValue(false)).build());
        this.backgroundColor = this.sgBackground.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color used for the background.")).visible(this.background::get)).defaultValue(new SettingColor(25, 25, 25, 50)).build());
        this.calculateSize();
    }

    @Override
    public void setSize(double width, double height) {
        super.setSize(width + (double)(this.border.get() * 2), height + (double)(this.border.get() * 2));
    }

    private void calculateSize() {
        switch (this.orientation.get().ordinal()) {
            case 0: {
                this.setSize(16.0 * this.scale.get() * 4.0 + 8.0, 16.0 * this.scale.get());
                break;
            }
            case 1: {
                this.setSize(16.0 * this.scale.get(), 16.0 * this.scale.get() * 4.0 + 8.0);
            }
        }
    }

    @Override
    public void render(HudRenderer renderer) {
        double x = this.x;
        double y = this.y;
        int slot = this.flipOrder.get() != false ? 3 : 0;
        for (int position = 0; position < 4; ++position) {
            double armorY;
            double armorX;
            ItemStack itemStack = this.getItem(slot);
            if (this.orientation.get() == Orientation.Vertical) {
                armorX = x;
                armorY = y + (double)(position * 18) * this.scale.get();
            } else {
                armorX = x + (double)(position * 18) * this.scale.get();
                armorY = y;
            }
            renderer.item(itemStack, (int)armorX, (int)armorY, this.scale.get().floatValue(), itemStack.isDamageable() && this.durability.get() == Durability.Bar);
            if (itemStack.isDamageable() && !this.isInEditor() && this.durability.get() != Durability.Bar && this.durability.get() != Durability.None) {
                String message = switch (this.durability.get().ordinal()) {
                    case 2 -> Integer.toString(itemStack.getMaxDamage() - itemStack.getDamage());
                    case 3 -> Integer.toString(Math.round((float)(itemStack.getMaxDamage() - itemStack.getDamage()) * 100.0f / (float)itemStack.getMaxDamage()));
                    default -> "err";
                };
                double messageWidth = renderer.textWidth(message);
                if (this.orientation.get() == Orientation.Vertical) {
                    armorX = x + 8.0 * this.scale.get() - messageWidth / 2.0;
                    armorY = y + (double)(18 * position) * this.scale.get() + (18.0 * this.scale.get() - renderer.textHeight());
                } else {
                    armorX = x + (double)(18 * position) * this.scale.get() + 8.0 * this.scale.get() - messageWidth / 2.0;
                    armorY = y + ((double)this.getHeight() - renderer.textHeight());
                }
                renderer.text(message, armorX, armorY, this.durabilityColor.get(), this.durabilityShadow.get());
            }
            if (this.flipOrder.get().booleanValue()) {
                --slot;
                continue;
            }
            ++slot;
        }
        if (this.background.get().booleanValue()) {
            renderer.quad(this.x, this.y, this.getWidth(), this.getHeight(), this.backgroundColor.get());
        }
    }

    private ItemStack getItem(int i) {
        if (this.isInEditor()) {
            return switch (i) {
                default -> Items.NETHERITE_BOOTS.getDefaultStack();
                case 1 -> Items.NETHERITE_LEGGINGS.getDefaultStack();
                case 2 -> Items.NETHERITE_CHESTPLATE.getDefaultStack();
                case 3 -> Items.NETHERITE_HELMET.getDefaultStack();
            };
        }
        return MeteorClient.mc.player.getInventory().getArmorStack(i);
    }

    public static enum Orientation {
        Horizontal,
        Vertical;

    }

    public static enum Durability {
        None,
        Bar,
        Total,
        Percentage;

    }
}

