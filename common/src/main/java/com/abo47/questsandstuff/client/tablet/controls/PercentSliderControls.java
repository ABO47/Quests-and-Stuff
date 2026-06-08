package com.abo47.questsandstuff.client.tablet.controls;

import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public final class PercentSliderControls {
    private static final int FIELD_W = 34;
    private static final int GAP = 6;

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

        TextFieldWidget field = StyledTextFields.numberField(
                x + sliderW + GAP,
                y + 1,
                FIELD_W,
                14,
                normalize(value),
                0,
                100,
                3,
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
        StyledTextFields.applyStandardStyle(field, ModColors.SURFACE_BASE, ModColors.BORDER_BASE);
        if (tooltips != null && tooltips.length > 0) {
            field.setHoverTooltips(tooltips);
        }
        parent.addWidget(field);
        return field;
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
