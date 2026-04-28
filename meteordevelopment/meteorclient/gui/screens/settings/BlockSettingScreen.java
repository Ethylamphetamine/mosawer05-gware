/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.block.Block
 *  net.minecraft.block.Blocks
 *  net.minecraft.registry.Registries
 *  org.apache.commons.lang3.StringUtils
 */
package meteordevelopment.meteorclient.gui.screens.settings;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.widgets.WItemWithLabel;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WButton;
import meteordevelopment.meteorclient.settings.BlockSetting;
import meteordevelopment.meteorclient.utils.misc.Names;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import org.apache.commons.lang3.StringUtils;

public class BlockSettingScreen
extends WindowScreen {
    private final BlockSetting setting;
    private WTable table;
    private WTextBox filter;
    private String filterText = "";

    public BlockSettingScreen(GuiTheme theme, BlockSetting setting) {
        super(theme, "Select Block");
        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        this.filter = this.add(this.theme.textBox("")).minWidth(400.0).expandX().widget();
        this.filter.setFocused(true);
        this.filter.action = () -> {
            this.filterText = this.filter.get().trim();
            this.table.clear();
            this.initTable();
        };
        this.table = this.add(this.theme.table()).expandX().widget();
        this.initTable();
    }

    private void initTable() {
        for (Block block : Registries.BLOCK) {
            if (this.setting.filter != null && !this.setting.filter.test(block) || this.skipValue(block)) continue;
            WItemWithLabel item = this.theme.itemWithLabel(block.asItem().getDefaultStack(), Names.get(block));
            if (!this.filterText.isEmpty() && !StringUtils.containsIgnoreCase((CharSequence)item.getLabelText(), (CharSequence)this.filterText)) continue;
            this.table.add(item);
            WButton select = this.table.add(this.theme.button("Select")).expandCellX().right().widget();
            select.action = () -> {
                this.setting.set(block);
                this.close();
            };
            this.table.row();
        }
    }

    protected boolean skipValue(Block value) {
        return value == Blocks.AIR || Registries.BLOCK.getId((Object)value).getPath().endsWith("_wall_banner");
    }
}

