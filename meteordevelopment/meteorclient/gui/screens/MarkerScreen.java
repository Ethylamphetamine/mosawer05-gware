/*
 * Decompiled with CFR 0.152.
 */
package meteordevelopment.meteorclient.gui.screens;

import meteordevelopment.meteorclient.gui.GuiTheme;
import meteordevelopment.meteorclient.gui.WindowScreen;
import meteordevelopment.meteorclient.gui.utils.Cell;
import meteordevelopment.meteorclient.gui.widgets.WWidget;
import meteordevelopment.meteorclient.gui.widgets.containers.WContainer;
import meteordevelopment.meteorclient.systems.modules.render.marker.BaseMarker;

public class MarkerScreen
extends WindowScreen {
    private final BaseMarker marker;
    private WContainer settingsContainer;

    public MarkerScreen(GuiTheme theme, BaseMarker marker) {
        super(theme, marker.name.get());
        this.marker = marker;
    }

    @Override
    public void initWidgets() {
        WWidget widget;
        if (!this.marker.settings.groups.isEmpty()) {
            this.settingsContainer = this.add(this.theme.verticalList()).expandX().widget();
            this.settingsContainer.add(this.theme.settings(this.marker.settings)).expandX();
        }
        if ((widget = this.getWidget(this.theme)) != null) {
            this.add(this.theme.horizontalSeparator()).expandX();
            Cell<WWidget> cell = this.add(widget);
            if (widget instanceof WContainer) {
                cell.expandX();
            }
        }
    }

    public void tick() {
        super.tick();
        this.marker.settings.tick(this.settingsContainer, this.theme);
    }

    public WWidget getWidget(GuiTheme theme) {
        return null;
    }
}

