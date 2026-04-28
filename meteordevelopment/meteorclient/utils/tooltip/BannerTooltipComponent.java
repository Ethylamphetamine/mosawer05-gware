/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.tooltip.TooltipComponent
 *  net.minecraft.client.model.ModelPart
 *  net.minecraft.client.render.DiffuseLighting
 *  net.minecraft.client.render.OverlayTexture
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.VertexConsumerProvider$Immediate
 *  net.minecraft.client.render.block.entity.BannerBlockEntityRenderer
 *  net.minecraft.client.render.entity.model.EntityModelLayers
 *  net.minecraft.client.render.model.ModelLoader
 *  net.minecraft.client.util.SpriteIdentifier
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.BannerPatternsComponent
 *  net.minecraft.item.BannerItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.DyeColor
 */
package meteordevelopment.meteorclient.utils.tooltip;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DyeColor;

public class BannerTooltipComponent
implements MeteorTooltipData,
TooltipComponent {
    private final DyeColor color;
    private final BannerPatternsComponent patterns;
    private final ModelPart bannerField;

    public BannerTooltipComponent(ItemStack banner) {
        this.color = ((BannerItem)banner.getItem()).getColor();
        this.patterns = (BannerPatternsComponent)banner.getOrDefault(DataComponentTypes.BANNER_PATTERNS, (Object)BannerPatternsComponent.DEFAULT);
        this.bannerField = MeteorClient.mc.getEntityModelLoader().getModelPart(EntityModelLayers.BANNER).getChild("flag");
    }

    public BannerTooltipComponent(DyeColor color, BannerPatternsComponent patterns) {
        this.color = color;
        this.patterns = patterns;
        this.bannerField = MeteorClient.mc.getEntityModelLoader().getModelPart(EntityModelLayers.BANNER).getChild("flag");
    }

    @Override
    public TooltipComponent getComponent() {
        return this;
    }

    public int getHeight() {
        return 158;
    }

    public int getWidth(TextRenderer textRenderer) {
        return 80;
    }

    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        DiffuseLighting.disableGuiDepthLighting();
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate((float)(x + 8), (float)(y + 8), 0.0f);
        matrices.push();
        matrices.translate(0.5, 16.0, 0.0);
        matrices.scale(6.0f, -6.0f, 1.0f);
        matrices.scale(2.0f, -2.0f, -2.0f);
        matrices.push();
        matrices.translate(2.5, 8.5, 0.0);
        matrices.scale(5.0f, 5.0f, 5.0f);
        VertexConsumerProvider.Immediate immediate = MeteorClient.mc.getBufferBuilders().getEntityVertexConsumers();
        this.bannerField.pitch = 0.0f;
        this.bannerField.pivotY = -32.0f;
        BannerBlockEntityRenderer.renderCanvas((MatrixStack)matrices, (VertexConsumerProvider)immediate, (int)0xF000F0, (int)OverlayTexture.DEFAULT_UV, (ModelPart)this.bannerField, (SpriteIdentifier)ModelLoader.BANNER_BASE, (boolean)true, (DyeColor)this.color, (BannerPatternsComponent)this.patterns);
        matrices.pop();
        matrices.pop();
        immediate.draw();
        matrices.pop();
        DiffuseLighting.enableGuiDepthLighting();
    }
}

