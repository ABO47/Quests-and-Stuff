package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntSupplier;

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
            CanvasTextLayer draft = text.withText(state.canvasTextEditDraft);
            double[] local = CanvasRenderer.canvasTextLocalScreenPoint(state, draft, lx, visibleY);
            CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, draft.x(), draft.y(), draft.w(), draft.h(), draft.rotation());
            hit[0] = local[0] >= 0 && local[0] <= box.width() && local[1] >= 0 && local[1] <= box.height();
        });
        return hit[0];
    }

    boolean dragSelectionTo(int lx, int visibleY) {
        if (!state.selectingCanvasTextRange || !isEditing()) {
            return false;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        updateCursor(model, state.questDetailsTextEditTarget, lx, visibleY, false);
        refresh.run();
        return true;
    }

    void updateCursor(QuestDetailsDescriptionModel model, String id, int lx, int visibleY, boolean resetAnchor) {
        CanvasTextLayer text = model.text(id);
        if (text == null) {
            return;
        }
        CanvasTextLayer draft = text.withText(state.canvasTextEditDraft);
        final int[] cursor = new int[]{state.canvasTextEditCursor};
        withSelectionGeometry(() -> cursor[0] = CanvasTextRenderer.canvasTextCursorAt(state, draft, lx, visibleY));
        TextEditSession.moveCursor(state, cursor[0], !resetAnchor);
        if (resetAnchor) {
            TextEditSession.moveCursor(state, state.canvasTextEditCursor, false);
        }
    }

    void finish(String reason) {
        if (!isEditing()) {
            return;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        CanvasTextLayer text = model.text(state.canvasTextEditTarget);
        if (text != null) {
            model.putText(fitEditedText(CanvasTextRenderer.fitTextHeight(text.withText(state.canvasTextEditDraft))));
            QuestDetailsDescriptionModel.save(Minecraft.getInstance().player, questId, model);
        } else {
            previewTextDraft();
        }
        TextEditSession.closeQuestDetails(state, true);
    }

    private void previewTextDraft() {
        if (state.canvasTextEditTarget.isBlank()) {
            return;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        CanvasTextLayer text = model.text(state.canvasTextEditTarget);
        if (text == null) {
            return;
        }
        state.questDetailsTextEditDraft = state.canvasTextEditDraft;
        model.putText(fitEditedText(CanvasTextRenderer.fitTextHeight(text.withText(state.canvasTextEditDraft))));
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
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        CanvasTextLayer text = model.text(state.canvasTextEditTarget);
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
        state.canvasContentW = contentW.getAsInt();
        state.canvasContentH = contentH.getAsInt();
        state.canvasOffsetX = 0;
        state.canvasOffsetY = 0;
        state.canvasZoom = 1.0f;
        state.gridSnapLocked = state.questDetailsGridSnapLocked;
        try {
            draw.run();
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
