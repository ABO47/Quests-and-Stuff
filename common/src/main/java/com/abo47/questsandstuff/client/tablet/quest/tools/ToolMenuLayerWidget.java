package com.abo47.questsandstuff.client.tablet.quest.tools;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

public final class ToolMenuLayerWidget extends WidgetGroup {
    private final TabletUiState state;
    private final Runnable refresh;

    public ToolMenuLayerWidget(int x, int y, int width, int height, TabletUiState state, Runnable refresh) {
        super(x, y, width, height);
        this.state = state;
        this.refresh = refresh == null ? () -> {
        } : refresh;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (ToolMenuAnimation.finishClosingIfDone(state)) {
            refresh.run();
        }
    }
}
