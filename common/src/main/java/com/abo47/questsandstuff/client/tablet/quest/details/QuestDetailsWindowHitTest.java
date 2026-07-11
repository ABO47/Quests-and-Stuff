package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.GAP;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.BODY_W;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.BODY_H;


final class QuestDetailsWindowHitTest {
    private QuestDetailsWindowHitTest() {
    }

    static boolean isInside(TabletUiState state, double mouseX, double mouseY) {
        if (state == null || !state.questDetails.questDetailsOpen) {
            return false;
        }
        return mouseX >= state.questDetails.questDetailsScreenX
                && mouseX <= state.questDetails.questDetailsScreenX + state.questDetails.questDetailsW
                && mouseY >= state.questDetails.questDetailsScreenY
                && mouseY <= state.questDetails.questDetailsScreenY + state.questDetails.questDetailsH;
    }

    static boolean isContextMenuHit(TabletUiState state, double mouseX, double mouseY) {
        if (state == null || !state.questDetails.questDetailsOpen || !state.questDetails.questDetailsContextOpen || state.questDetails.questDetailsContextW <= 0 || state.questDetails.questDetailsContextH <= 0) {
            return false;
        }
        int x = state.questDetails.questDetailsScreenX + state.questDetails.questDetailsContextX;
        int y = state.questDetails.questDetailsScreenY + state.questDetails.questDetailsContextY;
        return mouseX >= x && mouseX <= x + state.questDetails.questDetailsContextW
                && mouseY >= y && mouseY <= y + state.questDetails.questDetailsContextH;
    }

    static boolean isTextStyleMenuHit(TabletUiState state, double mouseX, double mouseY) {
        if (state == null || !state.questDetails.questDetailsOpen || !state.questDetails.questDetailsTextStyleOpen
                || state.questDetails.questDetailsTextStyleMenuW <= 0 || state.questDetails.questDetailsTextStyleMenuH <= 0) {
            return false;
        }
        int menuX = state.questDetails.questDetailsTextStyleMenuX;
        int menuY = state.questDetails.questDetailsTextStyleMenuY;
        int menuW = state.questDetails.questDetailsTextStyleMenuW;
        int menuH = state.questDetails.questDetailsTextStyleMenuH;
        int absX = state.questDetails.questDetailsScreenX + menuX;
        int absY = state.questDetails.questDetailsScreenY + menuY;
        return insideLoose(mouseX, mouseY, absX, absY, menuW, menuH)
                || insideLoose(mouseX, mouseY, menuX, menuY, menuW, menuH)
                || insideLoose(mouseX, mouseY, state.questDetails.questDetailsX + menuX, state.questDetails.questDetailsY + menuY, menuW, menuH);
    }

    static boolean isTextStyleOwnerHit(TabletUiState state, double mouseX, double mouseY) {
        if (isTextStyleMenuHit(state, mouseX, mouseY)) {
            return true;
        }
        if (state == null || !state.questDetails.questDetailsOpen
                || (!state.questDetails.questDetailsTextStyleOpen && state.questDetails.questDetailsTextFontSizeFieldTarget.isBlank())) {
            return false;
        }
        String target = state.questDetails.questDetailsTextStyleTarget == null ? "" : state.questDetails.questDetailsTextStyleTarget;
        if (target.isBlank()) {
            target = state.questDetails.questDetailsTextFontSizeFieldTarget == null ? "" : state.questDetails.questDetailsTextFontSizeFieldTarget;
        }
        if (target.isBlank()) {
            return false;
        }
        return isQuestDetailsTextHit(state, target, mouseX, mouseY, true);
    }

    static boolean isTextEditorHit(TabletUiState state, double mouseX, double mouseY) {
        if (state == null || !state.questDetails.questDetailsOpen || !TextEditSession.isQuestDetailsEditing(state)) {
            return false;
        }
        return isQuestDetailsTextHit(state, state.questDetails.questDetailsTextEditTarget, mouseX, mouseY, true);
    }

    private static boolean isQuestDetailsTextHit(TabletUiState state, String textId, double mouseX, double mouseY, boolean useDraftWhenEditing) {
        String questId = state.questDetails.questDetailsQuestId == null ? "" : state.questDetails.questDetailsQuestId.trim();
        String normalizedTextId = textId == null ? "" : textId.trim();
        if (questId.isBlank() || normalizedTextId.isBlank()) {
            return false;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestStateFacade.quest(questId));
        CanvasTextLayer text = model.text(normalizedTextId);
        if (text == null) {
            return false;
        }
        int leftW = QuestDetailsWindow.leftPanelWidth(state);
        int canvasX = leftW + GAP;
        int frameW = state.questDetails.questDetailsW > 0 ? state.questDetails.questDetailsW : BODY_W;
        int frameH = state.questDetails.questDetailsH > 0 ? state.questDetails.questDetailsH : BODY_H;
        int canvasW = QuestDetailsWindow.canvasPanelWidth(leftW, frameW);
        int[] viewport = QuestDetailsWindow.mainCanvasViewport(canvasW, frameH);
        int vx = state.questDetails.questDetailsScreenX + canvasX + viewport[0];
        int vy = state.questDetails.questDetailsScreenY + viewport[1];
        int lx = (int) Math.round(mouseX - vx);
        int ly = (int) Math.round(mouseY - vy);
        if (lx < 0 || ly < 0 || lx > viewport[2] || ly > viewport[3]) {
            return false;
        }
        CanvasTextLayer hitText = useDraftWhenEditing
                && TextEditSession.isEditingTarget(state, normalizedTextId)
                ? text.withText(state.canvas.canvasTextEditDraft)
                : text;
        return isTextLayerLocalHit(state, hitText, lx, ly, viewport[2], viewport[3]);
    }

    private static boolean insideLoose(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x - 2 && mouseX <= x + w + 2 && mouseY >= y - 2 && mouseY <= y + h + 2;
    }

    private static boolean isTextLayerLocalHit(TabletUiState state, CanvasTextLayer text, int localX, int localY, int viewportW, int viewportH) {
        int oldContentX = state.canvas.canvasContentX;
        int oldContentY = state.canvas.canvasContentY;
        int oldContentW = state.canvas.canvasContentW;
        int oldContentH = state.canvas.canvasContentH;
        int oldOffsetX = state.canvas.canvasOffsetX;
        int oldOffsetY = state.canvas.canvasOffsetY;
        float oldZoom = state.canvas.canvasZoom;
        boolean oldGridSnap = state.canvas.gridSnapLocked;
        state.canvas.canvasContentX = 0;
        state.canvas.canvasContentY = -state.questDetails.questDetailsDescScroll;
        state.canvas.canvasContentW = viewportW;
        state.canvas.canvasContentH = viewportH;
        state.canvas.canvasOffsetX = 0;
        state.canvas.canvasOffsetY = 0;
        state.canvas.canvasZoom = 1.0f;
        state.canvas.gridSnapLocked = state.questDetails.questDetailsGridSnapLocked;
        try {
            double[] local = CanvasRenderer.canvasTextLocalScreenPoint(state, text, localX, localY);
            int sw = CanvasGeometry.screenSpan(state, text.w());
            int sh = CanvasGeometry.screenSpan(state, text.h());
            return local[0] >= 0 && local[0] <= sw && local[1] >= 0 && local[1] <= sh;
        } finally {
            state.canvas.canvasContentX = oldContentX;
            state.canvas.canvasContentY = oldContentY;
            state.canvas.canvasContentW = oldContentW;
            state.canvas.canvasContentH = oldContentH;
            state.canvas.canvasOffsetX = oldOffsetX;
            state.canvas.canvasOffsetY = oldOffsetY;
            state.canvas.canvasZoom = oldZoom;
            state.canvas.gridSnapLocked = oldGridSnap;
        }
    }
}
