/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.systems.modules.misc;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;

public class NameProtect
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Boolean> nameProtect;
    private final Setting<String> name;
    private final Setting<Boolean> skinProtect;
    private String username;

    public NameProtect() {
        super(Categories.Player, "name-protect", "Hide player names and skins.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.nameProtect = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("name-protect")).description("Hides your name client-side.")).defaultValue(true)).build());
        this.name = this.sgGeneral.add(((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)((StringSetting.Builder)new StringSetting.Builder().name("name")).description("Name to be replaced with.")).defaultValue("seasnail")).visible(this.nameProtect::get)).build());
        this.skinProtect = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("skin-protect")).description("Make players become Steves.")).defaultValue(true)).build());
        this.username = "If you see this, something is wrong.";
    }

    @Override
    public void onActivate() {
        this.username = this.mc.getSession().getUsername();
    }

    public String replaceName(String string) {
        if (string != null && this.isActive()) {
            return string.replace(this.username, this.name.get());
        }
        return string;
    }

    public String getName(String original) {
        if (!this.name.get().isEmpty() && this.isActive()) {
            return this.name.get();
        }
        return original;
    }

    public boolean skinProtect() {
        return this.isActive() && this.skinProtect.get() != false;
    }
}

