/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.entity.effect.StatusEffectInstance
 *  net.minecraft.entity.effect.StatusEffects
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket$Action
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.BlockView
 */
package meteordevelopment.meteorclient.systems.modules.player;

import java.util.List;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.ClientPlayerInteractionManagerAccessor;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class SpeedMine
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<Mode> mode;
    private final Setting<List<Block>> blocks;
    private final Setting<ListMode> blocksFilter;
    public final Setting<Double> modifier;
    private final Setting<Integer> hasteAmplifier;
    private final Setting<Boolean> instamine;
    private final Setting<Boolean> grimBypass;

    public SpeedMine() {
        super(Categories.Player, "speed-mine", "Allows you to quickly mine blocks.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).defaultValue(Mode.Damage)).onChanged(mode -> this.removeHaste())).build());
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("Selected blocks.")).filter((Block block) -> block.getHardness() > 0.0f).visible(() -> this.mode.get() != Mode.Haste)).build());
        this.blocksFilter = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("blocks-filter")).description("How to use the blocks setting.")).defaultValue(ListMode.Blacklist)).visible(() -> this.mode.get() != Mode.Haste)).build());
        this.modifier = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("modifier")).description("Mining speed modifier. An additional value of 0.2 is equivalent to one haste level (1.2 = haste 1).")).defaultValue(1.4).visible(() -> this.mode.get() == Mode.Normal)).min(0.0).build());
        this.hasteAmplifier = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("haste-amplifier")).description("What value of haste to give you. Above 2 not recommended.")).defaultValue(2)).min(1).visible(() -> this.mode.get() == Mode.Haste)).onChanged(i -> this.removeHaste())).build());
        this.instamine = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("instamine")).description("Whether or not to instantly mine blocks under certain conditions.")).defaultValue(true)).visible(() -> this.mode.get() == Mode.Damage)).build());
        this.grimBypass = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("grim-bypass")).description("Bypasses Grim's fastbreak check, working as of 2.3.58")).defaultValue(false)).visible(() -> this.mode.get() == Mode.Damage)).build());
    }

    @Override
    public void onDeactivate() {
        this.removeHaste();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) {
            return;
        }
        if (this.mode.get() == Mode.Haste) {
            StatusEffectInstance haste = this.mc.player.getStatusEffect(StatusEffects.HASTE);
            if (haste == null || haste.getAmplifier() <= this.hasteAmplifier.get() - 1) {
                this.mc.player.setStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, -1, this.hasteAmplifier.get() - 1, false, false, false), null);
            }
        } else if (this.mode.get() == Mode.Damage) {
            ClientPlayerInteractionManagerAccessor im = (ClientPlayerInteractionManagerAccessor)this.mc.interactionManager;
            float progress = im.getBreakingProgress();
            BlockPos pos = im.getCurrentBreakingBlockPos();
            if (pos == null || progress <= 0.0f) {
                return;
            }
            if (progress + this.mc.world.getBlockState(pos).calcBlockBreakingDelta((PlayerEntity)this.mc.player, (BlockView)this.mc.world, pos) >= 0.7f) {
                im.setCurrentBreakingProgress(1.0f);
            }
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        PlayerActionC2SPacket packet;
        if (this.mode.get() != Mode.Damage || !this.grimBypass.get().booleanValue()) {
            return;
        }
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof PlayerActionC2SPacket && (packet = (PlayerActionC2SPacket)packet2).getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
            this.mc.getNetworkHandler().sendPacket((Packet)new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos().up(), packet.getDirection()));
        }
    }

    private void removeHaste() {
        if (!Utils.canUpdate()) {
            return;
        }
        StatusEffectInstance haste = this.mc.player.getStatusEffect(StatusEffects.HASTE);
        if (haste != null && !haste.shouldShowIcon()) {
            this.mc.player.removeStatusEffect(StatusEffects.HASTE);
        }
    }

    public boolean filter(Block block) {
        if (this.blocksFilter.get() == ListMode.Blacklist && !this.blocks.get().contains(block)) {
            return true;
        }
        return this.blocksFilter.get() == ListMode.Whitelist && this.blocks.get().contains(block);
    }

    public boolean instamine() {
        return this.isActive() && this.mode.get() == Mode.Damage && this.instamine.get() != false;
    }

    public static enum Mode {
        Normal,
        Haste,
        Damage;

    }

    public static enum ListMode {
        Whitelist,
        Blacklist;

    }
}

