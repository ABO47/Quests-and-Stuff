package com.abo47.questsandstuff.client.tablet.quest.canvas.text;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class TextEditSession {
    public static final int MAX_DRAFT_LENGTH = 2048;

    private TextEditSession() {
    }

    public static void beginMainCanvas(TabletUiState state, String targetId, String draft) {
        begin(state, targetId, draft);
        state.questDetailsTextEditTarget = "";
        state.questDetailsTextEditDraft = "";
        state.canvasTextMenuOpen = true;
        state.canvasTextMenuTarget = safe(targetId);
    }

    public static void beginQuestDetails(TabletUiState state, String targetId, String draft) {
        begin(state, targetId, draft);
        state.questDetailsTextEditTarget = safe(targetId);
        state.questDetailsTextEditDraft = state.canvasTextEditDraft;
        state.canvasTextMenuOpen = false;
        state.canvasTextMenuTarget = "";
    }

    private static void begin(TabletUiState state, String targetId, String draft) {
        String safeDraft = safeDraft(draft);
        state.canvasTextEditOpen = true;
        state.canvasTextEditTarget = safe(targetId);
        state.canvasTextEditDraft = safeDraft;
        state.canvasTextEditCursor = safeDraft.length();
        state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
        state.selectingCanvasTextRange = false;
    }

    public static boolean isAnyEditing(TabletUiState state) {
        return state != null && (state.canvasTextEditOpen || !state.questDetailsTextEditTarget.isBlank());
    }

    public static boolean isMainCanvasEditing(TabletUiState state) {
        return state != null
                && state.canvasTextEditOpen
                && state.questDetailsTextEditTarget.isBlank()
                && !state.canvasTextEditTarget.isBlank();
    }

    public static boolean isQuestDetailsEditing(TabletUiState state) {
        return state != null
                && state.canvasTextEditOpen
                && !state.questDetailsTextEditTarget.isBlank()
                && state.questDetailsTextEditTarget.equals(state.canvasTextEditTarget);
    }

    public static boolean isEditingTarget(TabletUiState state, String targetId) {
        return state != null
                && state.canvasTextEditOpen
                && state.canvasTextEditTarget.equals(safe(targetId));
    }

    public static void closeMainCanvas(TabletUiState state, boolean clearDraft) {
        if (state == null) {
            return;
        }
        if (isMainCanvasEditing(state) || state.questDetailsTextEditTarget.isBlank() && state.canvasTextEditOpen) {
            closeShared(state, clearDraft);
        }
    }

    public static void closeQuestDetails(TabletUiState state, boolean clearDraft) {
        if (state == null) {
            return;
        }
        if (isQuestDetailsEditing(state)) {
            closeShared(state, clearDraft);
        }
        state.questDetailsTextEditTarget = "";
        state.questDetailsTextEditDraft = "";
    }

    public static void closeAny(TabletUiState state, boolean clearDraft) {
        if (state == null || !isAnyEditing(state)) {
            return;
        }
        closeShared(state, clearDraft);
        state.questDetailsTextEditTarget = "";
        state.questDetailsTextEditDraft = "";
    }

    private static void closeShared(TabletUiState state, boolean clearDraft) {
        state.canvasTextEditOpen = false;
        state.canvasTextEditTarget = "";
        if (clearDraft) {
            state.canvasTextEditDraft = "";
        }
        state.canvasTextEditCursor = 0;
        state.canvasTextSelectionAnchor = 0;
        state.selectingCanvasTextRange = false;
    }

    public static void startRangeSelection(TabletUiState state) {
        state.selectingCanvasTextRange = true;
    }

    public static boolean finishRangeSelection(TabletUiState state) {
        if (!state.selectingCanvasTextRange) {
            return false;
        }
        state.selectingCanvasTextRange = false;
        return true;
    }

    public static void selectAll(TabletUiState state) {
        state.canvasTextSelectionAnchor = 0;
        state.canvasTextEditCursor = state.canvasTextEditDraft.length();
    }

    public static void moveCursor(TabletUiState state, int cursor, boolean extendSelection) {
        state.canvasTextEditCursor = Math.max(0, Math.min(cursor, state.canvasTextEditDraft.length()));
        if (!extendSelection) {
            state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
        }
    }

    public static int clampedCursor(TabletUiState state) {
        state.canvasTextEditCursor = Math.max(0, Math.min(state.canvasTextEditCursor, state.canvasTextEditDraft.length()));
        return state.canvasTextEditCursor;
    }

    public static int draftLength(TabletUiState state) {
        return state.canvasTextEditDraft.length();
    }

    public static int cursor(TabletUiState state) {
        return Math.max(0, Math.min(state.canvasTextEditCursor, state.canvasTextEditDraft.length()));
    }

    public static int selectionStart(TabletUiState state) {
        return Math.max(0, Math.min(state.canvasTextEditDraft.length(), Math.min(state.canvasTextEditCursor, state.canvasTextSelectionAnchor)));
    }

    public static int selectionEnd(TabletUiState state) {
        return Math.max(0, Math.min(state.canvasTextEditDraft.length(), Math.max(state.canvasTextEditCursor, state.canvasTextSelectionAnchor)));
    }

    public static boolean hasSelection(TabletUiState state) {
        return selectionStart(state) < selectionEnd(state);
    }

    public static String selectedText(TabletUiState state) {
        if (!hasSelection(state)) {
            return "";
        }
        return state.canvasTextEditDraft.substring(selectionStart(state), selectionEnd(state));
    }

    public static Replacement insert(TabletUiState state, String value) {
        String normalized = value == null ? "" : value.replace("\r\n", "\n").replace('\r', '\n');
        if (normalized.isEmpty()) {
            return Replacement.none();
        }
        int start = selectionStart(state);
        int end = selectionEnd(state);
        int keep = state.canvasTextEditDraft.length() - Math.max(0, end - start);
        if (keep + normalized.length() > MAX_DRAFT_LENGTH) {
            normalized = normalized.substring(0, Math.max(0, MAX_DRAFT_LENGTH - keep));
        }
        if (normalized.isEmpty()) {
            return Replacement.none();
        }
        return replaceRange(state, start, end, normalized);
    }

    public static Replacement deleteSelection(TabletUiState state) {
        if (!hasSelection(state)) {
            return Replacement.none();
        }
        return replaceRange(state, selectionStart(state), selectionEnd(state), "");
    }

    public static Replacement replaceRange(TabletUiState state, int start, int end, String replacement) {
        int safeStart = Math.max(0, Math.min(start, state.canvasTextEditDraft.length()));
        int safeEnd = Math.max(safeStart, Math.min(end, state.canvasTextEditDraft.length()));
        String value = safe(replacement);
        state.canvasTextEditDraft = state.canvasTextEditDraft.substring(0, safeStart) + value + state.canvasTextEditDraft.substring(safeEnd);
        state.canvasTextEditCursor = safeStart + value.length();
        state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
        if (isQuestDetailsEditing(state)) {
            state.questDetailsTextEditDraft = state.canvasTextEditDraft;
        }
        return new Replacement(safeStart, safeEnd, value);
    }

    private static String safeDraft(String value) {
        String draft = safe(value);
        return draft.length() > MAX_DRAFT_LENGTH ? draft.substring(0, MAX_DRAFT_LENGTH) : draft;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    public record Replacement(int start, int end, String value) {
        public boolean changed() {
            return start >= 0 && end >= start;
        }

        private static Replacement none() {
            return new Replacement(-1, -1, "");
        }
    }
}
