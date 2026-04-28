/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.NbtCompound
 */
package meteordevelopment.meteorclient.systems.hud;

import meteordevelopment.meteorclient.systems.hud.Alignment;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.XAnchor;
import meteordevelopment.meteorclient.systems.hud.YAnchor;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import net.minecraft.nbt.NbtCompound;

public class HudBox
implements ISerializable<HudBox> {
    private final HudElement element;
    public XAnchor xAnchor = XAnchor.Left;
    public YAnchor yAnchor = YAnchor.Top;
    public int x;
    public int y;
    int width;
    int height;

    public HudBox(HudElement element) {
        this.element = element;
    }

    public void setSize(double width, double height) {
        if (width >= 0.0) {
            this.width = (int)Math.ceil(width);
        }
        if (height >= 0.0) {
            this.height = (int)Math.ceil(height);
        }
    }

    public void setPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setXAnchor(XAnchor anchor) {
        if (this.xAnchor != anchor) {
            int renderX = this.getRenderX();
            switch (anchor) {
                case Left: {
                    this.x = renderX;
                    break;
                }
                case Center: {
                    this.x = renderX + this.width / 2 - Utils.getWindowWidth() / 2;
                    break;
                }
                case Right: {
                    this.x = renderX + this.width - Utils.getWindowWidth();
                }
            }
            this.xAnchor = anchor;
        }
    }

    public void setYAnchor(YAnchor anchor) {
        if (this.yAnchor != anchor) {
            int renderY = this.getRenderY();
            switch (anchor) {
                case Top: {
                    this.y = renderY;
                    break;
                }
                case Center: {
                    this.y = renderY + this.height / 2 - Utils.getWindowHeight() / 2;
                    break;
                }
                case Bottom: {
                    this.y = renderY + this.height - Utils.getWindowHeight();
                }
            }
            this.yAnchor = anchor;
        }
    }

    public void updateAnchors() {
        this.setXAnchor(this.getXAnchor(this.getRenderX()));
        this.setYAnchor(this.getYAnchor(this.getRenderY()));
    }

    public void move(int deltaX, int deltaY) {
        this.x += deltaX;
        this.y += deltaY;
        if (this.element.autoAnchors) {
            this.updateAnchors();
        }
        int border = Hud.get().border.get();
        if (this.xAnchor == XAnchor.Left && this.x < border) {
            this.x = border;
        } else if (this.xAnchor == XAnchor.Right && this.x > border) {
            this.x = border;
        }
        if (this.yAnchor == YAnchor.Top && this.y < border) {
            this.y = border;
        } else if (this.yAnchor == YAnchor.Bottom && this.y > border) {
            this.y = border;
        }
    }

    public XAnchor getXAnchor(double x) {
        boolean right;
        double splitLeft = (double)Utils.getWindowWidth() / 3.0;
        double splitRight = splitLeft * 2.0;
        boolean left = x <= splitLeft;
        boolean bl = right = x + (double)this.width >= splitRight;
        if (left && right || !left && !right) {
            return XAnchor.Center;
        }
        return left ? XAnchor.Left : XAnchor.Right;
    }

    public YAnchor getYAnchor(double y) {
        boolean bottom;
        double splitTop = (double)Utils.getWindowHeight() / 3.0;
        double splitBottom = splitTop * 2.0;
        boolean top = y <= splitTop;
        boolean bl = bottom = y + (double)this.height >= splitBottom;
        if (top && bottom || !top && !bottom) {
            return YAnchor.Center;
        }
        return top ? YAnchor.Top : YAnchor.Bottom;
    }

    public int getRenderX() {
        return switch (this.xAnchor) {
            default -> throw new MatchException(null, null);
            case XAnchor.Left -> this.x;
            case XAnchor.Center -> Utils.getWindowWidth() / 2 - this.width / 2 + this.x;
            case XAnchor.Right -> Utils.getWindowWidth() - this.width + this.x;
        };
    }

    public int getRenderY() {
        return switch (this.yAnchor) {
            default -> throw new MatchException(null, null);
            case YAnchor.Top -> this.y;
            case YAnchor.Center -> Utils.getWindowHeight() / 2 - this.height / 2 + this.y;
            case YAnchor.Bottom -> Utils.getWindowHeight() - this.height + this.y;
        };
    }

    public double alignX(double selfWidth, double width, Alignment alignment) {
        XAnchor anchor = this.xAnchor;
        if (alignment == Alignment.Left) {
            anchor = XAnchor.Left;
        } else if (alignment == Alignment.Center) {
            anchor = XAnchor.Center;
        } else if (alignment == Alignment.Right) {
            anchor = XAnchor.Right;
        }
        return switch (anchor) {
            default -> throw new MatchException(null, null);
            case XAnchor.Left -> 0.0;
            case XAnchor.Center -> selfWidth / 2.0 - width / 2.0;
            case XAnchor.Right -> selfWidth - width;
        };
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();
        tag.putString("x-anchor", this.xAnchor.name());
        tag.putString("y-anchor", this.yAnchor.name());
        tag.putInt("x", this.x);
        tag.putInt("y", this.y);
        return tag;
    }

    @Override
    public HudBox fromTag(NbtCompound tag) {
        if (tag.contains("x-anchor")) {
            this.xAnchor = XAnchor.valueOf(tag.getString("x-anchor"));
        }
        if (tag.contains("y-anchor")) {
            this.yAnchor = YAnchor.valueOf(tag.getString("y-anchor"));
        }
        if (tag.contains("x")) {
            this.x = tag.getInt("x");
        }
        if (tag.contains("y")) {
            this.y = tag.getInt("y");
        }
        return this;
    }
}

