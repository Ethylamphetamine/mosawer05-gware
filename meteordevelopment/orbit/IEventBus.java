/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.orbit;

import meteordevelopment.orbit.ICancellable;
import meteordevelopment.orbit.listeners.IListener;
import meteordevelopment.orbit.listeners.LambdaListener;

public interface IEventBus {
    public void registerLambdaFactory(String var1, LambdaListener.Factory var2);

    public boolean isListening(Class<?> var1);

    public <T> T post(T var1);

    public <T extends ICancellable> T post(T var1);

    public void subscribe(Object var1);

    public void subscribe(Class<?> var1);

    public void subscribe(IListener var1);

    public void unsubscribe(Object var1);

    public void unsubscribe(Class<?> var1);

    public void unsubscribe(IListener var1);
}

