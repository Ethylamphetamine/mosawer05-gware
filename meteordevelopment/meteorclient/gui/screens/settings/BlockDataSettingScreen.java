/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.client.gui.screen.Screen
 *  net.minecraft.registry.Registries
 *  org.apache.commons.lang3.StringUtils
 */
package meteordevelopment.meteorclient.gui.screens.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.renderer.GuiRenderer;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BlockDataSetting;
import meteordevelopment.meteorclient.settings.IBlockData;
import meteordevelopment.meteorclient.utils.misc.IChangeable;
import meteordevelopment.meteorclient.utils.misc.ICopyable;
import meteordevelopment.meteorclient.utils.misc.ISerializable;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.registry.Registries;
import org.apache.commons.lang3.StringUtils;

public class BlockDataSettingScreen
extends WindowScreen {
    private static final List<Block> BLOCKS = new ArrayList<Block>(100);
    private final BlockDataSetting<?> setting;
    private WTable table;
    private String filterText = "";

    public BlockDataSettingScreen(GuiTheme theme, BlockDataSetting<?> setting) {
        super(theme, "Configure Blocks");
        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        WTextBox filter = this.add(this.theme.textBox("")).minWidth(400.0).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            this.filterText = filter.get().trim();
            this.table.clear();
            this.initTable();
        };
        this.table = this.add(this.theme.table()).expandX().widget();
        this.initTable();
    }

    public <T extends ICopyable<T> & ISerializable<T> & IBlockData<T>> void initTable() {
        for (Block block : Registries.BLOCK) {
            ICopyable blockData = (ICopyable)((Map)this.setting.get()).get(block);
            if (blockData != null && ((IChangeable)((Object)blockData)).isChanged()) {
                BLOCKS.addFirst(block);
                continue;
            }
            BLOCKS.add(block);
        }
        for (Block block : BLOCKS) {
            String name = Names.get(block);
            if (!StringUtils.containsIgnoreCase((CharSequence)name, (CharSequence)this.filterText)) continue;
            ICopyable blockData = (ICopyable)((Map)this.setting.get()).get(block);
            this.table.add(this.theme.itemWithLabel(block.asItem().getDefaultStack(), Names.get(block))).expandCellX();
            this.table.add(this.theme.label(blockData != null && ((IChangeable)((Object)blockData)).isChanged() ? "*" : " "));
            WButton edit = this.table.add(this.theme.button(GuiRenderer.EDIT)).widget();
            edit.action = () -> {
                ICopyable data = blockData;
                if (data == null) {
                    data = ((ICopyable)this.setting.defaultData.get()).copy();
                }
                MeteorClient.mc.setScreen((Screen)((IBlockData)((Object)data)).createScreen(this.theme, block, this.setting));
            };
            WButton reset = this.table.add(this.theme.button(GuiRenderer.RESET)).widget();
            reset.action = () -> {
                ((Map)this.setting.get()).remove(block);
                this.setting.onChanged();
                if (blockData != null && ((IChangeable)((Object)blockData)).isChanged()) {
                    this.table.clear();
                    this.initTable();
                }
            };
            this.table.row();
        }
        BLOCKS.clear();
    }
}

