/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.tooltip.TooltipComponent
 *  net.minecraft.client.render.GameRenderer
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.VertexConsumerProvider$Immediate
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.component.type.MapIdComponent
 *  net.minecraft.item.FilledMapItem
 *  net.minecraft.item.map.MapState
 *  net.minecraft.util.Identifier
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.utils.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.BetterTooltips;
import meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapState;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class MapTooltipComponent
implements TooltipComponent,
MeteorTooltipData {
    private static final Identifier TEXTURE_MAP_BACKGROUND = Identifier.of((String)"textures/map/map_background.png");
    private final int mapId;

    public MapTooltipComponent(int mapId) {
        this.mapId = mapId;
    }

    public int getHeight() {
        double scale = Modules.get().get(BetterTooltips.class).mapsScale.get();
        return (int)(144.0 * scale) + 2;
    }

    public int getWidth(TextRenderer textRenderer) {
        double scale = Modules.get().get(BetterTooltips.class).mapsScale.get();
        return (int)(144.0 * scale);
    }

    @Override
    public TooltipComponent getComponent() {
        return this;
    }

    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        double scale = Modules.get().get(BetterTooltips.class).mapsScale.get();
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate((float)x, (float)y, 0.0f);
        matrices.scale((float)scale * 2.0f, (float)scale * 2.0f, 0.0f);
        matrices.scale(1.125f, 1.125f, 0.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        context.drawTexture(TEXTURE_MAP_BACKGROUND, 0, 0, 0, 0.0f, 0.0f, 64, 64, 64, 64);
        matrices.pop();
        VertexConsumerProvider.Immediate consumer = MeteorClient.mc.getBufferBuilders().getEntityVertexConsumers();
        MapState mapState = FilledMapItem.getMapState((MapIdComponent)new MapIdComponent(this.mapId), (World)MeteorClient.mc.world);
        if (mapState == null) {
            return;
        }
        matrices.push();
        matrices.translate((float)x, (float)y, 0.0f);
        matrices.scale((float)scale, (float)scale, 0.0f);
        matrices.translate(8.0f, 8.0f, 0.0f);
        MeteorClient.mc.gameRenderer.getMapRenderer().draw(matrices, (VertexConsumerProvider)consumer, new MapIdComponent(this.mapId), mapState, false, 0xF000F0);
        consumer.draw();
        matrices.pop();
    }
}

