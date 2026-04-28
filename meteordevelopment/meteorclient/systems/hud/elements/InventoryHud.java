/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

public class InventoryHud
extends HudElement {
    public static final HudElementInfo<InventoryHud> INFO = new HudElementInfo<InventoryHud>(Hud.GROUP, "inventory", "Displays your inventory.", InventoryHud::new);
    private static final Identifier TEXTURE = MeteorClient.identifier("textures/container.png");
    private static final Identifier TEXTURE_TRANSPARENT = MeteorClient.identifier("textures/container-transparent.png");
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> containers;
    private final Setting<Double> scale;
    private final Setting<Background> background;
    private final Setting<SettingColor> color;
    private final ItemStack[] containerItems;

    private InventoryHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.containers = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("containers")).description("Shows the contents of a container when holding them.")).defaultValue(false)).build());
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).onChanged(aDouble -> this.calculateSize())).build());
        this.background = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("background")).description("Background of inventory viewer.")).defaultValue(Background.Texture)).onChanged(bg -> this.calculateSize())).build());
        this.color = this.sgGeneral.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("background-color")).description("Color of the background.")).defaultValue(new SettingColor(255, 255, 255)).visible(() -> this.background.get() != Background.None)).build());
        this.containerItems = new ItemStack[27];
        this.calculateSize();
    }

    @Override
    public void render(HudRenderer renderer) {
        Color drawColor;
        boolean hasContainer;
        double x = this.x;
        double y = this.y;
        ItemStack container = this.getContainer();
        boolean bl = hasContainer = this.containers.get() != false && container != null;
        if (hasContainer) {
            Utils.getItemsInContainerItem(container, this.containerItems);
        }
        Color color = drawColor = hasContainer ? Utils.getShulkerColor(container) : (Color)this.color.get();
        if (this.background.get() != Background.None) {
            this.drawBackground(renderer, (int)x, (int)y, drawColor);
        }
        if (MeteorClient.mc.player == null) {
            return;
        }
        renderer.post(() -> {
            for (int row = 0; row < 3; ++row) {
                for (int i = 0; i < 9; ++i) {
                    ItemStack stack;
                    int index = row * 9 + i;
                    ItemStack itemStack = stack = hasContainer ? this.containerItems[index] : MeteorClient.mc.player.getInventory().getStack(index + 9);
                    if (stack == null) continue;
                    int itemX = this.background.get() == Background.Texture ? (int)(x + (double)(8 + i * 18) * this.scale.get()) : (int)(x + (double)(1 + i * 18) * this.scale.get());
                    int itemY = this.background.get() == Background.Texture ? (int)(y + (double)(7 + row * 18) * this.scale.get()) : (int)(y + (double)(1 + row * 18) * this.scale.get());
                    renderer.item(stack, itemX, itemY, this.scale.get().floatValue(), true);
                }
            }
        });
    }

    private void calculateSize() {
        this.setSize((double)this.background.get().width * this.scale.get(), (double)this.background.get().height * this.scale.get());
    }

    private void drawBackground(HudRenderer renderer, int x, int y, Color color) {
        int w = this.getWidth();
        int h = this.getHeight();
        switch (this.background.get().ordinal()) {
            case 1: 
            case 2: {
                renderer.texture(this.background.get() == Background.Texture ? TEXTURE : TEXTURE_TRANSPARENT, x, y, w, h, color);
                break;
            }
            case 3: {
                renderer.quad(x, y, w, h, color);
            }
        }
    }

    private ItemStack getContainer() {
        if (this.isInEditor() || MeteorClient.mc.player == null) {
            return null;
        }
        ItemStack stack = MeteorClient.mc.player.getOffHandStack();
        if (Utils.hasItems(stack) || stack.getItem() == Items.ENDER_CHEST) {
            return stack;
        }
        stack = MeteorClient.mc.player.getMainHandStack();
        if (Utils.hasItems(stack) || stack.getItem() == Items.ENDER_CHEST) {
            return stack;
        }
        return null;
    }

    public static enum Background {
        None(162, 54),
        Texture(176, 67),
        Outline(162, 54),
        Flat(162, 54);

        private final int width;
        private final int height;

        private Background(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}

