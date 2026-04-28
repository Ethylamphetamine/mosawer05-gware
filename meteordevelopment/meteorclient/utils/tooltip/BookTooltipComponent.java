/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.tooltip.TooltipComponent
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.text.OrderedText
 *  net.minecraft.text.StringVisitable
 *  net.minecraft.text.Text
 *  net.minecraft.util.Identifier
 */
package meteordevelopment.meteorclient.utils.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BookTooltipComponent
implements TooltipComponent,
MeteorTooltipData {
    private static final Identifier TEXTURE_BOOK_BACKGROUND = Identifier.of((String)"textures/gui/book.png");
    private final Text page;

    public BookTooltipComponent(Text page) {
        this.page = page;
    }

    @Override
    public TooltipComponent getComponent() {
        return this;
    }

    public int getHeight() {
        return 134;
    }

    public int getWidth(TextRenderer textRenderer) {
        return 112;
    }

    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        context.drawTexture(TEXTURE_BOOK_BACKGROUND, x, y, 0, 12.0f, 0.0f, 112, 134, 179, 179);
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate((float)(x + 16), (float)(y + 12), 1.0f);
        matrices.scale(0.7f, 0.7f, 1.0f);
        int offset = 0;
        for (OrderedText line : textRenderer.wrapLines((StringVisitable)this.page, 112)) {
            context.drawText(textRenderer, line, 0, offset, 0, false);
            offset += 8;
        }
        matrices.pop();
    }
}

