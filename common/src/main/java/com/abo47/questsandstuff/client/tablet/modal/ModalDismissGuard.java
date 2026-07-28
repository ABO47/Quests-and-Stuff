package com.abo47.questsandstuff.client.tablet.modal;

import org.lwjgl.glfw.GLFW;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletModalState;

public final class ModalDismissGuard extends WidgetGroup {
    private final TabletUiState state;
    private final Runnable refresh;

    public ModalDismissGuard(int x, int y, int width, int height, TabletUiState state, Runnable refresh) {
        super(x, y, width, height);
        this.state = state;
        this.refresh = refresh == null ? () -> {
        } : refresh;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (TabletModalState.finishClosingIfDone(state)) {
            refresh.run();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return state.modal.modalWindowClosing || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return state.modal.modalWindowClosing || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return state.modal.modalWindowClosing || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        return state.modal.modalWindowClosing || super.mouseWheelMove(mouseX, mouseY, wheelDelta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (state.modal.modalWindowClosing) {
            return keyCode != GLFW.GLFW_KEY_ESCAPE;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return state.modal.modalWindowClosing || super.charTyped(codePoint, modifiers);
    }
}
