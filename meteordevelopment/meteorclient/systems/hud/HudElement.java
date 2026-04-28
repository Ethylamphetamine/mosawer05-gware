/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 *  net.minecraft.nbt.NbtElement
 */
package meteordevelopment.meteorclient.systems.hud;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.settings.Settings;
import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudBox;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.systems.hud.HudRenderer;
import meteordevelopment.meteorclient.systems.hud.XAnchor;
import meteordevelopment.meteorclient.systems.hud.YAnchor;
import meteordevelopment.meteorclient.systems.hud.screens.HudEditorScreen;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.other.Snapper;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

public abstract class HudElement
implements Snapper.Element,
ISerializable<HudElement> {
    public final HudElementInfo<?> info;
    private boolean active;
    public final Settings settings = new Settings();
    public final HudBox box = new HudBox(this);
    public boolean autoAnchors = true;
    public int x;
    public int y;

    public HudElement(HudElementInfo<?> info) {
        this.info = info;
        this.active = true;
    }

    public boolean isActive() {
        return this.active;
    }

    public void toggle() {
        this.active = !this.active;
    }

    public void setSize(double width, double height) {
        this.box.setSize(width, height);
    }

    @Override
    public void setPos(int x, int y) {
        if (this.autoAnchors) {
            this.box.setPos(x, y);
            this.box.xAnchor = XAnchor.Left;
            this.box.yAnchor = YAnchor.Top;
            this.box.updateAnchors();
        } else {
            this.box.setPos(this.box.x + (x - this.x), this.box.y + (y - this.y));
        }
        this.updatePos();
    }

    @Override
    public void move(int deltaX, int deltaY) {
        this.box.move(deltaX, deltaY);
        this.updatePos();
    }

    public void updatePos() {
        this.x = this.box.getRenderX();
        this.y = this.box.getRenderY();
    }

    protected double alignX(double width, Alignment alignment) {
        return this.box.alignX(this.getWidth(), width, alignment);
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public int getWidth() {
        return this.box.width;
    }

    @Override
    public int getHeight() {
        return this.box.height;
    }

    protected boolean isInEditor() {
        return !Utils.canUpdate() || HudEditorScreen.isOpen();
    }

    public void remove() {
        Hud.get().remove(this);
    }

    public void tick(HudRenderer renderer) {
    }

    public void render(HudRenderer renderer) {
    }

    public void onFontChanged() {
    }

    public WWidget getWidget(GuiTheme theme) {
        return null;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("name", this.info.name);
        tag.putBoolean("active", this.active);
        tag.put("settings", (NbtElement)this.settings.toTag());
        tag.put("box", (NbtElement)this.box.toTag());
        tag.putBoolean("autoAnchors", this.autoAnchors);
        return tag;
    }

    @Override
    public HudElement fromTag(NbtCompound tag) {
        this.settings.reset();
        this.active = tag.getBoolean("active");
        this.settings.fromTag(tag.getCompound("settings"));
        this.box.fromTag(tag.getCompound("box"));
        this.autoAnchors = tag.getBoolean("autoAnchors");
        this.x = this.box.getRenderX();
        this.y = this.box.getRenderY();
        return this;
    }
}

