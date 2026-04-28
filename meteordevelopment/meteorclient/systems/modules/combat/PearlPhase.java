/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.ChatScreen
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.Packet
 *  net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket
 *  net.minecraft.screen.slot.SlotActionType
 *  net.minecraft.text.MutableText
 *  net.minecraft.text.Text
 *  net.minecraft.util.Formatting
 *  net.minecraft.util.Hand
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.MathHelper
 *  net.minecraft.util.math.Vec3d
 *  net.minecraft.util.math.Vec3i
 */
package meteordevelopment.meteorclient.systems.modules.combat;

import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.movement.MovementFix;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

public class PearlPhase
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Keybind> phaseBind;
    private final Setting<Boolean> chatFeedback;
    private final Setting<Double> pitch;
    private final Setting<Boolean> autoCenter;
    private final Setting<Boolean> placeBlock;
    private final Setting<Boolean> pauseOnEat;
    private boolean active;
    private boolean keyUnpressed;

    public PearlPhase() {
        super(Categories.Combat, "pearl-phase", "Phases into walls using pearls");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.phaseBind = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("key-bind")).description("Phase on keybind press")).build());
        this.chatFeedback = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("chat-feedback")).description("Sends a colored chat message when the phase key is pressed.")).defaultValue(false)).build());
        this.pitch = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("pitch")).description("The pitch to throw the ender pearl at.")).defaultValue(75.0).min(0.0).max(90.0).sliderMin(0.0).sliderMax(90.0).build());
        this.autoCenter = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("auto-center")).description("Snaps the rotation to the nearest 45 degrees.")).defaultValue(true)).build());
        this.placeBlock = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place-block")).description("Places a block at your phase spot.")).defaultValue(false)).build());
        this.pauseOnEat = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-eat")).description("Prevents placing the optional block while using an item.")).defaultValue(true)).build());
        this.active = false;
        this.keyUnpressed = false;
    }

    private void activate() {
        this.active = true;
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        this.update();
    }

    private void deactivate(boolean phased) {
        this.active = false;
    }

    private void update() {
        if (this.mc.player == null || this.mc.world == null) {
            return;
        }
        if (!this.active) {
            return;
        }
        if (!InvUtils.find(Items.ENDER_PEARL).found()) {
            this.deactivate(false);
            return;
        }
        if (this.mc.player.getItemCooldownManager().isCoolingDown(Items.ENDER_PEARL)) {
            this.deactivate(false);
            return;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        float finalPitch;
        float finalYaw;
        if (this.mc.player == null || this.mc.world == null || this.mc.player.input == null) {
            return;
        }
        if (!this.active) {
            return;
        }
        if (this.mc.player.isCrawling()) {
            Vec3d targetPos = this.calculateTargetPos();
            float[] angle = MeteorClient.ROTATION.getRotation(targetPos);
            finalYaw = angle[0];
            finalPitch = angle[1];
        } else {
            float currentYaw;
            float forward = this.mc.player.input.movementForward;
            float strafe = this.mc.player.input.movementSideways;
            float targetYaw = currentYaw = this.mc.player.getYaw();
            if (forward > 0.0f) {
                if (strafe > 0.0f) {
                    targetYaw -= 45.0f;
                } else if (strafe < 0.0f) {
                    targetYaw += 45.0f;
                }
            } else if (forward < 0.0f) {
                targetYaw += 180.0f;
                if (strafe > 0.0f) {
                    targetYaw += 45.0f;
                } else if (strafe < 0.0f) {
                    targetYaw -= 45.0f;
                }
            } else if (strafe > 0.0f) {
                targetYaw -= 90.0f;
            } else if (strafe < 0.0f) {
                targetYaw += 90.0f;
            }
            targetYaw = MathHelper.wrapDegrees((float)targetYaw);
            if (this.autoCenter.get().booleanValue()) {
                targetYaw = (float)Math.round(targetYaw / 45.0f) * 45.0f;
            }
            finalYaw = targetYaw;
            finalPitch = this.pitch.get().floatValue();
        }
        MovementFix.bypassRotationForThisTick = true;
        Rotations.rotate((double)finalYaw, (double)finalPitch, () -> this.throwPearl(finalYaw, finalPitch));
    }

    private void throwPearl(float yaw, float pitch) {
        FindItemResult pearl;
        if (this.mc.world == null || this.mc.getNetworkHandler() == null || this.mc.player == null || this.mc.interactionManager == null) {
            return;
        }
        if (!(!this.placeBlock.get().booleanValue() || this.pauseOnEat.get().booleanValue() && this.mc.player.isUsingItem())) {
            BlockPos offset = this.getPlacementDirectionOffset();
            BlockPos supportPos = this.mc.player.getBlockPos().add((Vec3i)offset);
            if (!(this.mc.player.getPos().distanceTo(Vec3d.ofCenter((Vec3i)supportPos)) > 1.25) && BlockUtils.canPlace(supportPos)) {
                FindItemResult obsidian = InvUtils.findInHotbar(Items.OBSIDIAN);
                if (obsidian.found()) {
                    if (MeteorClient.BLOCK.beginPlacement(List.of(supportPos), Items.OBSIDIAN)) {
                        InvUtils.swap(obsidian.slot(), true);
                        PacketManager.INSTANCE.incrementGlobal();
                        MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, supportPos);
                        InvUtils.swapBack();
                        PacketManager.INSTANCE.incrementGlobal();
                        MeteorClient.BLOCK.endPlacement();
                    }
                } else {
                    obsidian = InvUtils.find(Items.OBSIDIAN);
                    if (obsidian.found()) {
                        if (!PacketManager.INSTANCE.isClickAllowed()) {
                            return;
                        }
                        int inventorySlot = obsidian.slot();
                        int hotbarSlot = this.mc.player.getInventory().selectedSlot;
                        PacketManager.INSTANCE.incrementClick();
                        this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, inventorySlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
                        if (MeteorClient.BLOCK.beginPlacement(List.of(supportPos), Items.OBSIDIAN)) {
                            MeteorClient.BLOCK.placeBlock(Items.OBSIDIAN, supportPos);
                            MeteorClient.BLOCK.endPlacement();
                        }
                        PacketManager.INSTANCE.incrementClick();
                        this.mc.interactionManager.clickSlot(this.mc.player.playerScreenHandler.syncId, inventorySlot, hotbarSlot, SlotActionType.SWAP, (PlayerEntity)this.mc.player);
                    }
                }
            }
        }
        if ((pearl = InvUtils.find(Items.ENDER_PEARL)).found()) {
            if (MeteorClient.SWAP.beginSwap(pearl, true)) {
                PacketManager.INSTANCE.incrementGlobal();
                this.sendThrowPacket(Hand.MAIN_HAND, yaw, pitch);
                MeteorClient.SWAP.endSwap(true);
                PacketManager.INSTANCE.incrementGlobal();
            }
        } else {
            this.deactivate(false);
            return;
        }
        this.mc.player.playerScreenHandler.syncState();
        this.deactivate(true);
    }

    private void sendThrowPacket(Hand hand, float yaw, float pitch) {
        if (this.mc.world == null || this.mc.getNetworkHandler() == null) {
            return;
        }
        int sequence = this.mc.world.getPendingUpdateManager().incrementSequence().getSequence();
        this.mc.getNetworkHandler().sendPacket((Packet)new PlayerInteractItemC2SPacket(hand, sequence, yaw, pitch));
        PacketManager.INSTANCE.incrementInteract();
    }

    @EventHandler(priority=200)
    private void onRender(Render3DEvent event) {
        if (!this.phaseBind.get().isPressed()) {
            this.keyUnpressed = true;
        }
        if (this.phaseBind.get().isPressed() && this.keyUnpressed && !(this.mc.currentScreen instanceof ChatScreen)) {
            if (this.chatFeedback.get().booleanValue()) {
                this.sendChatFeedback();
            }
            this.activate();
            this.keyUnpressed = false;
        }
        this.update();
    }

    private BlockPos getPlacementDirectionOffset() {
        if (this.mc.player == null) {
            return BlockPos.ORIGIN;
        }
        int yaw = MathHelper.floor((double)((double)(this.mc.player.getYaw() * 8.0f / 360.0f) + 0.5)) & 7;
        return switch (yaw) {
            case 0 -> new BlockPos(0, 0, 1);
            case 1 -> new BlockPos(-1, 0, 1);
            case 2 -> new BlockPos(-1, 0, 0);
            case 3 -> new BlockPos(-1, 0, -1);
            case 4 -> new BlockPos(0, 0, -1);
            case 5 -> new BlockPos(1, 0, -1);
            case 6 -> new BlockPos(1, 0, 0);
            case 7 -> new BlockPos(1, 0, 1);
            default -> BlockPos.ORIGIN;
        };
    }

    private void sendChatFeedback() {
        if (this.mc.player == null) {
            return;
        }
        MutableText prefix = Text.literal((String)"[PearlPhase] ").formatted(Formatting.LIGHT_PURPLE);
        MutableText body = Text.literal((String)"Phase key pressed.").formatted(Formatting.GRAY);
        this.mc.player.sendMessage((Text)prefix.append((Text)body), false);
    }

    private Vec3d calculateTargetPos() {
        double X_OFFSET = 0.241660973353061;
        double Z_OFFSET = 0.7853981633974483;
        double playerX = this.mc.player.getX();
        double playerZ = this.mc.player.getZ();
        double x = playerX + MathHelper.clamp((double)(this.toClosest(playerX, Math.floor(playerX) + 0.241660973353061, Math.floor(playerX) + 0.7853981633974483) - playerX), (double)-0.2, (double)0.2);
        double z = playerZ + MathHelper.clamp((double)(this.toClosest(playerZ, Math.floor(playerZ) + 0.241660973353061, Math.floor(playerZ) + 0.7853981633974483) - playerZ), (double)-0.2, (double)0.2);
        return new Vec3d(x, this.mc.player.getY() + 0.35, z);
    }

    private double toClosest(double num, double min, double max) {
        double dmax = max - num;
        double dmin = num - min;
        if (dmax > dmin) {
            return min;
        }
        return max;
    }
}

