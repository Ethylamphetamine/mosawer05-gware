/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.AxeItem
 *  net.minecraft.item.SwordItem
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.Set;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.SwordItem;

public class Hitboxes
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<Double> value;
    private final Setting<Boolean> ignoreFriends;
    private final Setting<Boolean> onlyOnWeapon;

    public Hitboxes() {
        super(Categories.Combat, "hitboxes", "Expands an entity's hitboxes.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Which entities to target.")).defaultValue(EntityType.PLAYER).build());
        this.value = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("expand")).description("How much to expand the hitbox of the entity.")).defaultValue(0.5).build());
        this.ignoreFriends = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-friends")).description("Doesn't expand the hitboxes of friends.")).defaultValue(true)).build());
        this.onlyOnWeapon = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-weapon")).description("Only modifies hitbox when holding a weapon in hand.")).defaultValue(false)).build());
    }

    public double getEntityValue(Entity entity) {
        if (!this.isActive() || !this.testWeapon() || this.ignoreFriends.get().booleanValue() && entity instanceof PlayerEntity && Friends.get().isFriend((PlayerEntity)entity)) {
            return 0.0;
        }
        if (this.entities.get().contains(entity.getType())) {
            return this.value.get();
        }
        return 0.0;
    }

    private boolean testWeapon() {
        if (!this.onlyOnWeapon.get().booleanValue()) {
            return true;
        }
        return InvUtils.testInHands(itemStack -> itemStack.getItem() instanceof SwordItem || itemStack.getItem() instanceof AxeItem);
    }
}

