/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  net.minecraft.client.model.ModelPart
 *  net.minecraft.client.model.ModelPart$Cuboid
 *  net.minecraft.client.model.ModelPart$Quad
 *  net.minecraft.client.network.AbstractClientPlayerEntity
 *  net.minecraft.client.render.entity.BoatEntityRenderer
 *  net.minecraft.client.render.entity.EndCrystalEntityRenderer
 *  net.minecraft.client.render.entity.EntityRenderer
 *  net.minecraft.client.render.entity.ItemEntityRenderer
 *  net.minecraft.client.render.entity.LivingEntityRenderer
 *  net.minecraft.client.render.entity.PlayerEntityRenderer
 *  net.minecraft.client.render.entity.model.AnimalModel
 *  net.minecraft.client.render.entity.model.BipedEntityModel
 *  net.minecraft.client.render.entity.model.BipedEntityModel$ArmPose
 *  net.minecraft.client.render.entity.model.CompositeEntityModel
 *  net.minecraft.client.render.entity.model.EntityModel
 *  net.minecraft.client.render.entity.model.LlamaEntityModel
 *  net.minecraft.client.render.entity.model.ModelWithWaterPatch
 *  net.minecraft.client.render.entity.model.PlayerEntityModel
 *  net.minecraft.client.render.entity.model.RabbitEntityModel
 *  net.minecraft.client.render.entity.model.SinglePartEntityModel
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.decoration.EndCrystalEntity
 *  net.minecraft.entity.vehicle.BoatEntity
 *  net.minecraft.util.Arm
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.RotationAxis
 *  net.minecraft.util.math.Vec3d
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionf
 *  org.joml.Vector4f
 */
package meteordevelopment.meteorclient.utils.render;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Chams;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.BoatEntityRenderer;
import net.minecraft.client.render.entity.EndCrystalEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.CompositeEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.LlamaEntityModel;
import net.minecraft.client.render.entity.model.ModelWithWaterPatch;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.model.RabbitEntityModel;
import net.minecraft.client.render.entity.model.SinglePartEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector4f;

public class WireframeEntityRenderer {
    private static final MatrixStack matrices = new MatrixStack();
    private static final Vector4f pos1 = new Vector4f();
    private static final Vector4f pos2 = new Vector4f();
    private static final Vector4f pos3 = new Vector4f();
    private static final Vector4f pos4 = new Vector4f();
    private static double offsetX;
    private static double offsetY;
    private static double offsetZ;
    private static Color sideColor;
    private static Color lineColor;
    private static ShapeMode shapeMode;

    private WireframeEntityRenderer() {
    }

    public static void render(Render3DEvent event, Entity entity, double scale, Color sideColor, Color lineColor, ShapeMode shapeMode) {
        LivingEntityRenderer renderer;
        WireframeEntityRenderer.sideColor = sideColor;
        WireframeEntityRenderer.lineColor = lineColor;
        WireframeEntityRenderer.shapeMode = shapeMode;
        offsetX = MathHelper.lerp((double)event.tickDelta, (double)entity.lastRenderX, (double)entity.getX());
        offsetY = MathHelper.lerp((double)event.tickDelta, (double)entity.lastRenderY, (double)entity.getY());
        offsetZ = MathHelper.lerp((double)event.tickDelta, (double)entity.lastRenderZ, (double)entity.getZ());
        matrices.push();
        matrices.scale((float)scale, (float)scale, (float)scale);
        EntityRenderer entityRenderer = MeteorClient.mc.getEntityRenderDispatcher().getRenderer(entity);
        if (entityRenderer instanceof LivingEntityRenderer) {
            Entity entity2;
            renderer = (LivingEntityRenderer)entityRenderer;
            LivingEntity livingEntity = (LivingEntity)entity;
            EntityModel model = renderer.getModel();
            if (entityRenderer instanceof PlayerEntityRenderer) {
                PlayerEntityRenderer r = (PlayerEntityRenderer)entityRenderer;
                PlayerEntityModel playerModel = (PlayerEntityModel)r.getModel();
                playerModel.sneaking = entity.isInSneakingPose();
                BipedEntityModel.ArmPose armPose = PlayerEntityRenderer.getArmPose((AbstractClientPlayerEntity)((AbstractClientPlayerEntity)entity), (Hand)Hand.MAIN_HAND);
                BipedEntityModel.ArmPose armPose2 = PlayerEntityRenderer.getArmPose((AbstractClientPlayerEntity)((AbstractClientPlayerEntity)entity), (Hand)Hand.OFF_HAND);
                if (armPose.isTwoHanded()) {
                    BipedEntityModel.ArmPose armPose3 = armPose2 = livingEntity.getOffHandStack().isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;
                }
                if (livingEntity.getMainArm() == Arm.RIGHT) {
                    playerModel.rightArmPose = armPose;
                    playerModel.leftArmPose = armPose2;
                } else {
                    playerModel.rightArmPose = armPose2;
                    playerModel.leftArmPose = armPose;
                }
            }
            model.handSwingProgress = livingEntity.getHandSwingProgress(event.tickDelta);
            model.riding = livingEntity.hasVehicle();
            model.child = livingEntity.isBaby();
            float bodyYaw = MathHelper.lerpAngleDegrees((float)event.tickDelta, (float)livingEntity.prevBodyYaw, (float)livingEntity.bodyYaw);
            float headYaw = MathHelper.lerpAngleDegrees((float)event.tickDelta, (float)livingEntity.prevHeadYaw, (float)livingEntity.headYaw);
            float yaw = headYaw - bodyYaw;
            if (livingEntity.hasVehicle() && (entity2 = livingEntity.getVehicle()) instanceof LivingEntity) {
                LivingEntity livingEntity2 = (LivingEntity)entity2;
                bodyYaw = MathHelper.lerpAngleDegrees((float)event.tickDelta, (float)livingEntity2.prevBodyYaw, (float)livingEntity2.bodyYaw);
                yaw = headYaw - bodyYaw;
                float animationProgress = MathHelper.wrapDegrees((float)yaw);
                if (animationProgress < -85.0f) {
                    animationProgress = -85.0f;
                }
                if (animationProgress >= 85.0f) {
                    animationProgress = 85.0f;
                }
                bodyYaw = headYaw - animationProgress;
                if (animationProgress * animationProgress > 2500.0f) {
                    bodyYaw = (float)((double)bodyYaw + (double)animationProgress * 0.2);
                }
                yaw = headYaw - bodyYaw;
            }
            float pitch = MathHelper.lerp((float)event.tickDelta, (float)livingEntity.prevPitch, (float)livingEntity.getPitch());
            float animationProgress = renderer.getAnimationProgress(livingEntity, event.tickDelta);
            float limbDistance = 0.0f;
            float limbAngle = 0.0f;
            if (!livingEntity.hasVehicle() && livingEntity.isAlive()) {
                limbDistance = livingEntity.limbAnimator.getSpeed(event.tickDelta);
                limbAngle = livingEntity.limbAnimator.getPos(event.tickDelta);
                if (livingEntity.isBaby()) {
                    limbAngle *= 3.0f;
                }
                if (limbDistance > 1.0f) {
                    limbDistance = 1.0f;
                }
            }
            model.animateModel((Entity)livingEntity, limbAngle, limbDistance, event.tickDelta);
            model.setAngles((Entity)livingEntity, limbAngle, limbDistance, animationProgress, yaw, pitch);
            renderer.setupTransforms(livingEntity, matrices, animationProgress, bodyYaw, event.tickDelta, livingEntity.getScale());
            matrices.scale(-1.0f, -1.0f, 1.0f);
            renderer.scale(livingEntity, matrices, event.tickDelta);
            matrices.translate(0.0, (double)-1.501f, 0.0);
            if (model instanceof AnimalModel) {
                AnimalModel m = (AnimalModel)model;
                if (m.child) {
                    BipedEntityModel mo;
                    float g;
                    matrices.push();
                    if (m.headScaled) {
                        g = 1.5f / m.invertedChildHeadScale;
                        matrices.scale(g, g, g);
                    }
                    matrices.translate(0.0, (double)(m.childHeadYOffset / 16.0f), (double)(m.childHeadZOffset / 16.0f));
                    if (model instanceof BipedEntityModel) {
                        mo = (BipedEntityModel)model;
                        WireframeEntityRenderer.render(event.renderer, mo.head);
                    } else {
                        m.getHeadParts().forEach(modelPart -> WireframeEntityRenderer.render(event.renderer, (ModelPart)modelPart));
                    }
                    matrices.pop();
                    matrices.push();
                    g = 1.0f / m.invertedChildBodyScale;
                    matrices.scale(g, g, g);
                    matrices.translate(0.0, (double)(m.childBodyYOffset / 16.0f), 0.0);
                    if (model instanceof BipedEntityModel) {
                        mo = (BipedEntityModel)model;
                        WireframeEntityRenderer.render(event.renderer, mo.body);
                        WireframeEntityRenderer.render(event.renderer, mo.leftArm);
                        WireframeEntityRenderer.render(event.renderer, mo.rightArm);
                        WireframeEntityRenderer.render(event.renderer, mo.leftLeg);
                        WireframeEntityRenderer.render(event.renderer, mo.rightLeg);
                    } else {
                        m.getBodyParts().forEach(modelPart -> WireframeEntityRenderer.render(event.renderer, (ModelPart)modelPart));
                    }
                    matrices.pop();
                } else if (model instanceof BipedEntityModel) {
                    BipedEntityModel mo = (BipedEntityModel)model;
                    WireframeEntityRenderer.render(event.renderer, mo.head);
                    WireframeEntityRenderer.render(event.renderer, mo.body);
                    WireframeEntityRenderer.render(event.renderer, mo.leftArm);
                    WireframeEntityRenderer.render(event.renderer, mo.rightArm);
                    WireframeEntityRenderer.render(event.renderer, mo.leftLeg);
                    WireframeEntityRenderer.render(event.renderer, mo.rightLeg);
                } else {
                    m.getHeadParts().forEach(modelPart -> WireframeEntityRenderer.render(event.renderer, (ModelPart)modelPart));
                    m.getBodyParts().forEach(modelPart -> WireframeEntityRenderer.render(event.renderer, (ModelPart)modelPart));
                }
            } else if (model instanceof SinglePartEntityModel) {
                SinglePartEntityModel m = (SinglePartEntityModel)model;
                WireframeEntityRenderer.render(event.renderer, m.getPart());
            } else if (model instanceof CompositeEntityModel) {
                CompositeEntityModel m = (CompositeEntityModel)model;
                m.getParts().forEach(modelPart -> WireframeEntityRenderer.render(event.renderer, (ModelPart)modelPart));
            } else if (model instanceof LlamaEntityModel) {
                LlamaEntityModel m = (LlamaEntityModel)model;
                if (m.child) {
                    matrices.push();
                    matrices.scale(0.71428573f, 0.64935064f, 0.7936508f);
                    matrices.translate(0.0, 1.3125, (double)0.22f);
                    WireframeEntityRenderer.render(event.renderer, m.head);
                    matrices.pop();
                    matrices.push();
                    matrices.scale(0.625f, 0.45454544f, 0.45454544f);
                    matrices.translate(0.0, 2.0625, 0.0);
                    WireframeEntityRenderer.render(event.renderer, m.body);
                    matrices.pop();
                    matrices.push();
                    matrices.scale(0.45454544f, 0.41322312f, 0.45454544f);
                    matrices.translate(0.0, 2.0625, 0.0);
                    WireframeEntityRenderer.render(event.renderer, m.rightHindLeg);
                    WireframeEntityRenderer.render(event.renderer, m.leftHindLeg);
                    WireframeEntityRenderer.render(event.renderer, m.rightFrontLeg);
                    WireframeEntityRenderer.render(event.renderer, m.leftFrontLeg);
                    WireframeEntityRenderer.render(event.renderer, m.rightChest);
                    WireframeEntityRenderer.render(event.renderer, m.leftChest);
                    matrices.pop();
                } else {
                    WireframeEntityRenderer.render(event.renderer, m.head);
                    WireframeEntityRenderer.render(event.renderer, m.body);
                    WireframeEntityRenderer.render(event.renderer, m.rightHindLeg);
                    WireframeEntityRenderer.render(event.renderer, m.leftHindLeg);
                    WireframeEntityRenderer.render(event.renderer, m.rightFrontLeg);
                    WireframeEntityRenderer.render(event.renderer, m.leftFrontLeg);
                    WireframeEntityRenderer.render(event.renderer, m.rightChest);
                    WireframeEntityRenderer.render(event.renderer, m.leftChest);
                }
            } else if (model instanceof RabbitEntityModel) {
                RabbitEntityModel m = (RabbitEntityModel)model;
                if (m.child) {
                    matrices.push();
                    matrices.scale(0.56666666f, 0.56666666f, 0.56666666f);
                    matrices.translate(0.0, 1.375, 0.125);
                    WireframeEntityRenderer.render(event.renderer, m.head);
                    WireframeEntityRenderer.render(event.renderer, m.leftEar);
                    WireframeEntityRenderer.render(event.renderer, m.rightEar);
                    WireframeEntityRenderer.render(event.renderer, m.nose);
                    matrices.pop();
                    matrices.push();
                    matrices.scale(0.4f, 0.4f, 0.4f);
                    matrices.translate(0.0, 2.25, 0.0);
                    WireframeEntityRenderer.render(event.renderer, m.leftHindLeg);
                    WireframeEntityRenderer.render(event.renderer, m.rightHindLeg);
                    WireframeEntityRenderer.render(event.renderer, m.leftHaunch);
                    WireframeEntityRenderer.render(event.renderer, m.rightHaunch);
                    WireframeEntityRenderer.render(event.renderer, m.body);
                    WireframeEntityRenderer.render(event.renderer, m.leftFrontLeg);
                    WireframeEntityRenderer.render(event.renderer, m.rightFrontLeg);
                    WireframeEntityRenderer.render(event.renderer, m.tail);
                    matrices.pop();
                } else {
                    matrices.push();
                    matrices.scale(0.6f, 0.6f, 0.6f);
                    matrices.translate(0.0, 1.0, 0.0);
                    WireframeEntityRenderer.render(event.renderer, m.leftHindLeg);
                    WireframeEntityRenderer.render(event.renderer, m.rightHindLeg);
                    WireframeEntityRenderer.render(event.renderer, m.leftHaunch);
                    WireframeEntityRenderer.render(event.renderer, m.rightHaunch);
                    WireframeEntityRenderer.render(event.renderer, m.body);
                    WireframeEntityRenderer.render(event.renderer, m.leftFrontLeg);
                    WireframeEntityRenderer.render(event.renderer, m.rightFrontLeg);
                    WireframeEntityRenderer.render(event.renderer, m.head);
                    WireframeEntityRenderer.render(event.renderer, m.rightEar);
                    WireframeEntityRenderer.render(event.renderer, m.leftEar);
                    WireframeEntityRenderer.render(event.renderer, m.tail);
                    WireframeEntityRenderer.render(event.renderer, m.nose);
                    matrices.pop();
                }
            }
        }
        if (entityRenderer instanceof EndCrystalEntityRenderer) {
            float h;
            renderer = (EndCrystalEntityRenderer)entityRenderer;
            EndCrystalEntity crystalEntity = (EndCrystalEntity)entity;
            Chams chams = Modules.get().get(Chams.class);
            boolean chamsEnabled = chams.isActive() && chams.crystals.get() != false;
            matrices.push();
            if (chamsEnabled) {
                float f = (float)crystalEntity.endCrystalAge + event.tickDelta;
                float g = MathHelper.sin((float)(f * 0.2f)) / 2.0f + 0.5f;
                g = (g * g + g) * 0.4f * chams.crystalsBounce.get().floatValue();
                h = g - 1.4f;
            } else {
                h = EndCrystalEntityRenderer.getYOffset((EndCrystalEntity)crystalEntity, (float)event.tickDelta);
            }
            float j = ((float)crystalEntity.endCrystalAge + event.tickDelta) * 3.0f;
            matrices.push();
            if (chamsEnabled) {
                matrices.scale(2.0f * chams.crystalsScale.get().floatValue(), 2.0f * chams.crystalsScale.get().floatValue(), 2.0f * chams.crystalsScale.get().floatValue());
            } else {
                matrices.scale(2.0f, 2.0f, 2.0f);
            }
            matrices.translate(0.0, -0.5, 0.0);
            if (crystalEntity.shouldShowBottom()) {
                WireframeEntityRenderer.render(event.renderer, renderer.bottom);
            }
            if (chamsEnabled) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j * chams.crystalsRotationSpeed.get().floatValue()));
            } else {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            }
            matrices.translate(0.0, (double)(1.5f + h / 2.0f), 0.0);
            matrices.multiply(new Quaternionf().setAngleAxis(60.0f, EndCrystalEntityRenderer.SINE_45_DEGREES, 0.0f, EndCrystalEntityRenderer.SINE_45_DEGREES));
            if (!chamsEnabled || chams.renderFrame1.get().booleanValue()) {
                WireframeEntityRenderer.render(event.renderer, renderer.frame);
            }
            matrices.scale(0.875f, 0.875f, 0.875f);
            matrices.multiply(new Quaternionf().setAngleAxis(60.0f, EndCrystalEntityRenderer.SINE_45_DEGREES, 0.0f, EndCrystalEntityRenderer.SINE_45_DEGREES));
            if (chamsEnabled) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j * chams.crystalsRotationSpeed.get().floatValue()));
            } else {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            }
            if (!chamsEnabled || chams.renderFrame2.get().booleanValue()) {
                WireframeEntityRenderer.render(event.renderer, renderer.frame);
            }
            matrices.scale(0.875f, 0.875f, 0.875f);
            matrices.multiply(new Quaternionf().setAngleAxis(60.0f, EndCrystalEntityRenderer.SINE_45_DEGREES, 0.0f, EndCrystalEntityRenderer.SINE_45_DEGREES));
            if (chamsEnabled) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j * chams.crystalsRotationSpeed.get().floatValue()));
            } else {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            }
            if (!chamsEnabled || chams.renderCore.get().booleanValue()) {
                WireframeEntityRenderer.render(event.renderer, renderer.core);
            }
            matrices.pop();
            matrices.pop();
        } else if (entityRenderer instanceof BoatEntityRenderer) {
            float k;
            BoatEntityRenderer renderer2 = (BoatEntityRenderer)entityRenderer;
            BoatEntity boatEntity = (BoatEntity)entity;
            matrices.push();
            matrices.translate(0.0, 0.375, 0.0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - MathHelper.lerp((float)event.tickDelta, (float)entity.prevYaw, (float)entity.getYaw())));
            float h = (float)boatEntity.getDamageWobbleTicks() - event.tickDelta;
            float j = boatEntity.getDamageWobbleStrength() - event.tickDelta;
            if (j < 0.0f) {
                j = 0.0f;
            }
            if (h > 0.0f) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin((float)h) * h * j / 10.0f * (float)boatEntity.getDamageWobbleSide()));
            }
            if (!MathHelper.approximatelyEquals((float)(k = boatEntity.interpolateBubbleWobble(event.tickDelta)), (float)0.0f)) {
                matrices.multiply(new Quaternionf().setAngleAxis(boatEntity.interpolateBubbleWobble(event.tickDelta), 1.0f, 0.0f, 1.0f));
            }
            CompositeEntityModel boatEntityModel = (CompositeEntityModel)((Pair)renderer2.texturesAndModels.get(boatEntity.getVariant())).getSecond();
            matrices.scale(-1.0f, -1.0f, 1.0f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
            boatEntityModel.setAngles((Entity)boatEntity, event.tickDelta, 0.0f, -0.1f, 0.0f, 0.0f);
            boatEntityModel.getParts().forEach(modelPart -> WireframeEntityRenderer.render(event.renderer, modelPart));
            if (!boatEntity.isSubmergedInWater() && boatEntityModel instanceof ModelWithWaterPatch) {
                ModelWithWaterPatch modelWithWaterPatch = (ModelWithWaterPatch)boatEntityModel;
                WireframeEntityRenderer.render(event.renderer, modelWithWaterPatch.getWaterPatch());
            }
            matrices.pop();
        } else if (entityRenderer instanceof ItemEntityRenderer) {
            double dx = (entity.getX() - entity.prevX) * (double)event.tickDelta;
            double dy = (entity.getY() - entity.prevY) * (double)event.tickDelta;
            double dz = (entity.getZ() - entity.prevZ) * (double)event.tickDelta;
            Box box = entity.getBoundingBox();
            event.renderer.box(dx + box.minX, dy + box.minY, dz + box.minZ, dx + box.maxX, dy + box.maxY, dz + box.maxZ, sideColor, lineColor, shapeMode, 0);
        }
        matrices.pop();
    }

    private static void render(Renderer3D renderer, ModelPart part) {
        if (!part.visible || part.cuboids.isEmpty() && part.children.isEmpty()) {
            return;
        }
        matrices.push();
        part.rotate(matrices);
        for (ModelPart.Cuboid cuboid : part.cuboids) {
            WireframeEntityRenderer.render(renderer, cuboid, offsetX, offsetY, offsetZ);
        }
        for (ModelPart child : part.children.values()) {
            WireframeEntityRenderer.render(renderer, child);
        }
        matrices.pop();
    }

    private static void render(Renderer3D renderer, ModelPart.Cuboid cuboid, double offsetX, double offsetY, double offsetZ) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        for (ModelPart.Quad quad : cuboid.sides) {
            pos1.set(quad.vertices[0].pos.x / 16.0f, quad.vertices[0].pos.y / 16.0f, quad.vertices[0].pos.z / 16.0f, 1.0f);
            pos1.mul((Matrix4fc)matrix);
            pos2.set(quad.vertices[1].pos.x / 16.0f, quad.vertices[1].pos.y / 16.0f, quad.vertices[1].pos.z / 16.0f, 1.0f);
            pos2.mul((Matrix4fc)matrix);
            pos3.set(quad.vertices[2].pos.x / 16.0f, quad.vertices[2].pos.y / 16.0f, quad.vertices[2].pos.z / 16.0f, 1.0f);
            pos3.mul((Matrix4fc)matrix);
            pos4.set(quad.vertices[3].pos.x / 16.0f, quad.vertices[3].pos.y / 16.0f, quad.vertices[3].pos.z / 16.0f, 1.0f);
            pos4.mul((Matrix4fc)matrix);
            if (shapeMode.sides()) {
                renderer.triangles.quad(renderer.triangles.vec3(offsetX + (double)WireframeEntityRenderer.pos1.x, offsetY + (double)WireframeEntityRenderer.pos1.y, offsetZ + (double)WireframeEntityRenderer.pos1.z).color(sideColor).next(), renderer.triangles.vec3(offsetX + (double)WireframeEntityRenderer.pos2.x, offsetY + (double)WireframeEntityRenderer.pos2.y, offsetZ + (double)WireframeEntityRenderer.pos2.z).color(sideColor).next(), renderer.triangles.vec3(offsetX + (double)WireframeEntityRenderer.pos3.x, offsetY + (double)WireframeEntityRenderer.pos3.y, offsetZ + (double)WireframeEntityRenderer.pos3.z).color(sideColor).next(), renderer.triangles.vec3(offsetX + (double)WireframeEntityRenderer.pos4.x, offsetY + (double)WireframeEntityRenderer.pos4.y, offsetZ + (double)WireframeEntityRenderer.pos4.z).color(sideColor).next());
            }
            if (!shapeMode.lines()) continue;
            renderer.line(offsetX + (double)WireframeEntityRenderer.pos1.x, offsetY + (double)WireframeEntityRenderer.pos1.y, offsetZ + (double)WireframeEntityRenderer.pos1.z, offsetX + (double)WireframeEntityRenderer.pos2.x, offsetY + (double)WireframeEntityRenderer.pos2.y, offsetZ + (double)WireframeEntityRenderer.pos2.z, lineColor);
            renderer.line(offsetX + (double)WireframeEntityRenderer.pos2.x, offsetY + (double)WireframeEntityRenderer.pos2.y, offsetZ + (double)WireframeEntityRenderer.pos2.z, offsetX + (double)WireframeEntityRenderer.pos3.x, offsetY + (double)WireframeEntityRenderer.pos3.y, offsetZ + (double)WireframeEntityRenderer.pos3.z, lineColor);
            renderer.line(offsetX + (double)WireframeEntityRenderer.pos3.x, offsetY + (double)WireframeEntityRenderer.pos3.y, offsetZ + (double)WireframeEntityRenderer.pos3.z, offsetX + (double)WireframeEntityRenderer.pos4.x, offsetY + (double)WireframeEntityRenderer.pos4.y, offsetZ + (double)WireframeEntityRenderer.pos4.z, lineColor);
            renderer.line(offsetX + (double)WireframeEntityRenderer.pos1.x, offsetY + (double)WireframeEntityRenderer.pos1.y, offsetZ + (double)WireframeEntityRenderer.pos1.z, offsetX + (double)WireframeEntityRenderer.pos1.x, offsetY + (double)WireframeEntityRenderer.pos1.y, offsetZ + (double)WireframeEntityRenderer.pos1.z, lineColor);
        }
    }

    public static List<RenderablePart> cloneEntityForRendering(Render3DEvent event, Entity entity, Vec3d offset) {
        LivingEntityRenderer renderer;
        ArrayList<RenderablePart> parts = new ArrayList<RenderablePart>();
        offsetX = MathHelper.lerp((double)event.tickDelta, (double)entity.lastRenderX, (double)entity.getX());
        offsetY = MathHelper.lerp((double)event.tickDelta, (double)entity.lastRenderY, (double)entity.getY());
        offsetZ = MathHelper.lerp((double)event.tickDelta, (double)entity.lastRenderZ, (double)entity.getZ());
        ((IVec3d)offset).set(offsetX, offsetY, offsetZ);
        matrices.push();
        EntityRenderer entityRenderer = MeteorClient.mc.getEntityRenderDispatcher().getRenderer(entity);
        if (entityRenderer instanceof LivingEntityRenderer) {
            Entity entity2;
            renderer = (LivingEntityRenderer)entityRenderer;
            LivingEntity livingEntity = (LivingEntity)entity;
            EntityModel model = renderer.getModel();
            if (entityRenderer instanceof PlayerEntityRenderer) {
                PlayerEntityRenderer r = (PlayerEntityRenderer)entityRenderer;
                PlayerEntityModel playerModel = (PlayerEntityModel)r.getModel();
                playerModel.sneaking = entity.isInSneakingPose();
                BipedEntityModel.ArmPose armPose = PlayerEntityRenderer.getArmPose((AbstractClientPlayerEntity)((AbstractClientPlayerEntity)entity), (Hand)Hand.MAIN_HAND);
                BipedEntityModel.ArmPose armPose2 = PlayerEntityRenderer.getArmPose((AbstractClientPlayerEntity)((AbstractClientPlayerEntity)entity), (Hand)Hand.OFF_HAND);
                if (armPose.isTwoHanded()) {
                    BipedEntityModel.ArmPose armPose3 = armPose2 = livingEntity.getOffHandStack().isEmpty() ? BipedEntityModel.ArmPose.EMPTY : BipedEntityModel.ArmPose.ITEM;
                }
                if (livingEntity.getMainArm() == Arm.RIGHT) {
                    playerModel.rightArmPose = armPose;
                    playerModel.leftArmPose = armPose2;
                } else {
                    playerModel.rightArmPose = armPose2;
                    playerModel.leftArmPose = armPose;
                }
            }
            model.handSwingProgress = livingEntity.getHandSwingProgress(event.tickDelta);
            model.riding = livingEntity.hasVehicle();
            model.child = livingEntity.isBaby();
            float bodyYaw = MathHelper.lerpAngleDegrees((float)event.tickDelta, (float)livingEntity.prevBodyYaw, (float)livingEntity.bodyYaw);
            float headYaw = MathHelper.lerpAngleDegrees((float)event.tickDelta, (float)livingEntity.prevHeadYaw, (float)livingEntity.headYaw);
            float yaw = headYaw - bodyYaw;
            if (livingEntity.hasVehicle() && (entity2 = livingEntity.getVehicle()) instanceof LivingEntity) {
                LivingEntity livingEntity2 = (LivingEntity)entity2;
                bodyYaw = MathHelper.lerpAngleDegrees((float)event.tickDelta, (float)livingEntity2.prevBodyYaw, (float)livingEntity2.bodyYaw);
                yaw = headYaw - bodyYaw;
                float animationProgress = MathHelper.wrapDegrees((float)yaw);
                if (animationProgress < -85.0f) {
                    animationProgress = -85.0f;
                }
                if (animationProgress >= 85.0f) {
                    animationProgress = 85.0f;
                }
                bodyYaw = headYaw - animationProgress;
                if (animationProgress * animationProgress > 2500.0f) {
                    bodyYaw = (float)((double)bodyYaw + (double)animationProgress * 0.2);
                }
                yaw = headYaw - bodyYaw;
            }
            float pitch = MathHelper.lerp((float)event.tickDelta, (float)livingEntity.prevPitch, (float)livingEntity.getPitch());
            float animationProgress = renderer.getAnimationProgress(livingEntity, event.tickDelta);
            float limbDistance = 0.0f;
            float limbAngle = 0.0f;
            if (!livingEntity.hasVehicle() && livingEntity.isAlive()) {
                limbDistance = livingEntity.limbAnimator.getSpeed(event.tickDelta);
                limbAngle = livingEntity.limbAnimator.getPos(event.tickDelta);
                if (livingEntity.isBaby()) {
                    limbAngle *= 3.0f;
                }
                if (limbDistance > 1.0f) {
                    limbDistance = 1.0f;
                }
            }
            model.animateModel((Entity)livingEntity, limbAngle, limbDistance, event.tickDelta);
            model.setAngles((Entity)livingEntity, limbAngle, limbDistance, animationProgress, yaw, pitch);
            renderer.setupTransforms(livingEntity, matrices, animationProgress, bodyYaw, event.tickDelta, livingEntity.getScale());
            matrices.scale(-1.0f, -1.0f, 1.0f);
            renderer.scale(livingEntity, matrices, event.tickDelta);
            matrices.translate(0.0, (double)-1.501f, 0.0);
            if (model instanceof AnimalModel) {
                AnimalModel m = (AnimalModel)model;
                if (m.child) {
                    BipedEntityModel mo;
                    float g;
                    matrices.push();
                    if (m.headScaled) {
                        g = 1.5f / m.invertedChildHeadScale;
                        matrices.scale(g, g, g);
                    }
                    matrices.translate(0.0, (double)(m.childHeadYOffset / 16.0f), (double)(m.childHeadZOffset / 16.0f));
                    if (model instanceof BipedEntityModel) {
                        mo = (BipedEntityModel)model;
                        WireframeEntityRenderer.cloneRenderParts(parts, mo.head);
                    } else {
                        m.getHeadParts().forEach(modelPart -> WireframeEntityRenderer.cloneRenderParts(parts, (ModelPart)modelPart));
                    }
                    matrices.pop();
                    matrices.push();
                    g = 1.0f / m.invertedChildBodyScale;
                    matrices.scale(g, g, g);
                    matrices.translate(0.0, (double)(m.childBodyYOffset / 16.0f), 0.0);
                    if (model instanceof BipedEntityModel) {
                        mo = (BipedEntityModel)model;
                        WireframeEntityRenderer.cloneRenderParts(parts, mo.body);
                        WireframeEntityRenderer.cloneRenderParts(parts, mo.leftArm);
                        WireframeEntityRenderer.cloneRenderParts(parts, mo.rightArm);
                        WireframeEntityRenderer.cloneRenderParts(parts, mo.leftLeg);
                        WireframeEntityRenderer.cloneRenderParts(parts, mo.rightLeg);
                    } else {
                        m.getBodyParts().forEach(modelPart -> WireframeEntityRenderer.cloneRenderParts(parts, (ModelPart)modelPart));
                    }
                    matrices.pop();
                } else if (model instanceof BipedEntityModel) {
                    BipedEntityModel mo = (BipedEntityModel)model;
                    WireframeEntityRenderer.cloneRenderParts(parts, mo.head);
                    WireframeEntityRenderer.cloneRenderParts(parts, mo.body);
                    WireframeEntityRenderer.cloneRenderParts(parts, mo.leftArm);
                    WireframeEntityRenderer.cloneRenderParts(parts, mo.rightArm);
                    WireframeEntityRenderer.cloneRenderParts(parts, mo.leftLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, mo.rightLeg);
                } else {
                    m.getHeadParts().forEach(modelPart -> WireframeEntityRenderer.cloneRenderParts(parts, (ModelPart)modelPart));
                    m.getBodyParts().forEach(modelPart -> WireframeEntityRenderer.cloneRenderParts(parts, (ModelPart)modelPart));
                }
            } else if (model instanceof SinglePartEntityModel) {
                SinglePartEntityModel m = (SinglePartEntityModel)model;
                WireframeEntityRenderer.cloneRenderParts(parts, m.getPart());
            } else if (model instanceof CompositeEntityModel) {
                CompositeEntityModel m = (CompositeEntityModel)model;
                m.getParts().forEach(modelPart -> WireframeEntityRenderer.cloneRenderParts(parts, (ModelPart)modelPart));
            } else if (model instanceof LlamaEntityModel) {
                LlamaEntityModel m = (LlamaEntityModel)model;
                if (m.child) {
                    matrices.push();
                    matrices.scale(0.71428573f, 0.64935064f, 0.7936508f);
                    matrices.translate(0.0, 1.3125, (double)0.22f);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.head);
                    matrices.pop();
                    matrices.push();
                    matrices.scale(0.625f, 0.45454544f, 0.45454544f);
                    matrices.translate(0.0, 2.0625, 0.0);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.body);
                    matrices.pop();
                    matrices.push();
                    matrices.scale(0.45454544f, 0.41322312f, 0.45454544f);
                    matrices.translate(0.0, 2.0625, 0.0);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightHindLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftHindLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightFrontLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftFrontLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightChest);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftChest);
                    matrices.pop();
                } else {
                    WireframeEntityRenderer.cloneRenderParts(parts, m.head);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.body);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightHindLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftHindLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightFrontLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftFrontLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightChest);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftChest);
                }
            } else if (model instanceof RabbitEntityModel) {
                RabbitEntityModel m = (RabbitEntityModel)model;
                if (m.child) {
                    matrices.push();
                    matrices.scale(0.56666666f, 0.56666666f, 0.56666666f);
                    matrices.translate(0.0, 1.375, 0.125);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.head);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftEar);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightEar);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.nose);
                    matrices.pop();
                    matrices.push();
                    matrices.scale(0.4f, 0.4f, 0.4f);
                    matrices.translate(0.0, 2.25, 0.0);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftHindLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightHindLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftHaunch);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightHaunch);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.body);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftFrontLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightFrontLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.tail);
                    matrices.pop();
                } else {
                    matrices.push();
                    matrices.scale(0.6f, 0.6f, 0.6f);
                    matrices.translate(0.0, 1.0, 0.0);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftHindLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightHindLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftHaunch);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightHaunch);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.body);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftFrontLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightFrontLeg);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.head);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.rightEar);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.leftEar);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.tail);
                    WireframeEntityRenderer.cloneRenderParts(parts, m.nose);
                    matrices.pop();
                }
            }
        }
        if (entityRenderer instanceof EndCrystalEntityRenderer) {
            float h;
            renderer = (EndCrystalEntityRenderer)entityRenderer;
            EndCrystalEntity crystalEntity = (EndCrystalEntity)entity;
            Chams chams = Modules.get().get(Chams.class);
            boolean chamsEnabled = chams.isActive() && chams.crystals.get() != false;
            matrices.push();
            if (chamsEnabled) {
                float f = (float)crystalEntity.endCrystalAge + event.tickDelta;
                float g = MathHelper.sin((float)(f * 0.2f)) / 2.0f + 0.5f;
                g = (g * g + g) * 0.4f * chams.crystalsBounce.get().floatValue();
                h = g - 1.4f;
            } else {
                h = EndCrystalEntityRenderer.getYOffset((EndCrystalEntity)crystalEntity, (float)event.tickDelta);
            }
            float j = ((float)crystalEntity.endCrystalAge + event.tickDelta) * 3.0f;
            matrices.push();
            if (chamsEnabled) {
                matrices.scale(2.0f * chams.crystalsScale.get().floatValue(), 2.0f * chams.crystalsScale.get().floatValue(), 2.0f * chams.crystalsScale.get().floatValue());
            } else {
                matrices.scale(2.0f, 2.0f, 2.0f);
            }
            matrices.translate(0.0, -0.5, 0.0);
            if (crystalEntity.shouldShowBottom()) {
                WireframeEntityRenderer.cloneRenderParts(parts, renderer.bottom);
            }
            if (chamsEnabled) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j * chams.crystalsRotationSpeed.get().floatValue()));
            } else {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            }
            matrices.translate(0.0, (double)(1.5f + h / 2.0f), 0.0);
            matrices.multiply(new Quaternionf().setAngleAxis(60.0f, EndCrystalEntityRenderer.SINE_45_DEGREES, 0.0f, EndCrystalEntityRenderer.SINE_45_DEGREES));
            if (!chamsEnabled || chams.renderFrame1.get().booleanValue()) {
                WireframeEntityRenderer.cloneRenderParts(parts, renderer.frame);
            }
            matrices.scale(0.875f, 0.875f, 0.875f);
            matrices.multiply(new Quaternionf().setAngleAxis(60.0f, EndCrystalEntityRenderer.SINE_45_DEGREES, 0.0f, EndCrystalEntityRenderer.SINE_45_DEGREES));
            if (chamsEnabled) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j * chams.crystalsRotationSpeed.get().floatValue()));
            } else {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            }
            if (!chamsEnabled || chams.renderFrame2.get().booleanValue()) {
                WireframeEntityRenderer.cloneRenderParts(parts, renderer.frame);
            }
            matrices.scale(0.875f, 0.875f, 0.875f);
            matrices.multiply(new Quaternionf().setAngleAxis(60.0f, EndCrystalEntityRenderer.SINE_45_DEGREES, 0.0f, EndCrystalEntityRenderer.SINE_45_DEGREES));
            if (chamsEnabled) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j * chams.crystalsRotationSpeed.get().floatValue()));
            } else {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(j));
            }
            if (!chamsEnabled || chams.renderCore.get().booleanValue()) {
                WireframeEntityRenderer.cloneRenderParts(parts, renderer.core);
            }
            matrices.pop();
            matrices.pop();
        } else if (entityRenderer instanceof BoatEntityRenderer) {
            float k;
            BoatEntityRenderer renderer2 = (BoatEntityRenderer)entityRenderer;
            BoatEntity boatEntity = (BoatEntity)entity;
            matrices.push();
            matrices.translate(0.0, 0.375, 0.0);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0f - MathHelper.lerp((float)event.tickDelta, (float)entity.prevYaw, (float)entity.getYaw())));
            float h = (float)boatEntity.getDamageWobbleTicks() - event.tickDelta;
            float j = boatEntity.getDamageWobbleStrength() - event.tickDelta;
            if (j < 0.0f) {
                j = 0.0f;
            }
            if (h > 0.0f) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(MathHelper.sin((float)h) * h * j / 10.0f * (float)boatEntity.getDamageWobbleSide()));
            }
            if (!MathHelper.approximatelyEquals((float)(k = boatEntity.interpolateBubbleWobble(event.tickDelta)), (float)0.0f)) {
                matrices.multiply(new Quaternionf().setAngleAxis(boatEntity.interpolateBubbleWobble(event.tickDelta), 1.0f, 0.0f, 1.0f));
            }
            CompositeEntityModel boatEntityModel = (CompositeEntityModel)((Pair)renderer2.texturesAndModels.get(boatEntity.getVariant())).getSecond();
            matrices.scale(-1.0f, -1.0f, 1.0f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90.0f));
            boatEntityModel.setAngles((Entity)boatEntity, event.tickDelta, 0.0f, -0.1f, 0.0f, 0.0f);
            boatEntityModel.getParts().forEach(modelPart -> WireframeEntityRenderer.cloneRenderParts(parts, modelPart));
            if (!boatEntity.isSubmergedInWater() && boatEntityModel instanceof ModelWithWaterPatch) {
                ModelWithWaterPatch modelWithWaterPatch = (ModelWithWaterPatch)boatEntityModel;
                WireframeEntityRenderer.cloneRenderParts(parts, modelWithWaterPatch.getWaterPatch());
            }
            matrices.pop();
        }
        matrices.pop();
        return parts;
    }

    private static void cloneRenderParts(List<RenderablePart> list, ModelPart part) {
        if (!part.visible || part.cuboids.isEmpty() && part.children.isEmpty()) {
            return;
        }
        matrices.push();
        part.rotate(matrices);
        for (ModelPart.Cuboid cuboid : part.cuboids) {
            WireframeEntityRenderer.cloneRenderCuboids(list, cuboid);
        }
        for (ModelPart child : part.children.values()) {
            WireframeEntityRenderer.cloneRenderParts(list, child);
        }
        matrices.pop();
    }

    private static void cloneRenderCuboids(List<RenderablePart> list, ModelPart.Cuboid cuboid) {
        RenderablePart part = new RenderablePart();
        try {
            part.matrix = (Matrix4f)matrices.peek().getPositionMatrix().clone();
        }
        catch (CloneNotSupportedException cloneNotSupportedException) {
            // empty catch block
        }
        part.cuboid = cuboid;
        list.add(part);
    }

    public static void render(Render3DEvent event, Vec3d offset, List<RenderablePart> parts, double scale, Color sideColor, Color lineColor, ShapeMode shapeMode) {
        WireframeEntityRenderer.sideColor = sideColor;
        WireframeEntityRenderer.lineColor = lineColor;
        WireframeEntityRenderer.shapeMode = shapeMode;
        matrices.push();
        matrices.scale((float)scale, (float)scale, (float)scale);
        for (RenderablePart part : parts) {
            matrices.push();
            matrices.multiplyPositionMatrix(part.matrix);
            WireframeEntityRenderer.renderPart(event.renderer, offset, part);
            matrices.pop();
        }
        matrices.pop();
    }

    private static void renderPart(Renderer3D renderer, Vec3d offset, RenderablePart part) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        ModelPart.Cuboid cuboid = part.cuboid;
        double offsetX = offset.x;
        double offsetY = offset.y;
        double offsetZ = offset.z;
        for (ModelPart.Quad quad : cuboid.sides) {
            pos1.set(quad.vertices[0].pos.x / 16.0f, quad.vertices[0].pos.y / 16.0f, quad.vertices[0].pos.z / 16.0f, 1.0f);
            pos1.mul((Matrix4fc)matrix);
            pos2.set(quad.vertices[1].pos.x / 16.0f, quad.vertices[1].pos.y / 16.0f, quad.vertices[1].pos.z / 16.0f, 1.0f);
            pos2.mul((Matrix4fc)matrix);
            pos3.set(quad.vertices[2].pos.x / 16.0f, quad.vertices[2].pos.y / 16.0f, quad.vertices[2].pos.z / 16.0f, 1.0f);
            pos3.mul((Matrix4fc)matrix);
            pos4.set(quad.vertices[3].pos.x / 16.0f, quad.vertices[3].pos.y / 16.0f, quad.vertices[3].pos.z / 16.0f, 1.0f);
            pos4.mul((Matrix4fc)matrix);
            if (shapeMode.sides()) {
                renderer.triangles.quad(renderer.triangles.vec3(offsetX + (double)WireframeEntityRenderer.pos1.x, offsetY + (double)WireframeEntityRenderer.pos1.y, offsetZ + (double)WireframeEntityRenderer.pos1.z).color(sideColor).next(), renderer.triangles.vec3(offsetX + (double)WireframeEntityRenderer.pos2.x, offsetY + (double)WireframeEntityRenderer.pos2.y, offsetZ + (double)WireframeEntityRenderer.pos2.z).color(sideColor).next(), renderer.triangles.vec3(offsetX + (double)WireframeEntityRenderer.pos3.x, offsetY + (double)WireframeEntityRenderer.pos3.y, offsetZ + (double)WireframeEntityRenderer.pos3.z).color(sideColor).next(), renderer.triangles.vec3(offsetX + (double)WireframeEntityRenderer.pos4.x, offsetY + (double)WireframeEntityRenderer.pos4.y, offsetZ + (double)WireframeEntityRenderer.pos4.z).color(sideColor).next());
            }
            if (!shapeMode.lines()) continue;
            renderer.line(offsetX + (double)WireframeEntityRenderer.pos1.x, offsetY + (double)WireframeEntityRenderer.pos1.y, offsetZ + (double)WireframeEntityRenderer.pos1.z, offsetX + (double)WireframeEntityRenderer.pos2.x, offsetY + (double)WireframeEntityRenderer.pos2.y, offsetZ + (double)WireframeEntityRenderer.pos2.z, lineColor);
            renderer.line(offsetX + (double)WireframeEntityRenderer.pos2.x, offsetY + (double)WireframeEntityRenderer.pos2.y, offsetZ + (double)WireframeEntityRenderer.pos2.z, offsetX + (double)WireframeEntityRenderer.pos3.x, offsetY + (double)WireframeEntityRenderer.pos3.y, offsetZ + (double)WireframeEntityRenderer.pos3.z, lineColor);
            renderer.line(offsetX + (double)WireframeEntityRenderer.pos3.x, offsetY + (double)WireframeEntityRenderer.pos3.y, offsetZ + (double)WireframeEntityRenderer.pos3.z, offsetX + (double)WireframeEntityRenderer.pos4.x, offsetY + (double)WireframeEntityRenderer.pos4.y, offsetZ + (double)WireframeEntityRenderer.pos4.z, lineColor);
            renderer.line(offsetX + (double)WireframeEntityRenderer.pos1.x, offsetY + (double)WireframeEntityRenderer.pos1.y, offsetZ + (double)WireframeEntityRenderer.pos1.z, offsetX + (double)WireframeEntityRenderer.pos1.x, offsetY + (double)WireframeEntityRenderer.pos1.y, offsetZ + (double)WireframeEntityRenderer.pos1.z, lineColor);
        }
    }

    public static class RenderablePart {
        public Matrix4f matrix;
        public ModelPart.Cuboid cuboid;
    }
}

