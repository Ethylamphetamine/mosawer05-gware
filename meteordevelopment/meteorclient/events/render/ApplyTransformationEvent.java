/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.model.json.Transformation
 *  net.minecraft.client.util.math.MatrixStack
 */
package meteordevelopment.meteorclient.events.render;

import meteordevelopment.meteorclient.events.Cancellable;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;

public class ApplyTransformationEvent
extends Cancellable {
    private static final ApplyTransformationEvent INSTANCE = new ApplyTransformationEvent();
    public Transformation transformation;
    public boolean leftHanded;
    public MatrixStack matrices;

    public static ApplyTransformationEvent get(Transformation transformation, boolean leftHanded, MatrixStack matrices) {
        INSTANCE.setCancelled(false);
        ApplyTransformationEvent.INSTANCE.transformation = transformation;
        ApplyTransformationEvent.INSTANCE.leftHanded = leftHanded;
        ApplyTransformationEvent.INSTANCE.matrices = matrices;
        return INSTANCE;
    }
}

