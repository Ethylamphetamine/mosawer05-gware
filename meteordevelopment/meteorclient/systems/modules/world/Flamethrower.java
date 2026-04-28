/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityType
 *  net.minecraft.entity.LivingEntity
 *  net.minecraft.item.Items
 *  net.minecraft.util.Hand
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3d
 */
package meteordevelopment.meteorclient.systems.modules.world;

import java.util.Set;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Flamethrower
extends Module {
    private final SettingGroup sgGeneral;
    private final Setting<Double> distance;
    private final Setting<Boolean> antiBreak;
    private final Setting<Boolean> putOutFire;
    private final Setting<Boolean> targetBabies;
    private final Setting<Integer> tickInterval;
    private final Setting<Boolean> rotate;
    private final Setting<Set<EntityType<?>>> entities;
    private Entity entity;
    private int ticks;
    private Hand hand;

    public Flamethrower() {
        super(Categories.World, "flamethrower", "Ignites every alive piece of food.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.distance = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("distance")).description("The maximum distance the animal has to be to be roasted.")).min(0.0).defaultValue(5.0).build());
        this.antiBreak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-break")).description("Prevents flint and steel from being broken.")).defaultValue(false)).build());
        this.putOutFire = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("put-out-fire")).description("Tries to put out the fire when animal is low health, so the items don't burn.")).defaultValue(true)).build());
        this.targetBabies = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("target-babies")).description("If checked babies will also be killed.")).defaultValue(false)).build());
        this.tickInterval = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("tick-interval")).defaultValue(5)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Automatically faces towards the animal roasted.")).defaultValue(true)).build());
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Entities to cook.")).defaultValue(EntityType.PIG, EntityType.COW, EntityType.SHEEP, EntityType.CHICKEN, EntityType.RABBIT).build());
        this.ticks = 0;
    }

    @Override
    public void onDeactivate() {
        this.entity = null;
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        this.entity = null;
        ++this.ticks;
        for (Entity entity : this.mc.world.getEntities()) {
            if (!this.entities.get().contains(entity.getType()) || !PlayerUtils.isWithin(entity, (double)this.distance.get()) || entity.isFireImmune() || entity == this.mc.player || !this.targetBabies.get().booleanValue() && entity instanceof LivingEntity && ((LivingEntity)entity).isBaby()) continue;
            FindItemResult findFlintAndSteel = InvUtils.findInHotbar(itemStack -> itemStack.getItem() == Items.FLINT_AND_STEEL && (this.antiBreak.get() == false || itemStack.getDamage() < itemStack.getMaxDamage() - 1));
            if (!InvUtils.swap(findFlintAndSteel.slot(), true)) {
                return;
            }
            this.hand = findFlintAndSteel.getHand();
            this.entity = entity;
            if (this.rotate.get().booleanValue()) {
                Rotations.rotate(Rotations.getYaw(entity.getBlockPos()), Rotations.getPitch(entity.getBlockPos()), -100, this::interact);
            } else {
                this.interact();
            }
            return;
        }
    }

    private void interact() {
        LivingEntity animal;
        Entity entity;
        Block block = this.mc.world.getBlockState(this.entity.getBlockPos()).getBlock();
        Block bottom = this.mc.world.getBlockState(this.entity.getBlockPos().down()).getBlock();
        if (block == Blocks.WATER || bottom == Blocks.WATER || bottom == Blocks.DIRT_PATH) {
            return;
        }
        if (block == Blocks.GRASS_BLOCK) {
            this.mc.interactionManager.attackBlock(this.entity.getBlockPos(), Direction.DOWN);
        }
        if (this.putOutFire.get().booleanValue() && (entity = this.entity) instanceof LivingEntity && (animal = (LivingEntity)entity).getHealth() < 1.0f) {
            this.mc.interactionManager.attackBlock(this.entity.getBlockPos(), Direction.DOWN);
            this.mc.interactionManager.attackBlock(this.entity.getBlockPos().west(), Direction.DOWN);
            this.mc.interactionManager.attackBlock(this.entity.getBlockPos().east(), Direction.DOWN);
            this.mc.interactionManager.attackBlock(this.entity.getBlockPos().north(), Direction.DOWN);
            this.mc.interactionManager.attackBlock(this.entity.getBlockPos().south(), Direction.DOWN);
        } else if (this.ticks >= this.tickInterval.get() && !this.entity.isOnFire()) {
            this.mc.interactionManager.interactBlock(this.mc.player, this.hand, new BlockHitResult(this.entity.getPos().subtract(new Vec3d(0.0, 1.0, 0.0)), Direction.UP, this.entity.getBlockPos().down(), false));
            this.ticks = 0;
        }
        InvUtils.swapBack();
    }
}

