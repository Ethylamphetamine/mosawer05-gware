/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.BlockItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package meteordevelopment.meteorclient.systems.modules.movement;

import java.util.function.Predicate;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.Flight;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;

public class NoFall
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<PlacedItem> placedItem;
    private final Setting<PlaceMode> airPlaceMode;
    private final Setting<Boolean> anchor;
    private final Setting<Boolean> antiBounce;
    private boolean placedWater;
    private BlockPos targetPos;
    private int timer;
    private boolean prePathManagerNoFall;

    public NoFall() {
        super(Categories.Movement, "no-fall", "Attempts to prevent you from taking fall damage.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The way you are saved from fall damage.")).defaultValue(Mode.Packet)).build());
        this.placedItem = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("placed-item")).description("Which block to place.")).defaultValue(PlacedItem.Bucket)).visible(() -> this.mode.get() == Mode.Place)).build());
        this.airPlaceMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("air-place-mode")).description("Whether place mode places before you die or before you take damage.")).defaultValue(PlaceMode.BeforeDeath)).visible(() -> this.mode.get() == Mode.AirPlace)).build());
        this.anchor = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anchor")).description("Centers the player and reduces movement when using bucket or air place mode.")).defaultValue(true)).visible(() -> this.mode.get() != Mode.Packet)).build());
        this.antiBounce = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-bounce")).description("Disables bouncing on slime-block and bed upon landing.")).defaultValue(true)).build());
    }

    @Override
    public void onActivate() {
        this.prePathManagerNoFall = PathManagers.get().getSettings().getNoFall().get();
        if (this.mode.get() == Mode.Packet) {
            PathManagers.get().getSettings().getNoFall().set(true);
        }
        this.placedWater = false;
    }

    @Override
    public void onDeactivate() {
        PathManagers.get().getSettings().getNoFall().set(this.prePathManagerNoFall);
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (this.mc.player.getAbilities().creativeMode || !(event.packet instanceof PlayerMoveC2SPacket) || this.mode.get() != Mode.Packet || ((IPlayerMoveC2SPacket)event.packet).getTag() == 1337) {
            return;
        }
        if (!Modules.get().isActive(Flight.class)) {
            if (this.mc.player.isFallFlying()) {
                return;
            }
            if (this.mc.player.getVelocity().y > -0.5) {
                return;
            }
            ((PlayerMoveC2SPacketAccessor)event.packet).setOnGround(true);
        } else {
            ((PlayerMoveC2SPacketAccessor)event.packet).setOnGround(true);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (this.timer > 20) {
            this.placedWater = false;
            this.timer = 0;
        }
        if (this.mc.player.getAbilities().creativeMode) {
            return;
        }
        if (this.mode.get() == Mode.AirPlace) {
            if (!this.airPlaceMode.get().test(this.mc.player.fallDistance)) {
                return;
            }
            if (this.anchor.get().booleanValue()) {
                PlayerUtils.centerPlayer();
            }
            Rotations.rotate(this.mc.player.getYaw(), 90.0, Integer.MAX_VALUE, () -> {
                double preY = this.mc.player.getVelocity().y;
                ((IVec3d)this.mc.player.getVelocity()).setY(0.0);
                BlockUtils.place(this.mc.player.getBlockPos().down(), InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BlockItem), false, 0, true);
                ((IVec3d)this.mc.player.getVelocity()).setY(preY);
            });
        } else if (this.mode.get() == Mode.Place) {
            PlacedItem placedItem1;
            PlacedItem placedItem = placedItem1 = this.mc.world.getDimension().comp_644() && this.placedItem.get() == PlacedItem.Bucket ? PlacedItem.PowderSnow : this.placedItem.get();
            if (this.mc.player.fallDistance > 3.0f && !EntityUtils.isAboveWater((Entity)this.mc.player)) {
                BlockHitResult result;
                Item item = placedItem1.item;
                FindItemResult findItemResult = InvUtils.findInHotbar(item);
                if (!findItemResult.found()) {
                    return;
                }
                if (this.anchor.get().booleanValue()) {
                    PlayerUtils.centerPlayer();
                }
                if ((result = this.mc.world.raycast(new RaycastContext(this.mc.player.getPos(), this.mc.player.getPos().subtract(0.0, 5.0, 0.0), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)this.mc.player))) != null && result.getType() == HitResult.Type.BLOCK) {
                    this.targetPos = result.getBlockPos().up();
                    if (placedItem1 == PlacedItem.Bucket) {
                        this.useItem(findItemResult, true, this.targetPos, true);
                    } else {
                        this.useItem(findItemResult, placedItem1 == PlacedItem.PowderSnow, this.targetPos, false);
                    }
                }
            }
            if (this.placedWater) {
                ++this.timer;
                if (this.mc.player.getBlockStateAtPos().getBlock() == placedItem1.block) {
                    this.useItem(InvUtils.findInHotbar(Items.BUCKET), false, this.targetPos, true);
                } else if (this.mc.world.getBlockState(this.mc.player.getBlockPos().down()).getBlock() == Blocks.POWDER_SNOW && this.mc.player.fallDistance == 0.0f && placedItem1.block == Blocks.POWDER_SNOW) {
                    this.useItem(InvUtils.findInHotbar(Items.BUCKET), false, this.targetPos.down(), true);
                }
            }
        } else if (this.mode.get() == Mode.Elytra) {
            // empty if block
        }
    }

    public boolean cancelBounce() {
        return this.isActive() && this.antiBounce.get() != false;
    }

    private void useItem(FindItemResult item, boolean placedWater, BlockPos blockPos, boolean interactItem) {
        if (!item.found()) {
            return;
        }
        if (interactItem) {
            Rotations.rotate(Rotations.getYaw(blockPos), Rotations.getPitch(blockPos), 10, true, () -> {
                if (item.isOffhand()) {
                    this.mc.interactionManager.interactItem((PlayerEntity)this.mc.player, Hand.OFF_HAND);
                } else {
                    InvUtils.swap(item.slot(), true);
                    this.mc.interactionManager.interactItem((PlayerEntity)this.mc.player, Hand.MAIN_HAND);
                    InvUtils.swapBack();
                }
            });
        } else {
            BlockUtils.place(blockPos, item, true, 10, true);
        }
        this.placedWater = placedWater;
    }

    @Override
    public String getInfoString() {
        return this.mode.get().toString();
    }

    public static enum Mode {
        Packet,
        AirPlace,
        Place,
        Elytra;

    }

    public static enum PlacedItem {
        Bucket(Items.WATER_BUCKET, Blocks.WATER),
        PowderSnow(Items.POWDER_SNOW_BUCKET, Blocks.POWDER_SNOW),
        HayBale(Items.HAY_BLOCK, Blocks.HAY_BLOCK),
        Cobweb(Items.COBWEB, Blocks.COBWEB),
        SlimeBlock(Items.SLIME_BLOCK, Blocks.SLIME_BLOCK);

        private final Item item;
        private final Block block;

        private PlacedItem(Item item, Block block) {
            this.item = item;
            this.block = block;
        }
    }

    public static enum PlaceMode {
        BeforeDamage(height -> height.floatValue() > 2.0f),
        BeforeDeath(height -> height.floatValue() > Math.max(PlayerUtils.getTotalHealth(), 2.0f));

        private final Predicate<Float> fallHeight;

        private PlaceMode(Predicate<Float> fallHeight) {
            this.fallHeight = fallHeight;
        }

        public boolean test(float fallheight) {
            return this.fallHeight.test(Float.valueOf(fallheight));
        }
    }
}

