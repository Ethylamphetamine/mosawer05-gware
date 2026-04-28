/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.tooltip.TooltipComponent
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.item.ItemStack
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.utils.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ContainerTooltipComponent
implements TooltipComponent,
MeteorTooltipData {
    private static final Identifier TEXTURE_CONTAINER_BACKGROUND = MeteorClient.identifier("textures/container.png");
    private final ItemStack[] items;
    private final Color color;

    public ContainerTooltipComponent(ItemStack[] items, Color color) {
        this.items = items;
        this.color = color;
    }

    @Override
    public TooltipComponent getComponent() {
        return this;
    }

    public int getHeight() {
        return 67;
    }

    public int getWidth(TextRenderer textRenderer) {
        return 176;
    }

    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor((float)((float)this.color.r / 255.0f), (float)((float)this.color.g / 255.0f), (float)((float)this.color.b / 255.0f), (float)((float)this.color.a / 255.0f));
        context.drawTexture(TEXTURE_CONTAINER_BACKGROUND, x, y, 0, 0.0f, 0.0f, 176, 67, 176, 67);
        RenderSystem.setShaderColor((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        int row = 0;
        int i = 0;
        for (ItemStack itemStack : this.items) {
            RenderUtils.drawItem(context, itemStack, x + 8 + i * 18, y + 7 + row * 18, 1.0f, true);
            if (++i < 9) continue;
            i = 0;
            ++row;
        }
    }
}

