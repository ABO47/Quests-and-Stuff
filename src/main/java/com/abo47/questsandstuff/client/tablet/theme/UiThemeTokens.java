package com.abo47.questsandstuff.client.tablet.theme;


public final class UiThemeTokens {
    public static final int GRID_2 = 2;
    public static final int GRID_4 = 4;
    public static final int GRID_6 = 6;
    public static final int GRID_8 = 8;
    public static final int GRID_12 = 12;
    public static final int GRID_16 = 16;
    public static final int CONTEXT_ROW_H = 14;

    private UiThemeTokens() {
    }

    public static int withAlpha(int color, int alpha) {
        return ((alpha & 0xFF) << 24) | (color & 0x00FFFFFF);
    }
}
