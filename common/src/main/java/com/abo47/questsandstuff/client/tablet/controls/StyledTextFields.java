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
        field.setValidator(SearchFieldController::normalizeUserSearch);
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
        Runnable safeCommit = commit == null ? () -> {
        } : commit;
        Runnable safeCancel = cancel == null ? () -> {
        } : cancel;
        Runnable safeBlur = blur == null ? safeCommit : blur;
        TextFieldWidget field = new TextFieldWidget(x, y, width, height, null, responder) {
            private boolean suppressNextBlur;
            private boolean sanitizing;

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
                boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
                sanitizeVisibleNumber(this, min, max);
                return handled;
            }

            @Override
            public boolean charTyped(char codePoint, int modifiers) {
                if (codePoint < '0' || codePoint > '9') {
                    return false;
                }
                boolean handled = super.charTyped(codePoint, modifiers);
                sanitizeVisibleNumber(this, min, max);
                return handled;
            }

            @Override
            protected void onTextChanged(String newTextString) {
                if (sanitizing) {
                    super.onTextChanged(newTextString);
                    return;
                }
                String normalized = normalizeNumberInput(newTextString, getCurrentString(), min, max);
                if (!normalized.equals(newTextString)) {
                    sanitizing = true;
                    setCurrentString(normalized);
                    sanitizing = false;
                    if (isClientSideWidget && responder != null) {
                        responder.accept(normalized);
                    }
                    return;
                }
                super.onTextChanged(normalized);
            }
        };
        field.setClientSideWidget();
        field.setCurrentString(Integer.toString(current));
        field.setMaxStringLength(maxLength);
        field.setValidator(value -> normalizeNumberInput(value, field.getCurrentString(), min, max));
        applyStandardStyle(field, ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE);
        return field;
    }

    private static void sanitizeVisibleNumber(TextFieldWidget field, int min, int max) {
        String raw = field.getRawCurrentString();
        String normalized = normalizeNumberInput(raw, field.getCurrentString(), min, max);
        if (!normalized.equals(raw)) {
            field.setCurrentString(normalized);
        }
    }

    private static String normalizeNumberInput(String value, String fallback, int min, int max) {
        String raw = value == null ? "" : value;
        if (raw.isBlank()) {
            return "";
        }
        StringBuilder digits = new StringBuilder();
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c >= '0' && c <= '9') {
                digits.append(c);
            }
        }
        if (digits.isEmpty()) {
            return fallback == null ? "" : fallback;
        }
        try {
            int parsed = Integer.parseInt(digits.toString());
            return Integer.toString(Math.max(min, Math.min(max, parsed)));
        } catch (NumberFormatException ignored) {
            return Integer.toString(max);
        }
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
