package com.abo47.questsandstuff.client.tablet.context;

import com.abo47.questsandstuff.client.tablet.theme.UiActionColors;

import java.util.List;

public record ContextAction(String label, String icon, int accentColor, boolean closeAfterClick, boolean promoted, Runnable action, List<ContextAction> children) {
    public ContextAction {
        label = label == null ? "" : label;
        icon = icon == null ? "style" : icon;
        accentColor = UiActionColors.forAction(label, icon, accentColor);
        action = action == null ? () -> {
        } : action;
        children = children == null ? List.of() : List.copyOf(children);
    }

    public ContextAction(String label, String icon, int accentColor, Runnable action) {
        this(label, icon, accentColor, true, false, action);
    }

    public ContextAction(String label, String icon, int accentColor, boolean closeAfterClick, Runnable action) {
        this(label, icon, accentColor, closeAfterClick, false, action);
    }

    public ContextAction(String label, String icon, int accentColor, boolean closeAfterClick, boolean promoted, Runnable action) {
        this(label, icon, accentColor, closeAfterClick, promoted, action, List.of());
    }

    public boolean hasSubmenu() {
        return !children.isEmpty();
    }
}
