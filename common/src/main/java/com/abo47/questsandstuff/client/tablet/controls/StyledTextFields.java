package com.abo47.questsandstuff.client.tablet.controls;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.util.MathUtils;
import com.abo47.questsandstuff.util.naming.SafeNames;

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
        return search(x, y, width, height, () -> current, maxLength, responder, focusResponder);
    }

    public static TextFieldWidget search(
            int x,
            int y,
            int width,
            int height,
            Supplier<String> textSupplier,
            int maxLength,
            Consumer<String> responder,
            Consumer<Boolean> focusResponder
    ) {
        TextFieldWidget field = new TextFieldWidget(x, y, width, height, textSupplier, responder) {
            @Override
            public void onFocusChanged(Widget lastFocus, Widget focus) {
                super.onFocusChanged(lastFocus, focus);
                if (focusResponder != null) {
                    focusResponder.accept(isFocus());
                }
            }

            @Override
            public TextFieldWidget setCurrentString(Object currentString) {
                String newVal = currentString.toString();
                if (isRemote() && textField != null && !textField.getValue().equals(newVal)) {
                    int cursorPos = textField.getCursorPosition();
                    super.setCurrentString(newVal);
                    int clamped = Math.min(cursorPos, newVal.length());
                    textField.setCursorPosition(clamped);
                    textField.setHighlightPos(clamped);
                } else {
                    super.setCurrentString(currentString);
                }
                return this;
            }
        };
        field.setClientSideWidget();
        field.setCurrentString(currentText(textSupplier));
        field.setMaxStringLength(maxLength);
        field.setValidator(SearchNormalizer::normalizeUserSearch);
        applyStandardStyle(field, TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE);
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

            @Override
            public TextFieldWidget setCurrentString(Object currentString) {
                String newVal = currentString.toString();
                if (isRemote() && textField != null && !textField.getValue().equals(newVal)) {
                    int cursorPos = textField.getCursorPosition();
                    super.setCurrentString(newVal);
                    int clamped = Math.min(cursorPos, newVal.length());
                    textField.setCursorPosition(clamped);
                    textField.setHighlightPos(clamped);
                } else {
                    super.setCurrentString(currentString);
                }
                return this;
            }
        };
    }

    public static void applyStandardStyle(TextFieldWidget field, int fillColor, int borderColor) {
        field.setBordered(false);
        field.setBackground(SurfaceFactory.bordered(fillColor, borderColor));
        field.setTextColor(TabletColors.TEXT_PRIMARY);
    }

    public static TextFieldWidget textField(
            int x,
            int y,
            int width,
            int height,
            Supplier<String> textSupplier,
            int maxLength,
            Consumer<String> responder
    ) {
        TextFieldWidget field = new TextFieldWidget(x, y, width, height, textSupplier, responder) {
            @Override
            public TextFieldWidget setCurrentString(Object currentString) {
                String newVal = currentString.toString();
                if (isRemote() && textField != null && !textField.getValue().equals(newVal)) {
                    int cursorPos = textField.getCursorPosition();
                    super.setCurrentString(newVal);
                    int clamped = Math.min(cursorPos, newVal.length());
                    textField.setCursorPosition(clamped);
                    textField.setHighlightPos(clamped);
                } else {
                    super.setCurrentString(currentString);
                }
                return this;
            }
        };
        field.setClientSideWidget();
        field.setCurrentString(currentText(textSupplier));
        field.setMaxStringLength(maxLength);
        applyStandardStyle(field, TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE);
        return field;
    }

    public static TextFieldWidget resourceLocationField(
            int x,
            int y,
            int width,
            int height,
            Supplier<String> textSupplier,
            int maxLength,
            Consumer<String> responder,
            Runnable commit,
            Runnable cancel,
            Runnable blur
    ) {
        TextFieldWidget field = configuredCommitField(
                x,
                y,
                width,
                height,
                textSupplier,
                maxLength,
                responder,
                commit,
                cancel,
                blur,
                null,
                TabletColors.SURFACE_BASE,
                TabletColors.BORDER_BASE
        );
        return applyResourceLocationValidator(field);
    }

    public static TextFieldWidget compoundTagField(
            int x,
            int y,
            int width,
            int height,
            Supplier<String> textSupplier,
            int maxLength,
            Consumer<String> responder,
            Runnable commit,
            Runnable cancel,
            Runnable blur
    ) {
        TextFieldWidget field = configuredCommitField(
                x,
                y,
                width,
                height,
                textSupplier,
                maxLength,
                responder,
                commit,
                cancel,
                blur,
                null,
                TabletColors.SURFACE_BASE,
                TabletColors.BORDER_BASE
        );
        return applyCompoundTagValidator(field);
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
        return integerField(x, y, width, height, current, min, max, maxLength, responder, commit, cancel, blur, null);
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
        return integerField(x, y, width, height, current, min, max, maxLength, responder, commit, cancel, blur, focusResponder);
    }

    public static TextFieldWidget integerField(
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
        return integerField(x, y, width, height, current, min, max, maxLength, responder, commit, cancel, blur, null);
    }

    public static TextFieldWidget integerField(
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
        int value = MathUtils.clamp((int) current, min, max);
        TextFieldWidget field = configuredCommitField(
                x,
                y,
                width,
                height,
                () -> Integer.toString(value),
                maxLength,
                responder,
                commit,
                cancel,
                blur,
                focusResponder,
                TabletColors.SURFACE_PANEL_ALT,
                TabletColors.BORDER_BASE
        );
        return applyIntegerValidator(field, min, max);
    }

    public static TextFieldWidget floatField(
            int x,
            int y,
            int width,
            int height,
            float current,
            float min,
            float max,
            int maxLength,
            Consumer<String> responder,
            Runnable commit,
            Runnable cancel,
            Runnable blur
    ) {
        float value = clamp(current, min, max);
        TextFieldWidget field = configuredCommitField(
                x,
                y,
                width,
                height,
                () -> Float.toString(value),
                maxLength,
                responder,
                commit,
                cancel,
                blur,
                null,
                TabletColors.SURFACE_PANEL_ALT,
                TabletColors.BORDER_BASE
        );
        return applyFloatValidator(field, min, max);
    }

    public static TextFieldWidget percentageField(
            int x,
            int y,
            int width,
            int height,
            int current,
            Consumer<String> responder,
            Runnable commit,
            Runnable cancel,
            Runnable blur
    ) {
        TextFieldWidget field = configuredCommitField(
                x,
                y,
                width,
                height,
                () -> Integer.toString(MathUtils.clamp(current, 0, 100)),
                3,
                responder,
                commit,
                cancel,
                blur,
                null,
                TabletColors.SURFACE_PANEL_ALT,
                TabletColors.BORDER_BASE
        );
        return applyPercentageValidator(field);
    }

    public static TextFieldWidget identifierField(
            int x,
            int y,
            int width,
            int height,
            Supplier<String> textSupplier,
            int maxLength,
            Consumer<String> responder,
            Runnable commit,
            Runnable cancel,
            Runnable blur
    ) {
        TextFieldWidget field = configuredCommitField(
                x,
                y,
                width,
                height,
                textSupplier,
                maxLength,
                responder,
                commit,
                cancel,
                blur,
                null,
                TabletColors.SURFACE_BASE,
                TabletColors.BORDER_BASE
        );
        return applyIdentifierValidator(field);
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
        field.setValidator(SearchNormalizer::normalizeHexInput);
        applyStandardStyle(field, TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE);
        return field;
    }

    public static TextFieldWidget applyResourceLocationValidator(TextFieldWidget field) {
        return field.setResourceLocationOnly();
    }

    public static TextFieldWidget applyCompoundTagValidator(TextFieldWidget field) {
        return field.setCompoundTagOnly();
    }

    public static TextFieldWidget applyIntegerValidator(TextFieldWidget field, int min, int max) {
        return field.setNumbersOnly(min, max);
    }

    public static TextFieldWidget applyPercentageValidator(TextFieldWidget field) {
        return applyIntegerValidator(field, 0, 100);
    }

    public static TextFieldWidget applyFloatValidator(TextFieldWidget field, float min, float max) {
        return field.setNumbersOnly(min, max);
    }

    public static TextFieldWidget applyIdentifierValidator(TextFieldWidget field) {
        field.setValidator(value -> {
            String fallback = SafeNames.identifier(field.getCurrentString(), "");
            return SafeNames.identifier(value, fallback);
        });
        return field;
    }

    private static TextFieldWidget configuredCommitField(
            int x,
            int y,
            int width,
            int height,
            Supplier<String> textSupplier,
            int maxLength,
            Consumer<String> responder,
            Runnable commit,
            Runnable cancel,
            Runnable blur,
            Consumer<Boolean> focusResponder,
            int fillColor,
            int borderColor
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
                blur,
                focusResponder
        );
        field.setClientSideWidget();
        field.setCurrentString(currentText(textSupplier));
        field.setMaxStringLength(maxLength);
        applyStandardStyle(field, fillColor, borderColor);
        return field;
    }

    private static String currentText(Supplier<String> textSupplier) {
        if (textSupplier == null) {
            return "";
        }
        String value = textSupplier.get();
        return value == null ? "" : value;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
