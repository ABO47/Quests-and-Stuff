package com.abo47.questsandstuff.client.tablet.contextmenu;

import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawRectOutline;

import static com.abo47.questsandstuff.client.tablet.theme.render.Surfaces.withAlpha;


import com.abo47.questsandstuff.client.tablet.controls.TabletTextTextures;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.client.tablet.icons.SmoothResourceTexture;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.render.Surfaces;
import com.abo47.questsandstuff.client.tablet.theme.tokens.UiActionColors;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextTextureWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Locale;
import java.util.function.Consumer;

public final class ContextMenuRenderer {
    public static final int CONTEXT_MENU_WIDTH = 108;
    static final int OUTER_PAD = 4;
    private static final int ICON_X = 8;
    private static final int TEXT_X = 22;
    private static final int SUBMENU_PAD = 5;
    private static final int CONTEXT_ICON_SIZE = 10;
    private static final int FALLBACK_ICON_H = 8;
    private static final int TEXT_LINE_H = 9;
    private static final float TEXT_SCALE = 0.82f;

    private ContextMenuRenderer() {
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addWindowsContextRow(menu, y, width, text, icon, false, callback);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, boolean submenu, Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addWindowsContextRow(menu, y, width, text, icon, UiActionColors.forAction(text, icon, ModColors.INTERACTIVE), submenu, callback);
    }

    public static void addWindowsContextRow(WidgetGroup menu, int y, int width, String text, String icon, int iconColor, boolean submenu, Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        WidgetGroup rowBg = new WidgetGroup(4, y, width, UiThemeTokens.CONTEXT_ROW_H);
        rowBg.setBackground(Surfaces.fill(withAlpha(ModColors.SURFACE_PANEL_ALT, 84)));
        menu.addWidget(rowBg);

        int iconY = centeredY(y, UiThemeTokens.CONTEXT_ROW_H, CONTEXT_ICON_SIZE);
        if (!addAtlasIcon(menu, ICON_X, iconY, icon, iconColor)) {
            menu.addWidget(drawContextIcon(ICON_X, centeredY(y, UiThemeTokens.CONTEXT_ROW_H, FALLBACK_ICON_H), icon, iconColor));
        }

        int rightReserve = submenu ? CONTEXT_ICON_SIZE + SUBMENU_PAD + 2 : 2;
        addContextText(menu, y, width, text, rightReserve);

        if (submenu) {
            addContextIcon(menu, 4 + width - CONTEXT_ICON_SIZE - SUBMENU_PAD, iconY, "chevron-right", iconColor);
        }

        ButtonWidget hit = flatHitButton(4, y, width, UiThemeTokens.CONTEXT_ROW_H, callback);
        hit.setHoverTexture(Surfaces.bordered(withAlpha(ModColors.INTERACTIVE, 64), withAlpha(ModColors.BORDER_ACCENT, 220)));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 95)));
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
        Surfaces.fill(withAlpha(ModColors.SURFACE_BASE, 246)).draw(graphics, 0, 0, x, y, width, height);
        drawRectOutline(graphics, x, y, width, height, borderColor);
    }

    public static void drawVanillaContextRow(GuiGraphics graphics, int menuX, int rowY, int rowWidth, String text, String icon, boolean hovered) {
        drawVanillaContextRow(graphics, menuX, rowY, rowWidth, text, icon, UiActionColors.forAction(text, icon, ModColors.INTERACTIVE), hovered);
    }

    public static void drawVanillaContextRow(GuiGraphics graphics, int menuX, int rowY, int rowWidth, String text, String icon, int iconColor, boolean hovered) {
        int rowX = menuX + OUTER_PAD;
        int rowH = rowHeight();
        Surfaces.fill(withAlpha(ModColors.SURFACE_PANEL_ALT, 84)).draw(graphics, 0, 0, rowX, rowY, rowWidth, rowH);
        if (hovered) {
            Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 64)).draw(graphics, 0, 0, rowX, rowY, rowWidth, rowH);
            drawRectOutline(graphics, rowX, rowY, rowWidth, rowH, withAlpha(ModColors.BORDER_ACCENT, 220));
        }
        drawVanillaIcon(graphics, menuX + ICON_X, centeredY(rowY, rowH, CONTEXT_ICON_SIZE), icon, iconColor);
        drawScaledText(graphics, text == null ? "" : text, menuX + TEXT_X, rowY, rowH, ModColors.TEXT_PRIMARY);
    }

    public static void addContextIcon(WidgetGroup menu, int x, int y, String icon) {
        addContextIcon(menu, x, y, icon, UiActionColors.forAction("", icon, UiThemeManager.iconColor("context_" + contextIconFileKey(icon))));
    }

    public static void addContextIcon(WidgetGroup menu, int x, int y, String icon, int iconColor) {
        if (!addAtlasIcon(menu, x, y, icon, iconColor)) {
            menu.addWidget(drawContextIcon(x, y + Math.max(0, (CONTEXT_ICON_SIZE - FALLBACK_ICON_H) / 2), icon, iconColor));
        }
    }

    public static void addSeparator(WidgetGroup menu, int y, int width) {
        WidgetGroup sep = new WidgetGroup(4, y + 1, width, 1);
        sep.setBackground(Surfaces.fill(withAlpha(ModColors.BORDER_BASE, 120)));
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
        int content = Math.round(widest * TEXT_SCALE) + TEXT_X + 8;
        return Math.max(minWidth, Math.min(maxWidth, content));
    }

    private static boolean addAtlasIcon(WidgetGroup menu, int x, int y, String icon, int iconColor) {
        ResourceLocation id = contextIconLocation(icon);
        if (id == null) {
            return false;
        }
        var texture = new SmoothResourceTexture(id).setDynamicColor(() -> iconColor);
        menu.addWidget(new ImageWidget(x, y, CONTEXT_ICON_SIZE, CONTEXT_ICON_SIZE, texture));
        return true;
    }

    private static void drawVanillaIcon(GuiGraphics graphics, int x, int y, String icon, int iconColor) {
        ResourceLocation id = contextIconLocation(icon);
        if (id != null) {
            var texture = new SmoothResourceTexture(id).setDynamicColor(() -> iconColor);
            texture.draw(graphics, 0, 0, x, y, CONTEXT_ICON_SIZE, CONTEXT_ICON_SIZE);
            return;
        }
        int centerY = y + CONTEXT_ICON_SIZE / 2;
        Surfaces.fill(iconColor).draw(graphics, 0, 0, x + 2, centerY, CONTEXT_ICON_SIZE - 4, 1);
    }

    private static void addContextText(WidgetGroup menu, int rowY, int rowWidth, String text, int rightReserve) {
        int availableVisualW = Math.max(1, rowWidth - TEXT_X - rightReserve);
        int textureW = Math.max(1, Math.round(availableVisualW / TEXT_SCALE));
        String fullText = text == null ? "" : text;
        int textureY = rowY + Math.max(0, (UiThemeTokens.CONTEXT_ROW_H - TEXT_LINE_H) / 2);
        TextTextureWidget textWidget = TabletTextTextures.literal(TEXT_X, textureY, textureW, TEXT_LINE_H, fullText, ModColors.TEXT_PRIMARY, TextTexture.TextType.LEFT_HIDE);
        patchTextLines(textWidget.getTextTexture(), fullText, textureW);
        float xCompensation = -((1.0f - TEXT_SCALE) * textureW) / 2.0f;
        float yCompensation = (rowY + UiThemeTokens.CONTEXT_ROW_H / 2.0f) - (textureY + TEXT_LINE_H / 2.0f);
        textWidget.textureStyle(texture -> texture.scale(TEXT_SCALE).transform(xCompensation, yCompensation));
        menu.addWidget(textWidget);
    }

    private static void patchTextLines(TextTexture texture, String fullText, int maxWidth) {
        Font font = Minecraft.getInstance().font;
        if (font.width(fullText) <= maxWidth) {
            return;
        }
        texture.supplier = null;
        texture.text = fullText;
        int dotWidth = font.width("..");
        var truncated = font.substrByWidth(FormattedText.of(fullText), Math.max(0, maxWidth - dotWidth));
        try {
            java.lang.reflect.Field textsField = TextTexture.class.getDeclaredField("texts");
            textsField.setAccessible(true);
            textsField.set(texture, List.of(truncated.getString(), " "));
        } catch (Exception e) {
        }
    }

    private static void drawScaledText(GuiGraphics graphics, String text, int x, int rowY, int rowH, int color) {
        Font font = Minecraft.getInstance().font;
        float textY = rowY + (rowH - font.lineHeight * TEXT_SCALE) / 2.0f;
        graphics.pose().pushPose();
        graphics.pose().translate(x, textY, 0.0f);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1.0f);
        graphics.drawString(font, text, 0, 0, color, false);
        graphics.pose().popPose();
    }

    private static int centeredY(int y, int height, int contentHeight) {
        return y + Math.max(0, (height - contentHeight) / 2);
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
        ButtonWidget button = new ButtonWidget(x, y, w, h, Surfaces.transparentFill(), callback);
        button.setClientSideWidget();
        button.setHoverTexture(Surfaces.transparentFill());
        button.setClickedTexture(Surfaces.transparentFill());
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
