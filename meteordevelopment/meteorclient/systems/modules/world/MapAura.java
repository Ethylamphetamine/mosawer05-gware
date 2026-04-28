/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.decoration.ItemFrameEntity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.FilledMapItem
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket
 *  net.minecraft.util.ActionResult
 *  net.minecraft.util.Hand
 *  net.minecraft.util.TypeFilter
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.EntityHitResult
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.World
 */
package meteordevelopment.meteorclient.systems.modules.world;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MapAura
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> placeRange;
    private final Setting<Double> placeDelay;
    private final BlockPos.Mutable mutablePos;
    private final Map<BlockPos, Long> timeOfLastPlace;
    private final Map<Integer, Long> timeOfLastMapInteract;

    public MapAura() {
        super(Categories.World, "map-aura", "Places maps and item frames on every surface");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.placeRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-range")).description("How far you can reach")).defaultValue(4.0).min(0.0).sliderMax(6.0).build());
        this.placeDelay = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-delay")).description("How many seconds to wait between placing in the same spot")).defaultValue(0.2).min(0.0).sliderMax(2.0).build());
        this.mutablePos = new BlockPos.Mutable();
        this.timeOfLastPlace = new HashMap<BlockPos, Long>();
        this.timeOfLastMapInteract = new HashMap<Integer, Long>();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        FindItemResult mapItemResult;
        FindItemResult itemFrameResult = InvUtils.findInHotbar(Items.ITEM_FRAME);
        if (itemFrameResult.found()) {
            InvUtils.swap(itemFrameResult.slot(), true);
            this.placeNextItemFrame();
            InvUtils.swapBack();
        }
        if ((mapItemResult = InvUtils.findInHotbar(item -> item.getItem() instanceof FilledMapItem && item.getCount() > 1)).found()) {
            InvUtils.swap(mapItemResult.slot(), true);
            this.placeNextMap();
            InvUtils.swapBack();
        }
    }

    private boolean placeNextItemFrame() {
        long currentTime = System.currentTimeMillis();
        int r = (int)Math.floor(this.placeRange.get());
        BlockPos eyePos = BlockPos.ofFloored((Position)this.mc.player.getEyePos());
        int ex = eyePos.getX();
        int ey = eyePos.getY();
        int ez = eyePos.getZ();
        for (int x = -r; x <= r; ++x) {
            for (int y = -r; y <= r; ++y) {
                for (int z = -r; z <= r; ++z) {
                    BlockPos.Mutable blockPos = this.mutablePos.set(ex + x, ey + y, ez + z);
                    BlockState state = this.mc.world.getBlockState((BlockPos)blockPos);
                    if (state.isAir() || this.timeOfLastPlace.containsKey(blockPos) && ((double)currentTime - (double)this.timeOfLastPlace.get(blockPos).longValue()) / 1000.0 < this.placeDelay.get()) continue;
                    for (Direction dir : Direction.values()) {
                        BlockPos neighbour = blockPos.offset(dir);
                        if (!this.mc.world.getBlockState(neighbour).isAir() || !World.isValid((BlockPos)neighbour) || neighbour.getY() < -64) continue;
                        Vec3d hitPos = blockPos.toCenterPos().add((double)dir.getOffsetX() * 0.5, (double)dir.getOffsetY() * 0.5, (double)dir.getOffsetZ() * 0.5);
                        List entities = this.mc.world.getEntitiesByType(TypeFilter.instanceOf(ItemFrameEntity.class), Box.of((Vec3d)hitPos, (double)0.1, (double)0.1, (double)0.1), entity -> true);
                        if (!entities.isEmpty()) continue;
                        this.mc.getNetworkHandler().sendPacket((Packet)new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, new BlockHitResult(hitPos, dir, (BlockPos)blockPos, false), this.mc.world.getPendingUpdateManager().incrementSequence().getSequence()));
                        this.timeOfLastPlace.put((BlockPos)blockPos, currentTime);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean placeNextMap() {
        List entities = this.mc.world.getEntitiesByType(TypeFilter.instanceOf(ItemFrameEntity.class), Box.of((Vec3d)this.mc.player.getEyePos(), (double)(this.placeRange.get() * 2.0), (double)(this.placeRange.get() * 2.0), (double)(this.placeRange.get() * 2.0)), this::checkEntity);
        if (entities.isEmpty()) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        ItemFrameEntity entity = (ItemFrameEntity)entities.getFirst();
        MeteorClient.ROTATION.requestRotation(this.getClosestPointOnBox(entity.getBoundingBox(), this.mc.player.getEyePos()), 5.0);
        if (!MeteorClient.ROTATION.lookingAt(entity.getBoundingBox())) {
            return false;
        }
        if (this.timeOfLastMapInteract.containsKey(entity.getId()) && ((double)currentTime - (double)this.timeOfLastMapInteract.get(entity.getId()).longValue()) / 1000.0 < this.placeDelay.get()) {
            return false;
        }
        EntityHitResult entityHitResult = new EntityHitResult((Entity)entity, this.getClosestPointOnBox(entity.getBoundingBox(), this.mc.player.getEyePos()));
        ActionResult actionResult = this.mc.interactionManager.interactEntityAtLocation((PlayerEntity)this.mc.player, (Entity)entity, entityHitResult, Hand.MAIN_HAND);
        if (!actionResult.isAccepted()) {
            actionResult = this.mc.interactionManager.interactEntity((PlayerEntity)this.mc.player, (Entity)entity, Hand.MAIN_HAND);
        }
        if (actionResult.isAccepted() && actionResult.shouldSwingHand()) {
            this.mc.getNetworkHandler().sendPacket((Packet)new HandSwingC2SPacket(Hand.MAIN_HAND));
        }
        this.info("Placed map", new Object[0]);
        this.timeOfLastMapInteract.put(entity.getId(), currentTime);
        return true;
    }

    private boolean checkEntity(Entity entity) {
        if (entity instanceof ItemFrameEntity) {
            ItemFrameEntity itemFrame = (ItemFrameEntity)entity;
            if (!this.getClosestPointOnBox(entity.getBoundingBox(), this.mc.player.getEyePos()).isWithinRangeOf(this.mc.player.getEyePos(), this.placeRange.get().doubleValue(), this.placeRange.get().doubleValue())) {
                return false;
            }
            return itemFrame.getHeldItemStack() == null || itemFrame.getHeldItemStack().isEmpty();
        }
        return false;
    }

    public Vec3d getClosestPointOnBox(Box box, Vec3d point) {
        double x = Math.max(box.minX, Math.min(point.x, box.maxX));
        double y = Math.max(box.minY, Math.min(point.y, box.maxY));
        double z = Math.max(box.minZ, Math.min(point.z, box.maxZ));
        return new Vec3d(x, y, z);
    }
}

