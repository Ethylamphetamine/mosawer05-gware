/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  net.minecraft.block.Block
 */
package meteordevelopment.meteorclient.systems.modules.render.blockesp;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.ArrayDeque;
import java.util.Set;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.BlockESP;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlock;
import meteordevelopment.meteorclient.systems.modules.render.blockesp.ESPBlockData;
import meteordevelopment.meteorclient.utils.misc.UnorderedArrayList;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import net.minecraft.block.Block;

public class ESPGroup {
    private static final BlockESP blockEsp = Modules.get().get(BlockESP.class);
    private final Block block;
    public final UnorderedArrayList<ESPBlock> blocks = new UnorderedArrayList();
    private double sumX;
    private double sumY;
    private double sumZ;

    public ESPGroup(Block block) {
        this.block = block;
    }

    public void add(ESPBlock block, boolean removeFromOld, boolean splitGroup) {
        this.blocks.add(block);
        this.sumX += (double)block.x;
        this.sumY += (double)block.y;
        this.sumZ += (double)block.z;
        if (block.group != null && removeFromOld) {
            block.group.remove(block, splitGroup);
        }
        block.group = this;
    }

    public void add(ESPBlock block) {
        this.add(block, true, true);
    }

    public void remove(ESPBlock block, boolean splitGroup) {
        this.blocks.remove(block);
        this.sumX -= (double)block.x;
        this.sumY -= (double)block.y;
        this.sumZ -= (double)block.z;
        if (this.blocks.isEmpty()) {
            blockEsp.removeGroup(block.group);
        } else if (splitGroup) {
            this.trySplit(block);
        }
    }

    public void remove(ESPBlock block) {
        this.remove(block, true);
    }

    private void trySplit(ESPBlock block) {
        ObjectOpenHashSet neighbours = new ObjectOpenHashSet(6);
        for (int side : ESPBlock.SIDES) {
            ESPBlock neighbour;
            if ((block.neighbours & side) != side || (neighbour = block.getSideBlock(side)) == null) continue;
            neighbours.add(neighbour);
        }
        if (neighbours.size() <= 1) {
            return;
        }
        ObjectOpenHashSet remainingBlocks = new ObjectOpenHashSet(this.blocks);
        ArrayDeque<ESPBlock> blocksToCheck = new ArrayDeque<ESPBlock>();
        blocksToCheck.offer((ESPBlock)this.blocks.getFirst());
        remainingBlocks.remove(this.blocks.getFirst());
        neighbours.remove(this.blocks.getFirst());
        block1: while (!blocksToCheck.isEmpty()) {
            ESPBlock b = (ESPBlock)blocksToCheck.poll();
            for (int side : ESPBlock.SIDES) {
                ESPBlock neighbour;
                if ((b.neighbours & side) != side || (neighbour = b.getSideBlock(side)) == null || !remainingBlocks.contains(neighbour)) continue;
                blocksToCheck.offer(neighbour);
                remainingBlocks.remove(neighbour);
                neighbours.remove(neighbour);
                if (neighbours.isEmpty()) break block1;
            }
        }
        if (!neighbours.isEmpty()) {
            ESPGroup group = blockEsp.newGroup(this.block);
            group.blocks.ensureCapacity(remainingBlocks.size());
            this.blocks.removeIf(((Set)remainingBlocks)::contains);
            for (ESPBlock b : remainingBlocks) {
                group.add(b, false, false);
                this.sumX -= (double)b.x;
                this.sumY -= (double)b.y;
                this.sumZ -= (double)b.z;
            }
            if (neighbours.size() > 1) {
                block.neighbours = 0;
                for (ESPBlock b : neighbours) {
                    int x = b.x - block.x;
                    if (x == 1) {
                        block.neighbours |= 8;
                    } else if (x == -1) {
                        block.neighbours |= 0x80;
                    }
                    int y = b.y - block.y;
                    if (y == 1) {
                        block.neighbours |= 0x200;
                    } else if (y == -1) {
                        block.neighbours |= 0x4000;
                    }
                    int z = b.z - block.z;
                    if (z == 1) {
                        block.neighbours |= 2;
                        continue;
                    }
                    if (z != -1) continue;
                    block.neighbours |= 0x20;
                }
                group.trySplit(block);
            }
        }
    }

    public void merge(ESPGroup group) {
        this.blocks.ensureCapacity(this.blocks.size() + group.blocks.size());
        for (ESPBlock block : group.blocks) {
            this.add(block, false, false);
        }
        blockEsp.removeGroup(group);
    }

    public void render(Render3DEvent event) {
        ESPBlockData blockData = blockEsp.getBlockData(this.block);
        if (blockData.tracer) {
            event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, this.sumX / (double)this.blocks.size() + 0.5, this.sumY / (double)this.blocks.size() + 0.5, this.sumZ / (double)this.blocks.size() + 0.5, blockData.tracerColor);
        }
    }
}

