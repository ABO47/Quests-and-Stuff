package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
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
        if (!state.canvas.canvasTextMenuOpen || state.canvas.canvasTextMenuTarget.isBlank()) {
            return false;
        }
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, TabletStateQueries.selectedGroupName(state), state.canvas.canvasTextMenuTarget);
        return CanvasRenderer.isCanvasTextOwnerHit(state, text, localX, localY);
    }

    public boolean isMenuHit(int localX, int localY) {
        if (!state.canvas.canvasTextMenuOpen || state.canvas.canvasTextMenuTarget.isBlank()) {
            return false;
        }
        String group = TabletStateQueries.selectedGroupName(state);
        CanvasTextLayer text = CanvasLayerMutations.findCanvasText(state, group, state.canvas.canvasTextMenuTarget);
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
        TextEditSession.beginMainCanvas(state, text.id(), text.text());
        state.canvas.canvasSelection.setPrimaryTextId(text.id());
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.textIds().add(text.id());
        state.canvas.canvasSelection.setPrimaryImageId("");
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.questIds().clear();
        state.canvas.draggingCanvasText = false;
        state.canvas.resizingCanvasText = false;
        state.canvas.rotatingCanvasText = false;
        viewport.setFocus(true);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas text inline edit start id={}", text.id());
    }

    public void close(String reason) {
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas text inline edit close id={} reason={} length={}", state.canvas.canvasTextEditTarget, reason, state.canvas.canvasTextEditDraft.length());
        TextEditSession.closeMainCanvas(state, false);
    }

    public CanvasTextLayer activeText() {
        if (!TextEditSession.isMainCanvasEditing(state)) {
            return null;
        }
        return CanvasLayerMutations.findCanvasText(state, TabletStateQueries.selectedGroupName(state), state.canvas.canvasTextEditTarget);
    }

    public boolean handleKeyPressed(int keyCode) {
        if (!TextEditSession.isMainCanvasEditing(state)) {
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
            if (applyReplacement(TextEditSession.deleteSelection(state))) {
                refresh.run();
            } else {
                int cursor = TextEditSession.clampedCursor(state);
                if (cursor > 0) {
                    applyReplacement(TextEditSession.replaceRange(state, cursor - 1, cursor, ""));
                    refresh.run();
                }
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (applyReplacement(TextEditSession.deleteSelection(state))) {
                refresh.run();
            } else {
                int cursor = TextEditSession.clampedCursor(state);
                if (cursor < TextEditSession.draftLength(state)) {
                    applyReplacement(TextEditSession.replaceRange(state, cursor, cursor + 1, ""));
                    refresh.run();
                }
            }
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

    public boolean handleCharTyped(char codePoint) {
        if (!TextEditSession.isMainCanvasEditing(state)) {
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
        if (!state.canvas.selectingCanvasTextRange || !TextEditSession.isMainCanvasEditing(state)) {
            return false;
        }
        CanvasTextLayer editingText = activeText();
        if (editingText != null) {
            TextEditSession.moveCursor(state, CanvasRenderer.canvasTextCursorAt(state, editingText, localX, localY), true);
            refresh.run();
        }
        return true;
    }

    public boolean finishSelectionDrag() {
        if (!TextEditSession.isMainCanvasEditing(state)) {
            return false;
        }
        if (!TextEditSession.finishRangeSelection(state)) {
            return false;
        }
        refresh.run();
        return true;
    }

    public void moveCursor(int cursor, boolean extendSelection) {
        TextEditSession.moveCursor(state, cursor, extendSelection);
    }

    public boolean deleteSelection() {
        return applyReplacement(TextEditSession.deleteSelection(state));
    }

    public boolean copySelection() {
        if (!TextEditSession.hasSelection(state)) {
            return false;
        }
        int start = TextEditSession.selectionStart(state);
        int end = TextEditSession.selectionEnd(state);
        String value = TextEditSession.selectedText(state);
        Minecraft.getInstance().keyboardHandler.setClipboard(value);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas text inline edit copy range={}..{} length={}", start, end, value.length());
        return true;
    }

    public void insert(String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        applyReplacement(TextEditSession.insert(state, value));
    }

    private boolean applyReplacement(TextEditSession.Replacement replacement) {
        if (!replacement.changed()) {
            return false;
        }
        String group = TabletStateQueries.selectedGroupName(state);
        String id = state.canvas.canvasTextEditTarget;
        CanvasLayerMutations.updateCanvasText(state, group, id, text -> fitEditedText(CanvasTextRenderer.fitTextHeight(text.replaceTextRange(replacement.start(), replacement.end(), replacement.value()))));
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas text inline edit replace id={} range={}..{} insert={} length={} cursor={}", id, replacement.start(), replacement.end(), replacement.value().length(), state.canvas.canvasTextEditDraft.length(), state.canvas.canvasTextEditCursor);
        return true;
    }

    private CanvasTextLayer fitEditedText(CanvasTextLayer text) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedText(state, text) : text;
    }
}
