package com.abo47.questsandstuff.client.tablet.chunkclaimer;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.controls.TabletIconTextButton;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.text.ChunkClaimTranslationKeys;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.HEADER_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.dynamicLabel;

final class ChunkClaimerHeaderControls {
    private static final int TOOL_SIZE = HEADER_H;
    private static final int HEADER_GAP = 4;
    private static final int HEADER_INSET = 9;
    private static final int BUTTON_COUNT = 3;

    private final LabelWidget countLabel;
    private final ButtonWidget claimBtn;
    private final ButtonWidget forceBtn;
    private final ButtonWidget gridBtn;

    private ChunkClaimerHeaderControls(LabelWidget countLabel, ButtonWidget claimBtn, ButtonWidget forceBtn, ButtonWidget gridBtn) {
        this.countLabel = countLabel;
        this.claimBtn = claimBtn;
        this.forceBtn = forceBtn;
        this.gridBtn = gridBtn;
    }

    static ChunkClaimerHeaderControls create(TabletUiState state, Runnable refresh, int headerY, int bodyW) {
        int countW = Math.max(40, bodyW - HEADER_INSET * 2 - (TOOL_SIZE + HEADER_GAP) * BUTTON_COUNT);

        LabelWidget countLabel = dynamicLabel(HEADER_INSET, headerY + (HEADER_H - 12) / 2,
                ChunkClaimerHeaderControls::countText, TabletColors.TEXT_SECONDARY);
        countLabel.setSize(countW, 12);

        int btnAreaStartX = HEADER_INSET + countW;
        int claimX = btnAreaStartX + HEADER_GAP;
        int forceX = claimX + TOOL_SIZE + HEADER_GAP;
        int gridX = forceX + TOOL_SIZE + HEADER_GAP;

        ButtonWidget claimBtn = toggleButton(claimX, headerY, "claim_all",
                ChunkClaimTranslationKeys.ACTION_CLAIM, state, refresh,
                () -> state.chunkClaimer.claimArmed, v -> state.chunkClaimer.claimArmed = v);

        ButtonWidget forceBtn = toggleButton(forceX, headerY, "lock",
                ChunkClaimTranslationKeys.ACTION_FORCE, state, refresh,
                () -> state.chunkClaimer.forceLoadArmed, v -> state.chunkClaimer.forceLoadArmed = v);

        ButtonWidget gridBtn = toggleButton(gridX, headerY, "grid",
                ChunkClaimTranslationKeys.ACTION_GRID, state, refresh,
                () -> state.chunkClaimer.showGrid, v -> state.chunkClaimer.showGrid = v);

        return new ChunkClaimerHeaderControls(countLabel, claimBtn, forceBtn, gridBtn);
    }

    private static String countText() {
        int claimed = 0;
        int force = 0;
        for (var chunk : ClientChunkClaimCache.INSTANCE.snapshot()) {
            claimed++;
            if (chunk.forceLoaded()) {
                force++;
            }
        }
        int maxClaimed = QuestsAndStuffConfig.chunkClaimMaxClaimedChunks();
        int maxForce = QuestsAndStuffConfig.chunkClaimMaxForceLoadedChunks();
        return Component.translatable(ChunkClaimTranslationKeys.STATUS_SUMMARY,
                claimed, maxClaimed, force, maxForce).getString();
    }

    LabelWidget countLabel() {
        return countLabel;
    }

    void updateCount() {
        countLabel.setText(countText());
    }

    ButtonWidget claimBtn() {
        return claimBtn;
    }

    ButtonWidget forceBtn() {
        return forceBtn;
    }

    void layout(int headerY, int bodyW) {
        int countW = Math.max(40, bodyW - HEADER_INSET * 2 - (TOOL_SIZE + HEADER_GAP) * BUTTON_COUNT);
        int btnAreaStartX = HEADER_INSET + countW;
        int claimX = btnAreaStartX + HEADER_GAP;
        int forceX = claimX + TOOL_SIZE + HEADER_GAP;
        int gridX = forceX + TOOL_SIZE + HEADER_GAP;

        countLabel.setSelfPosition(HEADER_INSET, headerY + (HEADER_H - 12) / 2);
        countLabel.setSize(countW, 12);
        claimBtn.setSelfPosition(claimX, headerY);
        forceBtn.setSelfPosition(forceX, headerY);
        gridBtn.setSelfPosition(gridX, headerY);
    }

    void addTo(WidgetGroup mainPanel) {
        mainPanel.addWidget(countLabel);
        mainPanel.addWidget(claimBtn);
        mainPanel.addWidget(forceBtn);
        mainPanel.addWidget(gridBtn);
    }

    private static ButtonWidget toggleButton(int x, int y, String icon, String tooltipKey,
                                             TabletUiState state, Runnable refresh,
                                             BooleanSupplier armed, Consumer<Boolean> setArmed) {
        TabletIconTextButton.Visuals visuals = visualsFor(armed.getAsBoolean());
        TabletIconTextButton[] ref = new TabletIconTextButton[1];
        ref[0] = TabletIconTextButton.icon(x, y, TOOL_SIZE, TOOL_SIZE, icon, visuals,
                click -> {
                    setArmed.accept(!armed.getAsBoolean());
                    ref[0].visuals(visualsFor(armed.getAsBoolean()));
                    refresh.run();
                });
        ref[0].setHoverTooltips(Component.translatable(tooltipKey));
        ref[0].visuals(visualsFor(armed.getAsBoolean()));
        return ref[0];
    }

    private static TabletIconTextButton.Visuals visualsFor(boolean armed) {
        int accent = armed ? TabletColors.SUCCESS : TabletColors.ERROR;
        TabletIconTextButton.State idle = TabletIconTextButton.State.of(
                TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_BASE, accent);
        TabletIconTextButton.State hover = TabletIconTextButton.State.of(
                withAlpha(accent, 66), TabletColors.BORDER_ACCENT, accent);
        TabletIconTextButton.State pressed = TabletIconTextButton.State.of(
                withAlpha(accent, 90), accent, TabletColors.TEXT_PRIMARY);
        return new TabletIconTextButton.Visuals(idle, hover, pressed, armed ? accent : -1);
    }
}
