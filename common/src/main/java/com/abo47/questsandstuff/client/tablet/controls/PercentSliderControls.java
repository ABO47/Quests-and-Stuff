package com.abo47.questsandstuff.client.tablet.controls;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.GRID_6;

public final class PercentSliderControls {
    private static final int FIELD_W = 34;
    private static final int GAP = GRID_6;

    private PercentSliderControls() {
    }

    public static TextFieldWidget add(
            WidgetGroup parent,
            int x,
            int y,
            int width,
            int value,
            IntConsumer onChange,
            Runnable onCommit,
            BooleanSupplier dragging,
            Consumer<Boolean> setDragging,
            Component[] tooltips
    ) {
        Runnable commit = onCommit == null ? () -> {
        } : onCommit;
        int sliderW = Math.max(24, width - FIELD_W - GAP);
        parent.addWidget(new PercentSliderWidget(
                x,
                y,
                sliderW,
                16,
                value,
                next -> {
                    if (onChange != null) {
                        onChange.accept(normalize(next));
                    }
                },
                commit,
                dragging,
                setDragging
        ));

        TextFieldWidget field = StyledTextFields.percentageField(
                x + sliderW + GAP,
                y + 1,
                FIELD_W,
                14,
                normalize(value),
                raw -> {
                    if (onChange != null) {
                        onChange.accept(parsePercent(raw, value));
                    }
                },
                commit,
                () -> {
                },
                commit
        );
        StyledTextFields.applyStandardStyle(field, TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE);
        if (tooltips != null && tooltips.length > 0) {
            field.setHoverTooltips(tooltips);
        }
        parent.addWidget(field);
        return field;
    }

    public static TextFieldWidget addRanged(
            WidgetGroup parent,
            int x,
            int y,
            int width,
            int min,
            int max,
            int value,
            IntConsumer onChange,
            Runnable onCommit,
            BooleanSupplier dragging,
            Consumer<Boolean> setDragging,
            Component[] tooltips
    ) {
        Runnable commit = onCommit == null ? () -> {
        } : onCommit;
        int sliderW = Math.max(24, width - FIELD_W - GAP);
        parent.addWidget(new PercentSliderWidget(
                x,
                y,
                sliderW,
                16,
                min,
                max,
                value,
                next -> {
                    if (onChange != null) {
                        onChange.accept(normalize(next, min, max));
                    }
                },
                commit,
                dragging,
                setDragging
        ));

        TextFieldWidget field = StyledTextFields.integerField(
                x + sliderW + GAP,
                y + 1,
                FIELD_W,
                14,
                normalize(value, min, max),
                min,
                max,
                4,
                raw -> {
                    if (onChange != null) {
                        onChange.accept(parseRanged(raw, value, min, max));
                    }
                },
                commit,
                () -> {
                },
                commit
        );
        StyledTextFields.applyStandardStyle(field, TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE);
        if (tooltips != null && tooltips.length > 0) {
            field.setHoverTooltips(tooltips);
        }
        parent.addWidget(field);
        return field;
    }

    private static int parseRanged(String value, int fallback, int min, int max) {
        if (value == null || value.isBlank()) {
            return normalize(fallback, min, max);
        }
        try {
            return normalize(Integer.parseInt(value.trim()), min, max);
        } catch (NumberFormatException ignored) {
            return normalize(fallback, min, max);
        }
    }

    static int normalize(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    private static int parsePercent(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return normalize(fallback);
        }
        try {
            return normalize(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ignored) {
            return normalize(fallback);
        }
    }

    static int normalize(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
