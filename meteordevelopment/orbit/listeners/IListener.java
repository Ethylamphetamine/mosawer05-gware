/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.orbit.listeners;

public interface IListener {
    public void call(Object var1);

    public Class<?> getTarget();

    public int getPriority();

    @Deprecated
    public boolean isStatic();
}

