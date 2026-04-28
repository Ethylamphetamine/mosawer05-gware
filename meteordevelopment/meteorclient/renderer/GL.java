/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.GlStateManager
 *  net.fabricmc.loader.api.FabricLoader
 *  net.minecraft.util.Identifier
 *  org.joml.Matrix4f
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.opengl.GL32C
 */
package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.BufferRendererAccessor;
import meteordevelopment.meteorclient.mixininterface.ICapabilityTracker;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL32C;

public class GL {
    private static final FloatBuffer MAT = BufferUtils.createFloatBuffer((int)16);
    private static final ICapabilityTracker DEPTH = GL.getTracker("DEPTH");
    private static final ICapabilityTracker BLEND = GL.getTracker("BLEND");
    private static final ICapabilityTracker CULL = GL.getTracker("CULL");
    private static final ICapabilityTracker SCISSOR = GL.getTracker("SCISSOR");
    private static boolean depthSaved;
    private static boolean blendSaved;
    private static boolean cullSaved;
    private static boolean scissorSaved;
    public static int CURRENT_IBO;
    private static int prevIbo;

    private GL() {
    }

    public static int genVertexArray() {
        return GlStateManager._glGenVertexArrays();
    }

    public static int genBuffer() {
        return GlStateManager._glGenBuffers();
    }

    public static int genTexture() {
        return GlStateManager._genTexture();
    }

    public static int genFramebuffer() {
        return GlStateManager.glGenFramebuffers();
    }

    public static void deleteBuffer(int buffer) {
        GlStateManager._glDeleteBuffers((int)buffer);
    }

    public static void deleteVertexArray(int vao) {
        GlStateManager._glDeleteVertexArrays((int)vao);
    }

    public static void deleteShader(int shader) {
        GlStateManager.glDeleteShader((int)shader);
    }

    public static void deleteTexture(int id) {
        GlStateManager._deleteTexture((int)id);
    }

    public static void deleteFramebuffer(int fbo) {
        GlStateManager._glDeleteFramebuffers((int)fbo);
    }

    public static void deleteProgram(int program) {
        GlStateManager.glDeleteProgram((int)program);
    }

    public static void bindVertexArray(int vao) {
        GlStateManager._glBindVertexArray((int)vao);
        BufferRendererAccessor.setCurrentVertexBuffer(null);
    }

    public static void bindVertexBuffer(int vbo) {
        GlStateManager._glBindBuffer((int)34962, (int)vbo);
    }

    public static void bindIndexBuffer(int ibo) {
        if (ibo != 0) {
            prevIbo = CURRENT_IBO;
        }
        GlStateManager._glBindBuffer((int)34963, (int)(ibo != 0 ? ibo : prevIbo));
    }

    public static void bindFramebuffer(int fbo) {
        GlStateManager._glBindFramebuffer((int)36160, (int)fbo);
    }

    public static void bufferData(int target, ByteBuffer data, int usage) {
        GlStateManager._glBufferData((int)target, (ByteBuffer)data, (int)usage);
    }

    public static void drawElements(int mode, int first, int type) {
        GlStateManager._drawElements((int)mode, (int)first, (int)type, (long)0L);
    }

    public static void enableVertexAttribute(int i) {
        GlStateManager._enableVertexAttribArray((int)i);
    }

    public static void vertexAttribute(int index, int size, int type, boolean normalized, int stride, long pointer) {
        GlStateManager._vertexAttribPointer((int)index, (int)size, (int)type, (boolean)normalized, (int)stride, (long)pointer);
    }

    public static int createShader(int type) {
        return GlStateManager.glCreateShader((int)type);
    }

    public static void shaderSource(int shader, String source) {
        GlStateManager.glShaderSource((int)shader, List.of(source));
    }

    public static String compileShader(int shader) {
        GlStateManager.glCompileShader((int)shader);
        if (GlStateManager.glGetShaderi((int)shader, (int)35713) == 0) {
            return GlStateManager.glGetShaderInfoLog((int)shader, (int)512);
        }
        return null;
    }

    public static int createProgram() {
        return GlStateManager.glCreateProgram();
    }

    public static String linkProgram(int program, int vertShader, int fragShader) {
        GlStateManager.glAttachShader((int)program, (int)vertShader);
        GlStateManager.glAttachShader((int)program, (int)fragShader);
        GlStateManager.glLinkProgram((int)program);
        if (GlStateManager.glGetProgrami((int)program, (int)35714) == 0) {
            return GlStateManager.glGetProgramInfoLog((int)program, (int)512);
        }
        return null;
    }

    public static void useProgram(int program) {
        GlStateManager._glUseProgram((int)program);
    }

    public static void viewport(int x, int y, int width, int height) {
        GlStateManager._viewport((int)x, (int)y, (int)width, (int)height);
    }

    public static int getUniformLocation(int program, String name) {
        return GlStateManager._glGetUniformLocation((int)program, (CharSequence)name);
    }

    public static void uniformInt(int location, int v) {
        GlStateManager._glUniform1i((int)location, (int)v);
    }

    public static void uniformFloat(int location, float v) {
        GL32C.glUniform1f((int)location, (float)v);
    }

    public static void uniformFloat2(int location, float v1, float v2) {
        GL32C.glUniform2f((int)location, (float)v1, (float)v2);
    }

    public static void uniformFloat3(int location, float v1, float v2, float v3) {
        GL32C.glUniform3f((int)location, (float)v1, (float)v2, (float)v3);
    }

    public static void uniformFloat4(int location, float v1, float v2, float v3, float v4) {
        GL32C.glUniform4f((int)location, (float)v1, (float)v2, (float)v3, (float)v4);
    }

    public static void uniformFloat3Array(int location, float[] v) {
        GL32C.glUniform3fv((int)location, (float[])v);
    }

    public static void uniformMatrix(int location, Matrix4f v) {
        v.get(MAT);
        GlStateManager._glUniformMatrix4((int)location, (boolean)false, (FloatBuffer)MAT);
    }

    public static void pixelStore(int name, int param) {
        GlStateManager._pixelStore((int)name, (int)param);
    }

    public static void textureParam(int target, int name, int param) {
        GlStateManager._texParameter((int)target, (int)name, (int)param);
    }

    public static void textureImage2D(int target, int level, int internalFormat, int width, int height, int border, int format, int type, ByteBuffer pixels) {
        GL32C.glTexImage2D((int)target, (int)level, (int)internalFormat, (int)width, (int)height, (int)border, (int)format, (int)type, (ByteBuffer)pixels);
    }

    public static void defaultPixelStore() {
        GL.pixelStore(3312, 0);
        GL.pixelStore(3313, 0);
        GL.pixelStore(3314, 0);
        GL.pixelStore(32878, 0);
        GL.pixelStore(3315, 0);
        GL.pixelStore(3316, 0);
        GL.pixelStore(32877, 0);
        GL.pixelStore(3317, 4);
    }

    public static void generateMipmap(int target) {
        GL32C.glGenerateMipmap((int)target);
    }

    public static void framebufferTexture2D(int target, int attachment, int textureTarget, int texture, int level) {
        GlStateManager._glFramebufferTexture2D((int)target, (int)attachment, (int)textureTarget, (int)texture, (int)level);
    }

    public static void clear(int mask) {
        GlStateManager._clearColor((float)0.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        GlStateManager._clear((int)mask, (boolean)false);
    }

    public static void saveState() {
        depthSaved = DEPTH.get();
        blendSaved = BLEND.get();
        cullSaved = CULL.get();
        scissorSaved = SCISSOR.get();
    }

    public static void restoreState() {
        DEPTH.set(depthSaved);
        BLEND.set(blendSaved);
        CULL.set(cullSaved);
        SCISSOR.set(scissorSaved);
        GL.disableLineSmooth();
    }

    public static void enableDepth() {
        GlStateManager._enableDepthTest();
    }

    public static void disableDepth() {
        GlStateManager._disableDepthTest();
    }

    public static void enableBlend() {
        GlStateManager._enableBlend();
        GlStateManager._blendFunc((int)770, (int)771);
    }

    public static void disableBlend() {
        GlStateManager._disableBlend();
    }

    public static void enableCull() {
        GlStateManager._enableCull();
    }

    public static void disableCull() {
        GlStateManager._disableCull();
    }

    public static void enableScissorTest() {
        GlStateManager._enableScissorTest();
    }

    public static void disableScissorTest() {
        GlStateManager._disableScissorTest();
    }

    public static void enableLineSmooth() {
        GL32C.glEnable((int)2848);
        GL32C.glLineWidth((float)1.0f);
    }

    public static void disableLineSmooth() {
        GL32C.glDisable((int)2848);
    }

    public static void bindTexture(Identifier id) {
        GlStateManager._activeTexture((int)33984);
        MeteorClient.mc.getTextureManager().bindTexture(id);
    }

    public static void bindTexture(int i, int slot) {
        GlStateManager._activeTexture((int)(33984 + slot));
        GlStateManager._bindTexture((int)i);
    }

    public static void bindTexture(int i) {
        GL.bindTexture(i, 0);
    }

    public static void resetTextureSlot() {
        GlStateManager._activeTexture((int)33984);
    }

    private static ICapabilityTracker getTracker(String fieldName) {
        try {
            Class<GlStateManager> glStateManager = GlStateManager.class;
            Field field = glStateManager.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object state = field.get(null);
            String trackerName = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "com.mojang.blaze3d.platform.GlStateManager$class_1018");
            Field capStateField = null;
            for (Field f : state.getClass().getDeclaredFields()) {
                if (!f.getType().getName().equals(trackerName)) continue;
                capStateField = f;
                break;
            }
            capStateField.setAccessible(true);
            return (ICapabilityTracker)capStateField.get(state);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            throw new IllegalStateException("Could not find GL state tracker '" + fieldName + "'", e);
        }
    }
}

