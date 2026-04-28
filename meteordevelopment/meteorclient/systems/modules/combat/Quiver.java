/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.PotionContentsComponent
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.entity.effect.StatusEffect
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.BowItem
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$LookAndOnGround
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3i
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.AbstractBlockAccessor;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StatusEffectListSetting;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;

public class Quiver
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgSafety;
    private final Setting<List<StatusEffect>> effects;
    private final Setting<Integer> cooldown;
    private final Setting<Boolean> checkEffects;
    private final Setting<Boolean> silentBow;
    private final Setting<Boolean> chatInfo;
    private final Setting<Boolean> onlyInHoles;
    private final Setting<Boolean> onlyOnGround;
    private final Setting<Double> minHealth;
    private final List<Integer> arrowSlots;
    private FindItemResult bow;
    private boolean wasMainhand;
    private boolean wasHotbar;
    private int timer;
    private int prevSlot;
    private final BlockPos.Mutable testPos;

    public Quiver() {
        super(Categories.Combat, "quiver", "Shoots arrows at yourself.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgSafety = this.settings.createGroup("Safety");
        this.effects = this.sgGeneral.add(((StatusEffectListSetting.Builder)((StatusEffectListSetting.Builder)new StatusEffectListSetting.Builder().name("effects")).description("Which effects to shoot you with.")).defaultValue((StatusEffect)StatusEffects.STRENGTH.comp_349()).build());
        this.cooldown = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("cooldown")).description("How many ticks between shooting effects (19 minimum for NCP).")).defaultValue(10)).range(0, 40).sliderRange(0, 40).build());
        this.checkEffects = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("check-effects")).description("Won't shoot you with effects you already have.")).defaultValue(true)).build());
        this.silentBow = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("silent-bow")).description("Takes a bow from your inventory to quiver.")).defaultValue(true)).build());
        this.chatInfo = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat-info")).description("Sends info about quiver checks in chat.")).defaultValue(false)).build());
        this.onlyInHoles = this.sgSafety.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-in-holes")).description("Only quiver when you're in a hole.")).defaultValue(true)).build());
        this.onlyOnGround = this.sgSafety.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-on-ground")).description("Only quiver when you're on the ground.")).defaultValue(true)).build());
        this.minHealth = this.sgSafety.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-health")).description("How much health you must have to quiver.")).defaultValue(10.0).range(0.0, 36.0).sliderRange(0.0, 36.0).build());
        this.arrowSlots = new ArrayList<Integer>();
        this.testPos = new BlockPos.Mutable();
    }

    @Override
    public void onActivate() {
        this.bow = InvUtils.find(Items.BOW);
        if (!this.shouldQuiver()) {
            return;
        }
        this.mc.options.useKey.setPressed(false);
        this.mc.interactionManager.stopUsingItem((PlayerEntity)this.mc.player);
        this.prevSlot = this.bow.slot();
        this.wasHotbar = this.bow.isHotbar();
        this.timer = 0;
        if (!this.bow.isMainHand()) {
            if (this.wasHotbar) {
                InvUtils.swap(this.bow.slot(), true);
            } else {
                InvUtils.move().from(this.mc.player.getInventory().selectedSlot).to(this.prevSlot);
            }
        } else {
            this.wasMainhand = true;
        }
        this.arrowSlots.clear();
        ArrayList<StatusEffect> usedEffects = new ArrayList<StatusEffect>();
        for (int i = this.mc.player.getInventory().size(); i > 0; --i) {
            Iterator effects;
            ItemStack item;
            if (i == this.mc.player.getInventory().selectedSlot || (item = this.mc.player.getInventory().getStack(i)).getItem() != Items.TIPPED_ARROW || !(effects = ((PotionContentsComponent)item.getItem().getComponents().get(DataComponentTypes.POTION_CONTENTS)).getEffects().iterator()).hasNext()) continue;
            StatusEffect effect = (StatusEffect)((StatusEffectInstance)effects.next()).getEffectType().comp_349();
            if (!this.effects.get().contains(effect) || usedEffects.contains(effect) || this.hasEffect(effect) && this.checkEffects.get().booleanValue()) continue;
            usedEffects.add(effect);
            this.arrowSlots.add(i);
        }
    }

    @Override
    public void onDeactivate() {
        if (!this.wasMainhand) {
            if (this.wasHotbar) {
                InvUtils.swapBack();
            } else {
                InvUtils.move().from(this.mc.player.getInventory().selectedSlot).to(this.prevSlot);
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.bow = InvUtils.find(Items.BOW);
        if (!this.shouldQuiver()) {
            return;
        }
        if (this.arrowSlots.isEmpty()) {
            this.toggle();
            return;
        }
        if (this.timer > 0) {
            --this.timer;
            return;
        }
        boolean charging = this.mc.options.useKey.isPressed();
        if (!charging) {
            InvUtils.move().from(this.arrowSlots.getFirst()).to(9);
            this.mc.options.useKey.setPressed(true);
        } else if ((double)BowItem.getPullProgress((int)this.mc.player.getItemUseTime()) >= 0.12) {
            int targetSlot = this.arrowSlots.getFirst();
            this.arrowSlots.removeFirst();
            this.mc.getNetworkHandler().sendPacket((Packet)new PlayerMoveC2SPacket.LookAndOnGround(this.mc.player.getYaw(), -90.0f, this.mc.player.isOnGround()));
            this.mc.options.useKey.setPressed(false);
            this.mc.interactionManager.stopUsingItem((PlayerEntity)this.mc.player);
            if (targetSlot != 9) {
                InvUtils.move().from(9).to(targetSlot);
            }
            this.timer = this.cooldown.get();
        }
    }

    private boolean shouldQuiver() {
        if (!this.bow.found() || !this.bow.isHotbar() && !this.silentBow.get().booleanValue()) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("Couldn't find a usable bow, disabling.", new Object[0]);
            }
            this.toggle();
            return false;
        }
        if (!this.headIsOpen()) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("Not enough space to quiver, disabling.", new Object[0]);
            }
            this.toggle();
            return false;
        }
        if ((double)EntityUtils.getTotalHealth((LivingEntity)this.mc.player) < this.minHealth.get()) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("Not enough health to quiver, disabling.", new Object[0]);
            }
            this.toggle();
            return false;
        }
        if (this.onlyOnGround.get().booleanValue() && !this.mc.player.isOnGround()) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("You are not on the ground, disabling.", new Object[0]);
            }
            this.toggle();
            return false;
        }
        if (this.onlyInHoles.get().booleanValue() && !this.isSurrounded((PlayerEntity)this.mc.player)) {
            if (this.chatInfo.get().booleanValue()) {
                this.error("You are not in a hole, disabling.", new Object[0]);
            }
            this.toggle();
            return false;
        }
        return true;
    }

    private boolean headIsOpen() {
        this.testPos.set((Vec3i)this.mc.player.getBlockPos().add(0, 1, 0));
        BlockState pos1 = this.mc.world.getBlockState((BlockPos)this.testPos);
        if (((AbstractBlockAccessor)pos1.getBlock()).isCollidable()) {
            return false;
        }
        this.testPos.add(0, 1, 0);
        BlockState pos2 = this.mc.world.getBlockState((BlockPos)this.testPos);
        return !((AbstractBlockAccessor)pos2.getBlock()).isCollidable();
    }

    private boolean hasEffect(StatusEffect effect) {
        for (StatusEffectInstance statusEffect : this.mc.player.getStatusEffects()) {
            if (!((StatusEffect)statusEffect.getEffectType().comp_349()).equals(effect)) continue;
            return true;
        }
        return false;
    }

    private boolean isSurrounded(PlayerEntity target) {
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP || dir == Direction.DOWN) continue;
            this.testPos.set((Vec3i)target.getBlockPos()).offset(dir);
            Block block = this.mc.world.getBlockState((BlockPos)this.testPos).getBlock();
            if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK || block == Blocks.RESPAWN_ANCHOR || block == Blocks.CRYING_OBSIDIAN || block == Blocks.NETHERITE_BLOCK) continue;
            return false;
        }
        return true;
    }
}

