package com.abo47.questsandstuff.client.tablet.contextmenu;

import java.util.List;

public record ContextAction(String label, String icon, ActionTone tone, int accentColor, boolean closeAfterClick, boolean promoted, Runnable action, List<ContextAction> children) {
    public ContextAction {
        label = label == null ? "" : label;
        icon = icon == null ? "style" : icon;
        tone = tone == null ? ActionTone.fromLegacyColor(accentColor) : tone;
        accentColor = tone.accentColor(accentColor);
        action = action == null ? () -> {
        } : action;
        children = children == null ? List.of() : List.copyOf(children);
    }

    public ContextAction(String label, String icon, ActionTone tone, Runnable action) {
        this(label, icon, tone, defaultAccentColor(tone), true, false, action, List.of());
    }

    public ContextAction(String label, String icon, ActionTone tone, boolean closeAfterClick, Runnable action) {
        this(label, icon, tone, defaultAccentColor(tone), closeAfterClick, false, action, List.of());
    }

    public ContextAction(String label, String icon, ActionTone tone, boolean closeAfterClick, boolean promoted, Runnable action) {
        this(label, icon, tone, defaultAccentColor(tone), closeAfterClick, promoted, action, List.of());
    }

    public ContextAction(String label, String icon, ActionTone tone, boolean closeAfterClick, boolean promoted, Runnable action, List<ContextAction> children) {
        this(label, icon, tone, defaultAccentColor(tone), closeAfterClick, promoted, action, children);
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

    public ContextAction(String label, String icon, int accentColor, boolean closeAfterClick, boolean promoted, Runnable action, List<ContextAction> children) {
        this(label, icon, ActionTone.fromLegacyColor(accentColor), accentColor, closeAfterClick, promoted, action, children);
    }

    public boolean hasSubmenu() {
        return !children.isEmpty();
    }

    private static int defaultAccentColor(ActionTone tone) {
        return (tone == null ? ActionTone.PRIMARY : tone).defaultAccentColor();
    }
}
