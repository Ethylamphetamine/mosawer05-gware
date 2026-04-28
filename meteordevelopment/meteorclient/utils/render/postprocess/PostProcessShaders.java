/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.render.VertexConsumerProvider
 */
package meteordevelopment.meteorclient.utils.render.postprocess;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.render.postprocess.ChamsShader;
import meteordevelopment.meteorclient.utils.render.postprocess.EntityOutlineShader;
import meteordevelopment.meteorclient.utils.render.postprocess.EntityShader;
import meteordevelopment.meteorclient.utils.render.postprocess.PostProcessShader;
import meteordevelopment.meteorclient.utils.render.postprocess.StorageOutlineShader;
import net.minecraft.client.render.VertexConsumerProvider;

public class PostProcessShaders {
    public static EntityShader CHAMS;
    public static EntityShader ENTITY_OUTLINE;
    public static PostProcessShader STORAGE_OUTLINE;
    public static boolean rendering;

    private PostProcessShaders() {
    }

    @PreInit
    public static void init() {
        CHAMS = new ChamsShader();
        ENTITY_OUTLINE = new EntityOutlineShader();
        STORAGE_OUTLINE = new StorageOutlineShader();
    }

    public static void beginRender() {
        CHAMS.beginRender();
        ENTITY_OUTLINE.beginRender();
        STORAGE_OUTLINE.beginRender();
    }

    public static void endRender() {
        CHAMS.endRender();
        ENTITY_OUTLINE.endRender();
    }

    public static void onResized(int width, int height) {
        if (MeteorClient.mc == null) {
            return;
        }
        CHAMS.onResized(width, height);
        ENTITY_OUTLINE.onResized(width, height);
        STORAGE_OUTLINE.onResized(width, height);
    }

    public static boolean isCustom(VertexConsumerProvider vcp) {
        return vcp == PostProcessShaders.CHAMS.vertexConsumerProvider || vcp == PostProcessShaders.ENTITY_OUTLINE.vertexConsumerProvider;
    }
}

