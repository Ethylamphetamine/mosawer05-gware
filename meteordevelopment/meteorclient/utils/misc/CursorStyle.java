/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.glfw.GLFW
 */
package meteordevelopment.meteorclient.utils.misc;

import org.lwjgl.glfw.GLFW;

public enum CursorStyle {
    Default,
    Click,
    Type;

    private boolean created;
    private long cursor;

    public long getGlfwCursor() {
        if (!this.created) {
            switch (this.ordinal()) {
                case 1: {
                    this.cursor = GLFW.glfwCreateStandardCursor((int)221188);
                    break;
                }
                case 2: {
                    this.cursor = GLFW.glfwCreateStandardCursor((int)221186);
                }
            }
            this.created = true;
        }
        return this.cursor;
    }
}

