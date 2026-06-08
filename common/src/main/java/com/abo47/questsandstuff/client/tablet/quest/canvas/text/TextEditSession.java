package com.abo47.questsandstuff.client.tablet.quest.canvas.text;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

public final class TextEditSession {
    public static final int MAX_DRAFT_LENGTH = 2048;

    private TextEditSession() {
    }

    public static void beginMainCanvas(TabletUiState state, String targetId, String draft) {
        begin(state, targetId, draft);
        state.questDetails.questDetailsTextEditTarget = "";
        state.questDetails.questDetailsTextEditDraft = "";
        state.canvas.canvasTextMenuOpen = true;
        state.canvas.canvasTextMenuTarget = safe(targetId);
    }

    public static void beginQuestDetails(TabletUiState state, String targetId, String draft) {
        begin(state, targetId, draft);
        state.questDetails.questDetailsTextEditTarget = safe(targetId);
        state.questDetails.questDetailsTextEditDraft = state.canvas.canvasTextEditDraft;
        state.canvas.canvasTextMenuOpen = false;
        state.canvas.canvasTextMenuTarget = "";
    }

    private static void begin(TabletUiState state, String targetId, String draft) {
        String safeDraft = safeDraft(draft);
        state.canvas.canvasTextEditOpen = true;
        state.canvas.canvasTextEditTarget = safe(targetId);
        state.canvas.canvasTextEditDraft = safeDraft;
        state.canvas.canvasTextEditCursor = safeDraft.length();
        state.canvas.canvasTextSelectionAnchor = state.canvas.canvasTextEditCursor;
        state.canvas.selectingCanvasTextRange = false;
    }

    public static boolean isAnyEditing(TabletUiState state) {
        return state != null && (state.canvas.canvasTextEditOpen || !state.questDetails.questDetailsTextEditTarget.isBlank());
    }

    public static boolean isMainCanvasEditing(TabletUiState state) {
        return state != null
                && state.canvas.canvasTextEditOpen
                && state.questDetails.questDetailsTextEditTarget.isBlank()
                && !state.canvas.canvasTextEditTarget.isBlank();
    }

    public static boolean isQuestDetailsEditing(TabletUiState state) {
        return state != null
                && state.canvas.canvasTextEditOpen
                && !state.questDetails.questDetailsTextEditTarget.isBlank()
                && state.questDetails.questDetailsTextEditTarget.equals(state.canvas.canvasTextEditTarget);
    }

    public static boolean isEditingTarget(TabletUiState state, String targetId) {
        return state != null
                && state.canvas.canvasTextEditOpen
                && state.canvas.canvasTextEditTarget.equals(safe(targetId));
    }

    public static void closeMainCanvas(TabletUiState state, boolean clearDraft) {
        if (state == null) {
            return;
        }
        if (isMainCanvasEditing(state) || state.questDetails.questDetailsTextEditTarget.isBlank() && state.canvas.canvasTextEditOpen) {
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
        state.questDetails.questDetailsTextEditTarget = "";
        state.questDetails.questDetailsTextEditDraft = "";
    }

    public static void closeAny(TabletUiState state, boolean clearDraft) {
        if (state == null || !isAnyEditing(state)) {
            return;
        }
        closeShared(state, clearDraft);
        state.questDetails.questDetailsTextEditTarget = "";
        state.questDetails.questDetailsTextEditDraft = "";
    }

    private static void closeShared(TabletUiState state, boolean clearDraft) {
        state.canvas.canvasTextEditOpen = false;
        state.canvas.canvasTextEditTarget = "";
        if (clearDraft) {
            state.canvas.canvasTextEditDraft = "";
        }
        state.canvas.canvasTextEditCursor = 0;
        state.canvas.canvasTextSelectionAnchor = 0;
        state.canvas.selectingCanvasTextRange = false;
    }

    public static void startRangeSelection(TabletUiState state) {
        state.canvas.selectingCanvasTextRange = true;
    }

    public static boolean finishRangeSelection(TabletUiState state) {
        if (!state.canvas.selectingCanvasTextRange) {
            return false;
        }
        state.canvas.selectingCanvasTextRange = false;
        return true;
    }

    public static void selectAll(TabletUiState state) {
        state.canvas.canvasTextSelectionAnchor = 0;
        state.canvas.canvasTextEditCursor = state.canvas.canvasTextEditDraft.length();
    }

    public static void moveCursor(TabletUiState state, int cursor, boolean extendSelection) {
        state.canvas.canvasTextEditCursor = Math.max(0, Math.min(cursor, state.canvas.canvasTextEditDraft.length()));
        if (!extendSelection) {
            state.canvas.canvasTextSelectionAnchor = state.canvas.canvasTextEditCursor;
        }
    }

    public static int clampedCursor(TabletUiState state) {
        state.canvas.canvasTextEditCursor = Math.max(0, Math.min(state.canvas.canvasTextEditCursor, state.canvas.canvasTextEditDraft.length()));
        return state.canvas.canvasTextEditCursor;
    }

    public static int draftLength(TabletUiState state) {
        return state.canvas.canvasTextEditDraft.length();
    }

    public static int cursor(TabletUiState state) {
        return Math.max(0, Math.min(state.canvas.canvasTextEditCursor, state.canvas.canvasTextEditDraft.length()));
    }

    public static int selectionStart(TabletUiState state) {
        return Math.max(0, Math.min(state.canvas.canvasTextEditDraft.length(), Math.min(state.canvas.canvasTextEditCursor, state.canvas.canvasTextSelectionAnchor)));
    }

    public static int selectionEnd(TabletUiState state) {
        return Math.max(0, Math.min(state.canvas.canvasTextEditDraft.length(), Math.max(state.canvas.canvasTextEditCursor, state.canvas.canvasTextSelectionAnchor)));
    }

    public static boolean hasSelection(TabletUiState state) {
        return selectionStart(state) < selectionEnd(state);
    }

    public static String selectedText(TabletUiState state) {
        if (!hasSelection(state)) {
            return "";
        }
        return state.canvas.canvasTextEditDraft.substring(selectionStart(state), selectionEnd(state));
    }

    public static Replacement insert(TabletUiState state, String value) {
        String normalized = value == null ? "" : value.replace("\r\n", "\n").replace('\r', '\n');
        if (normalized.isEmpty()) {
            return Replacement.none();
        }
        int start = selectionStart(state);
        int end = selectionEnd(state);
        int keep = state.canvas.canvasTextEditDraft.length() - Math.max(0, end - start);
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
        int safeStart = Math.max(0, Math.min(start, state.canvas.canvasTextEditDraft.length()));
        int safeEnd = Math.max(safeStart, Math.min(end, state.canvas.canvasTextEditDraft.length()));
        String value = safe(replacement);
        state.canvas.canvasTextEditDraft = state.canvas.canvasTextEditDraft.substring(0, safeStart) + value + state.canvas.canvasTextEditDraft.substring(safeEnd);
        state.canvas.canvasTextEditCursor = safeStart + value.length();
        state.canvas.canvasTextSelectionAnchor = state.canvas.canvasTextEditCursor;
        if (isQuestDetailsEditing(state)) {
            state.questDetails.questDetailsTextEditDraft = state.canvas.canvasTextEditDraft;
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
