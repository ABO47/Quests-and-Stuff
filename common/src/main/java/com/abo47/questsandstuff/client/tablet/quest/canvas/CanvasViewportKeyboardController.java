package com.abo47.questsandstuff.client.tablet.quest.canvas;

import org.lwjgl.glfw.GLFW;

import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;

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
        if (textEditor.handleKeyPressed(keyCode)) {
            return true;
        }
        if (state.root.canEdit && viewport.ctrlDown() && keyCode == GLFW.GLFW_KEY_C) {
            if (CanvasClipboardController.copySelectionToClipboard(viewport, state)) {
                refresher.run();
            }
            return true;
        }
        if (state.root.canEdit && viewport.ctrlDown() && keyCode == GLFW.GLFW_KEY_V) {
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
        if (textEditor.handleCharTyped(codePoint)) {
            return true;
        }
        return viewport.callSuperCharTyped(codePoint, modifiers);
    }
}
