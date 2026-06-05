package com.abo47.questsandstuff.client.tablet.quest.chapter;

import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientChapterState;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.controls.FontSizeSliderWidget;
import com.abo47.questsandstuff.client.tablet.icons.SmoothResourceTexture;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ACTION_ICON_SIZE;

final class ChapterTextStyleMenu {
    private ChapterTextStyleMenu() {
    }

    static void render(WidgetGroup chapterList, TabletUiState state, Player player, Runnable refresh) {
        if (!state.chapterTextMenuOpen || state.chapterTextMenuTarget.isBlank()) {
            return;
        }
        int fy = TabletUiFactory.chapterTextMenuY(state, chapterList.getSize().height);
        int fx = TabletUiFactory.chapterTextMenuX(state);
        int fw = Math.min(chapterList.getSize().width - fx - 1, TabletUiFactory.chapterTextMenuWidth(state));
        int menuH = TabletUiFactory.chapterTextMenuHeight(state);
        WidgetGroup floating = TabletUiFactory.panel(
                fx,
                fy,
                fw,
                menuH,
                TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 244),
                ModColors.BORDER_ACCENT
        );
        floating.addWidget(TabletUiFactory.panel(
                1,
                1,
                fw - 2,
                menuH - 2,
                TabletUiFactory.withAlpha(ModColors.SURFACE_PANEL_ALT, 192),
                ModColors.BORDER_BASE
        ));
        floating.addWidget(TabletUiFactory.flatHitButton(0, 0, fw, menuH, click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text menu internal click target={}", state.chapterTextMenuTarget);
            refresh.run();
        }));

        String target = state.chapterTextMenuTarget;
        String align = ClientQuestCache.groupTextAlign(target);
        String style = ClientQuestCache.groupTextStyle(target);
        int fontSize = ClientQuestCache.groupTextSize(target);
        boolean bold = CanvasTextLayer.hasStyleFlag(style, "bold");
        boolean italic = CanvasTextLayer.hasStyleFlag(style, "italic");
        int toolCount = 8;
        boolean wrap = fw < 132;
        int firstRowCount = wrap ? 4 : toolCount;
        int secondRowCount = wrap ? toolCount - firstRowCount : 0;
        int btnW = wrap ? 16 : Math.min(16, Math.max(12, (fw - 4 - (toolCount - 1)) / toolCount));
        int topY = 2;
        int bottomY = 20;
        int[] topXs = distributedXs(fw, btnW, firstRowCount);
        int[] bottomXs = distributedXs(fw, btnW, Math.max(1, secondRowCount));

        addIconToggleButton(floating, topXs[0], topY, btnW, 16, "style_align_left", alignButtonBase(align, "left"), click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text align target={} align=left", target);
            EditorCommandClient.runGroupAction(player, state, "set_text_align", target, "left", 0);
            refresh.run();
        });
        addIconToggleButton(floating, topXs[1], topY, btnW, 16, "style_align_center", alignButtonBase(align, "center"), click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text align target={} align=center", target);
            EditorCommandClient.runGroupAction(player, state, "set_text_align", target, "center", 0);
            refresh.run();
        });
        addIconToggleButton(floating, topXs[2], topY, btnW, 16, "style_align_right", alignButtonBase(align, "right"), click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text align target={} align=right", target);
            EditorCommandClient.runGroupAction(player, state, "set_text_align", target, "right", 0);
            refresh.run();
        });
        addIconToggleButton(floating, topXs[3], topY, btnW, 16, "style_color", ModColors.SURFACE_PANEL_ALT, ClientQuestCache.groupTextColor(target), click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text color open picker target={}", target);
            ModalOpenActions.openColorPicker(state, target, ClientQuestCache.groupTextColor(target));
            refresh.run();
        });

        int styleY = wrap ? bottomY : topY;
        int normalX = wrap ? bottomXs[0] : topXs[4];
        int boldX = wrap ? bottomXs[1] : topXs[5];
        int italicX = wrap ? bottomXs[2] : topXs[6];
        int sizeX = wrap ? bottomXs[3] : topXs[7];
        addIconToggleButton(floating, normalX, styleY, btnW, 16, "context_style", toggleButtonBase(!bold && !italic), click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style=normal", target);
            EditorCommandClient.runGroupAction(player, state, "set_text_style", target, "normal", 0);
            refresh.run();
        });
        addIconToggleButton(floating, boldX, styleY, btnW, 16, "style_bold", toggleButtonBase(bold), click -> {
            String nextStyle = CanvasTextLayer.styleFromFlags(!bold, italic);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style={}", target, nextStyle);
            EditorCommandClient.runGroupAction(player, state, "set_text_style", target, nextStyle, 0);
            refresh.run();
        });
        addIconToggleButton(floating, italicX, styleY, btnW, 16, "style_italic", toggleButtonBase(italic), click -> {
            String nextStyle = CanvasTextLayer.styleFromFlags(bold, !italic);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style={}", target, nextStyle);
            EditorCommandClient.runGroupAction(player, state, "set_text_style", target, nextStyle, 0);
            refresh.run();
        });
        addFontSizeControl(floating, state, target, sizeX, styleY, btnW, 16, fontSize, refresh);
        chapterList.addWidget(floating);
    }

    static void renderFontSizePopover(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        if (!TabletUiFactory.isChapterFontSizeSliderOpen(state)) {
            return;
        }
        String target = state.chapterTextMenuTarget;
        int[] bounds = TabletUiFactory.chapterTextFontSizeSliderBounds(state);
        int x = TabletUiFactory.CHAPTER_X + state.chapterListOriginX + bounds[0];
        int y = TabletUiFactory.CHAPTER_Y + state.chapterListOriginY + bounds[1];
        addFontSizePopover(overlay, state, player, target, x, y, bounds[2], ClientQuestCache.groupTextSize(target), refresh);
    }

    private static int alignButtonBase(String currentAlign, String option) {
        String current = ClientChapterState.normalizeTextAlign(currentAlign);
        return current.equals(option) ? ModColors.SUCCESS : ModColors.SURFACE_PANEL_ALT;
    }

    private static int toggleButtonBase(boolean active) {
        return active ? ModColors.SUCCESS : ModColors.SURFACE_PANEL_ALT;
    }

    private static int[] distributedXs(int width, int buttonWidth, int count) {
        int safeCount = Math.max(1, count);
        int[] xs = new int[safeCount];
        if (safeCount == 1) {
            xs[0] = Math.max(2, (width - buttonWidth) / 2);
            return xs;
        }
        int usable = Math.max(0, width - 4 - buttonWidth);
        for (int i = 0; i < safeCount; i++) {
            xs[i] = 2 + Math.round((float) usable * ((float) i / (float) (safeCount - 1)));
        }
        return xs;
    }

    private static void addIconToggleButton(WidgetGroup parent, int x, int y, int w, int h, String iconName, int baseColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addIconToggleButton(parent, x, y, w, h, iconName, baseColor, null, null, callback);
    }

    private static void addIconToggleButton(WidgetGroup parent, int x, int y, int w, int h, String iconName, int baseColor, Integer iconTint, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addIconToggleButton(parent, x, y, w, h, iconName, baseColor, iconTint, null, callback);
    }

    private static void addIconToggleButton(WidgetGroup parent, int x, int y, int w, int h, String iconName, int baseColor, Integer iconTint, Component[] tooltips, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        int fill = baseColor == ModColors.SURFACE_PANEL_ALT
                ? TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 40)
                : TabletUiFactory.withAlpha(baseColor, 190);
        WidgetGroup bg = new WidgetGroup(x, y, w, h);
        bg.setBackground(Surfaces.fill(fill));
        parent.addWidget(bg);
        var texture = iconTint == null ? UiIconAtlas.iconTexture(iconName) : tintedIconTexture(iconName, iconTint);
        if (texture != null) {
            int iconSize = Math.min(ACTION_ICON_SIZE, Math.max(8, Math.min(w - 4, h - 4)));
            int iconX = x + (w - iconSize) / 2;
            int iconY = y + (h - iconSize) / 2;
            parent.addWidget(new ImageWidget(iconX, iconY, iconSize, iconSize, texture));
        }
        ButtonWidget hit = TabletUiFactory.flatHitButton(x, y, w, h, callback);
        hit.setHoverTexture(Surfaces.fill(TabletUiFactory.withAlpha(ModColors.INTERACTIVE, 70)));
        hit.setClickedTexture(Surfaces.fill(TabletUiFactory.withAlpha(ModColors.INTERACTIVE, 100)));
        if (tooltips != null) {
            hit.setHoverTooltips(tooltips);
        }
        parent.addWidget(hit);
    }

    private static void addFontSizeControl(WidgetGroup parent, TabletUiState state, String target, int x, int y, int w, int h, int fontSize, Runnable refresh) {
        boolean open = target.equals(state.chapterTextFontSizeSliderTarget);
        int baseColor = open || fontSize != CanvasTextLayer.DEFAULT_FONT_SIZE ? ModColors.SUCCESS : ModColors.SURFACE_PANEL_ALT;
        addIconToggleButton(parent, x, y, w, h, "size", baseColor, null, new Component[]{
                Component.literal("Font size: " + fontSize)
        }, click -> {
            state.chapterTextFontSizeSliderTarget = open ? "" : target;
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text font-size slider target={} open={}", target, !open);
            refresh.run();
        });
    }

    private static void addFontSizePopover(WidgetGroup parent, TabletUiState state, Player player, String target, int x, int y, int width, int fontSize, Runnable refresh) {
        int popoverW = Math.max(1, width);
        WidgetGroup popover = TabletUiFactory.panel(
                x,
                y,
                popoverW,
                TabletUiFactory.FONT_SIZE_SLIDER_POPOVER_H,
                TabletUiFactory.withAlpha(ModColors.SURFACE_BASE, 246),
                ModColors.BORDER_ACCENT
        );
        popover.addWidget(new FontSizeSliderWidget(
                0,
                0,
                popoverW,
                TabletUiFactory.FONT_SIZE_SLIDER_POPOVER_H,
                CanvasTextLayer.MIN_FONT_SIZE,
                18,
                fontSize,
                value -> {
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text size target={} size={}", target, value);
                    EditorCommandClient.runGroupAction(player, state, "set_text_size", target, String.valueOf(value), 0);
                },
                refresh
        ));
        parent.addWidget(popover);
    }

    private static ResourceTexture tintedIconTexture(String iconName, int argbColor) {
        ResourceLocation id = UiIconAtlas.icon(iconName);
        return id == null ? null : new SmoothResourceTexture(id).setColor(argbColor);
    }
}
