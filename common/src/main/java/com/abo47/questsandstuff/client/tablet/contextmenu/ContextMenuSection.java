package com.abo47.questsandstuff.client.tablet.contextmenu;

public enum ContextMenuSection {
    PRIMARY("ui.questsandstuff.context.section.primary"),
    APPEARANCE("ui.questsandstuff.context.section.appearance"),
    ARRANGE("ui.questsandstuff.context.section.arrange"),
    BEHAVIOR("ui.questsandstuff.context.section.behavior"),
    CLIPBOARD("ui.questsandstuff.context.section.clipboard"),
    DANGER("ui.questsandstuff.context.section.danger"),
    DEBUG("ui.questsandstuff.context.section.debug");

    private final String titleKey;

    ContextMenuSection(String titleKey) {
        this.titleKey = titleKey;
    }

    public String titleKey() {
        return titleKey;
    }
}
