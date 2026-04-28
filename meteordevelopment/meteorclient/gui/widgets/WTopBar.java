/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.Screen
 *  org.lwjgl.glfw.GLFW
 */
package meteordevelopment.meteorclient.gui.widgets;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPressable;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.gui.screen.Screen;
import org.lwjgl.glfw.GLFW;

public abstract class WTopBar
extends WHorizontalList {
    protected abstract Color getButtonColor(boolean var1, boolean var2);

    protected abstract Color getNameColor();

    public WTopBar() {
        this.spacing = 0.0;
    }

    @Override
    public void init() {
        for (Tab tab : Tabs.get()) {
            this.add(new WTopBarButton(tab));
        }
    }

    protected class WTopBarButton
    extends WPressable {
        private final Tab tab;

        public WTopBarButton(Tab tab) {
            this.tab = tab;
        }

        @Override
        protected void onCalculateSize() {
            double pad = this.pad();
            this.width = pad + this.theme.textWidth(this.tab.name) + pad;
            this.height = pad + this.theme.textHeight() + pad;
        }

        @Override
        protected void onPressed(int button) {
            Screen screen = MeteorClient.mc.currentScreen;
            if (!(screen instanceof TabScreen) || ((TabScreen)screen).tab != this.tab) {
                double mouseX = MeteorClient.mc.mouse.getX();
                double mouseY = MeteorClient.mc.mouse.getY();
                this.tab.openScreen(this.theme);
                GLFW.glfwSetCursorPos((long)MeteorClient.mc.getWindow().getHandle(), (double)mouseX, (double)mouseY);
            }
        }

        @Override
        protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            double pad = this.pad();
            Color color = WTopBar.this.getButtonColor(this.pressed || MeteorClient.mc.currentScreen instanceof TabScreen && ((TabScreen)MeteorClient.mc.currentScreen).tab == this.tab, this.mouseOver);
            renderer.quad(this.x, this.y, this.width, this.height, color);
            renderer.text(this.tab.name, this.x + pad, this.y + pad, WTopBar.this.getNameColor(), false);
        }
    }
}

