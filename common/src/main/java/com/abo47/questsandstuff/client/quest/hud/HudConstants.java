package com.abo47.questsandstuff.client.quest.hud;

public final class HudConstants {
    // Notification overlay
    public static final int NOTIFICATION_WIDTH = 128;
    public static final int NOTIFICATION_HEIGHT = 32;
    public static final int MAX_NOTIFICATIONS = 3;
    public static final int MIN_FONT_ALPHA = 4;
    public static final float NOTIFICATION_SLIDE_DISTANCE = 12.0f;

    // Pinned HUD
    public static final int PINNED_WIDTH = 168;
    public static final int PINNED_HEADER_HEIGHT = 25;
    public static final int PINNED_ROW_HEIGHT = 12;
    public static final int PINNED_PAD = 6;
    public static final int PINNED_STACK_GAP = 4;
    public static final int PINNED_MAX_QUESTS = 3;
    public static final int PINNED_MAX_TASK_ROWS = 4;

    // Layout manager defaults
    public static final int DEFAULT_OPACITY = 100;
    public static final int DEFAULT_COMPLETION_X = 225;
    public static final int DEFAULT_COMPLETION_Y = 241;
    public static final int DEFAULT_PINNED_X = 1;
    public static final int DEFAULT_PINNED_Y = 1;
    public static final int DEFAULT_COMPLETION_SCALE = 149;
    public static final int DEFAULT_COMPLETION_HEIGHT_SCALE = 147;
    public static final int DEFAULT_PINNED_SCALE = 104;
    public static final int DEFAULT_PINNED_HEIGHT_SCALE = 115;

    // Layout edit screen
    public static final int EDIT_BUTTON_W = 64;
    public static final int EDIT_BUTTON_H = 20;
    public static final int EDIT_BUTTON_GAP = 8;

    // Drag handler
    public static final int DRAG_GRID_STEP = 16;
    public static final int DRAG_HANDLE_SIZE = 6;
    public static final int DRAG_SELECTION_PAD = 1;

    private HudConstants() {}
}
