package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CANVAS_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.GAP;

final class QuestDetailsWindowHitTest {
    private QuestDetailsWindowHitTest() {
    }

    static boolean isInside(TabletUiState state, double mouseX, double mouseY) {
        if (state == null || !state.questDetailsOpen) {
            return false;
        }
        return mouseX >= state.questDetailsScreenX
                && mouseX <= state.questDetailsScreenX + state.questDetailsW
                && mouseY >= state.questDetailsScreenY
                && mouseY <= state.questDetailsScreenY + state.questDetailsH;
    }

    static boolean isContextMenuHit(TabletUiState state, double mouseX, double mouseY) {
        if (state == null || !state.questDetailsOpen || !state.questDetailsContextOpen || state.questDetailsContextW <= 0 || state.questDetailsContextH <= 0) {
            return false;
        }
        int x = state.questDetailsScreenX + state.questDetailsContextX;
        int y = state.questDetailsScreenY + state.questDetailsContextY;
        return mouseX >= x && mouseX <= x + state.questDetailsContextW
                && mouseY >= y && mouseY <= y + state.questDetailsContextH;
    }

    static boolean isTextStyleMenuHit(TabletUiState state, double mouseX, double mouseY) {
        if (state == null || !state.questDetailsOpen || !state.questDetailsTextStyleOpen
                || state.questDetailsTextStyleMenuW <= 0 || state.questDetailsTextStyleMenuH <= 0) {
            return false;
        }
        int menuX = state.questDetailsTextStyleMenuX;
        int menuY = state.questDetailsTextStyleMenuY;
        int menuW = state.questDetailsTextStyleMenuW;
        int menuH = state.questDetailsTextStyleMenuH;
        int absX = state.questDetailsScreenX + menuX;
        int absY = state.questDetailsScreenY + menuY;
        return insideLoose(mouseX, mouseY, absX, absY, menuW, menuH)
                || insideLoose(mouseX, mouseY, menuX, menuY, menuW, menuH)
                || insideLoose(mouseX, mouseY, state.questDetailsX + menuX, state.questDetailsY + menuY, menuW, menuH);
    }

    static boolean isTextStyleOwnerHit(TabletUiState state, double mouseX, double mouseY) {
        if (isTextStyleMenuHit(state, mouseX, mouseY)) {
            return true;
        }
        if (state == null || !state.questDetailsOpen
                || (!state.questDetailsTextStyleOpen && state.questDetailsTextFontSizeFieldTarget.isBlank())) {
            return false;
        }
        String target = state.questDetailsTextStyleTarget == null ? "" : state.questDetailsTextStyleTarget;
        if (target.isBlank()) {
            target = state.questDetailsTextFontSizeFieldTarget == null ? "" : state.questDetailsTextFontSizeFieldTarget;
        }
        if (target.isBlank()) {
            return false;
        }
        return isQuestDetailsTextHit(state, target, mouseX, mouseY, true);
    }

    static boolean isTextEditorHit(TabletUiState state, double mouseX, double mouseY) {
        if (state == null || !state.questDetailsOpen || !state.canvasTextEditOpen
                || state.questDetailsTextEditTarget.isBlank()
                || !state.questDetailsTextEditTarget.equals(state.canvasTextEditTarget)) {
            return false;
        }
        return isQuestDetailsTextHit(state, state.questDetailsTextEditTarget, mouseX, mouseY, true);
    }

    private static boolean isQuestDetailsTextHit(TabletUiState state, String textId, double mouseX, double mouseY, boolean useDraftWhenEditing) {
        String questId = state.questDetailsQuestId == null ? "" : state.questDetailsQuestId.trim();
        String normalizedTextId = textId == null ? "" : textId.trim();
        if (questId.isBlank() || normalizedTextId.isBlank()) {
            return false;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        CanvasTextLayer text = model.text(normalizedTextId);
        if (text == null) {
            return false;
        }
        int leftW = QuestDetailsWindow.leftPanelWidth(state);
        int canvasX = CHAPTER_X + leftW + GAP;
        int canvasW = QuestDetailsWindow.canvasPanelWidth(leftW);
        int[] viewport = QuestDetailsWindow.mainCanvasViewport(state, canvasW);
        int vx = state.questDetailsScreenX + canvasX + viewport[0];
        int vy = state.questDetailsScreenY + CANVAS_Y + viewport[1];
        int lx = (int) Math.round(mouseX - vx);
        int ly = (int) Math.round(mouseY - vy);
        if (lx < 0 || ly < 0 || lx > viewport[2] || ly > viewport[3]) {
            return false;
        }
        CanvasTextLayer hitText = useDraftWhenEditing
                && state.canvasTextEditOpen
                && normalizedTextId.equals(state.canvasTextEditTarget)
                ? text.withText(state.canvasTextEditDraft)
                : text;
        return isTextLayerLocalHit(state, hitText, lx, ly, viewport[2], viewport[3]);
    }

    private static boolean insideLoose(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x - 2 && mouseX <= x + w + 2 && mouseY >= y - 2 && mouseY <= y + h + 2;
    }

    private static boolean isTextLayerLocalHit(TabletUiState state, CanvasTextLayer text, int localX, int localY, int viewportW, int viewportH) {
        int oldContentX = state.canvasContentX;
        int oldContentY = state.canvasContentY;
        int oldContentW = state.canvasContentW;
        int oldContentH = state.canvasContentH;
        int oldOffsetX = state.canvasOffsetX;
        int oldOffsetY = state.canvasOffsetY;
        float oldZoom = state.canvasZoom;
        boolean oldGridSnap = state.gridSnapLocked;
        state.canvasContentX = 0;
        state.canvasContentY = -state.questDetailsDescScroll;
        state.canvasContentW = viewportW;
        state.canvasContentH = viewportH;
        state.canvasOffsetX = 0;
        state.canvasOffsetY = 0;
        state.canvasZoom = 1.0f;
        state.gridSnapLocked = state.questDetailsGridSnapLocked;
        try {
            double[] local = CanvasRenderer.canvasTextLocalScreenPoint(state, text, localX, localY);
            int sw = CanvasGeometry.screenSpan(state, text.w());
            int sh = CanvasGeometry.screenSpan(state, text.h());
            return local[0] >= 0 && local[0] <= sw && local[1] >= 0 && local[1] <= sh;
        } finally {
            state.canvasContentX = oldContentX;
            state.canvasContentY = oldContentY;
            state.canvasContentW = oldContentW;
            state.canvasContentH = oldContentH;
            state.canvasOffsetX = oldOffsetX;
            state.canvasOffsetY = oldOffsetY;
            state.canvasZoom = oldZoom;
            state.gridSnapLocked = oldGridSnap;
        }
    }
}
