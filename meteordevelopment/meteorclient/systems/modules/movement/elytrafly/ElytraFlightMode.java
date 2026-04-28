/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.MinecraftClient
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.ItemStack
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket
 *  net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket$Mode
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.movement.elytrafly;

import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFlightModes;
import meteordevelopment.meteorclient.systems.modules.movement.elytrafly.ElytraFly;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class ElytraFlightMode {
    protected final MinecraftClient mc;
    protected final ElytraFly elytraFly = Modules.get().get(ElytraFly.class);
    private final ElytraFlightModes type;
    protected boolean lastJumpPressed;
    protected boolean incrementJumpTimer;
    protected boolean lastForwardPressed;
    protected int jumpTimer;
    protected double velX;
    protected double velY;
    protected double velZ;
    protected double ticksLeft;
    protected Vec3d forward;
    protected Vec3d right;
    protected double acceleration;

    public ElytraFlightMode(ElytraFlightModes type) {
        this.mc = MinecraftClient.getInstance();
        this.type = type;
    }

    public void onTick() {
        ItemStack chestStack;
        FindItemResult fireworks;
        if (this.elytraFly.autoReplenish.get().booleanValue() && (fireworks = InvUtils.find(Items.FIREWORK_ROCKET)).found() && !fireworks.isHotbar()) {
            InvUtils.move().from(fireworks.slot()).toHotbar(this.elytraFly.replenishSlot.get() - 1);
        }
        if (this.elytraFly.replace.get().booleanValue() && (chestStack = this.mc.player.getInventory().getArmorStack(2)).getItem() == Items.ELYTRA && chestStack.getMaxDamage() - chestStack.getDamage() <= this.elytraFly.replaceDurability.get()) {
            FindItemResult elytra = InvUtils.find(stack -> stack.getMaxDamage() - stack.getDamage() > this.elytraFly.replaceDurability.get() && stack.getItem() == Items.ELYTRA);
            InvUtils.move().from(elytra.slot()).toArmor(2);
        }
    }

    public void onPreTick() {
    }

    public void onPacketSend(PacketEvent.Send event) {
    }

    public void onPacketReceive(PacketEvent.Receive event) {
    }

    public void onPlayerMove() {
    }

    public void onActivate() {
        this.lastJumpPressed = false;
        this.jumpTimer = 0;
        this.ticksLeft = 0.0;
        this.acceleration = 0.0;
    }

    public void onDeactivate() {
    }

    public void autoTakeoff() {
        if (this.incrementJumpTimer) {
            ++this.jumpTimer;
        }
        boolean jumpPressed = this.mc.options.jumpKey.isPressed();
        if (this.elytraFly.autoTakeOff.get().booleanValue() && jumpPressed) {
            if (!this.lastJumpPressed && !this.mc.player.isFallFlying()) {
                this.jumpTimer = 0;
                this.incrementJumpTimer = true;
            }
            if (this.jumpTimer >= 8) {
                this.jumpTimer = 0;
                this.incrementJumpTimer = false;
                this.mc.player.setJumping(false);
                this.mc.player.setSprinting(true);
                this.mc.player.jump();
                this.mc.getNetworkHandler().sendPacket((Packet)new ClientCommandC2SPacket((Entity)this.mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }
        this.lastJumpPressed = jumpPressed;
    }

    public void handleAutopilot() {
        if (!this.mc.player.isFallFlying()) {
            return;
        }
        if (this.elytraFly.autoPilot.get().booleanValue() && this.mc.player.getY() > this.elytraFly.autoPilotMinimumHeight.get() && this.elytraFly.flightMode.get() != ElytraFlightModes.Bounce) {
            this.mc.options.forwardKey.setPressed(true);
            this.lastForwardPressed = true;
        }
        if (this.elytraFly.useFireworks.get().booleanValue()) {
            if (this.ticksLeft <= 0.0) {
                this.ticksLeft = this.elytraFly.autoPilotFireworkDelay.get() * 20.0;
                FindItemResult itemResult = InvUtils.findInHotbar(Items.FIREWORK_ROCKET);
                if (!itemResult.found()) {
                    return;
                }
                if (itemResult.isOffhand()) {
                    this.mc.interactionManager.interactItem((PlayerEntity)this.mc.player, Hand.OFF_HAND);
                    this.mc.player.swingHand(Hand.OFF_HAND);
                } else {
                    InvUtils.swap(itemResult.slot(), true);
                    this.mc.interactionManager.interactItem((PlayerEntity)this.mc.player, Hand.MAIN_HAND);
                    this.mc.player.swingHand(Hand.MAIN_HAND);
                    InvUtils.swapBack();
                }
            }
            this.ticksLeft -= 1.0;
        }
    }

    public void handleHorizontalSpeed(PlayerMoveEvent event) {
        boolean a = false;
        boolean b = false;
        if (this.mc.options.forwardKey.isPressed()) {
            this.velX += this.forward.x * this.getSpeed() * 10.0;
            this.velZ += this.forward.z * this.getSpeed() * 10.0;
            a = true;
        } else if (this.mc.options.backKey.isPressed()) {
            this.velX -= this.forward.x * this.getSpeed() * 10.0;
            this.velZ -= this.forward.z * this.getSpeed() * 10.0;
            a = true;
        }
        if (this.mc.options.rightKey.isPressed()) {
            this.velX += this.right.x * this.getSpeed() * 10.0;
            this.velZ += this.right.z * this.getSpeed() * 10.0;
            b = true;
        } else if (this.mc.options.leftKey.isPressed()) {
            this.velX -= this.right.x * this.getSpeed() * 10.0;
            this.velZ -= this.right.z * this.getSpeed() * 10.0;
            b = true;
        }
        if (a && b) {
            double diagonal = 1.0 / Math.sqrt(2.0);
            this.velX *= diagonal;
            this.velZ *= diagonal;
        }
    }

    public void handleVerticalSpeed(PlayerMoveEvent event) {
        if (this.mc.options.jumpKey.isPressed()) {
            this.velY += 0.5 * this.elytraFly.verticalSpeed.get();
        } else if (this.mc.options.sneakKey.isPressed()) {
            this.velY -= 0.5 * this.elytraFly.verticalSpeed.get();
        }
    }

    public void handleFallMultiplier() {
        if (this.velY < 0.0) {
            this.velY *= this.elytraFly.fallMultiplier.get().doubleValue();
        } else if (this.velY > 0.0) {
            this.velY = 0.0;
        }
    }

    public void handleAcceleration() {
        if (this.elytraFly.acceleration.get().booleanValue()) {
            if (!PlayerUtils.isMoving()) {
                this.acceleration = 0.0;
            }
            this.acceleration = Math.min(this.acceleration + this.elytraFly.accelerationMin.get() + this.elytraFly.accelerationStep.get() * 0.1, this.elytraFly.horizontalSpeed.get());
        } else {
            this.acceleration = 0.0;
        }
    }

    public void zeroAcceleration() {
        this.acceleration = 0.0;
    }

    protected double getSpeed() {
        return this.elytraFly.acceleration.get() != false ? this.acceleration : this.elytraFly.horizontalSpeed.get();
    }

    public String getHudString() {
        return this.type.name();
    }
}

