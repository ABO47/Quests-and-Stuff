package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.render.CanvasTextRenderer;
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
        state.questDetailsTextEditTarget = id;
        state.questDetailsTextEditDraft = text.text();
        state.canvasTextEditOpen = true;
        state.canvasTextEditTarget = id;
        state.canvasTextEditDraft = text.text();
        state.canvasTextEditCursor = state.canvasTextEditDraft.length();
        state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
        state.selectingCanvasTextRange = false;
        state.canvasTextMenuOpen = false;
        state.canvasTextMenuTarget = "";
        state.questDetailsTextStyleOpen = true;
        state.questDetailsTextStyleTarget = id;
        state.questDetailsTextFontSizeSliderTarget = "";
        focus.run();
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details text inline edit start quest={} text={}", questId, id);
    }

    boolean handleKey(int keyCode) {
        if (!isEditing()) {
            return false;
        }
        if (isCtrlDown() && keyCode == GLFW.GLFW_KEY_A) {
            state.canvasTextSelectionAnchor = 0;
            state.canvasTextEditCursor = state.canvasTextEditDraft.length();
            refresh.run();
            return true;
        }
        if (isCtrlDown() && keyCode == GLFW.GLFW_KEY_C) {
            copySelection();
            return true;
        }
        if (isCtrlDown() && keyCode == GLFW.GLFW_KEY_X) {
            copySelection();
            if (deleteSelection()) {
                previewTextDraft();
            }
            return true;
        }
        if (isCtrlDown() && keyCode == GLFW.GLFW_KEY_V) {
            insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            previewTextDraft();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            finish("escape");
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            insertText("\n");
            previewTextDraft();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!deleteSelection()) {
                int cursor = clampedCursor();
                if (cursor > 0) {
                    replaceTextRange(cursor - 1, cursor, "");
                }
            }
            previewTextDraft();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (!deleteSelection()) {
                int cursor = clampedCursor();
                if (cursor < state.canvasTextEditDraft.length()) {
                    replaceTextRange(cursor, cursor + 1, "");
                }
            }
            previewTextDraft();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            moveTextCursor(Math.max(0, clampedCursor() - 1), isShiftDown());
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            moveTextCursor(Math.min(state.canvasTextEditDraft.length(), clampedCursor() + 1), isShiftDown());
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_HOME) {
            moveTextCursor(0, isShiftDown());
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_END) {
            moveTextCursor(state.canvasTextEditDraft.length(), isShiftDown());
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
        insertText(String.valueOf(codePoint));
        previewTextDraft();
        return true;
    }

    boolean isEditing() {
        return state.canvasTextEditOpen && !state.canvasTextEditTarget.isBlank()
                && state.canvasTextEditTarget.equals(state.questDetailsTextEditTarget);
    }

    boolean hitTextEditor(CanvasTextLayer text, int lx, int visibleY) {
        final boolean[] hit = new boolean[1];
        withSelectionGeometry(() -> {
            CanvasTextLayer draft = text.withText(state.canvasTextEditDraft);
            double[] local = CanvasRenderer.canvasTextLocalScreenPoint(state, draft, lx, visibleY);
            int sw = CanvasGeometry.screenSpan(state, draft.w());
            int sh = CanvasGeometry.screenSpan(state, draft.h());
            hit[0] = local[0] >= 0 && local[0] <= sw && local[1] >= 0 && local[1] <= sh;
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
        state.canvasTextEditCursor = Math.max(0, Math.min(cursor[0], state.canvasTextEditDraft.length()));
        if (resetAnchor) {
            state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
        }
    }

    void finish(String reason) {
        if (!isEditing()) {
            return;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        CanvasTextLayer text = model.text(state.canvasTextEditTarget);
        if (text != null) {
            model.putText(text.withText(state.canvasTextEditDraft));
            QuestDetailsDescriptionModel.save(Minecraft.getInstance().player, questId, model);
        } else {
            previewTextDraft();
        }
        state.canvasTextEditOpen = false;
        state.canvasTextEditTarget = "";
        state.questDetailsTextEditTarget = "";
        state.questDetailsTextEditDraft = "";
        state.canvasTextEditCursor = 0;
        state.canvasTextSelectionAnchor = 0;
        state.selectingCanvasTextRange = false;
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
        model.putText(text.withText(state.canvasTextEditDraft));
        QuestDetailsDescriptionModel.preview(questId, model);
        refresh.run();
    }

    private void insertText(String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        int start = selectionStart();
        int end = selectionEnd();
        int keep = state.canvasTextEditDraft.length() - Math.max(0, end - start);
        String insert = value.replace("\r\n", "\n").replace('\r', '\n');
        if (keep + insert.length() > 2048) {
            insert = insert.substring(0, Math.max(0, 2048 - keep));
        }
        replaceTextRange(start, end, insert);
    }

    private boolean deleteSelection() {
        if (selectionStart() >= selectionEnd()) {
            return false;
        }
        replaceTextRange(selectionStart(), selectionEnd(), "");
        return true;
    }

    private void copySelection() {
        int start = selectionStart();
        int end = selectionEnd();
        if (start >= end) {
            return;
        }
        Minecraft.getInstance().keyboardHandler.setClipboard(state.canvasTextEditDraft.substring(start, end));
    }

    private void replaceTextRange(int start, int end, String value) {
        int safeStart = Math.max(0, Math.min(start, state.canvasTextEditDraft.length()));
        int safeEnd = Math.max(safeStart, Math.min(end, state.canvasTextEditDraft.length()));
        String insert = value == null ? "" : value;
        state.canvasTextEditDraft = state.canvasTextEditDraft.substring(0, safeStart) + insert + state.canvasTextEditDraft.substring(safeEnd);
        state.canvasTextEditCursor = safeStart + insert.length();
        state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        CanvasTextLayer text = model.text(state.canvasTextEditTarget);
        if (text != null) {
            model.putText(text.replaceTextRange(safeStart, safeEnd, insert));
            QuestDetailsDescriptionModel.preview(questId, model);
        }
        refresh.run();
    }

    private void moveTextCursor(int cursor, boolean extendSelection) {
        state.canvasTextEditCursor = Math.max(0, Math.min(cursor, state.canvasTextEditDraft.length()));
        if (!extendSelection) {
            state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
        }
    }

    private int clampedCursor() {
        state.canvasTextEditCursor = Math.max(0, Math.min(state.canvasTextEditCursor, state.canvasTextEditDraft.length()));
        return state.canvasTextEditCursor;
    }

    private int selectionStart() {
        return Math.max(0, Math.min(state.canvasTextEditDraft.length(), Math.min(state.canvasTextEditCursor, state.canvasTextSelectionAnchor)));
    }

    private int selectionEnd() {
        return Math.max(0, Math.min(state.canvasTextEditDraft.length(), Math.max(state.canvasTextEditCursor, state.canvasTextSelectionAnchor)));
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
