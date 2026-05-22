package com.abo47.questsandstuff.client.canvas.selection;


import java.util.LinkedHashSet;
import java.util.Set;

public final class SelectionModel<T> {
    private final Set<T> selected = new LinkedHashSet<>();

    public Set<T> values() {
        return selected;
    }

    public boolean isEmpty() {
        return selected.isEmpty();
    }

    public void clear() {
        selected.clear();
    }

    public boolean contains(T value) {
        return selected.contains(value);
    }

    public void selectOnly(T value) {
        selected.clear();
        selected.add(value);
    }

    public void add(T value) {
        selected.add(value);
    }

    public void remove(T value) {
        selected.remove(value);
    }

    public boolean toggle(T value) {
        if (selected.contains(value)) {
            selected.remove(value);
            return false;
        }
        selected.add(value);
        return true;
    }
}
