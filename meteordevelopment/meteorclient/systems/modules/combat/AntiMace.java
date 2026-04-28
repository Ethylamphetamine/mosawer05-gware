/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.AxeItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.item.MaceItem
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket
 *  net.minecraft.registry.Registries
 *  net.minecraft.sound.SoundEvents
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.Identifier
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Position
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BlockListSetting;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.TargetUtils;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MaceItem;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class AntiMace
extends Module {
    private static final double GRAVITY = -0.08;
    private static final double TERMINAL_VELOCITY = -3.92;
    private static final double DRAG_XZ_FLY = 0.99;
    private static final double DRAG_Y_FLY = 0.98;
    private static final double ALIGN_D = 1.5;
    private static final double ALIGN_E = 0.01;
    private static final double LOOK_PUSH = 0.1;
    private static final int BOOST_DURATION_TICKS = 40;
    private static final double ELYTRA_GRAVITY = -0.04;
    private final SettingGroup sgGeneral;
    private final SettingGroup sgPrediction;
    private final SettingGroup sgRender;
    private final Setting<List<Block>> blocks;
    private final Setting<Boolean> onlyAbove;
    private final Setting<Keybind> toggleMaceReq;
    private final Setting<Boolean> pauseEat;
    private final Setting<Boolean> predictionEnabled;
    private final Setting<Integer> fallPredictionTicks;
    private final Setting<Integer> elytraPredictionTicks;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> predictColor;
    private final Setting<SettingColor> placementColor;
    private PlayerEntity target;
    private Vec3d lastPredictedCenter;
    private final List<BlockPos> lastPlacedThisTick;
    private final Map<UUID, Vec3d> lastPos;
    private final Map<UUID, Vec3d> estVel;
    private final Map<UUID, Long> lastServerTick;
    private final Map<UUID, Integer> boostingTicks;
    private UUID engagedTargetId;
    private boolean placedOnce;
    private double lastPredictedDistance;
    private boolean maceRequired;
    private boolean maceToggleKeyDown;

    public AntiMace() {
        super(Categories.Combat, "anti-mace", "Places a 7-block pattern at the predicted position of a falling/elytra mace attacker.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgPrediction = this.settings.createGroup("Prediction");
        this.sgRender = this.settings.createGroup("Render");
        this.blocks = this.sgGeneral.add(((BlockListSetting.Builder)((BlockListSetting.Builder)new BlockListSetting.Builder().name("whitelist")).description("Blocks to place.")).defaultValue(Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN).build());
        this.onlyAbove = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-above")).description("Only target players above you.")).defaultValue(true)).build());
        this.toggleMaceReq = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("toggle-mace-req")).description("Keybind to toggle the requirement for the target to hold a mace.")).defaultValue(Keybind.none())).build());
        this.pauseEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-eat")).description("Pause while using an item (e.g., eating).")).defaultValue(true)).build());
        this.predictionEnabled = this.sgPrediction.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("prediction")).description("Predict with vanilla-like physics (no ground raycast).")).defaultValue(true)).build());
        this.fallPredictionTicks = this.sgPrediction.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("fall-prediction-ticks")).description("Ticks to simulate ahead while falling.")).defaultValue(3)).min(0).sliderMax(20).visible(this.predictionEnabled::get)).build());
        this.elytraPredictionTicks = this.sgPrediction.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("elytra-prediction-ticks")).description("Ticks to simulate ahead while elytra flying.")).defaultValue(3)).min(0).sliderMax(20).visible(this.predictionEnabled::get)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Render predicted place position and last placed blocks.")).defaultValue(true)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.predictColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("predict-color")).description("Predicted place point color.")).defaultValue(new SettingColor(255, 140, 0, 35)).build());
        this.placementColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("placement-color")).description("Color of the placed blocks.")).defaultValue(new SettingColor(0, 200, 255, 35)).build());
        this.lastPlacedThisTick = new ArrayList<BlockPos>();
        this.lastPos = new HashMap<UUID, Vec3d>();
        this.estVel = new HashMap<UUID, Vec3d>();
        this.lastServerTick = new HashMap<UUID, Long>();
        this.boostingTicks = new HashMap<UUID, Integer>();
        this.lastPredictedDistance = 0.0;
        this.maceRequired = true;
        this.maceToggleKeyDown = false;
    }

    @Override
    public void onActivate() {
        this.target = null;
        this.lastPredictedCenter = null;
        this.lastPlacedThisTick.clear();
        this.lastPos.clear();
        this.estVel.clear();
        this.lastServerTick.clear();
        this.boostingTicks.clear();
        this.engagedTargetId = null;
        this.placedOnce = false;
        this.lastPredictedDistance = 0.0;
        this.maceRequired = true;
        this.maceToggleKeyDown = false;
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        PlaySoundS2CPacket packet;
        if (this.mc.world == null || this.mc.world.getPlayers().isEmpty()) {
            return;
        }
        Packet<?> packet2 = event.packet;
        if (packet2 instanceof PlaySoundS2CPacket && (packet = (PlaySoundS2CPacket)packet2).getSound().comp_349() == SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH) {
            Vec3d soundPos = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
            for (PlayerEntity player : this.mc.world.getPlayers()) {
                if (!(player.getPos().squaredDistanceTo(soundPos) < 9.0)) continue;
                this.boostingTicks.put(player.getUuid(), 40);
                break;
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.boostingTicks.isEmpty()) {
            this.boostingTicks.entrySet().removeIf(entry -> {
                int ticks = (Integer)entry.getValue() - 1;
                if (ticks <= 0) {
                    return true;
                }
                entry.setValue(ticks);
                return false;
            });
        }
        if (this.mc.currentScreen == null && this.toggleMaceReq.get().isPressed()) {
            if (!this.maceToggleKeyDown) {
                this.maceRequired = !this.maceRequired;
                this.sendChatFeedback(this.maceRequired ? "enabled" : "disabled");
                this.maceToggleKeyDown = true;
            }
        } else {
            this.maceToggleKeyDown = false;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        double distanceToRawFuture;
        this.lastPredictedCenter = null;
        this.lastPlacedThisTick.clear();
        this.lastPredictedDistance = 0.0;
        if (this.mc.world == null || this.mc.player == null) {
            return;
        }
        if (this.pauseEat.get().booleanValue() && this.mc.player.isUsingItem()) {
            return;
        }
        this.target = this.findTarget();
        if (this.target == null) {
            this.engagedTargetId = null;
            return;
        }
        if (this.engagedTargetId == null || !this.engagedTargetId.equals(this.target.getUuid())) {
            this.engagedTargetId = this.target.getUuid();
            this.placedOnce = false;
        }
        UUID id = this.target.getUuid();
        long svTime = this.mc.world.getTime();
        Vec3d currPos = this.target.getPos();
        Vec3d prevPos = this.lastPos.get(id);
        Long lastSv = this.lastServerTick.get(id);
        if (prevPos != null && lastSv != null) {
            long dt = Math.max(1L, svTime - lastSv);
            Vec3d delta = currPos.subtract(prevPos);
            Vec3d v = new Vec3d(delta.x / (double)dt, delta.y / (double)dt, delta.z / (double)dt);
            this.estVel.put(id, v);
        } else {
            this.estVel.put(id, this.target.getVelocity());
        }
        this.lastPos.put(id, currPos);
        this.lastServerTick.put(id, svTime);
        boolean elytra = this.target.isFallFlying();
        Vec3d future = this.predictionEnabled.get().booleanValue() ? (elytra ? (this.boostingTicks.containsKey(id) ? this.simulateBoostedElytraFuturePos(this.target, this.elytraPredictionTicks.get()) : this.simulateElytraFuturePos(this.target, this.elytraPredictionTicks.get())) : this.simulateFallFrom(currPos, this.estVel.getOrDefault(id, this.target.getVelocity()), this.fallPredictionTicks.get())) : currPos;
        Vec3d eyes = this.mc.player.getEyePos();
        this.lastPredictedDistance = distanceToRawFuture = eyes.distanceTo(future);
        BlockPos centerPos = BlockPos.ofFloored((Position)future);
        this.lastPredictedCenter = Vec3d.ofCenter((Vec3i)centerPos);
        if (distanceToRawFuture > 4.5) {
            this.placedOnce = false;
            return;
        }
        ArrayList<BlockPos> pattern = new ArrayList<BlockPos>(7);
        pattern.add(centerPos);
        pattern.add(centerPos.up());
        pattern.add(centerPos.down());
        pattern.add(centerPos.north());
        pattern.add(centerPos.south());
        pattern.add(centerPos.east());
        pattern.add(centerPos.west());
        if (this.placedOnce) {
            return;
        }
        pattern.sort(Comparator.comparingDouble(p -> eyes.squaredDistanceTo(Vec3d.ofCenter((Vec3i)p))));
        Item useItem = this.findUseItem();
        if (useItem == null) {
            return;
        }
        boolean placedAny = false;
        if (MeteorClient.BLOCK.beginPlacement(pattern, useItem)) {
            for (BlockPos pos : pattern) {
                if (!MeteorClient.BLOCK.placeBlock(useItem, pos)) continue;
                this.lastPlacedThisTick.add(pos);
                placedAny = true;
            }
            MeteorClient.BLOCK.endPlacement();
        }
        if (placedAny) {
            this.placedOnce = true;
        }
    }

    private PlayerEntity findTarget() {
        PlayerEntity bestCandidate = null;
        double bestDistanceSq = Double.MAX_VALUE;
        for (PlayerEntity player : this.mc.world.getPlayers()) {
            double distSq;
            boolean isFalling;
            if (TargetUtils.isBadTarget(player, 14.0) || !Friends.get().shouldAttack(player) || this.onlyAbove.get().booleanValue() && player.getY() <= this.mc.player.getY() + 1.0 || this.maceRequired && !this.isHoldingMace(player)) continue;
            boolean isElytra = player.isFallFlying();
            boolean bl = isFalling = !isElytra && !player.isOnGround();
            if (!isFalling && !isElytra || !((distSq = player.squaredDistanceTo((Entity)this.mc.player)) < bestDistanceSq)) continue;
            bestDistanceSq = distSq;
            bestCandidate = player;
        }
        return bestCandidate;
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (!this.render.get().booleanValue()) {
            return;
        }
        if (this.lastPredictedCenter != null) {
            event.renderer.box(Box.of((Vec3d)this.lastPredictedCenter, (double)0.1, (double)0.1, (double)0.1), (Color)this.predictColor.get(), (Color)this.predictColor.get(), ShapeMode.Both, 0);
        }
        if (!this.lastPlacedThisTick.isEmpty()) {
            for (BlockPos pos : this.lastPlacedThisTick) {
                event.renderer.box(pos, (Color)this.placementColor.get(), (Color)this.placementColor.get(), this.shapeMode.get(), 0);
            }
        }
    }

    @Override
    public String getInfoString() {
        if (this.target == null) {
            return this.maceRequired ? "Mace Only" : "Any Item";
        }
        return String.format("%s [%.1f]", EntityUtils.getName((Entity)this.target), this.lastPredictedDistance);
    }

    private boolean isHoldingMace(PlayerEntity p) {
        return this.isMace(p.getMainHandStack()) || this.isMace(p.getOffHandStack());
    }

    private boolean isMace(ItemStack stack) {
        Identifier idAxe;
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item == Items.MACE) {
            return true;
        }
        if (item instanceof MaceItem) {
            return true;
        }
        if (item == Items.NETHERITE_AXE) {
            return true;
        }
        if (item instanceof AxeItem && (idAxe = Registries.ITEM.getId((Object)item)) != null && ("netherite_axe".equals(idAxe.getPath()) || "minecraft:netherite_axe".equals(idAxe.toString()))) {
            return true;
        }
        Identifier id = Registries.ITEM.getId((Object)item);
        return id != null && ("mace".equals(id.getPath()) || "minecraft:mace".equals(id.toString()));
    }

    private Item findUseItem() {
        FindItemResult result = InvUtils.findInHotbar(itemStack -> {
            Item it = itemStack.getItem();
            for (Block b : this.blocks.get()) {
                if (b.asItem() != it) continue;
                return true;
            }
            return false;
        });
        if (!result.found()) {
            return null;
        }
        return this.mc.player.getInventory().getStack(result.slot()).getItem();
    }

    private void sendChatFeedback(String state) {
        MutableText body;
        if (this.mc.player == null) {
            return;
        }
        MutableText prefix = Text.literal((String)"[AntiMace] ").formatted(Formatting.GOLD);
        switch (state) {
            case "enabled": {
                body = Text.literal((String)"Mace requirement enabled.").formatted(Formatting.GREEN);
                break;
            }
            case "disabled": {
                body = Text.literal((String)"Mace requirement disabled.").formatted(Formatting.RED);
                break;
            }
            default: {
                return;
            }
        }
        this.mc.player.sendMessage((Text)prefix.append((Text)body), true);
    }

    private Vec3d simulateFallFrom(Vec3d pos, Vec3d velPerTick, int ticks) {
        double DRAG = 0.98f;
        Vec3d p = pos;
        Vec3d v = velPerTick;
        for (int i = 0; i < ticks; ++i) {
            p = p.add(v);
            double vy = v.y + -0.08;
            double vx = v.x * (double)0.98f;
            vy *= (double)0.98f;
            double vz = v.z * (double)0.98f;
            if (vy < -3.92) {
                vy = -3.92;
            }
            v = new Vec3d(vx, vy, vz);
        }
        return p;
    }

    private Vec3d simulateElytraFuturePos(PlayerEntity player, int ticks) {
        Vec3d pos = player.getPos();
        Vec3d vel = player.getVelocity();
        float pitchRad = (float)Math.toRadians(player.getPitch());
        Vec3d look = player.getRotationVector();
        double cos = Math.cos(pitchRad);
        for (int i = 0; i < ticks; ++i) {
            Vec3d vNext;
            pos = pos.add(vel);
            double horizSpeed = Math.hypot(vel.x, vel.z);
            double len = vel.length();
            double liftFactor = cos * cos * Math.min(1.0, len / 0.4);
            double vy = vel.y + (-0.04 + liftFactor * 0.06);
            if (vel.y < 0.0 && horizSpeed > 0.0) {
                vy += -0.1 * vel.y * liftFactor;
            }
            Vec3d vAfterLift = new Vec3d(vel.x, vy, vel.z);
            Vec3d align = new Vec3d(look.x * 0.1 + (look.x * 1.5 - vAfterLift.x) * 0.01, look.y * 0.1 + (look.y * 1.5 - vAfterLift.y) * 0.01, look.z * 0.1 + (look.z * 1.5 - vAfterLift.z) * 0.01);
            Vec3d vAligned = vAfterLift.add(align);
            vel = vNext = new Vec3d(vAligned.x * 0.99, vAligned.y * 0.98, vAligned.z * 0.99);
        }
        return pos;
    }

    private Vec3d simulateBoostedElytraFuturePos(PlayerEntity player, int ticks) {
        Vec3d pos = player.getPos();
        Vec3d vel = player.getVelocity();
        double BOOST_DRAG = 0.991;
        for (int i = 0; i < ticks; ++i) {
            pos = pos.add(vel);
            double vy = vel.y;
            if (vy < -3.92) {
                vy = -3.92;
            }
            vel = new Vec3d(vel.x * 0.991, vel.y, vel.z * 0.991);
        }
        return pos;
    }
}

