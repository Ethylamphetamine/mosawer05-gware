/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.item.ItemConvertible
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.systems.hud.elements;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;

public class ElytraHud
extends HudElement {
    public static final HudElementInfo<ElytraHud> INFO = new HudElementInfo<Object>(Hud.GROUP, "elytra-indicator", "Displays an icon if you are wearing an Elytra.", ElytraHud::new);
    private final SettingGroup sgGeneral;
    private final Setting<Double> scale;
    private final Setting<Double> alpha;
    private final Setting<Double> fadeDuration;
    private float currentFactor;
    private long lastRenderTime;

    public ElytraHud() {
        super(INFO);
        this.sgGeneral = this.settings.getDefaultGroup();
        this.scale = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("scale")).description("The scale of the icon.")).defaultValue(2.0).min(1.0).sliderRange(1.0, 5.0).onChanged(aDouble -> this.calculateSize())).build());
        this.alpha = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("alpha")).description("The max transparency of the icon.")).defaultValue(1.0).min(0.1).sliderMax(1.0).build());
        this.fadeDuration = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fade-duration")).description("Time in seconds to fade in/out.")).defaultValue(1.0).min(0.0).max(3.0).build());
        this.currentFactor = 0.0f;
        this.lastRenderTime = 0L;
        this.calculateSize();
    }

    private void calculateSize() {
        double s = this.scale.get();
        this.setSize(17.0 * s, 17.0 * s);
    }

    @Override
    public void render(HudRenderer renderer) {
        long now = System.currentTimeMillis();
        if (this.lastRenderTime == 0L) {
            this.lastRenderTime = now;
        }
        float deltaSeconds = (float)(now - this.lastRenderTime) / 1000.0f;
        this.lastRenderTime = now;
        if (MeteorClient.mc.player == null) {
            return;
        }
        ItemStack chestStack = MeteorClient.mc.player.getEquippedStack(EquipmentSlot.CHEST);
        boolean hasElytra = chestStack.getItem() == Items.ELYTRA;
        boolean shouldDisplay = hasElytra || this.isInEditor();
        double duration = this.fadeDuration.get();
        if (duration <= 0.0) {
            this.currentFactor = shouldDisplay ? 1.0f : 0.0f;
        } else {
            float change = deltaSeconds / (float)duration;
            this.currentFactor = shouldDisplay ? (this.currentFactor += change) : (this.currentFactor -= change);
        }
        this.currentFactor = MathHelper.clamp((float)this.currentFactor, (float)0.0f, (float)1.0f);
        if (this.currentFactor <= 0.0f) {
            return;
        }
        ItemStack stackToRender = hasElytra ? chestStack : new ItemStack((ItemConvertible)Items.ELYTRA);
        RenderSystem.enableBlend();
        float finalAlpha = this.alpha.get().floatValue() * this.currentFactor;
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)finalAlpha);
        renderer.item(stackToRender, this.x, this.y, this.scale.get().floatValue(), false);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
    }
}

