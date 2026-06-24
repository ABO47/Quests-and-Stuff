package com.abo47.questsandstuff.client.tablet.quest.canvas;

public enum CanvasMouseMode {
    SELECT_MOVE("select"),
    DRAG_CANVAS("drag"),
    ADD_QUEST("add"),
    CONNECT_QUESTS("connect");

    public final String label;

    CanvasMouseMode(String label) {
        this.label = label;
    }
}
