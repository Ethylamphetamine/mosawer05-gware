/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.util.math.MatrixStack
 */
package meteordevelopment.meteorclient.events.render;

import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.utils.Utils;
import net.minecraft.client.util.math.MatrixStack;

public class Render3DEvent {
    private static final Render3DEvent INSTANCE = new Render3DEvent();
    public MatrixStack matrices;
    public Renderer3D renderer;
    public double frameTime;
    public float tickDelta;
    public double offsetX;
    public double offsetY;
    public double offsetZ;

    public static Render3DEvent get(MatrixStack matrices, Renderer3D renderer, float tickDelta, double offsetX, double offsetY, double offsetZ) {
        Render3DEvent.INSTANCE.matrices = matrices;
        Render3DEvent.INSTANCE.renderer = renderer;
        Render3DEvent.INSTANCE.frameTime = Utils.frameTime;
        Render3DEvent.INSTANCE.tickDelta = tickDelta;
        Render3DEvent.INSTANCE.offsetX = offsetX;
        Render3DEvent.INSTANCE.offsetY = offsetY;
        Render3DEvent.INSTANCE.offsetZ = offsetZ;
        return INSTANCE;
    }
}

