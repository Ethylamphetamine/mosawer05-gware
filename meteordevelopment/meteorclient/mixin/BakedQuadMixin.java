/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.model.BakedQuad
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Shadow
 */
package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.mixininterface.IBakedQuad;
import net.minecraft.client.render.model.BakedQuad;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={BakedQuad.class})
public abstract class BakedQuadMixin
implements IBakedQuad {
    @Shadow
    @Final
    protected int[] vertexData;

    @Override
    public float meteor$getX(int vertexI) {
        return Float.intBitsToFloat(this.vertexData[vertexI * 8]);
    }

    @Override
    public float meteor$getY(int vertexI) {
        return Float.intBitsToFloat(this.vertexData[vertexI * 8 + 1]);
    }

    @Override
    public float meteor$getZ(int vertexI) {
        return Float.intBitsToFloat(this.vertexData[vertexI * 8 + 2]);
    }
}

