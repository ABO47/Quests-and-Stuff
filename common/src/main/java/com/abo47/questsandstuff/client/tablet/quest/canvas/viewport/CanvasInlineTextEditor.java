package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import static com.lowdragmc.lowdraglib.gui.widget.Widget.isCtrlDown;
import static com.lowdragmc.lowdraglib.gui.widget.Widget.isShiftDown;

public final class CanvasInlineTextEditor {
    private final WidgetGroup viewport;
    private final TabletUiState state;
    private final Runnable refresh;

    public CanvasInlineTextEditor(WidgetGroup viewport, TabletUiState state, Runnable refresh) {
        this.viewport = viewport;
        this.state = state;
        this.refresh = refresh == null ? () -> {
        } : refresh;
    }

    public boolean isEditorHit(int localX, int localY) {
        CanvasTextLayer text = activeText();
        if (text == null) {
            return false;
        }
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
        double[] local = CanvasRenderer.canvasTextLocalScreenPoint(state, text, localX, localY);
        return local[0] >= 0 && local[0] <= box.width() && local[1] >= 0 && local[1] <= box.height();
    }

    public boolean isOwnerHit(int localX, int localY) {
        if (!state.canvasTextMenuOpen || state.canvasTextMenuTarget.isBlank()) {
            return false;
        }
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, TabletStateQueries.selectedGroupName(state), state.canvasTextMenuTarget);
        return CanvasRenderer.isCanvasTextOwnerHit(state, text, localX, localY);
    }

    public boolean isMenuHit(int localX, int localY) {
        if (!state.canvasTextMenuOpen || state.canvasTextMenuTarget.isBlank()) {
            return false;
        }
        String group = TabletStateQueries.selectedGroupName(state);
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, group, state.canvasTextMenuTarget);
        if (text == null) {
            return false;
        }
        int[] bounds = CanvasRenderer.canvasTextMenuBounds(state, text, viewport.getSizeWidth(), viewport.getSizeHeight(), 8);
        return inside(localX, localY, bounds);
    }

    private static boolean inside(int localX, int localY, int[] bounds) {
        return localX >= bounds[0] && localX <= bounds[0] + bounds[2]
                && localY >= bounds[1] && localY <= bounds[1] + bounds[3];
    }

    public void begin(CanvasTextLayer text) {
        state.questDetailsTextEditTarget = "";
        state.questDetailsTextEditDraft = "";
        state.canvasTextEditOpen = true;
        state.canvasTextEditTarget = text.id();
        state.canvasTextEditDraft = text.text();
        state.canvasTextEditCursor = state.canvasTextEditDraft.length();
        state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
        state.selectingCanvasTextRange = false;
        state.canvasTextMenuOpen = true;
        state.canvasTextMenuTarget = text.id();
        state.selectedCanvasTextId = text.id();
        state.selectedCanvasTextIds.clear();
        state.selectedCanvasTextIds.add(text.id());
        state.selectedCanvasImageId = "";
        state.selectedCanvasImageIds.clear();
        state.selectedQuestIds.clear();
        state.draggingCanvasText = false;
        state.resizingCanvasText = false;
        state.rotatingCanvasText = false;
        viewport.setFocus(true);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas text inline edit start id={}", text.id());
    }

    public void close(String reason) {
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas text inline edit close id={} reason={} length={}", state.canvasTextEditTarget, reason, state.canvasTextEditDraft.length());
        state.canvasTextEditOpen = false;
        state.canvasTextEditTarget = "";
        state.canvasTextEditCursor = 0;
        state.canvasTextSelectionAnchor = 0;
        state.selectingCanvasTextRange = false;
    }

    public CanvasTextLayer activeText() {
        if (!mainCanvasTextEditOpen()) {
            return null;
        }
        return CanvasLayerMutations.findCanvasText(state, TabletStateQueries.selectedGroupName(state), state.canvasTextEditTarget);
    }

    public boolean handleKeyPressed(int keyCode) {
        if (!mainCanvasTextEditOpen()) {
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
            if (copySelection() && deleteSelection()) {
                refresh.run();
            }
            return true;
        }
        if (isCtrlDown() && keyCode == GLFW.GLFW_KEY_V) {
            String clip = Minecraft.getInstance().keyboardHandler.getClipboard();
            if (clip != null && !clip.isEmpty()) {
                insert(clip.replace("\r\n", "\n").replace('\r', '\n'));
                refresh.run();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close("escape");
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            insert("\n");
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (deleteSelection()) {
                refresh.run();
            } else {
                int cursor = clampedCursor();
                if (cursor > 0) {
                    replaceRange(cursor - 1, cursor, "");
                    refresh.run();
                }
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (deleteSelection()) {
                refresh.run();
            } else {
                int cursor = clampedCursor();
                if (cursor < state.canvasTextEditDraft.length()) {
                    replaceRange(cursor, cursor + 1, "");
                    refresh.run();
                }
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_LEFT) {
            moveCursor(Math.max(0, clampedCursor() - 1), isShiftDown());
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            moveCursor(Math.min(state.canvasTextEditDraft.length(), clampedCursor() + 1), isShiftDown());
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_HOME) {
            moveCursor(0, isShiftDown());
            refresh.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_END) {
            moveCursor(state.canvasTextEditDraft.length(), isShiftDown());
            refresh.run();
            return true;
        }
        return true;
    }

    public boolean handleCharTyped(char codePoint) {
        if (!mainCanvasTextEditOpen()) {
            return false;
        }
        if (!SharedConstants.isAllowedChatCharacter(codePoint)) {
            return true;
        }
        insert(String.valueOf(codePoint));
        refresh.run();
        return true;
    }

    public boolean dragSelectionTo(int localX, int localY) {
        if (!state.selectingCanvasTextRange || !mainCanvasTextEditOpen()) {
            return false;
        }
        CanvasTextLayer editingText = activeText();
        if (editingText != null) {
            state.canvasTextEditCursor = CanvasRenderer.canvasTextCursorAt(state, editingText, localX, localY);
            refresh.run();
        }
        return true;
    }

    public boolean finishSelectionDrag() {
        if (!state.questDetailsTextEditTarget.isBlank()) {
            return false;
        }
        if (!state.selectingCanvasTextRange) {
            return false;
        }
        state.selectingCanvasTextRange = false;
        refresh.run();
        return true;
    }

    public void moveCursor(int cursor, boolean extendSelection) {
        state.canvasTextEditCursor = Math.max(0, Math.min(cursor, state.canvasTextEditDraft.length()));
        if (!extendSelection) {
            state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
        }
    }

    public boolean deleteSelection() {
        if (!CanvasRenderer.hasTextSelection(state)) {
            return false;
        }
        replaceRange(CanvasRenderer.textSelectionStart(state), CanvasRenderer.textSelectionEnd(state), "");
        return true;
    }

    public boolean copySelection() {
        if (!CanvasRenderer.hasTextSelection(state)) {
            return false;
        }
        int start = CanvasRenderer.textSelectionStart(state);
        int end = CanvasRenderer.textSelectionEnd(state);
        String value = state.canvasTextEditDraft.substring(start, end);
        Minecraft.getInstance().keyboardHandler.setClipboard(value);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas text inline edit copy range={}..{} length={}", start, end, value.length());
        return true;
    }

    public void insert(String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        int start = CanvasRenderer.textSelectionStart(state);
        int end = CanvasRenderer.textSelectionEnd(state);
        int nextLength = state.canvasTextEditDraft.length() - Math.max(0, end - start) + value.length();
        if (nextLength > 2048) {
            value = value.substring(0, Math.max(0, 2048 - (state.canvasTextEditDraft.length() - Math.max(0, end - start))));
        }
        if (!value.isEmpty()) {
            replaceRange(start, end, value);
        }
    }

    private int clampedCursor() {
        state.canvasTextEditCursor = Math.max(0, Math.min(state.canvasTextEditCursor, state.canvasTextEditDraft.length()));
        return state.canvasTextEditCursor;
    }

    private void replaceRange(int start, int end, String replacement) {
        int safeStart = Math.max(0, Math.min(start, state.canvasTextEditDraft.length()));
        int safeEnd = Math.max(safeStart, Math.min(end, state.canvasTextEditDraft.length()));
        String value = replacement == null ? "" : replacement;
        state.canvasTextEditDraft = state.canvasTextEditDraft.substring(0, safeStart) + value + state.canvasTextEditDraft.substring(safeEnd);
        state.canvasTextEditCursor = safeStart + value.length();
        state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
        String group = TabletStateQueries.selectedGroupName(state);
        String id = state.canvasTextEditTarget;
        CanvasLayerMutations.updateCanvasText(state, group, id, text -> fitEditedText(CanvasTextRenderer.fitTextHeight(text.replaceTextRange(safeStart, safeEnd, value))));
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas text inline edit replace id={} range={}..{} insert={} length={} cursor={}", id, safeStart, safeEnd, value.length(), state.canvasTextEditDraft.length(), state.canvasTextEditCursor);
    }

    private CanvasTextLayer fitEditedText(CanvasTextLayer text) {
        return state.gridSnapLocked ? CanvasGridFitController.fittedText(state, text) : text;
    }

    private boolean mainCanvasTextEditOpen() {
        return state.canvasTextEditOpen
                && state.questDetailsTextEditTarget.isBlank()
                && !state.canvasTextEditTarget.isBlank();
    }
}
