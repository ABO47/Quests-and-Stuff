package com.abo47.questsandstuff.client.tablet.controls;

import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class TabletSelector {
    private TabletSelector() {
    }

    public static <T> SelectorWidget add(
            WidgetGroup parent,
            int x,
            int y,
            int width,
            int height,
            List<Option<T>> options,
            Supplier<T> selectedSupplier,
            Consumer<T> onChanged,
            int visibleRows
    ) {
        List<Option<T>> safeOptions = List.copyOf(options == null ? List.of() : options);
        SelectorWidget selector = new SelectorWidget(x, y, width, height, candidateLabels(safeOptions), ModColors.TEXT_PRIMARY)
                .setMaxCount(Math.max(1, visibleRows))
                .setSupplier(() -> selectedLabel(safeOptions, selectedSupplier == null ? null : selectedSupplier.get()))
                .setCandidatesSupplier(() -> candidateLabels(safeOptions))
                .setOnChanged(label -> {
                    Option<T> option = optionForLabel(safeOptions, label);
                    if (option != null && onChanged != null) {
                        onChanged.accept(option.value());
                    }
                })
                .setButtonBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE))
                .setBackground(Surfaces.bordered(withAlpha(ModColors.SURFACE_PANEL, 248), ModColors.BORDER_ACCENT));
        selector.setClientSideWidget();
        selector.setValue(selectedLabel(safeOptions, selectedSupplier == null ? null : selectedSupplier.get()));
        parent.addWidget(selector);
        return selector;
    }

    public static <T> Option<T> option(T value, String label) {
        return new Option<>(value, label);
    }

    static <T> List<String> candidateLabels(List<Option<T>> options) {
        return (options == null ? List.<Option<T>>of() : options).stream()
                .map(Option::label)
                .toList();
    }

    static <T> String selectedLabel(List<Option<T>> options, T selected) {
        Option<T> match = optionForValue(options, selected);
        if (match != null) {
            return match.label();
        }
        List<String> labels = candidateLabels(options);
        return labels.isEmpty() ? "" : labels.get(0);
    }

    static <T> Option<T> optionForLabel(List<Option<T>> options, String label) {
        String safeLabel = label == null ? "" : label;
        for (Option<T> option : options == null ? List.<Option<T>>of() : options) {
            if (option.label().equals(safeLabel)) {
                return option;
            }
        }
        return null;
    }

    private static <T> Option<T> optionForValue(List<Option<T>> options, T selected) {
        for (Option<T> option : options == null ? List.<Option<T>>of() : options) {
            if (Objects.equals(option.value(), selected)) {
                return option;
            }
        }
        return null;
    }

    public record Option<T>(T value, String label) {
        public Option {
            label = label == null ? "" : label;
        }
    }
}
