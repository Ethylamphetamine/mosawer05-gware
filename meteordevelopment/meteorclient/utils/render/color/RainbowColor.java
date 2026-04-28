/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.utils.render.color;

import meteordevelopment.meteorclient.utils.render.color.Color;

public class RainbowColor
extends Color {
    private double speed;
    private static final float[] hsb = new float[3];

    public double getSpeed() {
        return this.speed;
    }

    public RainbowColor setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public RainbowColor getNext() {
        return this.getNext(1.0);
    }

    public RainbowColor getNext(double delta) {
        if (this.speed > 0.0) {
            java.awt.Color.RGBtoHSB(this.r, this.g, this.b, hsb);
            int c = java.awt.Color.HSBtoRGB(hsb[0] + (float)(this.speed * delta), 1.0f, 1.0f);
            this.r = RainbowColor.toRGBAR(c);
            this.g = RainbowColor.toRGBAG(c);
            this.b = RainbowColor.toRGBAB(c);
        }
        return this;
    }

    @Override
    public RainbowColor set(RainbowColor color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
        this.speed = color.speed;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        return Double.compare(((RainbowColor)o).speed, this.speed) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp = Double.doubleToLongBits(this.speed);
        result = 31 * result + (int)(temp ^ temp >>> 32);
        return result;
    }
}

