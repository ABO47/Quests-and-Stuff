package com.abo47.questsandstuff.client.tablet.chunkclaimer;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

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

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.cyclePercent;
import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.toolPercentStep;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.HEADER_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.dynamicLabel;

public final class ChunkClaimerHeaderControls {
    private static final int TOOL_SIZE = HEADER_H;
    private static final int HEADER_GAP = GRID_4;
    private static final int HEADER_INSET = GRID_9;
    private static final int BUTTON_COUNT = 5;
    private static ChunkClaimerHeaderControls current;

    private final LabelWidget countLabel;
    private final ButtonWidget claimBtn;
    private final ButtonWidget forceBtn;
    private final ButtonWidget gridBtn;
    private final ButtonWidget scanBtn;
    private final ButtonWidget opacityBtn;

    private ChunkClaimerHeaderControls(LabelWidget countLabel, ButtonWidget claimBtn, ButtonWidget forceBtn, ButtonWidget gridBtn, ButtonWidget scanBtn, ButtonWidget opacityBtn) {
        this.countLabel = countLabel;
        this.claimBtn = claimBtn;
        this.forceBtn = forceBtn;
        this.gridBtn = gridBtn;
        this.scanBtn = scanBtn;
        this.opacityBtn = opacityBtn;
    }

    static ChunkClaimerHeaderControls create(TabletUiState state, Runnable refresh, int headerY, int bodyW) {
        int countW = Math.max(40, bodyW - HEADER_INSET * 2 - (TOOL_SIZE + HEADER_GAP) * BUTTON_COUNT);

        LabelWidget countLabel = dynamicLabel(HEADER_INSET, headerY + (HEADER_H - 12) / 2,
                ChunkClaimerHeaderControls::countText, TabletColors.TEXT_SECONDARY);
        countLabel.setSize(countW, 12);

        int btnAreaStartX = HEADER_INSET + countW;
        int opacityX = btnAreaStartX + HEADER_GAP;
        int gridX = opacityX + TOOL_SIZE + HEADER_GAP;
        int scanX = gridX + TOOL_SIZE + HEADER_GAP;
        int forceX = scanX + TOOL_SIZE + HEADER_GAP;
        int claimX = forceX + TOOL_SIZE + HEADER_GAP;

        ButtonWidget opacityBtn = createOpacityButton(opacityX, headerY, state, refresh);

        ButtonWidget gridBtn = toggleButton(gridX, headerY, "grid",
                ChunkClaimTranslationKeys.ACTION_GRID, "", state, refresh,
                () -> state.chunkClaimer.showGrid, v -> state.chunkClaimer.showGrid = v);

        ButtonWidget scanBtn = toggleButton(scanX, headerY, "layers",
                ChunkClaimTranslationKeys.ACTION_SCAN, "", state, refresh,
                () -> state.chunkClaimer.surfaceScan, v -> state.chunkClaimer.surfaceScan = v);

        ButtonWidget forceBtn = toggleButton(forceX, headerY, "anchor",
                ChunkClaimTranslationKeys.ACTION_FORCE, ChunkClaimTranslationKeys.ACTION_FORCE_TOOLTIP,
                state, refresh,
                () -> state.chunkClaimer.forceLoadArmed, v -> state.chunkClaimer.forceLoadArmed = v);

        ButtonWidget claimBtn = toggleButton(claimX, headerY, "land-plot",
                ChunkClaimTranslationKeys.ACTION_CLAIM, ChunkClaimTranslationKeys.ACTION_CLAIM_TOOLTIP,
                state, refresh,
                () -> state.chunkClaimer.claimArmed, v -> state.chunkClaimer.claimArmed = v);

        current = new ChunkClaimerHeaderControls(countLabel, claimBtn, forceBtn, gridBtn, scanBtn, opacityBtn);
        return current;
    }

    private static ButtonWidget createOpacityButton(int x, int y, TabletUiState state, Runnable refresh) {
        TabletIconTextButton.Visuals visuals = visualsFor(true);
        TabletIconTextButton[] ref = new TabletIconTextButton[1];
        ref[0] = TabletIconTextButton.icon(x, y, TOOL_SIZE, TOOL_SIZE, "opacity", visuals,
                click -> {
                    int next = cyclePercent(state.chunkClaimer.gridOpacityPercent, toolPercentStep(), click.button == 1);
                    state.chunkClaimer.gridOpacityPercent = next;
                    ref[0].setHoverTooltips(
                            Component.translatable(ChunkClaimTranslationKeys.ACTION_GRID_OPACITY, next));
                    refresh.run();
                });
        ref[0].setHoverTooltips(
                Component.translatable(ChunkClaimTranslationKeys.ACTION_GRID_OPACITY, state.chunkClaimer.gridOpacityPercent));
        return ref[0];
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

    public static void onSync() {
        if (current == null) return;
        var state = TabletUiFactory.getActiveTabletState();
        if (state != null && "chunkclaimer".equals(state.root.currentApp)) {
            current.updateCount();
        }
    }

    ButtonWidget claimBtn() {
        return claimBtn;
    }

    ButtonWidget forceBtn() {
        return forceBtn;
    }

    ButtonWidget gridBtn() {
        return gridBtn;
    }

    ButtonWidget scanBtn() {
        return scanBtn;
    }

    ButtonWidget opacityBtn() {
        return opacityBtn;
    }

    void layout(int headerY, int bodyW) {
        int countW = Math.max(40, bodyW - HEADER_INSET * 2 - (TOOL_SIZE + HEADER_GAP) * BUTTON_COUNT);
        int btnAreaStartX = HEADER_INSET + countW;
        int opacityX = btnAreaStartX + HEADER_GAP;
        int gridX = opacityX + TOOL_SIZE + HEADER_GAP;
        int scanX = gridX + TOOL_SIZE + HEADER_GAP;
        int forceX = scanX + TOOL_SIZE + HEADER_GAP;
        int claimX = forceX + TOOL_SIZE + HEADER_GAP;

        countLabel.setSelfPosition(HEADER_INSET, headerY + (HEADER_H - 12) / 2);
        countLabel.setSize(countW, 12);
        opacityBtn.setSelfPosition(opacityX, headerY);
        gridBtn.setSelfPosition(gridX, headerY);
        scanBtn.setSelfPosition(scanX, headerY);
        forceBtn.setSelfPosition(forceX, headerY);
        claimBtn.setSelfPosition(claimX, headerY);
    }

    void addTo(WidgetGroup mainPanel) {
        mainPanel.addWidget(countLabel);
        mainPanel.addWidget(claimBtn);
        mainPanel.addWidget(forceBtn);
        mainPanel.addWidget(gridBtn);
        mainPanel.addWidget(scanBtn);
        mainPanel.addWidget(opacityBtn);
    }

    private static ButtonWidget toggleButton(int x, int y, String icon, String tooltipKey, String shiftKey,
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
        if (shiftKey != null && !shiftKey.isEmpty()) {
            ref[0].setHoverTooltips(Component.translatable(tooltipKey), Component.translatable(shiftKey));
        } else {
            ref[0].setHoverTooltips(Component.translatable(tooltipKey));
        }
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
