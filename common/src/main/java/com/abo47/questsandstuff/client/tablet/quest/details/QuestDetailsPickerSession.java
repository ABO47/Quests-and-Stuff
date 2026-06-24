package com.abo47.questsandstuff.client.tablet.quest.details;

public record QuestDetailsPickerSession(
        Type type,
        String kind,
        String targetId,
        String itemSourceTarget,
        boolean xpTask,
        String xpQuestId,
        String xpEntryId,
        int x,
        int y
) {
    private static final QuestDetailsPickerSession NONE = new QuestDetailsPickerSession(
            Type.NONE,
            "",
            "",
            "",
            false,
            "",
            "",
            0,
            0
    );

    public static QuestDetailsPickerSession none() {
        return NONE;
    }

    public static QuestDetailsPickerSession type(String kind, String targetId, int x, int y) {
        return new QuestDetailsPickerSession(
                Type.TYPE,
                clean(kind),
                clean(targetId),
                "",
                false,
                "",
                "",
                x,
                y
        );
    }

    public static QuestDetailsPickerSession itemSource(String target, int x, int y) {
        return new QuestDetailsPickerSession(
                Type.ITEM_SOURCE,
                "",
                "",
                clean(target),
                false,
                "",
                "",
                x,
                y
        );
    }

    public static QuestDetailsPickerSession xp(String questId, String entryId, boolean task, int x, int y) {
        return new QuestDetailsPickerSession(
                Type.XP,
                "",
                "",
                "",
                task,
                clean(questId),
                clean(entryId),
                x,
                y
        );
    }

    public boolean active() {
        return type != Type.NONE;
    }

    public boolean typePicker() {
        return type == Type.TYPE;
    }

    public boolean itemSourcePicker() {
        return type == Type.ITEM_SOURCE;
    }

    public boolean xpPicker() {
        return type == Type.XP;
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }

    public enum Type {
        NONE,
        TYPE,
        ITEM_SOURCE,
        XP
    }
}
