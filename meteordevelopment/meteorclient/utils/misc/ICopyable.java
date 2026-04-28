/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.utils.misc;

public interface ICopyable<T extends ICopyable<T>> {
    public T set(T var1);

    public T copy();
}

