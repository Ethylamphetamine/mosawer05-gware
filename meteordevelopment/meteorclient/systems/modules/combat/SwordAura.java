/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen
 *  net.minecraft.client.gui.screen.ingame.GenericContainerScreen
 *  net.minecraft.client.network.ClientPlayNetworkHandler
 *  net.minecraft.component.DataComponentTypes
 *  net.minecraft.component.type.AttributeModifiersComponent
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EquipmentSlot
 *  net.minecraft.entity.attribute.EntityAttributes
 *  net.minecraft.entity.player.PlayerInventory
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket
 *  net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$Full
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  org.apache.commons.lang3.mutable.MutableDouble
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.RotationManager;
import meteordevelopment.meteorclient.systems.managers.SwapManager;
import meteordevelopment.meteorclient.systems.managers.TargetManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableDouble;

public class SwordAura
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgTargeting;
    private final SettingGroup sgCrits;
    private final SettingGroup sgSafety;
    private final SettingGroup sgRender;
    private final Setting<Boolean> tpsSync;
    private final Setting<Boolean> silentSwapOverrideDelay;
    private final Setting<Boolean> rotate;
    private final Setting<Boolean> snapRotation;
    private final Setting<Boolean> swordPull;
    private final Setting<CritMode> critMode;
    private final Setting<Boolean> awaitJumpCrit;
    private final Setting<Boolean> wallCritsPauseOnMove;
    private final Setting<Boolean> wallCritsOnlyOnSword;
    private final Setting<Boolean> onlyCritWhenFullyPhased;
    private final Setting<Boolean> forcePauseEat;
    private final Setting<Boolean> pauseInAir;
    private final Setting<Boolean> pauseInventoryOepn;
    private final Setting<Boolean> render;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Double> fadeTime;
    private final TargetManager targetManager;
    private long lastAttackTime;
    private List<Entity> targets;
    private Entity lastAttackedEntity;
    private int targetIndex;
    private boolean isEating;
    private int originalSlot;
    private int movedFromInvSlot;

    public SwordAura() {
        super(Categories.Combat, "sword-aura", "Automatically attacks entities with your sword");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgTargeting = this.settings.createGroup("Targeting");
        this.sgCrits = this.settings.createGroup("Crits");
        this.sgSafety = this.settings.createGroup("Safety");
        this.sgRender = this.settings.createGroup("Render");
        this.tpsSync = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("tps-sync")).description("Adjusts attack speed to match the server's TPS.")).defaultValue(true)).build());
        this.silentSwapOverrideDelay = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("silent-swap-override-delay")).description("Whether or not to use the held items delay when attacking with silent swap")).defaultValue(false)).visible(() -> MeteorClient.SWAP.getItemSwapMode() != SwapManager.SwapMode.None)).build());
        this.rotate = this.sgTargeting.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Whether or not to rotate to the entity to attack it.")).defaultValue(true)).build());
        this.snapRotation = this.sgTargeting.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("snap-rotate")).description("Instantly rotates to the targeted entity.")).defaultValue(false)).visible(this.rotate::get)).build());
        this.swordPull = this.sgTargeting.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("sword-pull")).description("Pulls the target towards you")).defaultValue(false)).build());
        this.critMode = this.sgCrits.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("crit-mode")).description("The mode to use for critical hits.")).defaultValue(CritMode.OldWall)).build());
        this.awaitJumpCrit = this.sgCrits.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("await-jump-crit")).description("Waits until you are falling after a jump to perform a vanilla critical hit.")).defaultValue(false)).build());
        this.wallCritsPauseOnMove = this.sgCrits.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("wall-crits-pause-on-move")).description("Only for Old modes. Pauses crits when moving.")).defaultValue(true)).visible(() -> this.critMode.get() == CritMode.OldWall || this.critMode.get() == CritMode.OldAlways)).build());
        this.wallCritsOnlyOnSword = this.sgCrits.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("wall-crits-only-on-sword")).description("Only for Old modes. Only crits when you swapped to the sword.")).defaultValue(true)).visible(() -> this.critMode.get() == CritMode.OldWall || this.critMode.get() == CritMode.OldAlways)).build());
        this.onlyCritWhenFullyPhased = this.sgCrits.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-crit-when-fully-phased")).description("Only perform OldWall crits if your head is also phased (fully phased).")).defaultValue(true)).visible(() -> this.critMode.get() == CritMode.OldWall)).build());
        this.forcePauseEat = this.sgSafety.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("force-pause-on-eat")).description("Does not attack while using an item.")).defaultValue(true)).build());
        this.pauseInAir = this.sgSafety.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-in-air")).description("Does not attack while rising during a jump.")).defaultValue(false)).build());
        this.pauseInventoryOepn = this.sgSafety.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-inventory")).description("Does not attack when the inventory is open.")).defaultValue(true)).build());
        this.render = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render")).description("Whether or not to render attacks")).defaultValue(false)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).visible(this.render::get)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the rendering.")).defaultValue(new SettingColor(160, 0, 225, 35)).visible(() -> this.shapeMode.get().sides())).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the rendering.")).defaultValue(new SettingColor(255, 255, 255, 50)).visible(() -> this.render.get() != false && this.shapeMode.get().lines())).build());
        this.fadeTime = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("fade-time")).description("How long to fade the bounding box render.")).min(0.0).sliderMax(2.0).defaultValue(0.8).build());
        this.targetManager = new TargetManager(this, true);
        this.lastAttackTime = 0L;
        this.targets = null;
        this.lastAttackedEntity = null;
        this.targetIndex = 0;
        this.isEating = false;
        this.originalSlot = -1;
        this.movedFromInvSlot = -1;
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (this.mc.player == null || this.mc.world == null || this.mc.player.isDead() || this.mc.player.isSpectator()) {
            this.isEating = false;
            return;
        }
        if (this.forcePauseEat.get().booleanValue() && this.mc.player.isUsingItem() && this.mc.player.getActiveHand() == Hand.MAIN_HAND) {
            return;
        }
        if (this.pauseInventoryOepn.get().booleanValue() && (this.mc.currentScreen instanceof AbstractInventoryScreen || this.mc.currentScreen instanceof GenericContainerScreen)) {
            return;
        }
        FindItemResult result = MeteorClient.SWAP.getSlot(Items.NETHERITE_SWORD);
        if (!result.found()) {
            result = MeteorClient.SWAP.getSlot(Items.DIAMOND_SWORD);
        }
        if (!result.found()) {
            return;
        }
        this.targets = this.targetManager.getEntityTargets();
        if (this.targets.isEmpty()) {
            return;
        }
        Entity target = this.targets.get(this.targetIndex % this.targets.size());
        int delayCheckSlot = result.slot();
        if (this.silentSwapOverrideDelay.get().booleanValue()) {
            delayCheckSlot = this.mc.player.getInventory().selectedSlot;
        }
        if (this.delayCheck(delayCheckSlot)) {
            boolean isFalling = !this.mc.player.isOnGround() && this.mc.player.getVelocity().y < -0.1 && !this.mc.player.isClimbing() && !this.mc.player.isRiding() && !this.mc.player.isFallFlying() && !this.mc.player.isSwimming();
            boolean isMoving = Math.abs(this.mc.player.input.movementForward) > 1.0E-5f || Math.abs(this.mc.player.input.movementSideways) > 1.0E-5f;
            boolean isStandingOnGround = this.mc.player.isOnGround() && !isMoving && !this.mc.player.isClimbing() && !this.mc.player.isRiding();
            CritMode currentCritMode = this.critMode.get();
            boolean awaitingCrit = this.awaitJumpCrit.get();
            if (this.pauseInAir.get().booleanValue() && !this.mc.player.isOnGround()) {
                return;
            }
            if (awaitingCrit && !this.mc.player.isOnGround() && !isFalling && !this.mc.player.isFallFlying()) {
                return;
            }
            boolean sendPackets = isStandingOnGround ? currentCritMode != CritMode.None : false;
            if (this.rotate.get().booleanValue() || !this.mc.player.isFallFlying()) {
                Vec3d point = this.getClosestPointOnBox(target.getBoundingBox(), this.mc.player.getEyePos());
                if (this.snapRotation.get().booleanValue() || !this.mc.player.isFallFlying()) {
                    MeteorClient.ROTATION.snapAt(point);
                }
                MeteorClient.ROTATION.requestRotation(point, 9.0);
                if (!MeteorClient.ROTATION.lookingAt(target.getBoundingBox())) {
                    return;
                }
            }
            boolean isHolding = result.isMainHand();
            if (MeteorClient.SWAP.beginSwap(result, true)) {
                this.attack(target, !isHolding, sendPackets);
                MeteorClient.SWAP.endSwap(true);
            }
        }
    }

    @EventHandler
    private void onRender3D(Render3DEvent event) {
        double secondsSinceAttack;
        if (this.render.get().booleanValue() && this.lastAttackedEntity != null && this.mc.world != null && this.mc.player != null && (secondsSinceAttack = (double)(System.currentTimeMillis() - this.lastAttackTime) / 1000.0) <= this.fadeTime.get()) {
            double alpha = 1.0 - secondsSinceAttack / this.fadeTime.get();
            double x = MathHelper.lerp((double)event.tickDelta, (double)this.lastAttackedEntity.lastRenderX, (double)this.lastAttackedEntity.getX()) - this.lastAttackedEntity.getX();
            double y = MathHelper.lerp((double)event.tickDelta, (double)this.lastAttackedEntity.lastRenderY, (double)this.lastAttackedEntity.getY()) - this.lastAttackedEntity.getY();
            double z = MathHelper.lerp((double)event.tickDelta, (double)this.lastAttackedEntity.lastRenderZ, (double)this.lastAttackedEntity.getZ()) - this.lastAttackedEntity.getZ();
            Box box = this.lastAttackedEntity.getBoundingBox();
            event.renderer.box(x + box.minX, y + box.minY, z + box.minZ, x + box.maxX, y + box.maxY, z + box.maxZ, this.sideColor.get().copy().a((int)((double)this.sideColor.get().a * alpha)), this.lineColor.get().copy().a((int)((double)this.lineColor.get().a * alpha)), this.shapeMode.get(), 0);
        }
    }

    public void attack(Entity target, boolean didSwap, boolean sendPackets) {
        float legitPitch;
        float legitYaw;
        Vec3d pos;
        if (this.mc.player == null || this.mc.getNetworkHandler() == null) {
            return;
        }
        ClientPlayNetworkHandler net = this.mc.getNetworkHandler();
        if (RotationManager.lastGround) {
            pos = new Vec3d(MeteorClient.ROTATION.lastX, MeteorClient.ROTATION.lastY, MeteorClient.ROTATION.lastZ);
            legitYaw = MeteorClient.ROTATION.lastYaw;
            legitPitch = MeteorClient.ROTATION.lastPitch;
        } else {
            pos = this.mc.player.getPos();
            legitYaw = MeteorClient.ROTATION.rotationYaw;
            legitPitch = MeteorClient.ROTATION.rotationPitch;
        }
        boolean willSendCrits = false;
        if (sendPackets) {
            boolean isMoving;
            boolean bl = isMoving = Math.abs(this.mc.player.input.movementForward) > 1.0E-5f || Math.abs(this.mc.player.input.movementSideways) > 1.0E-5f;
            if (!(this.wallCritsPauseOnMove.get().booleanValue() && isMoving || this.wallCritsOnlyOnSword.get().booleanValue() && didSwap)) {
                switch (this.critMode.get().ordinal()) {
                    case 1: {
                        if (!PlayerUtils.isPlayerPhased() || !this.mc.player.isCrawling() && !this.isHeadPhasedWithBox() && this.onlyCritWhenFullyPhased.get().booleanValue()) break;
                        willSendCrits = true;
                        break;
                    }
                    case 2: {
                        willSendCrits = true;
                        break;
                    }
                }
            }
        }
        boolean pulling = this.swordPull.get();
        if (willSendCrits) {
            pulling = false;
        }
        if (pulling) {
            float spoofYaw = (legitYaw + 180.0f) % 360.0f;
            net.sendPacket((Packet)new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, legitYaw, legitPitch, this.mc.player.isOnGround()));
            net.sendPacket((Packet)new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, spoofYaw, legitPitch, this.mc.player.isOnGround()));
        } else if (willSendCrits) {
            this.sendCrits(didSwap);
        }
        net.sendPacket((Packet)PlayerInteractEntityC2SPacket.attack((Entity)target, (boolean)this.mc.player.isSneaking()));
        this.mc.player.swingHand(Hand.MAIN_HAND);
        this.lastAttackedEntity = target;
        this.lastAttackTime = System.currentTimeMillis();
        ++this.targetIndex;
    }

    private boolean delayCheck(int slot) {
        long currentTime;
        double tps;
        if (this.mc.player == null) {
            return false;
        }
        PlayerInventory inventory = this.mc.player.getInventory();
        ItemStack itemStack = inventory.getStack(slot);
        MutableDouble attackSpeed = new MutableDouble(this.mc.player.getAttributeBaseValue(EntityAttributes.GENERIC_ATTACK_SPEED));
        AttributeModifiersComponent attributeModifiers = (AttributeModifiersComponent)itemStack.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attributeModifiers != null) {
            attributeModifiers.applyModifiers(EquipmentSlot.MAINHAND, (entry, modifier) -> {
                if (entry.matches(EntityAttributes.GENERIC_ATTACK_SPEED)) {
                    attackSpeed.add(modifier.comp_2449());
                }
            });
        }
        double attackCooldownTicks = 1.0 / attackSpeed.getValue() * 20.0;
        if (this.tpsSync.get().booleanValue() && (tps = (double)TickRate.INSTANCE.getTickRate() * 0.95) < 19.5) {
            double tpsFactor = tps / 20.0;
            attackCooldownTicks /= tpsFactor;
        }
        return (double)((currentTime = System.currentTimeMillis()) - this.lastAttackTime) / 50.0 > attackCooldownTicks;
    }

    private void sendCrits(boolean didSwap) {
        boolean isMoving;
        float packetPitch;
        float packetYaw;
        Vec3d pos;
        if (this.mc.player == null || this.mc.getNetworkHandler() == null) {
            return;
        }
        ClientPlayNetworkHandler net = this.mc.getNetworkHandler();
        if (RotationManager.lastGround) {
            pos = new Vec3d(MeteorClient.ROTATION.lastX, MeteorClient.ROTATION.lastY, MeteorClient.ROTATION.lastZ);
            packetYaw = MeteorClient.ROTATION.lastYaw;
            packetPitch = MeteorClient.ROTATION.lastPitch;
        } else {
            pos = this.mc.player.getPos();
            packetYaw = this.mc.player.getYaw();
            packetPitch = this.mc.player.getPitch();
        }
        boolean bl = isMoving = Math.abs(this.mc.player.input.movementForward) > 1.0E-5f || Math.abs(this.mc.player.input.movementSideways) > 1.0E-5f;
        if (!(this.wallCritsPauseOnMove.get().booleanValue() && isMoving || this.wallCritsOnlyOnSword.get().booleanValue() && didSwap)) {
            switch (this.critMode.get().ordinal()) {
                case 1: {
                    if (!PlayerUtils.isPlayerPhased() || !this.mc.player.isCrawling() && !this.isHeadPhasedWithBox() && this.onlyCritWhenFullyPhased.get().booleanValue()) break;
                    net.sendPacket((Packet)new PlayerMoveC2SPacket.Full(pos.x, pos.y + 0.0625, pos.z, packetYaw, packetPitch, false));
                    net.sendPacket((Packet)new PlayerMoveC2SPacket.Full(pos.x, pos.y + 0.045, pos.z, packetYaw, packetPitch, false));
                    break;
                }
                case 2: {
                    net.sendPacket((Packet)new PlayerMoveC2SPacket.Full(pos.x, pos.y, pos.z, packetYaw, packetPitch, true));
                    net.sendPacket((Packet)new PlayerMoveC2SPacket.Full(pos.x, pos.y + 0.0625, pos.z, packetYaw, packetPitch, false));
                    net.sendPacket((Packet)new PlayerMoveC2SPacket.Full(pos.x, pos.y + 0.045, pos.z, packetYaw, packetPitch, false));
                    break;
                }
            }
        }
    }

    private boolean isHeadPhasedWithBox() {
        if (this.mc.player == null || this.mc.world == null) {
            return false;
        }
        Box boundingBox = this.mc.player.getBoundingBox();
        Box headBox = new Box(boundingBox.minX, boundingBox.minY + (double)this.mc.player.getHeight() - 0.5, boundingBox.minZ, boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ);
        return this.mc.world.getBlockCollisions((Entity)this.mc.player, headBox).iterator().hasNext();
    }

    public Vec3d getClosestPointOnBox(Box box, Vec3d point) {
        if (this.mc.player == null) {
            return Vec3d.ZERO;
        }
        double x = Math.max(box.minX, Math.min(point.x, box.maxX));
        double y = Math.max(box.minY, Math.min(point.y, box.maxY));
        double z = Math.max(box.minZ, Math.min(point.z, box.maxZ));
        return new Vec3d(x, y, z);
    }

    public static enum CritMode {
        None,
        OldWall,
        OldAlways;

    }
}

