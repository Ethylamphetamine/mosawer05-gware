/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.AbstractCauldronBlock
 *  net.minecraft.block.AbstractFireBlock
 *  net.minecraft.block.AbstractPressurePlateBlock
 *  net.minecraft.block.AbstractRailBlock
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.block.CactusBlock
 *  net.minecraft.block.CampfireBlock
 *  net.minecraft.block.CobwebBlock
 *  net.minecraft.block.HoneyBlock
 *  net.minecraft.block.PowderSnowBlock
 *  net.minecraft.block.SweetBerryBushBlock
 *  net.minecraft.block.TrapdoorBlock
 *  net.minecraft.block.TripwireBlock
 *  net.minecraft.block.TripwireHookBlock
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket
 *  net.minecraft.util.shape.VoxelShapes
 */
package meteordevelopment.meteorclient.systems.modules.world;

import java.util.List;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.CollisionShapeEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.CobwebBlock;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.TripwireBlock;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.shape.VoxelShapes;

public class Collisions
extends Module {
    private final SettingGroup sgGeneral;
    public final Setting<List<Block>> blocks;
    private final Setting<Boolean> magma;
    private final Setting<Boolean> unloadedChunks;
    private final Setting<Boolean> ignoreBorder;

    public Collisions() {
        super(Categories.World, "collisions", "Adds collision boxes to certain blocks/areas.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("blocks")).description("What blocks should be added collision box.")).filter(this::blockFilter).build());
        this.magma = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("magma")).description("Prevents you from walking over magma blocks.")).defaultValue(false)).build());
        this.unloadedChunks = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("unloaded-chunks")).description("Stops you from going into unloaded chunks.")).defaultValue(false)).build());
        this.ignoreBorder = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-border")).description("Removes world border collision.")).defaultValue(false)).build());
    }

    @EventHandler
    private void onCollisionShape(CollisionShapeEvent event) {
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        if (!event.state.getFluidState().isEmpty()) {
            return;
        }
        if (this.blocks.get().contains(event.state.getBlock())) {
            event.shape = VoxelShapes.fullCube();
        } else if (this.magma.get().booleanValue() && !this.mc.player.isSneaking() && event.state.isAir() && this.mc.world.getBlockState(event.pos.down()).getBlock() == Blocks.MAGMA_BLOCK) {
            event.shape = VoxelShapes.fullCube();
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        int x = (int)(this.mc.player.getX() + event.movement.x) >> 4;
        int z = (int)(this.mc.player.getZ() + event.movement.z) >> 4;
        if (this.unloadedChunks.get().booleanValue() && !this.mc.world.getChunkManager().isChunkLoaded(x, z)) {
            ((IVec3d)event.movement).set(0.0, event.movement.y, 0.0);
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (!this.unloadedChunks.get().booleanValue()) {
            return;
        }
        Packet<?> packet = event.packet;
        if (packet instanceof VehicleMoveC2SPacket) {
            VehicleMoveC2SPacket packet2 = (VehicleMoveC2SPacket)packet;
            if (!this.mc.world.getChunkManager().isChunkLoaded((int)packet2.getX() >> 4, (int)packet2.getZ() >> 4)) {
                this.mc.player.getVehicle().updatePosition(this.mc.player.getVehicle().prevX, this.mc.player.getVehicle().prevY, this.mc.player.getVehicle().prevZ);
                event.cancel();
            }
        } else {
            packet = event.packet;
            if (packet instanceof PlayerMoveC2SPacket) {
                PlayerMoveC2SPacket packet3 = (PlayerMoveC2SPacket)packet;
                if (!this.mc.world.getChunkManager().isChunkLoaded((int)packet3.getX(this.mc.player.getX()) >> 4, (int)packet3.getZ(this.mc.player.getZ()) >> 4)) {
                    event.cancel();
                }
            }
        }
    }

    private boolean blockFilter(Block block) {
        return block instanceof AbstractFireBlock || block instanceof AbstractPressurePlateBlock || block instanceof TripwireBlock || block instanceof TripwireHookBlock || block instanceof CobwebBlock || block instanceof CampfireBlock || block instanceof SweetBerryBushBlock || block instanceof CactusBlock || block instanceof AbstractRailBlock || block instanceof TrapdoorBlock || block instanceof PowderSnowBlock || block instanceof AbstractCauldronBlock || block instanceof HoneyBlock;
    }

    public boolean ignoreBorder() {
        return this.isActive() && this.ignoreBorder.get() != false;
    }
}

