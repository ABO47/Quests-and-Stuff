package com.abo47.questsandstuff.client.tablet.context;


import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeTokens;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;

import java.util.Collection;
import java.util.Locale;
import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ACTION_ICON_SIZE;

public final class ContextMenuSystem {
    private ContextMenuSystem() {
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        WidgetGroup rowBg = new WidgetGroup(4, y, width, UiThemeTokens.CONTEXT_ROW_H);
        rowBg.setBackground(Surfaces.fill(UiThemeTokens.withAlpha(ModColors.SURFACE_PANEL_ALT, 84)));
        menu.addWidget(rowBg);

        if (!addAtlasIcon(menu, 8, y, icon)) {
            menu.addWidget(drawContextIcon(8, y + 2, icon));
        }

        LabelWidget textWidget = new LabelWidget(24, y + 3, text);
        textWidget.setColor(ModColors.TEXT_PRIMARY);
        menu.addWidget(textWidget);

        ButtonWidget hit = flatHitButton(4, y, width, UiThemeTokens.CONTEXT_ROW_H, callback);
        hit.setHoverTexture(Surfaces.bordered(UiThemeTokens.withAlpha(ModColors.INTERACTIVE, 64), UiThemeTokens.withAlpha(ModColors.BORDER_ACCENT, 220)));
        hit.setClickedTexture(Surfaces.fill(UiThemeTokens.withAlpha(ModColors.INTERACTIVE, 95)));
        menu.addWidget(hit);
    }

    public static void addContextIcon(WidgetGroup menu, int x, int y, String icon) {
        if (!addAtlasIcon(menu, x, y, icon)) {
            menu.addWidget(drawContextIcon(x, y + 2, icon));
        }
    }

    public static void addSeparator(WidgetGroup menu, int y, int width) {
        WidgetGroup sep = new WidgetGroup(4, y + 1, width, 1);
        sep.setBackground(Surfaces.fill(UiThemeTokens.withAlpha(ModColors.BORDER_BASE, 120)));
        menu.addWidget(sep);
    }

    public static String iconForLabel(String label) {
        String v = label == null ? "" : label.toLowerCase(Locale.ROOT);
        if (v.contains("delete")) return "delete";
        if (v.contains("copy")) return "copy";
        if (v.contains("paste")) return "paste";
        if (v.contains("rename")) return "rename";
        if (v.contains("new") || v.contains("add") || v.contains("create")) return "add";
        if (v.contains("icon")) return "icon";
        if (v.contains("style") || v.contains("align") || v.contains("grid") || v.contains("snap")) return "style";
        if (v.contains("up")) return "up";
        if (v.contains("down")) return "down";
        if (v.contains("show") || v.contains("hide")) return "image";
        if (v.contains("open")) return "open";
        return "style";
    }

    public static int preferredMenuWidth(Collection<String> labels, int minWidth, int maxWidth) {
        int widest = 0;
        if (labels != null) {
            for (String label : labels) {
                widest = Math.max(widest, Minecraft.getInstance().font.width(label == null ? "" : label));
            }
        }
        int content = widest + 34;
        return Math.max(minWidth, Math.min(maxWidth, content));
    }

    private static boolean addAtlasIcon(WidgetGroup menu, int x, int y, String icon) {
        String key = contextIconFileKey(icon);
        var texture = UiIconAtlas.iconTexture("context_" + key);
        if (texture == null) {
            texture = UiIconAtlas.iconTexture(key);
        }
        if (texture == null) {
            return false;
        }
        menu.addWidget(new ImageWidget(x, y, ACTION_ICON_SIZE, ACTION_ICON_SIZE, texture));
        return true;
    }

    private static WidgetGroup drawContextIcon(int x, int y, String icon) {
        WidgetGroup g = new WidgetGroup(x, y, 10, 8);
        int c = UiThemeManager.iconColor("context_" + contextIconFileKey(icon));
        switch (icon) {
            case "rename" -> {
                g.addWidget(pixel(0, 6, 8, 1, c));
                g.addWidget(pixel(1, 4, 5, 1, c));
                g.addWidget(pixel(5, 1, 3, 3, c));
            }
            case "add" -> {
                g.addWidget(pixel(4, 1, 1, 6, c));
                g.addWidget(pixel(1, 4, 7, 1, c));
            }
            case "delete" -> {
                g.addWidget(pixel(1, 2, 6, 1, c));
                g.addWidget(pixel(2, 3, 4, 4, c));
            }
            case "copy" -> {
                g.addWidget(pixel(2, 1, 5, 5, c));
                g.addWidget(pixel(0, 3, 5, 5, c));
            }
            case "paste" -> {
                g.addWidget(pixel(2, 0, 4, 2, c));
                g.addWidget(pixel(1, 2, 6, 6, c));
                g.addWidget(pixel(3, 4, 3, 1, c));
            }
            case "icon" -> {
                g.addWidget(pixel(1, 1, 6, 6, c));
                g.addWidget(pixel(2, 2, 4, 4, c));
            }
            case "image" -> {
                g.addWidget(pixel(1, 1, 7, 6, c));
                g.addWidget(pixel(2, 5, 5, 1, c));
            }
            case "style" -> {
                g.addWidget(pixel(1, 1, 6, 1, c));
                g.addWidget(pixel(2, 3, 4, 1, c));
                g.addWidget(pixel(3, 5, 2, 1, c));
            }
            case "up" -> {
                g.addWidget(pixel(4, 1, 1, 6, c));
                g.addWidget(pixel(2, 3, 5, 1, c));
            }
            case "down" -> {
                g.addWidget(pixel(4, 1, 1, 6, c));
                g.addWidget(pixel(2, 5, 5, 1, c));
            }
            default -> g.addWidget(pixel(1, 3, 6, 1, c));
        }
        return g;
    }

    private static WidgetGroup pixel(int x, int y, int w, int h, int color) {
        WidgetGroup px = new WidgetGroup(x, y, w, h);
        px.setBackground(Surfaces.fill(color));
        return px;
    }

    private static ButtonWidget flatHitButton(int x, int y, int w, int h, Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        ButtonWidget button = new ButtonWidget(x, y, w, h, Surfaces.fill(0x00000000), callback);
        button.setClientSideWidget();
        button.setHoverTexture(Surfaces.fill(0x00000000));
        button.setClickedTexture(Surfaces.fill(0x00000000));
        return button;
    }

    public static String contextIconFileKey(String icon) {
        String clean = icon == null ? "" : icon.trim().toLowerCase(Locale.ROOT);
        if (!isSafeAtlasKey(clean)) {
            return "style";
        }
        return switch (clean) {
            case "up" -> "move_up";
            case "down" -> "move_down";
            case "eye_off" -> "eye-off";
            case "audio_lines" -> "audio-lines";
            default -> clean;
        };
    }

    private static boolean isSafeAtlasKey(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            boolean allowed = (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '/'
                    || c == '.'
                    || c == '_'
                    || c == '-';
            if (!allowed) {
                return false;
            }
        }
        return true;
    }
}
