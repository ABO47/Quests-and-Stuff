package com.abo47.questsandstuff.client.tablet.controls;

import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;

import org.lwjgl.glfw.GLFW;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;

import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

public final class FontSizeFieldWidget extends TextFieldWidget {
    public static final int MIN = 1;
    public static final int MAX = 100;

    private final IntConsumer onChange;
    private final IntConsumer onPreview;
    private final Runnable onCommit;
    private final Runnable onCancel;
    private final Runnable onBlur;
    private final BooleanSupplier yieldEnterToEditor;
    private boolean suppressNextBlur;
    private boolean focusWhenReady = true;
    private int currentValue;

    public FontSizeFieldWidget(
            int x,
            int y,
            int width,
            int height,
            int currentValue,
            IntConsumer onChange,
            Runnable onCommit,
            Runnable onCancel,
            Runnable onBlur
    ) {
        this(x, y, width, height, currentValue, onChange, null, onCommit, onCancel, onBlur, null);
    }

    public FontSizeFieldWidget(
            int x,
            int y,
            int width,
            int height,
            int currentValue,
            IntConsumer onChange,
            Runnable onCommit,
            Runnable onCancel,
            Runnable onBlur,
            BooleanSupplier yieldEnterToEditor
    ) {
        this(x, y, width, height, currentValue, onChange, null, onCommit, onCancel, onBlur, yieldEnterToEditor);
    }

    public FontSizeFieldWidget(
            int x,
            int y,
            int width,
            int height,
            int currentValue,
            IntConsumer onChange,
            IntConsumer onPreview,
            Runnable onCommit,
            Runnable onCancel,
            Runnable onBlur,
            BooleanSupplier yieldEnterToEditor
    ) {
        super(x, y, width, height, null, null);
        this.currentValue = clamp(currentValue);
        this.onChange = onChange == null ? value -> {
        } : onChange;
        this.onPreview = onPreview != null ? onPreview : this.onChange;
        this.onCommit = onCommit == null ? () -> {
        } : onCommit;
        this.onCancel = onCancel == null ? () -> {
        } : onCancel;
        this.onBlur = onBlur == null ? this.onCommit : onBlur;
        this.yieldEnterToEditor = yieldEnterToEditor;
        setClientSideWidget();
        setBordered(false);
        setMaxStringLength(3);
        StyledTextFields.applyIntegerValidator(this, MIN, MAX);
        setTextResponder(this::handleChanged);
        setCurrentString(Integer.toString(this.currentValue));
        setBackground(SurfaceFactory.bordered(withAlpha(TabletColors.INTERACTIVE, 150), TabletColors.BORDER_ACCENT));
        setTextColor(TabletColors.TEXT_PRIMARY);
        updateTooltip();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        if (focusWhenReady && getGui() != null) {
            focusWhenReady = false;
            setFocus(true);
            selectAll();
        }
    }

    @Override
    public void onFocusChanged(Widget lastFocus, Widget focus) {
        super.onFocusChanged(lastFocus, focus);
        if (isFocus()) {
            return;
        }
        if (lastFocus == this && focus != this) {
            if (suppressNextBlur) {
                suppressNextBlur = false;
                return;
            }
            commitCurrentText();
            onBlur.run();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (!isFocus()) {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            if (yieldEnterToEditor != null && yieldEnterToEditor.getAsBoolean()) {
                return false;
            }
            commitCurrentText();
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

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!isFocus()) {
            return super.charTyped(codePoint, modifiers);
        }
        if (codePoint >= '0' && codePoint <= '9') {
            return super.charTyped(codePoint, modifiers);
        }
        return true;
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        int step = wheelDelta > 0 ? 1 : -1;
        int next = clamp(parseClamped(getCurrentString(), currentValue) + step);
        currentValue = next;
        setCurrentString(Integer.toString(currentValue));
        updateTooltip();
        onPreview.accept(next);
        return true;
    }

    @Override
    protected void onTextChanged(String newTextString) {
        String lastText = getCurrentString();
        String nextText = textValidator.apply(newTextString);
        boolean changed = !nextText.equals(lastText);
        if (changed || !nextText.equals(newTextString)) {
            if (nextText.equals(newTextString)) {
                currentString = nextText;
            } else {
                setCurrentString(nextText);
            }
            if (changed && isClientSideWidget && textResponder != null) {
                textResponder.accept(nextText);
            }
            if (changed) {
                writeClientAction(1, buffer -> buffer.writeUtf(nextText));
            }
        } else if (isRemote() && textField != null) {
            textField.setTextColor(textColor);
        }
        updateTooltip();
    }

    private void handleChanged(String raw) {
        updateTooltip();
    }

    private void commitCurrentText() {
        int next = parseClamped(getCurrentString(), currentValue);
        currentValue = next;
        setCurrentString(Integer.toString(currentValue));
        updateTooltip();
        onChange.accept(next);
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

    private void selectAll() {
        if (isRemote() && textField != null) {
            textField.setCursorPosition(0);
            textField.setHighlightPos(textField.getValue().length());
        }
    }

    private void updateTooltip() {
        setHoverTooltips(new Component[]{Component.literal("Font size: " + currentValue + " (1-100)")});
    }

    private static int parseClamped(String raw, int fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return clamp(Integer.parseInt(raw.trim()));
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int clamp(int value) {
        return Math.max(MIN, Math.min(MAX, value));
    }
}
