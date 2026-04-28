/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.entity.LivingEntity
 */
package meteordevelopment.meteorclient.mixininterface;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;

public interface ILivingEntityRenderer {
    public void setupTransformsInterface(LivingEntity var1, MatrixStack var2, float var3, float var4, float var5);

    public void scaleInterface(LivingEntity var1, MatrixStack var2, float var3);

    public boolean isVisibleInterface(LivingEntity var1);

    public float getAnimationCounterInterface(LivingEntity var1, float var2);
}

