package com.abo47.questsandstuff.client.tablet.details;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.details.description.QuestDetailsDescriptionPanel;
import com.abo47.questsandstuff.client.tablet.details.objective.QuestDetailsObjectivesPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.tools.TabletToolsMenu;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawCanvasPanelChrome;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawCanvasPanelOutlines;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawPanelChrome;
import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawPanelOutline;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.GAP;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.SPLITTER_W;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class QuestDetailsWindowLayout {
    private QuestDetailsWindowLayout() {
    }

    static void rebuild(WidgetGroup layer, TabletUiState state, Player player, Runnable refresh) {
        layer.clearAllWidgets();
        layer.setVisible(state.questDetailsOpen);
        layer.setActive(state.questDetailsOpen);
        if (!state.questDetailsOpen) {
            return;
        }

        String questId = state.questDetailsQuestId == null ? "" : state.questDetailsQuestId.trim();
        CompoundTag quest = ClientQuestCache.quests().get(questId);
        if (questId.isBlank() || quest == null) {
            QuestDetailsWindowLifecycle.close(state);
            return;
        }

        QuestDetailsWindowFrame frame = QuestDetailsWindowFrame.centered(layer);
        rememberFrame(state, frame);
        addDimLayer(layer);
        WidgetGroup modal = addModal(layer, frame);
        state.questDetailsScreenX = modal.getPositionX();
        state.questDetailsScreenY = modal.getPositionY();

        int leftW = QuestDetailsWindowGeometry.leftPanelWidth(state);
        int splitterX = CHAPTER_X + leftW + Math.max(0, (GAP - SPLITTER_W) / 2);
        int canvasX = CHAPTER_X + leftW + GAP;
        int canvasW = QuestDetailsWindowGeometry.canvasPanelWidth(leftW);
        int[] viewport = QuestDetailsWindowGeometry.mainCanvasViewport(state, canvasW);
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

    private static void rememberFrame(TabletUiState state, QuestDetailsWindowFrame frame) {
        state.questDetailsX = frame.x();
        state.questDetailsY = frame.y();
        state.questDetailsScreenX = frame.x();
        state.questDetailsScreenY = frame.y();
        state.questDetailsW = frame.w();
        state.questDetailsH = frame.h();
    }

    private static void addDimLayer(WidgetGroup layer) {
        WidgetGroup dim = new WidgetGroup(0, 0, layer.getSizeWidth(), layer.getSizeHeight());
        dim.setBackground(Surfaces.fill(withAlpha(ModColors.SURFACE_BASE, 120)));
        layer.addWidget(dim);
    }

    private static WidgetGroup addModal(WidgetGroup layer, QuestDetailsWindowFrame frame) {
        WidgetGroup modal = new WidgetGroup(frame.x(), frame.y(), frame.w(), frame.h());
        modal.setBackground(Surfaces.transparentBorder(ModColors.BORDER_BASE));
        layer.addWidget(modal);
        return modal;
    }

    private static void addObjectivePanel(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int leftW) {
        WidgetGroup objectivePanel = new WidgetGroup(CHAPTER_X, CHAPTER_Y, leftW, CHAPTER_H) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawPanelChrome(graphics, this);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                drawPanelOutline(graphics, this);
            }
        };
        modal.addWidget(objectivePanel);
        QuestDetailsObjectivesPanel.rebuild(
                objectivePanel,
                state,
                player,
                refresh,
                questId,
                quest,
                QuestDetailsWindow.CONTENT_INSET,
                QuestDetailsWindow.CONTENT_INSET,
                leftW - QuestDetailsWindow.CONTENT_INSET * 2,
                CHAPTER_H - QuestDetailsWindow.CONTENT_INSET * 2
        );
    }

    private static WidgetGroup canvasPanel(TabletUiState state, int canvasX, int canvasW, int[] viewport) {
        return new WidgetGroup(canvasX, CANVAS_Y, canvasW, CANVAS_H) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawCanvasPanelChrome(graphics, this, viewport[0], viewport[1], viewport[2], viewport[3]);
                drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
                drawCanvasPanelOutlines(graphics, this, viewport[0], viewport[1], viewport[2], viewport[3], state.canEdit && state.questDetailsEditMode, false, state.questDetailsGridOpacityPercent);
            }
        };
    }

    private record QuestDetailsWindowFrame(int x, int y, int w, int h) {
        static QuestDetailsWindowFrame centered(WidgetGroup layer) {
            int w = Math.min(QuestDetailsWindow.WINDOW_W, Math.max(64, layer.getSizeWidth()));
            int h = Math.min(QuestDetailsWindow.WINDOW_H, Math.max(64, layer.getSizeHeight()));
            int x = Math.max(0, (layer.getSizeWidth() - w) / 2);
            int y = Math.max(0, (layer.getSizeHeight() - h) / 2);
            return new QuestDetailsWindowFrame(x, y, w, h);
        }
    }
}
