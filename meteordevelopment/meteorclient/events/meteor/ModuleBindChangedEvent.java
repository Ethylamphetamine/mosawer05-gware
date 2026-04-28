/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.events.meteor;

import meteordevelopment.meteorclient.systems.modules.Module;

public class ModuleBindChangedEvent {
    private static final ModuleBindChangedEvent INSTANCE = new ModuleBindChangedEvent();
    public Module module;

    public static ModuleBindChangedEvent get(Module module) {
        ModuleBindChangedEvent.INSTANCE.module = module;
        return INSTANCE;
    }
}

