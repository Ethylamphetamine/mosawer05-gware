/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.gui.screen.Screen
 */
package meteordevelopment.meteorclient.gui.tabs.builtin;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.tabs.Tab;
import meteordevelopment.meteorclient.gui.tabs.TabScreen;
import meteordevelopment.meteorclient.gui.tabs.WindowTabScreen;
import meteordevelopment.meteorclient.gui.widgets.containers.WHorizontalList;
import meteordevelopment.meteorclient.gui.widgets.containers.WTable;
import meteordevelopment.meteorclient.gui.widgets.input.WTextBox;
import meteordevelopment.meteorclient.gui.widgets.pressable.WMinus;
import meteordevelopment.meteorclient.gui.widgets.pressable.WPlus;
import meteordevelopment.meteorclient.systems.friends.Friend;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.utils.misc.NbtUtils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import net.minecraft.client.gui.screen.Screen;

public class FriendsTab
extends Tab {
    public FriendsTab() {
        super("Friends");
    }

    @Override
    public TabScreen createScreen(GuiTheme theme) {
        return new FriendsScreen(theme, this);
    }

    @Override
    public boolean isScreen(Screen screen) {
        return screen instanceof FriendsScreen;
    }

    private static class FriendsScreen
    extends WindowTabScreen {
        public FriendsScreen(GuiTheme theme, Tab tab) {
            super(theme, tab);
        }

        @Override
        public void initWidgets() {
            WTable table = this.add(this.theme.table()).expandX().minWidth(400.0).widget();
            this.initTable(table);
            this.add(this.theme.horizontalSeparator()).expandX();
            WHorizontalList list = this.add(this.theme.horizontalList()).expandX().widget();
            WTextBox nameW = list.add(this.theme.textBox("", (text, c) -> c != ' ')).expandX().widget();
            nameW.setFocused(true);
            WPlus add = list.add(this.theme.plus()).widget();
            this.enterAction = add.action = () -> {
                String name = nameW.get().trim();
                Friend friend = new Friend(name, Friend.FriendType.Friend);
                if (Friends.get().add(friend)) {
                    nameW.set("");
                    this.reload();
                    MeteorExecutor.execute(() -> {
                        friend.updateInfo();
                        this.reload();
                    });
                }
            };
        }

        private void initTable(WTable table) {
            table.clear();
            if (Friends.get().isEmpty()) {
                return;
            }
            Friends.get().friendStream().forEach(friend -> MeteorExecutor.execute(() -> {
                if (friend.headTextureNeedsUpdate()) {
                    friend.updateInfo();
                    this.reload();
                }
            }));
            Friends.get().friendStream().forEach(friend -> {
                table.add(this.theme.texture(32.0, 32.0, friend.getHead().needsRotate() ? 90.0 : 0.0, friend.getHead()));
                table.add(this.theme.label(friend.getName()));
                WMinus remove = table.add(this.theme.minus()).expandCellX().right().widget();
                remove.action = () -> {
                    Friends.get().remove((Friend)friend);
                    this.reload();
                };
                table.row();
            });
        }

        @Override
        public boolean toClipboard() {
            return NbtUtils.toClipboard(Friends.get());
        }

        @Override
        public boolean fromClipboard() {
            return NbtUtils.fromClipboard(Friends.get());
        }
    }
}

