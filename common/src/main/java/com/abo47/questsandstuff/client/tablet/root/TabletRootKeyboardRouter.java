package com.abo47.questsandstuff.client.tablet.root;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletKeybindings;
import com.abo47.questsandstuff.client.tablet.bootstrap.TabletLifecycle;
import com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.TabletAssetPickerModal;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasMouseMode;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformMode;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestDetailsTasksPanel;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletModalState;

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
        boolean textInputActive = TabletRootWindowController.isTextInputActive(state, root);
        if (!textInputActive && !root.isAnyModalOpen() && root.isFrontWindowOpen() && handleQuestDetailsRecipeViewerShortcut(state, keyCode, scanCode)) {
            return true;
        }
        if (!textInputActive && !root.isAnyModalOpen() && handleGizmoModeShortcut(state, refresher, keyCode, scanCode)) {
            return true;
        }
        if (!textInputActive && root.isAnyModalOpen() && modalLayer != null && modalLayer.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if ((TabletKeybindings.openUiMatches(keyCode, scanCode) || TabletKeybindings.openQuestsUiMatches(keyCode, scanCode)) && !textInputActive) {
            TabletLifecycle.closeTabletUi(state, true, "keybind");
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (root.isContextMenuOpen()) {
                root.closeContextMenu();
                refresher.run();
                return true;
            }
            if (state.modal.modalWindowClosing) {
                TabletModalState.closeAllModalsImmediately(state);
            }
            if (state.questDetails.questDetailsClosing) {
                QuestDetailsWindow.finishCloseAnimation(state);
            }
            if (root.isAnyModalOpen()) {
                if (state.modal.modalWindowClosing) {
                    TabletModalState.closeAllModalsImmediately(state);
                } else {
                    ModalCloseActions.closeAll(state);
                }
                refresher.run();
                return true;
            }
            if (TabletRootWindowController.closeFrontmostWindow(state)) {
                refresher.run();
                return true;
            }
            if (cancelInteractionStates(state)) {
                refresher.run();
                return true;
            }
            if (state.root.skinEditMode) {
                state.root.skinEditSelectedTarget = "";
                state.root.skinEditMode = false;
                root.closeContextMenu();
                TabletUiFactory.persistSkinState(state);
                refresher.run();
                return true;
            }
            TabletLifecycle.closeTabletUi(state, false, "escape");
            return true;
        }
        if (root.isAnyModalOpen()) {
            if (!TabletRootWindowController.isTextInputActive(state, root) && TabletAssetPickerModal.handleKeyPressed(state, refresher, keyCode)) {
                return true;
            }
            if (modalLayer != null) {
                modalLayer.keyPressed(keyCode, scanCode, modifiers);
            }
            return true;
        }
        if (root.isFrontWindowOpen()) {
            if (TabletKeybindings.toggleSkinEditMatches(keyCode, scanCode) && !ModalStateQueries.anyOpen(state)) {
                state.root.skinEditMode = !state.root.skinEditMode;
                state.root.skinEditSelectedTarget = "";
                root.closeContextMenu();
                TabletUiFactory.persistSkinState(state);
                refresher.run();
                return true;
            }
            return keyPressedForFrontWindow(root, state, frontWindowLayer, canvasViewport, refresher, undoAction, redoAction, keyCode, scanCode, modifiers);
        }
        if (!Widget.isCtrlDown() && TabletKeybindings.quickConnectMatches(keyCode, scanCode)) {
            state.canvas.quickConnectHeld = true;
        }
        if (handleRenameCommit(root, state, refresher, keyCode)) {
            return true;
        }
        if (!TabletRootWindowController.isTextInputActive(state, root)
                && TabletShortcutActions.handleGlobal(root.resolvePlayer(), state, canvasViewport, keyCode, scanCode, Widget.isCtrlDown(), Widget.isShiftDown())) {
            refresher.run();
            return true;
        }
        if (handleCanvasClipboardShortcut(root, state, canvasViewport, refresher, keyCode)) {
            return true;
        }
        if (selfKey.invoke(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_Z && !Widget.isCtrlDown()) {
            if (state.canvas.canvasZoom != 1.0f) {
                CanvasCameraController.resetZoom(state, true);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas zoom reset key=Z");
                refresher.run();
            }
            return true;
        }
        if (state.root.editorAvailable && Widget.isCtrlDown() && keyCode == GLFW.GLFW_KEY_E) {
            state.root.editMode = !state.root.editMode;
            state.root.canEdit = state.root.editorAvailable && state.root.editMode;
            TabletUiFactory.persistEditMode(state.root.editMode);
            QuestsAndStuffMod.debugLog("[QnS:UI] editor mode shortcut enabled={}", state.root.editMode);
            refresher.run();
            return true;
        }
        if (TabletKeybindings.toggleSkinEditMatches(keyCode, scanCode)) {
            state.root.skinEditMode = !state.root.skinEditMode;
            state.root.skinEditSelectedTarget = "";
            root.closeContextMenu();
            TabletUiFactory.persistSkinState(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] skin edit mode toggled enabled={}", state.root.skinEditMode);
            refresher.run();
            return true;
        }
        if (!state.root.canEdit || !Widget.isCtrlDown()) {
            return false;
        }
        return handleEditorShortcut(state, refresher, undoAction, redoAction, keyCode);
    }

    private static boolean keyPressedForFrontWindow(
            TabletRootWidget root,
            TabletUiState state,
            WidgetGroup frontWindowLayer,
            CanvasViewport canvasViewport,
            Runnable refresher,
            Runnable undoAction,
            Runnable redoAction,
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (!QuestDetailsWindow.isInteractive(state)) {
            return true;
        }
        if (!TabletRootWindowController.isTextInputActive(state, root)
                && TabletShortcutActions.handleGlobal(root.resolvePlayer(), state, canvasViewport, keyCode, scanCode, Widget.isCtrlDown(), Widget.isShiftDown())) {
            refresher.run();
            return true;
        }
        if (handleQuestDetailsClipboardShortcut(root, state, refresher, keyCode)) {
            return true;
        }
        if (handleQuestDetailsHistoryShortcut(root, state, refresher, undoAction, redoAction, keyCode)) {
            return true;
        }
        String renameDraftBefore = state.questDetails.questDetailsTaskRenameDraft;
        if (frontWindowLayer != null) {
            frontWindowLayer.keyPressed(keyCode, scanCode, modifiers);
        }
        if (QuestDetailsTasksPanel.handleRenameKey(root.resolvePlayer(), state, keyCode, renameDraftBefore.equals(state.questDetails.questDetailsTaskRenameDraft))) {
            refresher.run();
        }
        return true;
    }

    private static boolean handleRenameCommit(TabletRootWidget root, TabletUiState state, Runnable refresher, int keyCode) {
        if (keyCode != GLFW.GLFW_KEY_ENTER && keyCode != GLFW.GLFW_KEY_KP_ENTER) {
            return false;
        }
        if (TabletUiFactory.DRAFT_CHAPTER.equals(state.canvas.pendingChapterRename)) {
            String typed = TabletUiFactory.sanitizeChapterName(state.chapterPanel.chapterDraftName);
            String created = TabletUiFactory.uniqueChapterName(typed.isBlank() ? tr("ui.questsandstuff.chapter.default_name") : typed, "");
            TabletUiFactory.runChapterAction(root.resolvePlayer(), state, "create", created, created, 0);
            state.root.selectedChapter = created;
            state.chapterPanel.chapterDraft = created;
            state.chapterPanel.chapterDraftName = created;
            state.canvas.pendingChapterRename = "";
            refresher.run();
            return true;
        }
        if (!state.canvas.pendingChapterRename.isBlank()) {
            String from = state.canvas.pendingChapterRename;
            String typed = TabletUiFactory.sanitizeChapterName(state.chapterPanel.chapterDraftName);
            String renamed = TabletUiFactory.uniqueChapterName(typed, from);
            if (!renamed.equals(from)) {
                TabletUiFactory.runChapterAction(root.resolvePlayer(), state, "rename", from, renamed, 0);
            }
            state.root.selectedChapter = renamed;
            state.chapterPanel.chapterDraft = renamed;
            state.chapterPanel.chapterDraftName = renamed;
            state.canvas.pendingChapterRename = "";
            refresher.run();
            return true;
        }
        if (!state.questDetails.pendingQuestTitleChangeId.isBlank() && EditorQuestCommandClient.commitQuestTitleChange(root.resolvePlayer(), state)) {
            refresher.run();
            return true;
        }
        return false;
    }

    private static boolean handleEditorShortcut(TabletUiState state, Runnable refresher, Runnable undoAction, Runnable redoAction, int keyCode) {
        boolean changed = switch (keyCode) {
            case GLFW.GLFW_KEY_1 -> {
                state.canvas.mouseMode = CanvasMouseMode.SELECT_MOVE;
                yield true;
            }
            case GLFW.GLFW_KEY_2 -> {
                state.canvas.mouseMode = CanvasMouseMode.DRAG_CANVAS;
                yield true;
            }
            case GLFW.GLFW_KEY_3 -> {
                state.canvas.mouseMode = CanvasMouseMode.ADD_QUEST;
                yield true;
            }
            case GLFW.GLFW_KEY_4 -> {
                state.canvas.mouseMode = CanvasMouseMode.CONNECT_QUESTS;
                yield true;
            }
            case GLFW.GLFW_KEY_G -> {
                state.canvas.gridEnabled = !state.canvas.gridEnabled;
                yield true;
            }
            case GLFW.GLFW_KEY_H -> {
                state.canvas.gridSnapLocked = !state.canvas.gridSnapLocked;
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

    private static boolean handleQuestDetailsRecipeViewerShortcut(TabletUiState state, int keyCode, int scanCode) {
        double[] mouse = currentMousePosition();
        return QuestDetailsTasksPanel.handleRecipeViewerShortcut(state, keyCode, scanCode, mouse[0], mouse[1]);
    }

    private static boolean handleGizmoModeShortcut(TabletUiState state, Runnable refresher, int keyCode, int scanCode) {
        if (!state.root.canEdit && !QuestDetailsEditController.canEdit(state)) {
            return false;
        }
        CanvasTransformMode mode = null;
        if (TabletKeybindings.gizmoMoveMatches(keyCode, scanCode)) {
            mode = CanvasTransformMode.MOVE;
        } else if (TabletKeybindings.gizmoResizeMatches(keyCode, scanCode)) {
            mode = CanvasTransformMode.RESIZE;
        } else if (TabletKeybindings.gizmoRotateMatches(keyCode, scanCode)) {
            mode = CanvasTransformMode.ROTATE;
        }
        if (mode == null) {
            return false;
        }
        CanvasTransformGizmo.setMode(state, mode);
        QuestsAndStuffMod.debugLog("[QnS:UI] transform gizmo shortcut mode={}", mode.id);
        refresher.run();
        return true;
    }

    private static boolean handleCanvasClipboardShortcut(TabletRootWidget root, TabletUiState state, CanvasViewport canvasViewport, Runnable refresher, int keyCode) {
        if (canvasViewport == null || !state.root.canEdit || !Widget.isCtrlDown() || TabletRootWindowController.isTextInputActive(state, root)) {
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

    private static double[] currentMousePosition() {
        Minecraft minecraft = Minecraft.getInstance();
        Window window = minecraft.getWindow();
        double mouseX = minecraft.mouseHandler.xpos() * window.getGuiScaledWidth() / window.getScreenWidth();
        double mouseY = minecraft.mouseHandler.ypos() * window.getGuiScaledHeight() / window.getScreenHeight();
        return new double[]{mouseX, mouseY};
    }

    private static boolean handleQuestDetailsClipboardShortcut(TabletRootWidget root, TabletUiState state, Runnable refresher, int keyCode) {
        if (!QuestDetailsEditController.canEdit(state) || !Widget.isCtrlDown() || TabletRootWindowController.isTextInputActive(state, root)) {
            return false;
        }
        if (QuestDetailsWindow.handleClipboardShortcut(root.resolvePlayer(), state, keyCode)) {
            refresher.run();
            return true;
        }
        return false;
    }

    private static boolean handleQuestDetailsHistoryShortcut(
            TabletRootWidget root,
            TabletUiState state,
            Runnable refresher,
            Runnable undoAction,
            Runnable redoAction,
            int keyCode
    ) {
        if (!QuestDetailsEditController.canEdit(state) || !Widget.isCtrlDown() || TabletRootWindowController.isTextInputActive(state, root)) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_Z) {
            undoAction.run();
            refresher.run();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_Y) {
            redoAction.run();
            refresher.run();
            return true;
        }
        return false;
    }

    private static boolean cancelInteractionStates(TabletUiState state) {
        if (!state.canvas.connectSourceQuestId.isBlank() || !state.canvas.connectSourceQuestIds.isEmpty()) {
            state.canvas.connectSourceQuestId = "";
            state.canvas.connectSourceQuestIds.clear();
            state.canvas.connectEcId = "";
            state.canvas.quickConnectEcId = "";
            return true;
        }
        if (state.canvas.blueprintPlacement.active()) {
            state.canvas.blueprintPlacement.cancel();
            return true;
        }
        if (state.canvas.canvasSelection.hasAny() || state.canvas.selectionBoundsVisible) {
            state.canvas.canvasSelection.clear();
            state.canvas.selectionBoundsVisible = false;
            return true;
        }
        if (TabletShortcutActions.cancelTransient(state)) {
            return true;
        }
        return false;
    }

    static boolean keyReleased(TabletRootWidget root, TabletUiState state, Runnable refresher, KeyDelegate selfKeyRelease, int keyCode, int scanCode, int modifiers) {
        if (TabletKeybindings.quickConnectMatches(keyCode, scanCode)) {
            state.canvas.quickConnectHeld = false;
            state.canvas.quickConnectSourceQuestId = "";
            state.canvas.quickConnectEcId = "";
            refresher.run();
            return true;
        }
        return selfKeyRelease.invoke(keyCode, scanCode, modifiers);
    }

    static boolean charTyped(TabletRootWidget root, TabletUiState state, WidgetGroup modalLayer, WidgetGroup frontWindowLayer, Runnable refresher, CharTypedDelegate selfCharTyped, char c, int modifiers) {
        if (root.isAnyModalOpen()) {
            if (modalLayer != null) {
                modalLayer.charTyped(c, modifiers);
            }
            return true;
        }
        if (root.isFrontWindowOpen()) {
            String renameDraftBefore = state.questDetails.questDetailsTaskRenameDraft;
            if (frontWindowLayer != null) {
                frontWindowLayer.charTyped(c, modifiers);
            }
            if (QuestDetailsTasksPanel.handleRenameChar(state, c, renameDraftBefore.equals(state.questDetails.questDetailsTaskRenameDraft))) {
                refresher.run();
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
