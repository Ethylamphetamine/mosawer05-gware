/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.systems.RenderSystem
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  net.minecraft.resource.Resource
 *  org.apache.commons.io.IOUtils
 *  org.joml.Matrix4f
 */
package meteordevelopment.meteorclient.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.renderer.GL;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.resource.Resource;
import org.apache.commons.io.IOUtils;
import org.joml.Matrix4f;

public class Shader {
    public static Shader BOUND;
    private final int id;
    private final Object2IntMap<String> uniformLocations = new Object2IntOpenHashMap();

    public Shader(String vertPath, String fragPath) {
        int vert = GL.createShader(35633);
        GL.shaderSource(vert, this.read(vertPath));
        String vertError = GL.compileShader(vert);
        if (vertError != null) {
            MeteorClient.LOG.error("Failed to compile vertex shader (" + vertPath + "): " + vertError);
            throw new RuntimeException("Failed to compile vertex shader (" + vertPath + "): " + vertError);
        }
        int frag = GL.createShader(35632);
        GL.shaderSource(frag, this.read(fragPath));
        String fragError = GL.compileShader(frag);
        if (fragError != null) {
            MeteorClient.LOG.error("Failed to compile fragment shader (" + fragPath + "): " + fragError);
            throw new RuntimeException("Failed to compile fragment shader (" + fragPath + "): " + fragError);
        }
        this.id = GL.createProgram();
        String programError = GL.linkProgram(this.id, vert, frag);
        if (programError != null) {
            MeteorClient.LOG.error("Failed to link program: " + programError);
            throw new RuntimeException("Failed to link program: " + programError);
        }
        GL.deleteShader(vert);
        GL.deleteShader(frag);
    }

    private String read(String path) {
        try {
            return IOUtils.toString((InputStream)((Resource)MeteorClient.mc.getResourceManager().getResource(MeteorClient.identifier("shaders/" + path)).get()).getInputStream(), (Charset)StandardCharsets.UTF_8);
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not read shader '" + path + "'", e);
        }
    }

    public void bind() {
        GL.useProgram(this.id);
        BOUND = this;
    }

    private int getLocation(String name) {
        if (this.uniformLocations.containsKey((Object)name)) {
            return this.uniformLocations.getInt((Object)name);
        }
        int location = GL.getUniformLocation(this.id, name);
        this.uniformLocations.put((Object)name, location);
        return location;
    }

    public void set(String name, boolean v) {
        GL.uniformInt(this.getLocation(name), v ? 1 : 0);
    }

    public void set(String name, int v) {
        GL.uniformInt(this.getLocation(name), v);
    }

    public void set(String name, double v) {
        GL.uniformFloat(this.getLocation(name), (float)v);
    }

    public void set(String name, double v1, double v2) {
        GL.uniformFloat2(this.getLocation(name), (float)v1, (float)v2);
    }

    public void set(String name, Color color) {
        GL.uniformFloat4(this.getLocation(name), (float)color.r / 255.0f, (float)color.g / 255.0f, (float)color.b / 255.0f, (float)color.a / 255.0f);
    }

    public void set(String name, Matrix4f mat) {
        GL.uniformMatrix(this.getLocation(name), mat);
    }

    public void setDefaults() {
        this.set("u_Proj", RenderSystem.getProjectionMatrix());
        this.set("u_ModelView", (Matrix4f)RenderSystem.getModelViewStack());
    }
}

