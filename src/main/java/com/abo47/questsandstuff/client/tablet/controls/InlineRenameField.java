package com.abo47.questsandstuff.client.tablet.controls;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class InlineRenameField extends TextFieldWidget {
    private final Runnable onCommit;
    private final Runnable onCancel;
    private final Runnable onBlur;
    private final Consumer<Boolean> onFocus;
    private boolean suppressNextBlur;

    public InlineRenameField(
            int x,
            int y,
            int width,
            int height,
            Supplier<String> textSupplier,
            Consumer<String> textResponder,
            Runnable onCommit,
            Runnable onCancel,
            Runnable onBlur,
            Consumer<Boolean> onFocus
    ) {
        super(x, y, width, height, textSupplier, textResponder);
        this.onCommit = onCommit == null ? () -> {
        } : onCommit;
        this.onCancel = onCancel == null ? () -> {
        } : onCancel;
        this.onBlur = onBlur == null ? this.onCommit : onBlur;
        this.onFocus = onFocus == null ? focused -> {
        } : onFocus;
    }

    @Override
    public void onFocusChanged(Widget lastFocus, Widget focus) {
        super.onFocusChanged(lastFocus, focus);
        if (focus == this) {
            onFocus.accept(true);
            return;
        }
        if (lastFocus == this && focus != this) {
            onFocus.accept(false);
            if (suppressNextBlur) {
                suppressNextBlur = false;
                return;
            }
            onBlur.run();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            onCommit.run();
            suppressNextBlur = true;
            setFocus(false);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            onCancel.run();
            suppressNextBlur = true;
            setFocus(false);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
