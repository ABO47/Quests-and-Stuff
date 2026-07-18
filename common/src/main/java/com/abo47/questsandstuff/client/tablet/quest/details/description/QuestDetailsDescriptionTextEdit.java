package com.abo47.questsandstuff.client.tablet.quest.details.description;

import java.util.function.IntSupplier;

import org.lwjgl.glfw.GLFW;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.lowdragmc.lowdraglib.gui.widget.Widget.isCtrlDown;
import static com.lowdragmc.lowdraglib.gui.widget.Widget.isShiftDown;

public final class QuestDetailsDescriptionTextEdit {
    private final TabletUiState state;
    private final Runnable refresh;
    private final String questId;
    private final IntSupplier contentW;
    private final IntSupplier contentH;

    QuestDetailsDescriptionTextEdit(TabletUiState state, Runnable refresh, String questId, IntSupplier contentW, IntSupplier contentH) {
        this.state = state;
        this.refresh = refresh;
        this.questId = questId;
        this.contentW = contentW;
        this.contentH = contentH;
    }

    void begin(QuestDetailsDescriptionModel model, String id, Runnable focus) {
        CanvasTextLayer text = model.text(id);
        if (text == null) {
            return;
        }
        TextEditSession.beginQuestDetails(state, id, text.text());
        TextStyleSession.openQuestDetails(state, id);
        TextStyleSession.setFontSizeTarget(state, TextStyleSession.Surface.QUEST_DETAILS, "");
        focus.run();
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details text inline edit start quest={} text={}", questId, id);
    }

    boolean handleKey(int keyCode) {
        if (!isEditing()) {
            return false;
        }
        if (isCtrlDown() && keyCode == GLFW.GLFW_KEY_A) {
            TextEditSession.selectAll(state);
            refresh.run();
            return true;
        }
        if (isCtrlDown() && keyCode == GLFW.GLFW_KEY_C) {
            copySelection();
            return true;
        }
        if (isCtrlDown() && keyCode == GLFW.GLFW_KEY_X) {
            copySelection();
            if (applyReplacement(TextEditSession.deleteSelection(state))) {
                previewTextDraft();
            }
            return true;
        }
        if (isCtrlDown() && keyCode == GLFW.GLFW_KEY_V) {
            applyReplacement(TextEditSession.insert(state, Minecraft.getInstance().keyboardHandler.getClipboard()));
            previewTextDraft();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            finish("escape");
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            applyReplacement(TextEditSession.insert(state, "\n"));
            previewTextDraft();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!applyReplacement(TextEditSession.deleteSelection(state))) {
                int cursor = TextEditSession.clampedCursor(state);
                if (cursor > 0) {
                    applyReplacement(TextEditSession.replaceRange(state, cursor - 1, cursor, ""));
                }
            }
            previewTextDraft();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (!applyReplacement(TextEditSession.deleteSelection(state))) {
                int cursor = TextEditSession.clampedCursor(state);
                if (cursor < TextEditSession.draftLength(state)) {
                    applyReplacement(TextEditSession.replaceRange(state, cursor, cursor + 1, ""));
                }
            }
            previewTextDraft();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            TextEditSession.moveCursor(state, Math.max(0, TextEditSession.clampedCursor(state) - 1), isShiftDown());
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            TextEditSession.moveCursor(state, Math.min(TextEditSession.draftLength(state), TextEditSession.clampedCursor(state) + 1), isShiftDown());
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_HOME) {
            TextEditSession.moveCursor(state, 0, isShiftDown());
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_END) {
            TextEditSession.moveCursor(state, TextEditSession.draftLength(state), isShiftDown());
            refresh.run();
            return true;
        }
        return true;
    }

    boolean handleChar(char codePoint) {
        if (!isEditing()) {
            return false;
        }
        if (!SharedConstants.isAllowedChatCharacter(codePoint)) {
            return true;
        }
        applyReplacement(TextEditSession.insert(state, String.valueOf(codePoint)));
        previewTextDraft();
        return true;
    }

    boolean isEditing() {
        return TextEditSession.isQuestDetailsEditing(state);
    }

    boolean hitTextEditor(CanvasTextLayer text, int lx, int visibleY) {
        final boolean[] hit = new boolean[1];
        withSelectionGeometry(() -> {
            CanvasTextLayer draft = text.withText(state.canvas.canvasTextEditDraft);
            double[] local = CanvasRenderer.canvasTextLocalScreenPoint(state, draft, lx, visibleY);
            CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, draft.x(), draft.y(), draft.w(), draft.h(), draft.rotation());
            hit[0] = local[0] >= 0 && local[0] <= box.width() && local[1] >= 0 && local[1] <= box.height();
        });
        return hit[0];
    }

    boolean dragSelectionTo(int lx, int visibleY) {
        if (!state.canvas.selectingCanvasTextRange || !isEditing()) {
            return false;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestStateFacade.quest(questId));
        updateCursor(model, state.questDetails.questDetailsTextEditTarget, lx, visibleY, false);
        refresh.run();
        return true;
    }

    void updateCursor(QuestDetailsDescriptionModel model, String id, int lx, int visibleY, boolean resetAnchor) {
        CanvasTextLayer text = model.text(id);
        if (text == null) {
            return;
        }
        CanvasTextLayer draft = text.withText(state.canvas.canvasTextEditDraft);
        final int[] cursor = new int[]{state.canvas.canvasTextEditCursor};
        withSelectionGeometry(() -> cursor[0] = CanvasTextRenderer.canvasTextCursorAt(state, draft, lx, visibleY));
        TextEditSession.moveCursor(state, cursor[0], !resetAnchor);
        if (resetAnchor) {
            TextEditSession.moveCursor(state, state.canvas.canvasTextEditCursor, false);
        }
    }

    void finish(String reason) {
        if (!isEditing()) {
            return;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestStateFacade.quest(questId));
        CanvasTextLayer text = model.text(state.canvas.canvasTextEditTarget);
        if (text != null) {
            model.putText(fitEditedText(CanvasTextRenderer.fitTextHeight(text.withText(state.canvas.canvasTextEditDraft))));
            QuestDetailsDescriptionModel.save(Minecraft.getInstance().player, questId, model);
        } else {
            previewTextDraft();
        }
        TextEditSession.closeQuestDetails(state, true);
    }

    private void previewTextDraft() {
        if (state.canvas.canvasTextEditTarget.isBlank()) {
            return;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestStateFacade.quest(questId));
        CanvasTextLayer text = model.text(state.canvas.canvasTextEditTarget);
        if (text == null) {
            return;
        }
        state.questDetails.questDetailsTextEditDraft = state.canvas.canvasTextEditDraft;
        model.putText(fitEditedText(CanvasTextRenderer.fitTextHeight(text.withText(state.canvas.canvasTextEditDraft))));
        QuestDetailsDescriptionModel.preview(questId, model);
        refresh.run();
    }

    private void copySelection() {
        if (!TextEditSession.hasSelection(state)) {
            return;
        }
        Minecraft.getInstance().keyboardHandler.setClipboard(TextEditSession.selectedText(state));
    }

    private boolean applyReplacement(TextEditSession.Replacement replacement) {
        if (!replacement.changed()) {
            return false;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestStateFacade.quest(questId));
        CanvasTextLayer text = model.text(state.canvas.canvasTextEditTarget);
        if (text != null) {
            model.putText(fitEditedText(CanvasTextRenderer.fitTextHeight(text.replaceTextRange(replacement.start(), replacement.end(), replacement.value()))));
            QuestDetailsDescriptionModel.preview(questId, model);
        }
        refresh.run();
        return true;
    }

    private CanvasTextLayer fitEditedText(CanvasTextLayer text) {
        return QuestDetailsDescriptionLayout.fitAndClampText(state, text, contentW.getAsInt());
    }

    private void withSelectionGeometry(Runnable draw) {
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
        state.canvas.canvasContentW = contentW.getAsInt();
        state.canvas.canvasContentH = contentH.getAsInt();
        state.canvas.canvasOffsetX = 0;
        state.canvas.canvasOffsetY = 0;
        state.canvas.canvasZoom = 1.0f;
        state.canvas.gridSnapLocked = state.questDetails.questDetailsGridSnapLocked;
        try {
            draw.run();
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
