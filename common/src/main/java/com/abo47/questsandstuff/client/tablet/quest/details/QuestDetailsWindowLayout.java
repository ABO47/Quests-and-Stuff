package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.animation.SourceOriginRevealWidget;
import com.abo47.questsandstuff.client.tablet.layout.SplitPanelLayout;
import com.abo47.questsandstuff.client.tablet.layout.TabletGridControls;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionPanel;
import com.abo47.questsandstuff.client.tablet.quest.details.objective.QuestDetailsObjectivesPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.quest.tools.TabletToolsMenu;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.ui.TabletWidgetCoordinates;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;


import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_PANEL_GUTTER_BOTTOM;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_PANEL_GUTTER_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_Y;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

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
        int splitterX = SplitPanelLayout.splitterX(CHAPTER_X, leftW);
        int canvasX = SplitPanelLayout.rightPanelX(CHAPTER_X, leftW);
        int canvasW = QuestDetailsWindowGeometry.canvasPanelWidth(leftW);
        int[] viewport = QuestDetailsWindowGeometry.mainCanvasViewport(state, canvasW);
        WidgetGroup modal = addModal(layer, state, frame, canvasX + viewport[0], CANVAS_Y + viewport[1], viewport[2], viewport[3], fillsLayer);
        addObjectivePanel(modal, state, player, refresh, questId, quest, leftW);
        modal.addWidget(new QuestDetailsSplitterWidget(splitterX, state, refresh));

        WidgetGroup canvasPanel = canvasPanel(state, canvasX, canvasW, viewport);
        modal.addWidget(canvasPanel);
        int toolsX = QuestDetailsHeader.renderCanvasHeader(canvasPanel, state, player, refresh, questId, viewport[0], viewport[2]);
        QuestDetailsDescriptionPanel.rebuild(modal, state, player, refresh, questId, quest, canvasX + viewport[0], CANVAS_Y + viewport[1], viewport[2], viewport[3]);
        QuestDetailsObjectivesPanel.renderContextMenu(modal, state, player, refresh, questId);
        QuestDetailsObjectivesPanel.renderTypePicker(modal, state, player, refresh, questId, quest, frame.w(), frame.h());
        TabletToolsMenu.rebuildQuestDetails(modal, state, player, refresh, questId, canvasX + toolsX, CANVAS_Y + QuestDetailsWindow.TOP_Y, QuestDetailsWindow.HEADER_H, QuestDetailsWindow.TOOL_SIZE);
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

    private static WidgetGroup addModal(WidgetGroup layer, TabletUiState state, QuestDetailsWindowFrame frame, int holeX, int holeY, int holeW, int holeH, boolean fillsLayer) {
        WidgetGroup modal = new WidgetGroup(frame.x(), frame.y(), frame.w(), frame.h()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawModalSurface(graphics, this, holeX, holeY, holeW, holeH, fillsLayer);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
            }
        };
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

    private static void drawModalSurface(GuiGraphics graphics, WidgetGroup modal, int holeX, int holeY, int holeW, int holeH, boolean fillsLayer) {
        int x = modal.getPositionX();
        int y = modal.getPositionY();
        int w = modal.getSizeWidth();
        int h = modal.getSizeHeight();
        int left = x + 1;
        int top = y + 1;
        int right = x + Math.max(1, w - 1);
        int bottom = y + Math.max(1, h - 1);
        int holeLeft = Math.max(left, Math.min(right, x + holeX));
        int holeTop = Math.max(top, Math.min(bottom, y + holeY));
        int holeRight = Math.max(left, Math.min(right, x + holeX + Math.max(0, holeW)));
        int holeBottom = Math.max(top, Math.min(bottom, y + holeY + Math.max(0, holeH)));
        IGuiTexture fill = Surfaces.fill(ModColors.SURFACE_BASE);
        if (holeRight <= holeLeft || holeBottom <= holeTop) {
            fillModalRect(fill, graphics, left, top, right, bottom);
        } else {
            fillModalRect(fill, graphics, left, top, right, holeTop);
            fillModalRect(fill, graphics, left, holeBottom, right, bottom);
            fillModalRect(fill, graphics, left, holeTop, holeLeft, holeBottom);
            fillModalRect(fill, graphics, holeRight, holeTop, right, holeBottom);
        }
        if (!fillsLayer) {
            TabletPanelChrome.drawRectOutline(graphics, x, y, w, h, ModColors.BORDER_BASE);
        }
    }

    private static void fillModalRect(IGuiTexture fill, GuiGraphics graphics, int left, int top, int right, int bottom) {
        if (right > left && bottom > top) {
            fill.draw(graphics, 0, 0, left, top, right - left, bottom - top);
        }
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

    private static void addObjectivePanel(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int leftW) {
        WidgetGroup objectivePanel = SplitPanelLayout.leftPanel(CHAPTER_X, CHAPTER_Y, leftW, CHAPTER_H);
        modal.addWidget(objectivePanel);
        int contentX = CHAPTER_PANEL_GUTTER_X;
        int contentY = QuestDetailsWindow.CONTENT_INSET;
        int contentW = Math.max(1, leftW - contentX * 2);
        int contentH = Math.max(1, CHAPTER_H - contentY - CHAPTER_PANEL_GUTTER_BOTTOM);
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
    }

    private static WidgetGroup canvasPanel(TabletUiState state, int canvasX, int canvasW, int[] viewport) {
        return SplitPanelLayout.rightPanel(canvasX, CANVAS_Y, canvasW, CANVAS_H,
                viewport[0], viewport[1], viewport[2], viewport[3],
                QuestDetailsEditState.canEdit(state), false,
                state.questDetails.questDetailsGridOpacityPercent,
                TabletGridControls.defaultGridColor(state));
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
