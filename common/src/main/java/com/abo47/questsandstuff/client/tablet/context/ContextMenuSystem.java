package com.abo47.questsandstuff.client.tablet.context;


import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.icons.SmoothResourceTexture;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.UiActionColors;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeTokens;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Locale;
import java.util.function.Consumer;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ACTION_ICON_SIZE;

public final class ContextMenuSystem {
    private static final int OUTER_PAD = 4;

    private ContextMenuSystem() {
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addWindowsContextRow(menu, y, width, text, icon, false, callback);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, boolean submenu, Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addWindowsContextRow(menu, y, width, text, icon, UiActionColors.forAction(text, icon, ModColors.INTERACTIVE), submenu, callback);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, int iconColor, boolean submenu, Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        WidgetGroup rowBg = new WidgetGroup(4, y, width, UiThemeTokens.CONTEXT_ROW_H);
        rowBg.setBackground(Surfaces.fill(UiThemeTokens.withAlpha(ModColors.SURFACE_PANEL_ALT, 84)));
        menu.addWidget(rowBg);

        if (!addAtlasIcon(menu, 8, y, icon, iconColor)) {
            menu.addWidget(drawContextIcon(8, y + 2, icon, iconColor));
        }

        LabelWidget textWidget = new LabelWidget(24, y + 3, text);
        textWidget.setColor(ModColors.TEXT_PRIMARY);
        menu.addWidget(textWidget);

        if (submenu) {
            addContextIcon(menu, 4 + width - ACTION_ICON_SIZE - 3, y, "chevron-right", iconColor);
        }

        ButtonWidget hit = flatHitButton(4, y, width, UiThemeTokens.CONTEXT_ROW_H, callback);
        hit.setHoverTexture(Surfaces.bordered(UiThemeTokens.withAlpha(ModColors.INTERACTIVE, 64), UiThemeTokens.withAlpha(ModColors.BORDER_ACCENT, 220)));
        hit.setClickedTexture(Surfaces.fill(UiThemeTokens.withAlpha(ModColors.INTERACTIVE, 95)));
        menu.addWidget(hit);
    }

    public static int rowHeight() {
        return UiThemeTokens.CONTEXT_ROW_H;
    }

    public static int outerPad() {
        return OUTER_PAD;
    }

    public static int menuHeightForRows(int rows) {
        return OUTER_PAD * 2 + Math.max(1, rows) * rowHeight();
    }

    public static void drawVanillaPanel(GuiGraphics graphics, int x, int y, int width, int height, int borderColor) {
        graphics.fill(x, y, x + width, y + height, UiThemeTokens.withAlpha(ModColors.SURFACE_BASE, 246));
        graphics.renderOutline(x, y, width, height, borderColor);
    }

    public static void drawVanillaContextRow(GuiGraphics graphics, int menuX, int rowY, int rowWidth, String text, String icon, boolean hovered) {
        drawVanillaContextRow(graphics, menuX, rowY, rowWidth, text, icon, UiActionColors.forAction(text, icon, ModColors.INTERACTIVE), hovered);
    }

    public static void drawVanillaContextRow(GuiGraphics graphics, int menuX, int rowY, int rowWidth, String text, String icon, int iconColor, boolean hovered) {
        int rowX = menuX + OUTER_PAD;
        int rowH = rowHeight();
        graphics.fill(rowX, rowY, rowX + rowWidth, rowY + rowH, UiThemeTokens.withAlpha(ModColors.SURFACE_PANEL_ALT, 84));
        if (hovered) {
            graphics.fill(rowX, rowY, rowX + rowWidth, rowY + rowH, UiThemeTokens.withAlpha(ModColors.INTERACTIVE, 64));
            graphics.renderOutline(rowX, rowY, rowWidth, rowH, UiThemeTokens.withAlpha(ModColors.BORDER_ACCENT, 220));
        }
        drawVanillaIcon(graphics, menuX + 8, rowY, icon, iconColor);
        graphics.drawString(Minecraft.getInstance().font, text == null ? "" : text, menuX + 24, rowY + 3, ModColors.TEXT_PRIMARY, false);
    }

    public static void addContextIcon(WidgetGroup menu, int x, int y, String icon) {
        addContextIcon(menu, x, y, icon, UiActionColors.forAction("", icon, UiThemeManager.iconColor("context_" + contextIconFileKey(icon))));
    }

    public static void addContextIcon(WidgetGroup menu, int x, int y, String icon, int iconColor) {
        if (!addAtlasIcon(menu, x, y, icon, iconColor)) {
            menu.addWidget(drawContextIcon(x, y + 2, icon, iconColor));
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
        if (v.contains("fit") && v.contains("grid")) return "fit_grid";
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

    private static boolean addAtlasIcon(WidgetGroup menu, int x, int y, String icon, int iconColor) {
        ResourceLocation id = contextIconLocation(icon);
        if (id == null) {
            return false;
        }
        var texture = new SmoothResourceTexture(id).setDynamicColor(() -> iconColor);
        menu.addWidget(new ImageWidget(x, y, ACTION_ICON_SIZE, ACTION_ICON_SIZE, texture));
        return true;
    }

    private static void drawVanillaIcon(GuiGraphics graphics, int x, int y, String icon, int iconColor) {
        ResourceLocation id = contextIconLocation(icon);
        if (id != null) {
            var texture = new SmoothResourceTexture(id).setDynamicColor(() -> iconColor);
            texture.draw(graphics, 0, 0, x, y, ACTION_ICON_SIZE, ACTION_ICON_SIZE);
            return;
        }
        int centerY = y + ACTION_ICON_SIZE / 2;
        graphics.fill(x + 2, centerY, x + ACTION_ICON_SIZE - 2, centerY + 1, iconColor);
    }

    private static WidgetGroup drawContextIcon(int x, int y, String icon, int color) {
        WidgetGroup g = new WidgetGroup(x, y, 10, 8);
        switch (icon) {
            case "rename" -> {
                g.addWidget(pixel(0, 6, 8, 1, color));
                g.addWidget(pixel(1, 4, 5, 1, color));
                g.addWidget(pixel(5, 1, 3, 3, color));
            }
            case "add" -> {
                g.addWidget(pixel(4, 1, 1, 6, color));
                g.addWidget(pixel(1, 4, 7, 1, color));
            }
            case "delete" -> {
                g.addWidget(pixel(1, 2, 6, 1, color));
                g.addWidget(pixel(2, 3, 4, 4, color));
            }
            case "copy" -> {
                g.addWidget(pixel(2, 1, 5, 5, color));
                g.addWidget(pixel(0, 3, 5, 5, color));
            }
            case "paste" -> {
                g.addWidget(pixel(2, 0, 4, 2, color));
                g.addWidget(pixel(1, 2, 6, 6, color));
                g.addWidget(pixel(3, 4, 3, 1, color));
            }
            case "icon" -> {
                g.addWidget(pixel(1, 1, 6, 6, color));
                g.addWidget(pixel(2, 2, 4, 4, color));
            }
            case "image" -> {
                g.addWidget(pixel(1, 1, 7, 6, color));
                g.addWidget(pixel(2, 5, 5, 1, color));
            }
            case "style" -> {
                g.addWidget(pixel(1, 1, 6, 1, color));
                g.addWidget(pixel(2, 3, 4, 1, color));
                g.addWidget(pixel(3, 5, 2, 1, color));
            }
            case "up" -> {
                g.addWidget(pixel(4, 1, 1, 6, color));
                g.addWidget(pixel(2, 3, 5, 1, color));
            }
            case "down" -> {
                g.addWidget(pixel(4, 1, 1, 6, color));
                g.addWidget(pixel(2, 5, 5, 1, color));
            }
            default -> g.addWidget(pixel(1, 3, 6, 1, color));
        }
        return g;
    }

    private static ResourceLocation contextIconLocation(String icon) {
        String key = contextIconFileKey(icon);
        ResourceLocation id = UiIconAtlas.icon("context_" + key);
        return id == null ? UiIconAtlas.icon(key) : id;
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
