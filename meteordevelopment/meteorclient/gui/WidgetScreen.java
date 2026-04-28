/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.client.gui.DrawContext
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.client.util.math.MatrixStack
 *  net.minecraft.text.Text
 *  net.minecraft.util.math.MathHelper
 */
package meteordevelopment.meteorclient.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiKeyEvents;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.renderer.GuiDebugRenderer;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WRoot;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.CursorStyle;
import meteordevelopment.meteorclient.utils.misc.input.Input;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public abstract class WidgetScreen
extends Screen {
    private static final GuiRenderer RENDERER = new GuiRenderer();
    private static final GuiDebugRenderer DEBUG_RENDERER = new GuiDebugRenderer();
    public Runnable taskAfterRender;
    protected Runnable enterAction;
    public Screen parent;
    private final WContainer root;
    protected final GuiTheme theme;
    public boolean locked;
    public boolean lockedAllowClose;
    private boolean closed;
    private boolean onClose;
    private boolean debug;
    private double lastMouseX;
    private double lastMouseY;
    public double animProgress;
    private List<Runnable> onClosed;
    protected boolean firstInit = true;

    public WidgetScreen(GuiTheme theme, String title) {
        super((Text)Text.literal((String)title));
        this.parent = MeteorClient.mc.currentScreen;
        this.root = new WFullScreenRoot();
        this.theme = theme;
        this.root.theme = theme;
        if (this.parent != null) {
            this.animProgress = 1.0;
            if (this instanceof TabScreen && this.parent instanceof TabScreen) {
                this.parent = ((TabScreen)this.parent).parent;
            }
        }
    }

    public <W extends WWidget> Cell<W> add(W widget) {
        return this.root.add(widget);
    }

    public void clear() {
        this.root.clear();
    }

    public void invalidate() {
        this.root.invalidate();
    }

    protected void init() {
        MeteorClient.EVENT_BUS.subscribe((Object)this);
        this.closed = false;
        if (this.firstInit) {
            this.firstInit = false;
            this.initWidgets();
        }
    }

    public abstract void initWidgets();

    public void reload() {
        this.clear();
        this.initWidgets();
    }

    public void onClosed(Runnable action) {
        if (this.onClosed == null) {
            this.onClosed = new ArrayList<Runnable>(2);
        }
        this.onClosed.add(action);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.locked) {
            return false;
        }
        double s = MeteorClient.mc.getWindow().getScaleFactor();
        return this.root.mouseClicked(mouseX *= s, mouseY *= s, button, false);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.locked) {
            return false;
        }
        double s = MeteorClient.mc.getWindow().getScaleFactor();
        return this.root.mouseReleased(mouseX *= s, mouseY *= s, button);
    }

    public void mouseMoved(double mouseX, double mouseY) {
        if (this.locked) {
            return;
        }
        double s = MeteorClient.mc.getWindow().getScaleFactor();
        this.root.mouseMoved(mouseX *= s, mouseY *= s, this.lastMouseX, this.lastMouseY);
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (this.locked) {
            return false;
        }
        this.root.mouseScrolled(verticalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.locked) {
            return false;
        }
        if ((modifiers == 2 || modifiers == 8) && keyCode == 57) {
            this.debug = !this.debug;
            return true;
        }
        if ((keyCode == 257 || keyCode == 335) && this.enterAction != null) {
            this.enterAction.run();
            return true;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        boolean control;
        boolean shouldReturn;
        if (this.locked) {
            return false;
        }
        boolean bl = shouldReturn = this.root.keyPressed(keyCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
        if (shouldReturn) {
            return true;
        }
        if (keyCode == 258) {
            AtomicReference<Object> firstTextBox = new AtomicReference<Object>(null);
            AtomicBoolean done = new AtomicBoolean(false);
            AtomicBoolean foundFocused = new AtomicBoolean(false);
            this.loopWidgets(this.root, wWidget -> {
                if (done.get() || !(wWidget instanceof WTextBox)) {
                    return;
                }
                WTextBox textBox = (WTextBox)wWidget;
                if (foundFocused.get()) {
                    textBox.setFocused(true);
                    textBox.setCursorMax();
                    done.set(true);
                } else if (textBox.isFocused()) {
                    textBox.setFocused(false);
                    foundFocused.set(true);
                }
                if (firstTextBox.get() == null) {
                    firstTextBox.set(textBox);
                }
            });
            if (!done.get() && firstTextBox.get() != null) {
                ((WTextBox)firstTextBox.get()).setFocused(true);
                ((WTextBox)firstTextBox.get()).setCursorMax();
            }
            return true;
        }
        boolean bl2 = MinecraftClient.IS_SYSTEM_MAC ? modifiers == 8 : (control = modifiers == 2);
        if (control && keyCode == 67 && this.toClipboard()) {
            return true;
        }
        if (control && keyCode == 86 && this.fromClipboard()) {
            this.reload();
            Screen screen = this.parent;
            if (screen instanceof WidgetScreen) {
                WidgetScreen wScreen = (WidgetScreen)screen;
                wScreen.reload();
            }
            return true;
        }
        return false;
    }

    public void keyRepeated(int key, int modifiers) {
        if (this.locked) {
            return;
        }
        this.root.keyRepeated(key, modifiers);
    }

    public boolean charTyped(char chr, int keyCode) {
        if (this.locked) {
            return false;
        }
        return this.root.charTyped(chr);
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!Utils.canUpdate()) {
            this.renderBackground(context, mouseX, mouseY, delta);
        }
        double s = MeteorClient.mc.getWindow().getScaleFactor();
        mouseX = (int)((double)mouseX * s);
        mouseY = (int)((double)mouseY * s);
        this.animProgress += (double)(delta / 20.0f * 14.0f);
        this.animProgress = MathHelper.clamp((double)this.animProgress, (double)0.0, (double)1.0);
        GuiKeyEvents.canUseKeys = true;
        Utils.unscaledProjection();
        this.onRenderBefore(context, delta);
        WidgetScreen.RENDERER.theme = this.theme;
        this.theme.beforeRender();
        RENDERER.begin(context);
        RENDERER.setAlpha(this.animProgress);
        this.root.render(RENDERER, mouseX, mouseY, delta / 20.0f);
        RENDERER.setAlpha(1.0);
        RENDERER.end();
        boolean tooltip = RENDERER.renderTooltip(context, mouseX, mouseY, delta / 20.0f);
        if (this.debug) {
            MatrixStack matrices = context.getMatrices();
            DEBUG_RENDERER.render(this.root, matrices);
            if (tooltip) {
                DEBUG_RENDERER.render(WidgetScreen.RENDERER.tooltipWidget, matrices);
            }
        }
        Utils.scaledProjection();
        this.runAfterRenderTasks();
    }

    protected void runAfterRenderTasks() {
        if (this.taskAfterRender != null) {
            this.taskAfterRender.run();
            this.taskAfterRender = null;
        }
    }

    protected void onRenderBefore(DrawContext drawContext, float delta) {
    }

    public void resize(MinecraftClient client, int width, int height) {
        super.resize(client, width, height);
        this.root.invalidate();
    }

    public void close() {
        if (!this.locked || this.lockedAllowClose) {
            boolean preOnClose = this.onClose;
            this.onClose = true;
            this.removed();
            this.onClose = preOnClose;
        }
    }

    public void removed() {
        if (!this.closed || this.lockedAllowClose) {
            this.closed = true;
            this.onClosed();
            Input.setCursorStyle(CursorStyle.Default);
            this.loopWidgets(this.root, widget -> {
                WTextBox textBox;
                if (widget instanceof WTextBox && (textBox = (WTextBox)widget).isFocused()) {
                    textBox.setFocused(false);
                }
            });
            MeteorClient.EVENT_BUS.unsubscribe((Object)this);
            GuiKeyEvents.canUseKeys = true;
            if (this.onClosed != null) {
                for (Runnable action : this.onClosed) {
                    action.run();
                }
            }
            if (this.onClose) {
                this.taskAfterRender = () -> {
                    this.locked = true;
                    MeteorClient.mc.setScreen(this.parent);
                };
            }
        }
    }

    private void loopWidgets(WWidget widget, Consumer<WWidget> action) {
        action.accept(widget);
        if (widget instanceof WContainer) {
            for (Cell<?> cell : ((WContainer)widget).cells) {
                this.loopWidgets((WWidget)cell.widget(), action);
            }
        }
    }

    protected void onClosed() {
    }

    public boolean toClipboard() {
        return false;
    }

    public boolean fromClipboard() {
        return false;
    }

    public boolean shouldCloseOnEsc() {
        return !this.locked || this.lockedAllowClose;
    }

    public boolean shouldPause() {
        return false;
    }

    private static class WFullScreenRoot
    extends WContainer
    implements WRoot {
        private boolean valid;

        private WFullScreenRoot() {
        }

        @Override
        public void invalidate() {
            this.valid = false;
        }

        @Override
        protected void onCalculateSize() {
            this.width = Utils.getWindowWidth();
            this.height = Utils.getWindowHeight();
        }

        @Override
        protected void onCalculateWidgetPositions() {
            for (Cell cell : this.cells) {
                cell.x = 0.0;
                cell.y = 0.0;
                cell.width = this.width;
                cell.height = this.height;
                cell.alignWidget();
            }
        }

        @Override
        public boolean render(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
            if (!this.valid) {
                this.calculateSize();
                this.calculateWidgetPositions();
                this.valid = true;
                this.mouseMoved(MeteorClient.mc.mouse.getX(), MeteorClient.mc.mouse.getY(), MeteorClient.mc.mouse.getX(), MeteorClient.mc.mouse.getY());
            }
            return super.render(renderer, mouseX, mouseY, delta);
        }
    }
}

