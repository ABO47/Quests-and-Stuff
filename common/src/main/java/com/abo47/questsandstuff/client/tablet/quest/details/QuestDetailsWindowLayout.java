package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.layout.SplitPanelLayout;
import com.abo47.questsandstuff.client.tablet.layout.TabletGridControls;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionPanel;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestDetailsObjectivesPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.quest.tools.TabletToolsMenu;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.abo47.questsandstuff.client.tablet.ui.widget.TabletWidgetCoordinates;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;


import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_PANEL_GUTTER_BOTTOM;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_PANEL_GUTTER_X;
import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;

final class QuestDetailsWindowLayout {
    private QuestDetailsWindowLayout() {
    }

    static void rebuild(WidgetGroup layer, TabletUiState state, Player player, Runnable refresh) {
        layer.clearAllWidgets();
        boolean visible = QuestDetailsWindow.isVisible(state);
        layer.setVisible(visible);
        layer.setActive(visible);
        if (!visible) {
            return;
        }

        String questId = state.questDetails.questDetailsQuestId == null ? "" : state.questDetails.questDetailsQuestId.trim();
        if (questId.isBlank() || !ClientQuestCache.containsQuest(questId)) {
            QuestDetailsWindowLifecycle.finishClose(state);
            return;
        }
        CompoundTag quest = ClientQuestCache.quest(questId);
        if (!state.root.canEdit && (ClientQuestCache.questLockedPreview(quest) || ClientQuestCache.questHiddenPreview(quest))) {
            QuestDetailsWindowLifecycle.finishClose(state);
            return;
        }

        QuestDetailsWindowFrame frame = QuestDetailsWindowFrame.centered(layer);
        boolean fillsLayer = frame.fills(layer);
        rememberFrame(layer, state, frame);
        if (!fillsLayer) {
            addDimLayer(layer, state);
        }

        int leftW = QuestDetailsWindowGeometry.leftPanelWidth(state);
        int splitterX = SplitPanelLayout.splitterX(0, leftW);
        int canvasX = SplitPanelLayout.rightPanelX(0, leftW);
        int canvasW = QuestDetailsWindowGeometry.canvasPanelWidth(leftW);
        int[] viewport = QuestDetailsWindowGeometry.mainCanvasViewport(state, canvasW);
        WidgetGroup modal = addModal(layer, state, frame, fillsLayer);
        WidgetGroup objectivePanel = addObjectivePanel(modal, state, player, refresh, questId, quest, leftW, frame.h());
        SkinAnchorRegistry.register("quest_details_objectives", objectivePanel);
        WidgetGroup questDetailsSplitter = new QuestDetailsSplitterWidget(splitterX, 0, frame.h(), state, refresh);
        modal.addWidget(questDetailsSplitter);

        WidgetGroup canvasPanel = canvasPanel(state, canvasX, 0, canvasW, frame.h(), viewport);
        modal.addWidget(canvasPanel);

        state.questDetails.questDetailsViewportOriginX = canvasX + viewport[0];
        state.questDetails.questDetailsViewportOriginY = viewport[1];

        WidgetGroup viewportBg = new WidgetGroup(viewport[0], viewport[1], viewport[2], viewport[3]);
        viewportBg.setBackground(Surfaces.fill(ModColors.SURFACE_PANEL));
        canvasPanel.addWidget(viewportBg);

        SkinAnchorRegistry.register("quest_details_splitter", questDetailsSplitter);
        SkinAnchorRegistry.register("quest_details_modal", modal);
        SkinAnchorRegistry.register("quest_details_canvas_panel", canvasPanel);
        SkinAnchorRegistry.register("quest_details_canvas_background", viewportBg);

        int toolsX = QuestDetailsHeader.renderCanvasHeader(canvasPanel, state, player, refresh, questId, viewport[0], viewport[2]);
        QuestDetailsDescriptionPanel.rebuild(viewportBg, state, player, refresh, questId, quest, 0, 0, viewport[2], viewport[3]);
        QuestDetailsObjectivesPanel.renderContextMenu(modal, state, player, refresh, questId);
        QuestDetailsObjectivesPanel.renderTypePicker(modal, state, player, refresh, questId, quest, frame.w(), frame.h());
        TabletToolsMenu.rebuildQuestDetails(modal, state, player, refresh, questId, canvasX + toolsX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.HEADER_H, QuestDetailsWindow.TOOL_SIZE);
    }

    static void syncScreenOrigin(WidgetGroup layer, TabletUiState state) {
        if (layer == null || state == null || !QuestDetailsWindow.isVisible(state)) {
            return;
        }
        state.questDetails.questDetailsScreenX = TabletWidgetCoordinates.screenX(layer, state.questDetails.questDetailsX);
        state.questDetails.questDetailsScreenY = TabletWidgetCoordinates.screenY(layer, state.questDetails.questDetailsY);
    }

    private static void rememberFrame(WidgetGroup layer, TabletUiState state, QuestDetailsWindowFrame frame) {
        state.questDetails.questDetailsX = frame.x();
        state.questDetails.questDetailsY = frame.y();
        state.questDetails.questDetailsW = frame.w();
        state.questDetails.questDetailsH = frame.h();
        syncScreenOrigin(layer, state);
    }

    private static void addDimLayer(WidgetGroup layer, TabletUiState state) {
        WidgetGroup dim = new WidgetGroup(0, 0, layer.getSizeWidth(), layer.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int alpha = dimAlpha(state);
                if (alpha <= 0) {
                    return;
                }
                Surfaces.fill(withAlpha(ModColors.SURFACE_BASE, alpha)).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
        };
        layer.addWidget(dim);
    }

    private static int dimAlpha(TabletUiState state) {
        if (!QuestsAndStuffConfig.questWindowAnimationsEnabled()) {
            return 120;
        }
        float amount = SourceOriginRevealWidget.windowOpenAmount(state.questDetails.questDetailsAnimationStartMs, !state.questDetails.questDetailsClosing);
        return Math.round(120 * amount);
    }

    private static WidgetGroup addModal(WidgetGroup layer, TabletUiState state, QuestDetailsWindowFrame frame, boolean fillsLayer) {
        WidgetGroup modal = new WidgetGroup(frame.x(), frame.y(), frame.w(), frame.h()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                Surfaces.fill(ModColors.SURFACE_BASE).draw(graphics, mouseX, mouseY, getPosition().x, getPosition().y, getSize().width, getSize().height);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                if (!fillsLayer) {
                    TabletPanelChrome.drawPanelOutline(graphics, this);
                }
            }
        };
        modal.setBackground(Surfaces.fill(ModColors.SURFACE_BASE));
        
        if (QuestsAndStuffConfig.questWindowAnimationsEnabled()) {
            layer.addWidget(SourceOriginRevealWidget.windowNoShadow(
                    modal,
                    () -> state.questDetails.questDetailsAnimationStartMs,
                    () -> !state.questDetails.questDetailsClosing,
                    () -> sourceRect(state)
            ));
        } else {
            layer.addWidget(modal);
        }
        return modal;
    }

    private static SourceOriginRevealWidget.SourceRect sourceRect(TabletUiState state) {
        if (!state.questDetails.questDetailsAnimationHasSource) {
            return null;
        }
        return new SourceOriginRevealWidget.SourceRect(
                state.questDetails.questDetailsAnimationSourceX,
                state.questDetails.questDetailsAnimationSourceY,
                state.questDetails.questDetailsAnimationSourceW,
                state.questDetails.questDetailsAnimationSourceH
        );
    }

    private static WidgetGroup addObjectivePanel(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int leftW, int frameH) {
        WidgetGroup objectivePanel = SplitPanelLayout.leftPanel(0, 0, leftW, frameH, state);
        modal.addWidget(objectivePanel);
        int contentX = CHAPTER_PANEL_GUTTER_X;
        int contentY = QuestDetailsWindow.CONTENT_INSET;
        int contentW = Math.max(1, leftW - contentX * 2);
        int contentH = Math.max(1, frameH - contentY - CHAPTER_PANEL_GUTTER_BOTTOM);
        QuestDetailsObjectivesPanel.rebuild(
                objectivePanel,
                state,
                player,
                refresh,
                questId,
                quest,
                contentX,
                contentY,
                contentW,
                contentH
        );
        return objectivePanel;
    }

    private static WidgetGroup canvasPanel(TabletUiState state, int canvasX, int canvasY, int canvasW, int canvasH, int[] viewport) {
        return SplitPanelLayout.rightPanel(canvasX, canvasY, canvasW, canvasH,
                viewport[0], viewport[1], viewport[2], viewport[3],
                QuestDetailsEditController.canEdit(state), false,
                state.questDetails.questDetailsGridOpacityPercent,
                TabletGridControls.defaultGridColor(state),
                state);
    }

    private record QuestDetailsWindowFrame(int x, int y, int w, int h) {
        static QuestDetailsWindowFrame centered(WidgetGroup layer) {
            int w = Math.min(QuestDetailsWindow.WINDOW_W, Math.max(64, layer.getSizeWidth()));
            int h = Math.min(QuestDetailsWindow.WINDOW_H, Math.max(64, layer.getSizeHeight()));
            int x = Math.max(0, (layer.getSizeWidth() - w) / 2);
            int y = Math.max(0, (layer.getSizeHeight() - h) / 2);
            return new QuestDetailsWindowFrame(x, y, w, h);
        }

        boolean fills(WidgetGroup layer) {
            return x <= 0
                    && y <= 0
                    && w >= layer.getSizeWidth()
                    && h >= layer.getSizeHeight();
        }
    }
}
