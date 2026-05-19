package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasMouseMode;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.details.objective.QuestDetailsObjectivesPanel;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.screen.TabletClientHooks;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import org.lwjgl.glfw.GLFW;

final class TabletRootKeyboardRouter {
    private TabletRootKeyboardRouter() {
    }

    static boolean keyPressed(
            TabletRootWidget root,
            TabletUiState state,
            WidgetGroup modalLayer,
            WidgetGroup frontWindowLayer,
            CanvasViewport canvasViewport,
            Runnable refresher,
            Runnable undoAction,
            Runnable redoAction,
            KeyDelegate selfKey,
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (TabletClientHooks.openUiMatches(keyCode, scanCode) && !TabletRootWindowController.isTextInputActive(state, root)) {
            TabletClientHooks.rememberActiveWindow(state);
            TabletClientHooks.suppressNextOpenClick();
            Minecraft.getInstance().setScreen(null);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && TabletRootWindowController.closeFrontmostWindow(state)) {
            refresher.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && TabletShortcutActions.cancelTransient(state)) {
            refresher.run();
            return true;
        }
        if (root.isAnyModalOpen()) {
            if (modalLayer != null) {
                modalLayer.keyPressed(keyCode, scanCode, modifiers);
            }
            return true;
        }
        if (root.isFrontWindowOpen()) {
            return keyPressedForFrontWindow(root, state, frontWindowLayer, canvasViewport, refresher, keyCode, scanCode, modifiers);
        }
        if (!root.isCtrlDown() && TabletClientHooks.quickConnectMatches(keyCode, scanCode)) {
            state.quickConnectHeld = true;
        }
        if (handleRenameCommit(root, state, refresher, keyCode)) {
            return true;
        }
        if (!TabletRootWindowController.isTextInputActive(state, root)
                && TabletShortcutActions.handleGlobal(root.resolvePlayer(), state, canvasViewport, keyCode, scanCode, root.isCtrlDown(), root.isShiftDown())) {
            refresher.run();
            return true;
        }
        if (handleCanvasClipboardShortcut(root, state, canvasViewport, refresher, keyCode)) {
            return true;
        }
        if (selfKey.invoke(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_Z && !root.isCtrlDown()) {
            if (state.canvasZoom != 1.0f) {
                state.canvasZoom = 1.0f;
                TabletUiFactory.persistUiState(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas zoom reset key=Z");
                refresher.run();
            }
            return true;
        }
        if (state.editorAvailable && root.isCtrlDown() && keyCode == GLFW.GLFW_KEY_E) {
            state.editMode = !state.editMode;
            state.canEdit = state.editorAvailable && state.editMode;
            TabletUiFactory.persistEditMode(state.editMode);
            QuestsAndStuffMod.debugLog("[QnS:UI] editor mode shortcut enabled={}", state.editMode);
            refresher.run();
            return true;
        }
        if (!state.canEdit || !root.isCtrlDown()) {
            return false;
        }
        return handleEditorShortcut(state, refresher, undoAction, redoAction, keyCode);
    }

    private static boolean keyPressedForFrontWindow(TabletRootWidget root, TabletUiState state, WidgetGroup frontWindowLayer, CanvasViewport canvasViewport, Runnable refresher, int keyCode, int scanCode, int modifiers) {
        if (!QuestDetailsWindow.isInteractive(state)) {
            return true;
        }
        if (!TabletRootWindowController.isTextInputActive(state, root)
                && TabletShortcutActions.handleGlobal(root.resolvePlayer(), state, canvasViewport, keyCode, scanCode, root.isCtrlDown(), root.isShiftDown())) {
            refresher.run();
            return true;
        }
        if (handleQuestDetailsClipboardShortcut(root, state, refresher, keyCode)) {
            return true;
        }
        if (frontWindowLayer != null) {
            frontWindowLayer.keyPressed(keyCode, scanCode, modifiers);
        }
        if (QuestDetailsObjectivesPanel.handleRenameKey(root.resolvePlayer(), state, keyCode)) {
            refresher.run();
        }
        return true;
    }

    private static boolean handleRenameCommit(TabletRootWidget root, TabletUiState state, Runnable refresher, int keyCode) {
        if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
            return false;
        }
        if (TabletUiFactory.DRAFT_CHAPTER.equals(state.pendingChapterRename)) {
            String typed = TabletUiFactory.sanitizeGroupName(state.chapterDraftName);
            String created = TabletUiFactory.uniqueGroupName(typed.isBlank() ? tr("ui.questsandstuff.chapter.default_name") : typed, "");
            TabletUiFactory.runGroupAction(root.resolvePlayer(), state, "create", created, created, 0);
            state.selectedGroup = created;
            state.groupDraft = created;
            state.chapterDraftName = created;
            state.pendingChapterRename = "";
            refresher.run();
            return true;
        }
        if (!state.pendingChapterRename.isBlank()) {
            String from = state.pendingChapterRename;
            String typed = TabletUiFactory.sanitizeGroupName(state.chapterDraftName);
            String renamed = TabletUiFactory.uniqueGroupName(typed, from);
            if (!renamed.equals(from)) {
                TabletUiFactory.runGroupAction(root.resolvePlayer(), state, "rename", from, renamed, 0);
            }
            state.selectedGroup = renamed;
            state.groupDraft = renamed;
            state.chapterDraftName = renamed;
            state.pendingChapterRename = "";
            refresher.run();
            return true;
        }
        if (!state.pendingQuestRenameId.isBlank() && EditorCommandClient.commitQuestRename(root.resolvePlayer(), state)) {
            refresher.run();
            return true;
        }
        return false;
    }

    private static boolean handleEditorShortcut(TabletUiState state, Runnable refresher, Runnable undoAction, Runnable redoAction, int keyCode) {
        boolean changed = switch (keyCode) {
            case GLFW.GLFW_KEY_1 -> {
                state.mouseMode = CanvasMouseMode.SELECT_MOVE;
                yield true;
            }
            case GLFW.GLFW_KEY_2 -> {
                state.mouseMode = CanvasMouseMode.DRAG_CANVAS;
                yield true;
            }
            case GLFW.GLFW_KEY_3 -> {
                state.mouseMode = CanvasMouseMode.ADD_QUEST;
                yield true;
            }
            case GLFW.GLFW_KEY_4 -> {
                state.mouseMode = CanvasMouseMode.CONNECT_QUESTS;
                yield true;
            }
            case GLFW.GLFW_KEY_G -> {
                state.gridEnabled = !state.gridEnabled;
                yield true;
            }
            case GLFW.GLFW_KEY_H -> {
                state.gridSnapLocked = !state.gridSnapLocked;
                yield true;
            }
            case GLFW.GLFW_KEY_Z -> {
                undoAction.run();
                yield true;
            }
            case GLFW.GLFW_KEY_Y -> {
                redoAction.run();
                yield true;
            }
            default -> false;
        };

        if (changed) {
            refresher.run();
        }
        return changed;
    }

    private static boolean handleCanvasClipboardShortcut(TabletRootWidget root, TabletUiState state, CanvasViewport canvasViewport, Runnable refresher, int keyCode) {
        if (canvasViewport == null || !state.canEdit || !root.isCtrlDown() || TabletRootWindowController.isTextInputActive(state, root)) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_C) {
            if (CanvasClipboardController.copySelectionToClipboard(canvasViewport, state)) {
                refresher.run();
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_V) {
            if (CanvasClipboardController.pasteNearSelectionOrViewportCenter(root.resolvePlayer(), state, canvasViewport)) {
                refresher.run();
            }
            return true;
        }
        return false;
    }

    private static boolean handleQuestDetailsClipboardShortcut(TabletRootWidget root, TabletUiState state, Runnable refresher, int keyCode) {
        if (!state.canEdit || !root.isCtrlDown() || TabletRootWindowController.isTextInputActive(state, root)) {
            return false;
        }
        if (QuestDetailsWindow.handleClipboardShortcut(root.resolvePlayer(), state, keyCode)) {
            refresher.run();
            return true;
        }
        return false;
    }

    static boolean keyReleased(TabletRootWidget root, TabletUiState state, Runnable refresher, KeyDelegate selfKeyRelease, int keyCode, int scanCode, int modifiers) {
        if (TabletClientHooks.quickConnectMatches(keyCode, scanCode)) {
            state.quickConnectHeld = false;
            state.quickConnectSourceQuestId = "";
            refresher.run();
            return true;
        }
        return selfKeyRelease.invoke(keyCode, scanCode, modifiers);
    }

    static boolean charTyped(TabletRootWidget root, WidgetGroup modalLayer, WidgetGroup frontWindowLayer, CharTypedDelegate selfCharTyped, char c, int modifiers) {
        if (root.isAnyModalOpen()) {
            if (modalLayer != null) {
                modalLayer.charTyped(c, modifiers);
            }
            return true;
        }
        if (root.isFrontWindowOpen()) {
            if (frontWindowLayer != null) {
                frontWindowLayer.charTyped(c, modifiers);
            }
            return true;
        }
        return selfCharTyped.invoke(c, modifiers);
    }

    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }

    @FunctionalInterface
    interface KeyDelegate {
        boolean invoke(int keyCode, int scanCode, int modifiers);
    }

    @FunctionalInterface
    interface CharTypedDelegate {
        boolean invoke(char c, int modifiers);
    }
}
