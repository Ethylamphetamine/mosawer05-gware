/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.item.PickaxeItem
 */
package meteordevelopment.meteorclient.systems.modules.player;

import java.util.Set;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.PickaxeItem;

public class NoMiningTrace
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Boolean> onlyWhenHoldingPickaxe;

    public NoMiningTrace() {
        super(Categories.Player, "no-mining-trace", "Allows you to mine blocks through entities.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("blacklisted-entities")).description("Entities you will interact with as normal.")).defaultValue(new EntityType[0]).build());
        this.onlyWhenHoldingPickaxe = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-when-holding-a-pickaxe")).description("Whether or not to work only when holding a pickaxe.")).defaultValue(true)).build());
    }

    public boolean canWork(Entity entity) {
        if (!this.isActive()) {
            return false;
        }
        return !(this.onlyWhenHoldingPickaxe.get() != false && !(this.mc.player.getMainHandStack().getItem() instanceof PickaxeItem) && !(this.mc.player.getOffHandStack().getItem() instanceof PickaxeItem) || entity != null && this.entities.get().contains(entity.getType()));
    }
}

