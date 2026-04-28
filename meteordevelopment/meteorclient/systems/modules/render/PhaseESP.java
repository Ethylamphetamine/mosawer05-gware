/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.BlockState
 *  net.minecraft.block.Blocks
 *  net.minecraft.entity.Entity
 *  net.minecraft.util.hit.BlockHitResult
 *  net.minecraft.util.hit.HitResult$Type
 *  net.minecraft.util.math.BlockPos
 *  net.minecraft.util.math.BlockPos$Mutable
 *  net.minecraft.util.math.Box
 *  net.minecraft.util.math.Direction
 *  net.minecraft.util.math.Vec3i
 *  net.minecraft.world.RaycastContext
 *  net.minecraft.world.RaycastContext$FluidHandling
 *  net.minecraft.world.RaycastContext$ShapeType
 */
package meteordevelopment.meteorclient.systems.modules.render;

import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.renderer.Renderer3D;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.RaycastContext;

public class PhaseESP
extends Module {
    private final SettingGroup sgRender;
    private final Setting<SettingColor> safeBedrockColor;
    private final Setting<SettingColor> unsafeBedrockColor;
    private final Setting<SettingColor> safeOpenHeadBedrockColor;
    private final Setting<SettingColor> safeObsidianColor;
    private final Setting<SettingColor> unsafesafeObsidianColor;
    private final Setting<SettingColor> openHeadColor;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> lineColor;
    private final Pool<PhaseBlock> phaseBlockPool;
    private final List<PhaseBlock> phaseBlocks;

    public PhaseESP() {
        super(Categories.Render, "phase-esp", "Shows you where it's safe to phase.");
        this.sgRender = this.settings.createGroup("Render");
        this.safeBedrockColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("safe-bedrock-color")).description("Bedrock that has a safe block below it")).defaultValue(new SettingColor(150, 0, 255, 50)).build());
        this.unsafeBedrockColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("unsafe-bedrock-color")).description("Bedrock that does not have a safe block below it")).defaultValue(new SettingColor(255, 0, 0, 70)).build());
        this.safeOpenHeadBedrockColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("safe-open-head-bedrock-color")).description("Bedrock that has a safe block below it and an open head")).defaultValue(new SettingColor(135, 160, 20, 50)).build());
        this.safeObsidianColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("safe-obsidian-color")).description("Obsidian that has a safe block below it")).defaultValue(new SettingColor(140, 0, 255, 10)).build());
        this.unsafesafeObsidianColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("unsafe-obsidian-color")).description("Obsidian that does not have a safe block below it")).defaultValue(new SettingColor(255, 0, 0, 30)).build());
        this.openHeadColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("open-head-color")).description("A block where the head is open")).defaultValue(new SettingColor(255, 0, 240, 30)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the rendering.")).defaultValue(new SettingColor(255, 255, 255, 20)).visible(() -> this.shapeMode.get().lines())).build());
        this.phaseBlockPool = new Pool<PhaseBlock>(() -> new PhaseBlock());
        this.phaseBlocks = new ArrayList<PhaseBlock>();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        for (PhaseBlock hole : this.phaseBlocks) {
            this.phaseBlockPool.free(hole);
        }
        this.phaseBlocks.clear();
        BlockPos playerPos = this.mc.player.getBlockPos();
        Box boundingBox = this.mc.player.getBoundingBox().expand(0.999, 0.0, 0.999);
        double feetY = this.mc.player.getY();
        Box feetBox = new Box(boundingBox.minX, feetY, boundingBox.minZ, boundingBox.maxX, feetY + 0.1, boundingBox.maxZ);
        boolean isAccorssMultipleBlocks = false;
        if ((int)Math.floor(feetBox.maxX) - (int)Math.floor(feetBox.minX) >= 1 || (int)Math.floor(feetBox.maxZ) - (int)Math.floor(feetBox.minZ) >= 1) {
            isAccorssMultipleBlocks = true;
        }
        for (int x = (int)Math.floor(feetBox.minX); x <= (int)Math.floor(feetBox.maxX); ++x) {
            for (int z = (int)Math.floor(feetBox.minZ); z <= (int)Math.floor(feetBox.maxZ); ++z) {
                BlockHitResult result;
                BlockPos blockPos = new BlockPos(x, playerPos.getY(), z);
                if (!isAccorssMultipleBlocks && playerPos.getX() == x && playerPos.getZ() == z || (result = this.mc.world.raycast(new RaycastContext(this.mc.player.getPos().add(0.0, 0.05, 0.0), blockPos.toBottomCenterPos().add(0.0, 0.05, 0.0), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, (Entity)this.mc.player))) != null && result.getType() != HitResult.Type.BLOCK) continue;
                this.checkBlock(blockPos);
            }
        }
    }

    private void checkBlock(BlockPos pos) {
        boolean obsidianUp;
        BlockState block = this.mc.world.getBlockState(pos);
        BlockState downBlock = this.mc.world.getBlockState(pos.offset(Direction.DOWN));
        BlockState upBlock = this.mc.world.getBlockState(pos.offset(Direction.UP));
        if (downBlock == null || block == null) {
            return;
        }
        boolean obsidian = block.isOf(Blocks.OBSIDIAN) || block.isOf(Blocks.CRYING_OBSIDIAN);
        boolean bedrock = block.isOf(Blocks.BEDROCK);
        boolean obsidianDown = downBlock.isOf(Blocks.OBSIDIAN) || downBlock.isOf(Blocks.CRYING_OBSIDIAN);
        boolean bedrockDown = downBlock.isOf(Blocks.BEDROCK);
        boolean airUp = upBlock.isAir();
        boolean bedrockUp = upBlock.isOf(Blocks.BEDROCK);
        boolean bl = obsidianUp = upBlock.isOf(Blocks.OBSIDIAN) || upBlock.isOf(Blocks.CRYING_OBSIDIAN);
        if (bedrock) {
            if (bedrockDown) {
                if (bedrockUp) {
                    this.phaseBlocks.add(this.phaseBlockPool.get().set(pos, PhaseBlock.Type.SafeBedrock));
                } else {
                    this.phaseBlocks.add(this.phaseBlockPool.get().set(pos, PhaseBlock.Type.SafeBedrockOpenHead));
                }
            } else {
                this.phaseBlocks.add(this.phaseBlockPool.get().set(pos, PhaseBlock.Type.UnsafeBedrock));
            }
        } else if (obsidian) {
            if (obsidianDown || bedrockDown) {
                if (airUp) {
                    this.phaseBlocks.add(this.phaseBlockPool.get().set(pos, PhaseBlock.Type.OpenHead));
                } else {
                    this.phaseBlocks.add(this.phaseBlockPool.get().set(pos, PhaseBlock.Type.SafeObsidian));
                }
            } else {
                this.phaseBlocks.add(this.phaseBlockPool.get().set(pos, PhaseBlock.Type.UnsafeObsidian));
            }
        } else if (obsidianUp) {
            this.phaseBlocks.add(this.phaseBlockPool.get().set(pos, PhaseBlock.Type.UnsafeObsidian));
        } else if (airUp && (obsidianDown || bedrockDown)) {
            this.phaseBlocks.add(this.phaseBlockPool.get().set(pos, PhaseBlock.Type.OpenHead));
        }
    }

    @EventHandler(priority=200)
    private void onRender(Render3DEvent event) {
        for (PhaseBlock phaseBlock : this.phaseBlocks) {
            phaseBlock.render(event.renderer);
        }
    }

    private class PhaseBlock {
        public BlockPos.Mutable blockPos = new BlockPos.Mutable();
        public Type type;

        public PhaseBlock set(BlockPos blockPos, Type type) {
            this.blockPos.set((Vec3i)blockPos);
            this.type = type;
            return this;
        }

        public void render(Renderer3D renderer) {
            int x1 = this.blockPos.getX();
            int y1 = this.blockPos.getY();
            int z1 = this.blockPos.getZ();
            int x2 = this.blockPos.getX() + 1;
            int z2 = this.blockPos.getZ() + 1;
            SettingColor color = switch (this.type.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> PhaseESP.this.safeBedrockColor.get();
                case 2 -> PhaseESP.this.unsafeBedrockColor.get();
                case 1 -> PhaseESP.this.safeObsidianColor.get();
                case 3 -> PhaseESP.this.unsafesafeObsidianColor.get();
                case 4 -> PhaseESP.this.safeOpenHeadBedrockColor.get();
                case 5 -> PhaseESP.this.openHeadColor.get();
            };
            renderer.sideHorizontal(x1, y1, z1, x2, z2, color, PhaseESP.this.lineColor.get(), PhaseESP.this.shapeMode.get());
        }

        public static enum Type {
            SafeBedrock,
            SafeObsidian,
            UnsafeBedrock,
            UnsafeObsidian,
            SafeBedrockOpenHead,
            OpenHead;

        }
    }
}

