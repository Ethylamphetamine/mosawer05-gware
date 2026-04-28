/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.font.TextRenderer
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.tooltip.TooltipComponent
 *  net.minecraft.client.render.DiffuseLighting
 *  net.minecraft.client.render.VertexConsumerProvider
 *  net.minecraft.client.render.VertexConsumerProvider$Immediate
 *  net.minecraft.client.render.entity.EntityRenderDispatcher
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.passive.GoatEntity
 *  net.minecraft.entity.passive.SquidEntity
 *  net.minecraft.util.math.RotationAxis
 *  org.joml.Quaternionf
 */
package meteordevelopment.meteorclient.utils.tooltip;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.tooltip.MeteorTooltipData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.GoatEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.util.math.RotationAxis;
import org.joml.Quaternionf;

public class EntityTooltipComponent
implements MeteorTooltipData,
TooltipComponent {
    protected final Entity entity;

    public EntityTooltipComponent(Entity entity) {
        this.entity = entity;
    }

    @Override
    public TooltipComponent getComponent() {
        return this;
    }

    public int getHeight() {
        return 24;
    }

    public int getWidth(TextRenderer textRenderer) {
        return 60;
    }

    public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(15.0f, 2.0f, 0.0f);
        this.entity.setVelocity(1.0, 1.0, 1.0);
        this.renderEntity(matrices, x, y);
        matrices.pop();
    }

    protected void renderEntity(MatrixStack matrices, int x, int y) {
        if (MeteorClient.mc.player == null) {
            return;
        }
        float size = 24.0f;
        if ((double)Math.max(this.entity.getWidth(), this.entity.getHeight()) > 1.0) {
            size /= Math.max(this.entity.getWidth(), this.entity.getHeight());
        }
        DiffuseLighting.disableGuiDepthLighting();
        matrices.push();
        int yOffset = 16;
        if (this.entity instanceof SquidEntity) {
            size = 16.0f;
            yOffset = 2;
        }
        matrices.translate((float)(x + 10), (float)(y + yOffset), 1050.0f);
        matrices.scale(1.0f, 1.0f, -1.0f);
        matrices.translate(0.0f, 0.0f, 1000.0f);
        matrices.scale(size, size, size);
        Quaternionf quaternion = RotationAxis.POSITIVE_Z.rotationDegrees(180.0f);
        Quaternionf quaternion2 = RotationAxis.POSITIVE_X.rotationDegrees(-10.0f);
        this.hamiltonProduct(quaternion, quaternion2);
        matrices.multiply(quaternion);
        this.setupAngles();
        EntityRenderDispatcher entityRenderDispatcher = MeteorClient.mc.getEntityRenderDispatcher();
        quaternion2.conjugate();
        entityRenderDispatcher.setRotation(quaternion2);
        entityRenderDispatcher.setRenderShadows(false);
        VertexConsumerProvider.Immediate immediate = MeteorClient.mc.getBufferBuilders().getEntityVertexConsumers();
        this.entity.age = MeteorClient.mc.player.age;
        this.entity.setCustomNameVisible(false);
        entityRenderDispatcher.render(this.entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, matrices, (VertexConsumerProvider)immediate, 0xF000F0);
        immediate.draw();
        entityRenderDispatcher.setRenderShadows(true);
        matrices.pop();
        DiffuseLighting.enableGuiDepthLighting();
    }

    public void hamiltonProduct(Quaternionf q, Quaternionf other) {
        float f = q.x();
        float g = q.y();
        float h = q.z();
        float i = q.w();
        float j = other.x();
        float k = other.y();
        float l = other.z();
        float m = other.w();
        q.x = i * j + f * m + g * l - h * k;
        q.y = i * k - f * l + g * m + h * j;
        q.z = i * l + f * k - g * j + h * m;
        q.w = i * m - f * j - g * k - h * l;
    }

    protected void setupAngles() {
        float yaw = (float)System.currentTimeMillis() / 10.0f % 360.0f;
        this.entity.setYaw(yaw);
        this.entity.setHeadYaw(yaw);
        this.entity.setPitch(0.0f);
        Entity entity = this.entity;
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (this.entity instanceof GoatEntity) {
                livingEntity.headYaw = yaw;
            }
            livingEntity.bodyYaw = yaw;
        }
    }
}

