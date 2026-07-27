package com.abo47.questsandstuff.client.tablet.controls;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.util.ClickData;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;

public final class TextStyleButtons {
    public static final int TOOL_COUNT = 12;
    public static final int TEXTBOX_FRAME_GAP = 2;
    public static final int CHAPTER_FRAME_GAP = 3;
    public static final int BUTTON_SIZE = GRID_18;
    public static final int BUTTON_H = BUTTON_SIZE;

    private static final int PAD = GRID_2;
    private static final int GAP = GRID_2;
    private static final int COMPACT_COLUMNS = 4;
    private static final int WIDE_COLUMNS = 8;

    private TextStyleButtons() {
    }

    public static WidgetGroup shell(int x, int y, int width, int height, Consumer<ClickData> callback) {
        WidgetGroup floating = new WidgetGroup(x, y, width, height);
        floating.setBackground(SurfaceFactory.bordered(withAlpha(TabletColors.SURFACE_BASE, 246), TabletColors.BORDER_ACCENT));
        floating.addWidget(TabletUiFactory.panel(
                1,
                1,
                Math.max(1, width - 2),
                Math.max(1, height - 2),
                withAlpha(TabletColors.SURFACE_PANEL_ALT, 212),
                withAlpha(TabletColors.BORDER_BASE, 130)
        ));
        floating.addWidget(TabletUiFactory.flatHitButton(0, 0, width, height, callback));
        return floating;
    }

    public static int preferredSingleRowWidth() {
        return widthForColumns(WIDE_COLUMNS);
    }

    public static int minimumWidth() {
        return widthForColumns(COMPACT_COLUMNS);
    }

    public static int menuWidthForAvailable(int availableWidth) {
        int available = Math.max(1, availableWidth);
        if (available >= preferredSingleRowWidth()) {
            return preferredSingleRowWidth();
        }
        return Math.max(minimumWidth(), available);
    }

    public static int menuHeightForWidth(int menuWidth) {
        return menuHeightForColumns(columnsForWidth(menuWidth));
    }

    public static int menuHeightForColumns(int columns) {
        int rows = rowsForColumns(columns);
        return PAD * 2 + rows * BUTTON_H + Math.max(0, rows - 1) * GAP;
    }

    public static int columnsForWidth(int menuWidth) {
        return menuWidth >= preferredSingleRowWidth() ? WIDE_COLUMNS : COMPACT_COLUMNS;
    }

    public static int buttonWidth(int menuWidth, int columns) {
        return BUTTON_SIZE;
    }

    public static int toolX(int index, int menuWidth, int columns) {
        int safeColumns = Math.max(1, Math.min(TOOL_COUNT, columns));
        return PAD + (index % safeColumns) * (buttonWidth(menuWidth, safeColumns) + GAP);
    }

    public static int toolY(int index, int columns) {
        int safeColumns = Math.max(1, Math.min(TOOL_COUNT, columns));
        return PAD + (index / safeColumns) * (BUTTON_H + GAP);
    }

    public static void addTool(WidgetGroup parent, int index, int menuWidth, int columns, String iconName, int baseColor, Consumer<ClickData> callback) {
        addTool(parent, index, menuWidth, columns, iconName, baseColor, null, null, callback);
    }

    public static void addTool(WidgetGroup parent, int index, int menuWidth, int columns, String iconName, int baseColor, Integer iconTint, Consumer<ClickData> callback) {
        addTool(parent, index, menuWidth, columns, iconName, baseColor, iconTint, null, callback);
    }

    public static void addTool(WidgetGroup parent, int index, int menuWidth, int columns, String iconName, int baseColor, Integer iconTint, Component[] tooltips, Consumer<ClickData> callback) {
        int buttonW = buttonWidth(menuWidth, columns);
        add(parent, toolX(index, menuWidth, columns), toolY(index, columns), buttonW, BUTTON_H, iconName, baseColor, iconTint, tooltips, callback);
    }

    public static void add(WidgetGroup parent, int x, int y, int width, int height, String iconName, int baseColor, Consumer<ClickData> callback) {
        add(parent, x, y, width, height, iconName, baseColor, null, null, callback);
    }

    public static void add(WidgetGroup parent, int x, int y, int width, int height, String iconName, int baseColor, Integer iconTint, Consumer<ClickData> callback) {
        add(parent, x, y, width, height, iconName, baseColor, iconTint, null, callback);
    }

    public static void add(WidgetGroup parent, int x, int y, int width, int height, String iconName, int baseColor, Integer iconTint, Component[] tooltips, Consumer<ClickData> callback) {
        boolean active = baseColor != TabletColors.SURFACE_PANEL_ALT;
        int accent = active ? baseColor : TabletColors.INTERACTIVE;
        int iconColor = iconTint == null ? (active ? TabletColors.TEXT_PRIMARY : TabletColors.TEXT_SECONDARY) : iconTint;
        TabletIconTextButton.Visuals visuals = new TabletIconTextButton.Visuals(
                TabletIconTextButton.State.of(
                        active ? withAlpha(accent, 158) : withAlpha(TabletColors.SURFACE_BASE, 72),
                        active ? accent : withAlpha(TabletColors.BORDER_BASE, 116),
                        iconColor
                ),
                TabletIconTextButton.State.of(
                        active ? withAlpha(accent, 196) : withAlpha(TabletColors.INTERACTIVE, 82),
                        active ? TabletColors.TEXT_PRIMARY : withAlpha(TabletColors.BORDER_ACCENT, 210),
                        iconColor
                ),
                TabletIconTextButton.State.of(
                        active ? withAlpha(accent, 222) : withAlpha(TabletColors.INTERACTIVE, 118),
                        active ? TabletColors.TEXT_PRIMARY : TabletColors.BORDER_ACCENT,
                        iconColor
                )
        );
        parent.addWidget(TabletIconTextButton.icon(x, y, width, height, iconName, visuals, callback)
                .iconSize(Math.min(TabletUiFactory.ACTION_ICON_SIZE - 1, Math.max(8, Math.min(width - 7, height - 7))))
                .tooltips(tooltips));
    }

    public static FontSizeFieldWidget addFontSizeField(
            WidgetGroup parent,
            int index,
            int menuWidth,
            int columns,
            int currentValue,
            IntConsumer onChange,
            Runnable onCommit,
            Runnable onCancel,
            Runnable onBlur
    ) {
        int buttonW = buttonWidth(menuWidth, columns);
        FontSizeFieldWidget field = new FontSizeFieldWidget(
                toolX(index, menuWidth, columns),
                toolY(index, columns),
                buttonW,
                BUTTON_H,
                currentValue,
                onChange,
                onCommit,
                onCancel,
                onBlur
        );
        parent.addWidget(field);
        return field;
    }

    private static int widthForColumns(int columns) {
        int safeColumns = Math.max(1, columns);
        return PAD * 2 + safeColumns * BUTTON_SIZE + Math.max(0, safeColumns - 1) * GAP;
    }

    private static int rowsForColumns(int columns) {
        int safeColumns = Math.max(1, Math.min(TOOL_COUNT, columns));
        return Math.max(1, (TOOL_COUNT + safeColumns - 1) / safeColumns);
    }

}
