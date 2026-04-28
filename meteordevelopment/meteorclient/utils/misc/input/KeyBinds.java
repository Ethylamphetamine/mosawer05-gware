/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.option.KeyBinding
 *  net.minecraft.client.util.InputUtil$Type
 */
package meteordevelopment.meteorclient.utils.misc.input;

import java.util.Map;
import meteordevelopment.meteorclient.mixin.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class KeyBinds {
    private static final String CATEGORY = "Meteor Client";
    public static KeyBinding OPEN_GUI = new KeyBinding("key.meteor-client.open-gui", InputUtil.Type.KEYSYM, 344, "Meteor Client");
    public static KeyBinding OPEN_COMMANDS = new KeyBinding("key.meteor-client.open-commands", InputUtil.Type.KEYSYM, 46, "Meteor Client");

    private KeyBinds() {
    }

    public static KeyBinding[] apply(KeyBinding[] binds) {
        Map<String, Integer> categories = KeyBindingAccessor.getCategoryOrderMap();
        int highest = 0;
        for (int i : categories.values()) {
            if (i <= highest) continue;
            highest = i;
        }
        categories.put(CATEGORY, highest + 1);
        KeyBinding[] newBinds = new KeyBinding[binds.length + 2];
        System.arraycopy(binds, 0, newBinds, 0, binds.length);
        newBinds[binds.length] = OPEN_GUI;
        newBinds[binds.length + 1] = OPEN_COMMANDS;
        return newBinds;
    }
}

