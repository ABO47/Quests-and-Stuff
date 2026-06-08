package com.abo47.questsandstuff.client.tablet.controls;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class SearchScrollState {
    private final Supplier<String> searchSupplier;
    private final Consumer<String> searchConsumer;
    private final BooleanSupplier focusedSupplier;
    private final Consumer<Boolean> focusedConsumer;
    private final ScrollState scroll;

    private SearchScrollState(
            Supplier<String> searchSupplier,
            Consumer<String> searchConsumer,
            BooleanSupplier focusedSupplier,
            Consumer<Boolean> focusedConsumer,
            ScrollState scroll
    ) {
        this.searchSupplier = Objects.requireNonNull(searchSupplier, "searchSupplier");
        this.searchConsumer = Objects.requireNonNull(searchConsumer, "searchConsumer");
        this.focusedSupplier = Objects.requireNonNull(focusedSupplier, "focusedSupplier");
        this.focusedConsumer = Objects.requireNonNull(focusedConsumer, "focusedConsumer");
        this.scroll = Objects.requireNonNull(scroll, "scroll");
    }

    public static SearchScrollState bind(
            Supplier<String> searchSupplier,
            Consumer<String> searchConsumer,
            BooleanSupplier focusedSupplier,
            Consumer<Boolean> focusedConsumer,
            ScrollState scroll
    ) {
        return new SearchScrollState(searchSupplier, searchConsumer, focusedSupplier, focusedConsumer, scroll);
    }

    public static SearchScrollState bind(
            Supplier<String> searchSupplier,
            Consumer<String> searchConsumer,
            BooleanSupplier focusedSupplier,
            Consumer<Boolean> focusedConsumer,
            IntSupplier scrollSupplier,
            IntConsumer scrollConsumer,
            BooleanSupplier draggingSupplier,
            Consumer<Boolean> draggingConsumer
    ) {
        return bind(
                searchSupplier,
                searchConsumer,
                focusedSupplier,
                focusedConsumer,
                ScrollState.bind(scrollSupplier, scrollConsumer, draggingSupplier, draggingConsumer)
        );
    }

    public String search() {
        String value = searchSupplier.get();
        return value == null ? "" : value;
    }

    public String normalizedSearch() {
        return SearchFilter.normalize(search());
    }

    public String normalizedKey() {
        return SearchFilter.normalizeKey(search());
    }

    public void setSearch(String search) {
        searchConsumer.accept(SearchFilter.normalizeUserInput(search));
    }

    public boolean focused() {
        return focusedSupplier.getAsBoolean();
    }

    public void setFocused(boolean focused) {
        focusedConsumer.accept(focused);
    }

    public void clearFocus() {
        setFocused(false);
    }

    public ScrollState scroll() {
        return scroll;
    }

    public int scrollValue() {
        return scroll.value();
    }

    public void setScrollValue(int value) {
        scroll.setValue(value);
    }

    public boolean dragging() {
        return scroll.dragging();
    }

    public void setDragging(boolean dragging) {
        scroll.setDragging(dragging);
    }

    public void scrollToTop() {
        setScrollValue(0);
        setDragging(false);
    }

    public void clearInteraction() {
        clearFocus();
        setDragging(false);
    }

    public void reset() {
        setSearch("");
        clearFocus();
        scrollToTop();
    }
}
