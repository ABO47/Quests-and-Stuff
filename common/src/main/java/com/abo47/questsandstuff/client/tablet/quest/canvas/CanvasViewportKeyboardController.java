package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWindowController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import org.lwjgl.glfw.GLFW;

final class CanvasViewportKeyboardController {
    private CanvasViewportKeyboardController() {
    }

    static boolean keyPressed(
            CanvasViewport viewport,
            TabletUiState state,
            Runnable refresher,
            CanvasInlineTextEditor textEditor,
            int keyCode,
            int scanCode,
            int modifiers
    ) {
        if (EntityMotionEditor.isMainCanvasOpen(state) && viewport.callSuperKeyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (TabletRootWindowController.isFontSizeFieldOpen(state)) {
            return viewport.callSuperKeyPressed(keyCode, scanCode, modifiers);
        }
        if (textEditor.handleKeyPressed(keyCode)) {
            return true;
        }
        if (state.canEdit && viewport.ctrlDown() && keyCode == GLFW.GLFW_KEY_C) {
            if (CanvasClipboardController.copySelectionToClipboard(viewport, state)) {
                refresher.run();
            }
            return true;
        }
        if (state.canEdit && viewport.ctrlDown() && keyCode == GLFW.GLFW_KEY_V) {
            if (CanvasClipboardController.pasteNearSelectionOrViewportCenter(viewport.player(), state, viewport)) {
                refresher.run();
            }
            return true;
        }
        return viewport.callSuperKeyPressed(keyCode, scanCode, modifiers);
    }

    static boolean charTyped(
            CanvasViewport viewport,
            TabletUiState state,
            CanvasInlineTextEditor textEditor,
            char codePoint,
            int modifiers
    ) {
        if (EntityMotionEditor.isMainCanvasOpen(state) && viewport.callSuperCharTyped(codePoint, modifiers)) {
            return true;
        }
        if (TabletRootWindowController.isFontSizeFieldOpen(state)) {
            return viewport.callSuperCharTyped(codePoint, modifiers);
        }
        if (textEditor.handleCharTyped(codePoint)) {
            return true;
        }
        return viewport.callSuperCharTyped(codePoint, modifiers);
    }
}
