/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.BlockState
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.item.ElytraItem
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$OnGroundOnly
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package meteordevelopment.meteorclient.systems.modules.movement.elytrafly;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightMode;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Bounce;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Packet;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Pitch40;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Slide;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.modes.Vanilla;
import meteordevelopment.meteorclient.systems.modules.player.ChestSwap;
import meteordevelopment.meteorclient.systems.modules.player.Rotation;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

public class ElytraFly
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgInventory;
    private final SettingGroup sgAutopilot;
    public final Setting<ElytraFlightModes> flightMode;
    public final Setting<Boolean> autoTakeOff;
    public final Setting<Double> fallMultiplier;
    public final Setting<Double> horizontalSpeed;
    public final Setting<Double> verticalSpeed;
    public final Setting<Boolean> acceleration;
    public final Setting<Double> accelerationStep;
    public final Setting<Double> accelerationMin;
    public final Setting<Boolean> stopInWater;
    public final Setting<Boolean> dontGoIntoUnloadedChunks;
    public final Setting<Boolean> autoHover;
    public final Setting<Boolean> noCrash;
    public final Setting<Integer> crashLookAhead;
    private final Setting<Boolean> instaDrop;
    public final Setting<Double> pitch40lowerBounds;
    public final Setting<Double> pitch40upperBounds;
    public final Setting<Double> pitch40rotationSpeed;
    public final Setting<Boolean> autoJump;
    public final Setting<Rotation.LockMode> yawLockMode;
    public final Setting<Double> pitch;
    public final Setting<Double> yaw;
    public final Setting<Boolean> restart;
    public final Setting<Integer> restartDelay;
    public final Setting<Boolean> sprint;
    public final Setting<Double> slideAccel;
    public final Setting<Double> slideMaxSpeed;
    public final Setting<Boolean> replace;
    public final Setting<Integer> replaceDurability;
    public final Setting<ChestSwapMode> chestSwap;
    public final Setting<Boolean> autoReplenish;
    public final Setting<Integer> replenishSlot;
    public final Setting<Boolean> autoPilot;
    public final Setting<Boolean> useFireworks;
    public final Setting<Double> autoPilotFireworkDelay;
    public final Setting<Double> autoPilotMinimumHeight;
    private ElytraFlightMode currentMode;
    private final StaticGroundListener staticGroundListener;
    private final StaticInstaDropListener staticInstadropListener;

    public ElytraFly() {
        super(Categories.Movement, "elytra-fly", "Gives you more control over your elytra.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgInventory = this.settings.createGroup("Inventory");
        this.sgAutopilot = this.settings.createGroup("Autopilot");
        this.flightMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("The mode of flying.")).defaultValue(ElytraFlightModes.Vanilla)).onModuleActivated(flightModesSetting -> this.onModeChanged((ElytraFlightModes)((Object)((Object)flightModesSetting.get()))))).onChanged(this::onModeChanged)).build());
        this.autoTakeOff = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-take-off")).description("Automatically takes off when you hold jump without needing to double jump.")).defaultValue(false)).visible(() -> this.flightMode.get() != ElytraFlightModes.Pitch40 && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.fallMultiplier = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fall-multiplier")).description("Controls how fast will you go down naturally.")).defaultValue(0.01).min(0.0).visible(() -> this.flightMode.get() != ElytraFlightModes.Pitch40 && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.horizontalSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("horizontal-speed")).description("How fast you go forward and backward.")).defaultValue(1.0).min(0.0).visible(() -> this.flightMode.get() != ElytraFlightModes.Pitch40 && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.verticalSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("vertical-speed")).description("How fast you go up and down.")).defaultValue(1.0).min(0.0).visible(() -> this.flightMode.get() != ElytraFlightModes.Pitch40 && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.acceleration = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("acceleration")).defaultValue(false)).visible(() -> this.flightMode.get() != ElytraFlightModes.Pitch40 && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.accelerationStep = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("acceleration-step")).min(0.1).max(5.0).defaultValue(1.0).visible(() -> this.flightMode.get() != ElytraFlightModes.Pitch40 && this.acceleration.get() != false && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.accelerationMin = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("acceleration-start")).min(0.1).defaultValue(0.0).visible(() -> this.flightMode.get() != ElytraFlightModes.Pitch40 && this.acceleration.get() != false && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.stopInWater = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("stop-in-water")).description("Stops flying in water.")).defaultValue(true)).visible(() -> this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.dontGoIntoUnloadedChunks = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("no-unloaded-chunks")).description("Stops you from going into unloaded chunks.")).defaultValue(true)).build());
        this.autoHover = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-hover")).description("Automatically hover .3 blocks off ground when holding shift.")).defaultValue(false)).visible(() -> this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.noCrash = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("no-crash")).description("Stops you from going into walls.")).defaultValue(false)).visible(() -> this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.crashLookAhead = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("crash-look-ahead")).description("Distance to look ahead when flying.")).defaultValue(5)).range(1, 15).sliderMin(1).visible(() -> this.noCrash.get() != false && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.instaDrop = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("insta-drop")).description("Makes you drop out of flight instantly.")).defaultValue(false)).visible(() -> this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.pitch40lowerBounds = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("pitch40-lower-bounds")).description("The bottom height boundary for pitch40.")).defaultValue(80.0).min(-128.0).sliderMax(360.0).visible(() -> this.flightMode.get() == ElytraFlightModes.Pitch40)).build());
        this.pitch40upperBounds = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("pitch40-upper-bounds")).description("The upper height boundary for pitch40.")).defaultValue(120.0).min(-128.0).sliderMax(360.0).visible(() -> this.flightMode.get() == ElytraFlightModes.Pitch40)).build());
        this.pitch40rotationSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("pitch40-rotate-speed")).description("The speed for pitch rotation (degrees per tick)")).defaultValue(4.0).min(1.0).sliderMax(6.0).visible(() -> this.flightMode.get() == ElytraFlightModes.Pitch40)).build());
        this.autoJump = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-jump")).description("Automatically jumps for you.")).defaultValue(true)).visible(() -> this.flightMode.get() == ElytraFlightModes.Bounce)).build());
        this.yawLockMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("yaw-lock")).description("Whether to enable yaw lock or not")).defaultValue(Rotation.LockMode.Smart)).visible(() -> this.flightMode.get() == ElytraFlightModes.Bounce)).build());
        this.pitch = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("pitch")).description("The pitch angle to look at when using the bounce mode.")).defaultValue(85.0).range(0.0, 90.0).sliderRange(0.0, 90.0).visible(() -> this.flightMode.get() == ElytraFlightModes.Bounce)).build());
        this.yaw = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("yaw")).description("The yaw angle to look at when using simple rotation lock in bounce mode.")).defaultValue(0.0).range(0.0, 360.0).sliderRange(0.0, 360.0).visible(() -> this.flightMode.get() == ElytraFlightModes.Bounce && this.yawLockMode.get() == Rotation.LockMode.Simple)).build());
        this.restart = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("restart")).description("Restarts flying with the elytra when rubberbanding.")).defaultValue(true)).visible(() -> this.flightMode.get() == ElytraFlightModes.Bounce)).build());
        this.restartDelay = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("restart-delay")).description("How many ticks to wait before restarting the elytra again after rubberbanding.")).defaultValue(7)).min(0).sliderRange(0, 20).visible(() -> this.flightMode.get() == ElytraFlightModes.Bounce && this.restart.get() != false)).build());
        this.sprint = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sprint")).description("Sprints all the time. If turned off, it will only sprint when the player is touching the ground.")).defaultValue(true)).visible(() -> this.flightMode.get() == ElytraFlightModes.Bounce)).build());
        this.slideAccel = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("slide-accel-speed")).description("The acceleration of slide.")).defaultValue(7.0).min(0.0).sliderMax(5.0).visible(() -> this.flightMode.get() == ElytraFlightModes.Slide)).build());
        this.slideMaxSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("slide-max-speed")).description("The maximum speed of slide")).defaultValue(7.0).min(0.0).sliderMax(200.0).visible(() -> this.flightMode.get() == ElytraFlightModes.Slide)).build());
        this.replace = this.sgInventory.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("elytra-replace")).description("Replaces broken elytra with a new elytra.")).defaultValue(false)).build());
        this.replaceDurability = this.sgInventory.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("replace-durability")).description("The durability threshold your elytra will be replaced at.")).defaultValue(2)).range(1, (Integer)Items.ELYTRA.getComponents().get(DataComponentTypes.MAX_DAMAGE) - 1).sliderRange(1, (Integer)Items.ELYTRA.getComponents().get(DataComponentTypes.MAX_DAMAGE) - 1).visible(this.replace::get)).build());
        this.chestSwap = this.sgInventory.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("chest-swap")).description("Enables ChestSwap when toggling this module.")).defaultValue(ChestSwapMode.Never)).build());
        this.autoReplenish = this.sgInventory.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("replenish-fireworks")).description("Moves fireworks into a selected hotbar slot.")).defaultValue(false)).build());
        this.replenishSlot = this.sgInventory.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("replenish-slot")).description("The slot auto move moves fireworks to.")).defaultValue(9)).range(1, 9).sliderRange(1, 9).visible(this.autoReplenish::get)).build());
        this.autoPilot = this.sgAutopilot.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-pilot")).description("Moves forward while elytra flying.")).defaultValue(false)).visible(() -> this.flightMode.get() != ElytraFlightModes.Pitch40 && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.useFireworks = this.sgAutopilot.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("use-fireworks")).description("Uses firework rockets every second of your choice.")).defaultValue(false)).visible(() -> this.autoPilot.get() != false && this.flightMode.get() != ElytraFlightModes.Pitch40 && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.autoPilotFireworkDelay = this.sgAutopilot.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("firework-delay")).description("The delay in seconds in between using fireworks if \"Use Fireworks\" is enabled.")).min(1.0).defaultValue(8.0).sliderMax(20.0).visible(() -> this.useFireworks.get() != false && this.flightMode.get() != ElytraFlightModes.Pitch40 && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.autoPilotMinimumHeight = this.sgAutopilot.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("minimum-height")).description("The minimum height for autopilot.")).defaultValue(120.0).min(-128.0).sliderMax(260.0).visible(() -> this.autoPilot.get() != false && this.flightMode.get() != ElytraFlightModes.Pitch40 && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide)).build());
        this.currentMode = new Vanilla();
        this.staticGroundListener = new StaticGroundListener();
        this.staticInstadropListener = new StaticInstaDropListener();
    }

    @Override
    public void onActivate() {
        this.currentMode.onActivate();
        if ((this.chestSwap.get() == ChestSwapMode.Always || this.chestSwap.get() == ChestSwapMode.WaitForGround) && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() != Items.ELYTRA && this.isActive()) {
            Modules.get().get(ChestSwap.class).swap();
        }
    }

    @Override
    public void onDeactivate() {
        if (this.autoPilot.get().booleanValue()) {
            this.mc.options.forwardKey.setPressed(false);
        }
        if (this.chestSwap.get() == ChestSwapMode.Always && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            Modules.get().get(ChestSwap.class).swap();
        } else if (this.chestSwap.get() == ChestSwapMode.WaitForGround) {
            this.enableGroundListener();
        }
        if (this.mc.player.isFallFlying() && this.instaDrop.get().booleanValue()) {
            this.enableInstaDropListener();
        }
        this.currentMode.onDeactivate();
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (!(this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof ElytraItem)) {
            return;
        }
        this.currentMode.autoTakeoff();
        if (this.mc.player.isFallFlying()) {
            if (this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
                this.currentMode.velX = 0.0;
                this.currentMode.velY = event.movement.y;
                this.currentMode.velZ = 0.0;
                this.currentMode.forward = Vec3d.fromPolar((float)0.0f, (float)this.mc.player.getYaw()).multiply(0.1);
                this.currentMode.right = Vec3d.fromPolar((float)0.0f, (float)(this.mc.player.getYaw() + 90.0f)).multiply(0.1);
                if (this.mc.player.isTouchingWater() && this.stopInWater.get().booleanValue()) {
                    this.mc.getNetworkHandler().sendPacket((net.minecraft.network.packet.Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    return;
                }
                this.currentMode.handleFallMultiplier();
                this.currentMode.handleAutopilot();
                this.currentMode.handleAcceleration();
                this.currentMode.handleHorizontalSpeed(event);
                this.currentMode.handleVerticalSpeed(event);
            }
            int chunkX = (int)((this.mc.player.getX() + this.currentMode.velX) / 16.0);
            int chunkZ = (int)((this.mc.player.getZ() + this.currentMode.velZ) / 16.0);
            if (this.dontGoIntoUnloadedChunks.get().booleanValue()) {
                if (this.mc.world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
                    if (this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
                        ((IVec3d)event.movement).set(this.currentMode.velX, this.currentMode.velY, this.currentMode.velZ);
                    }
                } else {
                    this.currentMode.zeroAcceleration();
                    ((IVec3d)event.movement).set(0.0, this.currentMode.velY, 0.0);
                }
            } else if (this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
                ((IVec3d)event.movement).set(this.currentMode.velX, this.currentMode.velY, this.currentMode.velZ);
            }
            if (this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
                this.currentMode.onPlayerMove();
            }
        } else if (this.currentMode.lastForwardPressed && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
            this.mc.options.forwardKey.setPressed(false);
            this.currentMode.lastForwardPressed = false;
        }
        if (this.noCrash.get().booleanValue() && this.mc.player.isFallFlying() && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
            Vec3d lookAheadPos = this.mc.player.getPos().add(this.mc.player.getVelocity().normalize().multiply((double)this.crashLookAhead.get().intValue()));
            RaycastContext raycastContext = new RaycastContext(this.mc.player.getPos(), new Vec3d(lookAheadPos.getX(), this.mc.player.getY(), lookAheadPos.getZ()), RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, (Entity)this.mc.player);
            BlockHitResult hitResult = this.mc.world.raycast(raycastContext);
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                ((IVec3d)event.movement).set(0.0, this.currentMode.velY, 0.0);
            }
        }
        if (this.autoHover.get().booleanValue() && this.mc.player.input.sneaking && !Modules.get().get(Freecam.class).isActive() && this.mc.player.isFallFlying() && this.flightMode.get() != ElytraFlightModes.Bounce && this.flightMode.get() != ElytraFlightModes.Slide) {
            boolean under2Collidable;
            BlockState underState = this.mc.world.getBlockState(this.mc.player.getBlockPos().down());
            Block under = underState.getBlock();
            BlockState under2State = this.mc.world.getBlockState(this.mc.player.getBlockPos().down().down());
            Block under2 = under2State.getBlock();
            boolean underCollidable = under.collidable || !underState.getFluidState().isEmpty();
            boolean bl = under2Collidable = under2.collidable || !under2State.getFluidState().isEmpty();
            if (!underCollidable && under2Collidable) {
                ((IVec3d)event.movement).set(event.movement.x, -0.1f, event.movement.z);
                this.mc.player.setPitch(MathHelper.clamp((float)this.mc.player.getPitch(0.0f), (float)-50.0f, (float)20.0f));
            }
            if (underCollidable) {
                ((IVec3d)event.movement).set(event.movement.x, -0.03f, event.movement.z);
                this.mc.player.setPitch(MathHelper.clamp((float)this.mc.player.getPitch(0.0f), (float)-50.0f, (float)20.0f));
                if (this.mc.player.getPos().y <= (double)((float)this.mc.player.getBlockPos().down().getY() + 1.34f)) {
                    ((IVec3d)event.movement).set(event.movement.x, 0.0, event.movement.z);
                    this.mc.player.setSneaking(false);
                    this.mc.player.input.sneaking = false;
                }
            }
        }
    }

    public boolean canPacketEfly() {
        return this.isActive() && this.flightMode.get() == ElytraFlightModes.Packet && this.mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof ElytraItem && !this.mc.player.isOnGround();
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        this.currentMode.onTick();
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        this.currentMode.onPreTick();
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        this.currentMode.onPacketSend(event);
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        this.currentMode.onPacketReceive(event);
    }

    private void onModeChanged(ElytraFlightModes mode) {
        switch (mode) {
            case Vanilla: {
                this.currentMode = new Vanilla();
                break;
            }
            case Packet: {
                this.currentMode = new Packet();
                break;
            }
            case Pitch40: {
                this.currentMode = new Pitch40();
                this.autoPilot.set(false);
                break;
            }
            case Bounce: {
                this.currentMode = new Bounce();
                break;
            }
            case Slide: {
                this.currentMode = new Slide();
            }
        }
    }

    protected void enableGroundListener() {
        MeteorClient.EVENT_BUS.subscribe(this.staticGroundListener);
    }

    protected void disableGroundListener() {
        MeteorClient.EVENT_BUS.unsubscribe(this.staticGroundListener);
    }

    protected void enableInstaDropListener() {
        MeteorClient.EVENT_BUS.subscribe(this.staticInstadropListener);
    }

    protected void disableInstaDropListener() {
        MeteorClient.EVENT_BUS.unsubscribe(this.staticInstadropListener);
    }

    @Override
    public String getInfoString() {
        return this.currentMode.getHudString();
    }

    public static enum ChestSwapMode {
        Always,
        Never,
        WaitForGround;

    }

    private class StaticGroundListener {
        private StaticGroundListener() {
        }

        @EventHandler
        private void chestSwapGroundListener(PlayerMoveEvent event) {
            if (((ElytraFly)ElytraFly.this).mc.player != null && ((ElytraFly)ElytraFly.this).mc.player.isOnGround() && ((ElytraFly)ElytraFly.this).mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
                Modules.get().get(ChestSwap.class).swap();
                ElytraFly.this.disableGroundListener();
            }
        }
    }

    private class StaticInstaDropListener {
        private StaticInstaDropListener() {
        }

        @EventHandler
        private void onInstadropTick(TickEvent.Post event) {
            if (((ElytraFly)ElytraFly.this).mc.player != null && ((ElytraFly)ElytraFly.this).mc.player.isFallFlying()) {
                ((ElytraFly)ElytraFly.this).mc.player.setVelocity(0.0, 0.0, 0.0);
                ((ElytraFly)ElytraFly.this).mc.player.networkHandler.sendPacket((net.minecraft.network.packet.Packet)new PlayerMoveC2SPacket.OnGroundOnly(true));
            } else {
                ElytraFly.this.disableInstaDropListener();
            }
        }
    }

    public static enum AutoPilotMode {
        Vanilla,
        Pitch40;

    }
}

