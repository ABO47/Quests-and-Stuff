package com.abo47.questsandstuff.client.tablet.controls;

import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class StyledTextFields {
    private StyledTextFields() {
    }

    public static TextFieldWidget search(
            int x,
            int y,
            int width,
            int height,
            String current,
            int maxLength,
            Consumer<String> responder,
            Consumer<Boolean> focusResponder
    ) {
        TextFieldWidget field = new TextFieldWidget(x, y, width, height, null, responder) {
            @Override
            public void onFocusChanged(Widget lastFocus, Widget focus) {
                super.onFocusChanged(lastFocus, focus);
                if (focusResponder != null) {
                    focusResponder.accept(isFocus());
                }
            }
        };
        field.setClientSideWidget();
        field.setCurrentString(current == null ? "" : current);
        field.setMaxStringLength(maxLength);
        applyStandardStyle(field, ModColors.SURFACE_BASE, ModColors.BORDER_BASE);
        return field;
    }

    public static TextFieldWidget commitField(
            int x,
            int y,
            int width,
            int height,
            Supplier<String> textSupplier,
            Consumer<String> responder,
            Runnable commit,
            Runnable cancel,
            Runnable blur
    ) {
        return commitField(x, y, width, height, textSupplier, responder, commit, cancel, blur, null);
    }

    public static TextFieldWidget commitField(
            int x,
            int y,
            int width,
            int height,
            Supplier<String> textSupplier,
            Consumer<String> responder,
            Runnable commit,
            Runnable cancel,
            Runnable blur,
            Consumer<Boolean> focusResponder
    ) {
        Runnable safeCommit = commit == null ? () -> {
        } : commit;
        Runnable safeCancel = cancel == null ? () -> {
        } : cancel;
        Runnable safeBlur = blur == null ? safeCommit : blur;
        return new TextFieldWidget(x, y, width, height, textSupplier, responder) {
            private boolean suppressNextBlur;

            @Override
            public void onFocusChanged(Widget lastFocus, Widget focus) {
                super.onFocusChanged(lastFocus, focus);
                if (focusResponder != null) {
                    focusResponder.accept(isFocus());
                }
                if (lastFocus == this && focus != this) {
                    if (suppressNextBlur) {
                        suppressNextBlur = false;
                        return;
                    }
                    safeBlur.run();
                }
            }

            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                    safeCommit.run();
                    suppressNextBlur = true;
                    setFocus(false);
                    return true;
                }
                if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                    safeCancel.run();
                    suppressNextBlur = true;
                    setFocus(false);
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        };
    }

    public static void applyStandardStyle(TextFieldWidget field, int fillColor, int borderColor) {
        field.setBordered(false);
        field.setBackground(Surfaces.bordered(fillColor, borderColor));
        field.setTextColor(ModColors.TEXT_PRIMARY);
    }

    public static TextFieldWidget numberField(
            int x,
            int y,
            int width,
            int height,
            int current,
            int min,
            int max,
            int maxLength,
            Consumer<String> responder,
            Runnable commit,
            Runnable cancel,
            Runnable blur
    ) {
        return numberField(x, y, width, height, current, min, max, maxLength, responder, commit, cancel, blur, null);
    }

    public static TextFieldWidget numberField(
            int x,
            int y,
            int width,
            int height,
            int current,
            int min,
            int max,
            int maxLength,
            Consumer<String> responder,
            Runnable commit,
            Runnable cancel,
            Runnable blur,
            Consumer<Boolean> focusResponder
    ) {
        TextFieldWidget field = commitField(
                x,
                y,
                width,
                height,
                null,
                responder,
                commit,
                cancel,
                blur,
                focusResponder
        );
        field.setClientSideWidget();
        field.setCurrentString(Integer.toString(current));
        field.setMaxStringLength(maxLength);
        field.setNumbersOnly(min, max);
        applyStandardStyle(field, ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE);
        return field;
    }

    public static TextFieldWidget hexField(
            int x,
            int y,
            int width,
            int height,
            Supplier<String> textSupplier,
            Consumer<String> responder,
            Runnable commit,
            Runnable cancel,
            Runnable blur
    ) {
        TextFieldWidget field = commitField(
                x,
                y,
                width,
                height,
                textSupplier,
                responder,
                commit,
                cancel,
                blur
        );
        field.setClientSideWidget();
        field.setMaxStringLength(9);
        field.setValidator(SearchFieldController::normalizeHexInput);
        applyStandardStyle(field, ModColors.SURFACE_BASE, ModColors.BORDER_BASE);
        return field;
    }
}
