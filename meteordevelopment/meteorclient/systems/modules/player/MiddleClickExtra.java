/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.PlayerEntity
 *  net.minecraft.item.BowItem
 *  net.minecraft.item.Item
 *  net.minecraft.item.Items
 *  net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket
 *  net.minecraft.util.Hand
 */
package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.FinishUsingItemEvent;
import meteordevelopment.meteorclient.events.entity.player.StoppedUsingItemEvent;
import meteordevelopment.meteorclient.events.meteor.MouseButtonEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.managers.PacketManager;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.input.KeyAction;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;

public class MiddleClickExtra
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Mode> mode;
    private final Setting<Boolean> message;
    private final Setting<Boolean> quickSwap;
    private final Setting<Boolean> swapBack;
    private final Setting<Boolean> notify;
    private final Setting<Boolean> rocketInAir;
    private boolean isUsing;
    private boolean wasHeld;
    private int itemSlot;
    private int selectedSlot;

    public MiddleClickExtra() {
        super(Categories.Player, "middle-click-extra", "Perform various actions when you middle click.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("mode")).description("Which item to use when you middle click.")).defaultValue(Mode.Pearl)).build());
        this.message = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("message")).description("Sends a message to the player when you add them as a friend.")).defaultValue(false)).visible(() -> this.mode.get() == Mode.AddFriend)).build());
        this.quickSwap = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("quick-swap")).description("Allows you to use items in your inventory by simulating hotbar key presses. May get flagged by anticheats.")).defaultValue(false)).visible(() -> this.mode.get() != Mode.AddFriend)).build());
        this.swapBack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("swap-back")).description("Swap back to your original slot when you finish using an item.")).defaultValue(false)).visible(() -> this.mode.get() != Mode.AddFriend && this.quickSwap.get() == false)).build());
        this.notify = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("notify")).description("Notifies you when you do not have the specified item in your hotbar.")).defaultValue(true)).visible(() -> this.mode.get() != Mode.AddFriend)).build());
        this.rocketInAir = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rocker-in-air")).description("Uses a rocket when flying.")).defaultValue(true)).build());
    }

    @Override
    public void onDeactivate() {
        this.stopIfUsing(false);
    }

    @EventHandler
    private void onMouseButton(MouseButtonEvent event) {
        if (event.action != KeyAction.Press || event.button != 2 || this.mc.currentScreen != null) {
            return;
        }
        if (this.rocketInAir.get().booleanValue() && this.mc.player.isFallFlying()) {
            FindItemResult result = InvUtils.find(Items.FIREWORK_ROCKET);
            if (!result.found()) {
                if (this.notify.get().booleanValue()) {
                    this.warning("Unable to find specified item.", new Object[0]);
                }
                return;
            }
            if (MeteorClient.SWAP.beginSwap(result, true)) {
                PacketManager.INSTANCE.incrementGlobal();
                this.mc.interactionManager.interactItem((PlayerEntity)this.mc.player, Hand.MAIN_HAND);
                MeteorClient.SWAP.endSwap(true);
                PacketManager.INSTANCE.incrementGlobal();
                this.mc.player.playerScreenHandler.syncState();
            }
            event.cancel();
            return;
        }
        if (this.mode.get() == Mode.AddFriend) {
            if (this.mc.targetedEntity == null) {
                return;
            }
            Entity entity = this.mc.targetedEntity;
            if (!(entity instanceof PlayerEntity)) {
                return;
            }
            PlayerEntity player = (PlayerEntity)entity;
            if (!Friends.get().isFriend(player)) {
                Friends.get().add(new Friend(player, Friend.FriendType.Friend));
                this.info("Added %s to friends", player.getName().getString());
                if (this.message.get().booleanValue()) {
                    ChatUtils.sendPlayerMsg("/msg " + String.valueOf(player.getName()) + " I just friended you on Meteor.");
                }
            } else {
                Friends.get().remove(Friends.get().get(player));
                this.info("Removed %s from friends", player.getName().getString());
            }
            event.cancel();
            return;
        }
        FindItemResult result = InvUtils.find(this.mode.get().item);
        if (!result.found()) {
            if (this.notify.get().booleanValue()) {
                this.warning("Unable to find specified item.", new Object[0]);
            }
            return;
        }
        if (this.mode.get().immediate) {
            if (MeteorClient.SWAP.beginSwap(result, true)) {
                PacketManager.INSTANCE.incrementGlobal();
                this.mc.interactionManager.interactItem((PlayerEntity)this.mc.player, Hand.MAIN_HAND);
                MeteorClient.SWAP.endSwap(true);
                PacketManager.INSTANCE.incrementGlobal();
                this.mc.player.playerScreenHandler.syncState();
            }
        } else {
            this.selectedSlot = this.mc.player.getInventory().selectedSlot;
            this.itemSlot = result.slot();
            this.wasHeld = result.isMainHand();
            if (!this.wasHeld) {
                if (this.quickSwap.get().booleanValue()) {
                    InvUtils.quickSwap().fromId(this.selectedSlot).to(this.itemSlot);
                } else {
                    InvUtils.swap(result.slot(), this.swapBack.get());
                }
            }
            this.mc.options.useKey.setPressed(true);
            this.isUsing = true;
        }
        event.cancel();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!this.isUsing) {
            return;
        }
        boolean pressed = true;
        if (this.mc.player.getMainHandStack().getItem() instanceof BowItem) {
            pressed = BowItem.getPullProgress((int)this.mc.player.getItemUseTime()) < 1.0f;
        }
        this.mc.options.useKey.setPressed(pressed);
    }

    @EventHandler
    private void onPacketSendEvent(PacketEvent.Send event) {
        if (event.packet instanceof UpdateSelectedSlotC2SPacket) {
            this.stopIfUsing(true);
        }
    }

    @EventHandler
    private void onStoppedUsingItem(StoppedUsingItemEvent event) {
        this.stopIfUsing(false);
    }

    @EventHandler
    private void onFinishUsingItem(FinishUsingItemEvent event) {
        this.stopIfUsing(false);
    }

    private void stopIfUsing(boolean wasCancelled) {
        if (this.isUsing) {
            this.swapBack(wasCancelled);
            this.mc.options.useKey.setPressed(false);
            this.isUsing = false;
        }
    }

    void swapBack(boolean wasCancelled) {
        if (this.wasHeld) {
            return;
        }
        if (this.quickSwap.get().booleanValue()) {
            InvUtils.quickSwap().fromId(this.selectedSlot).to(this.itemSlot);
        } else {
            if (!this.swapBack.get().booleanValue() || wasCancelled) {
                return;
            }
            InvUtils.swapBack();
        }
    }

    public static enum Mode {
        Pearl(Items.ENDER_PEARL, true),
        XP(Items.EXPERIENCE_BOTTLE, true),
        Rocket(Items.FIREWORK_ROCKET, true),
        Bow(Items.BOW, false),
        Gap(Items.GOLDEN_APPLE, false),
        EGap(Items.ENCHANTED_GOLDEN_APPLE, false),
        Chorus(Items.CHORUS_FRUIT, false),
        AddFriend(null, true);

        private final Item item;
        private final boolean immediate;

        private Mode(Item item, boolean immediate) {
            this.item = item;
            this.immediate = immediate;
        }
    }
}

