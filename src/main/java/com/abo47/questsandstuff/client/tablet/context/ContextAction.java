package com.abo47.questsandstuff.client.tablet.context;


public record ContextAction(String label, String icon, int accentColor, boolean closeAfterClick, Runnable action) {
    public ContextAction(String label, String icon, int accentColor, Runnable action) {
        this(label, icon, accentColor, true, action);
    }
}
