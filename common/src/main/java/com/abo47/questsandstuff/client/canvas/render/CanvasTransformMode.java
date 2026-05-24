package com.abo47.questsandstuff.client.canvas.render;

public enum CanvasTransformMode {
    MOVE("move", "gizmo_move"),
    RESIZE("resize", "gizmo_resize"),
    ROTATE("rotate", "gizmo_rotate");

    public final String id;
    public final String icon;

    CanvasTransformMode(String id, String icon) {
        this.id = id;
        this.icon = icon;
    }

    public static CanvasTransformMode fromId(String id) {
        String value = id == null ? "" : id.trim();
        for (CanvasTransformMode mode : values()) {
            if (mode.id.equals(value)) {
                return mode;
            }
        }
        return MOVE;
    }
}
