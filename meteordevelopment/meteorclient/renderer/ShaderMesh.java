/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.renderer;

import meteordevelopment.meteorclient.renderer.DrawMode;
import meteordevelopment.meteorclient.renderer.Mesh;
import meteordevelopment.meteorclient.renderer.Shader;

public class ShaderMesh
extends Mesh {
    private final Shader shader;

    public ShaderMesh(Shader shader, DrawMode drawMode, Mesh.Attrib ... attributes) {
        super(drawMode, attributes);
        this.shader = shader;
    }

    @Override
    protected void beforeRender() {
        this.shader.bind();
    }
}

