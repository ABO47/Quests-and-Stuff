package com.abo47.questsandstuff.client.tablet.quest.chapter;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientChapterState;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.TextStyleButtons;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorChapterCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

final class ChapterTextStyleMenu {
    private ChapterTextStyleMenu() {
    }

    static void render(WidgetGroup overlay, TabletUiState state, Player player, Runnable refresh) {
        if (!state.chapterPanel.chapterTextMenuOpen || state.chapterPanel.chapterTextMenuTarget.isBlank()) {
            return;
        }
        int listHeight = state.chapterPanel.chapterListHeight > 0 ? state.chapterPanel.chapterListHeight : TabletUiFactory.chapterHeight(state) - 12;
        int fy = TabletUiFactory.chapterTextMenuY(state, listHeight);
        int fx = TabletUiFactory.chapterTextMenuX(state);
        int fw = TabletUiFactory.chapterTextMenuWidth(state);
        if (fw <= 0) {
            return;
        }
        int menuH = TextStyleButtons.menuHeightForWidth(fw);
        int overlayX = TabletUiFactory.CHAPTER_X + state.chapterPanel.chapterListOriginX + fx;
        int overlayY = TabletUiFactory.CHAPTER_Y + state.chapterPanel.chapterListOriginY + fy;
        WidgetGroup floating = TextStyleButtons.shell(overlayX, overlayY, fw, menuH, click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text menu internal click target={}", state.chapterPanel.chapterTextMenuTarget);
            refresh.run();
        });

        String target = state.chapterPanel.chapterTextMenuTarget;
        String align = ClientQuestStateFacade.chapterTextAlign(target);
        String style = ClientQuestStateFacade.chapterTextStyle(target);
        int fontSize = ClientQuestStateFacade.chapterTextSize(target);
        boolean bold = CanvasTextLayer.hasStyleFlag(style, "bold");
        boolean italic = CanvasTextLayer.hasStyleFlag(style, "italic");
        boolean underline = CanvasTextLayer.hasStyleFlag(style, "underline");
        boolean strikethrough = CanvasTextLayer.hasStyleFlag(style, "strikethrough");
        boolean quote = CanvasTextLayer.hasStyleFlag(style, "quote");
        boolean spoiler = CanvasTextLayer.hasStyleFlag(style, "spoiler");
        int columns = TextStyleButtons.columnsForWidth(fw);
        Component tooltipLeft = tooltip(TabletTranslationKeys.STYLE_TOOLTIP_ALIGN_LEFT);
        Component tooltipCenter = tooltip(TabletTranslationKeys.STYLE_TOOLTIP_ALIGN_CENTER);
        Component tooltipRight = tooltip(TabletTranslationKeys.STYLE_TOOLTIP_ALIGN_RIGHT);
        Component tooltipColor = tooltip(TabletTranslationKeys.STYLE_TOOLTIP_COLOR);
        Component tooltipBold = tooltip(TabletTranslationKeys.STYLE_TOOLTIP_BOLD);
        Component tooltipItalic = tooltip(TabletTranslationKeys.STYLE_TOOLTIP_ITALIC);
        Component tooltipUnderline = tooltip(TabletTranslationKeys.STYLE_TOOLTIP_UNDERLINE);
        Component tooltipStrikethrough = tooltip(TabletTranslationKeys.STYLE_TOOLTIP_STRIKETHROUGH);
        Component tooltipReset = tooltip(TabletTranslationKeys.STYLE_TOOLTIP_RESET);

        addIconToggleButton(floating, 0, fw, columns, "style_align_left", alignButtonBase(align, "left"), null, new Component[]{tooltipLeft}, click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text align target={} align=left", target);
            EditorChapterCommandClient.runChapterAction(player, state, "set_text_align", target, "left", 0);
            refresh.run();
        });
        addIconToggleButton(floating, 1, fw, columns, "style_align_center", alignButtonBase(align, "center"), null, new Component[]{tooltipCenter}, click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text align target={} align=center", target);
            EditorChapterCommandClient.runChapterAction(player, state, "set_text_align", target, "center", 0);
            refresh.run();
        });
        addIconToggleButton(floating, 2, fw, columns, "style_align_right", alignButtonBase(align, "right"), null, new Component[]{tooltipRight}, click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text align target={} align=right", target);
            EditorChapterCommandClient.runChapterAction(player, state, "set_text_align", target, "right", 0);
            refresh.run();
        });
        addIconToggleButton(floating, 3, fw, columns, "style_color", TabletColors.SURFACE_PANEL_ALT, ClientQuestStateFacade.chapterTextColor(target), new Component[]{tooltipColor}, click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text color open picker target={}", target);
            ModalOpenActions.openColorPicker(state, target, ClientQuestStateFacade.chapterTextColor(target));
            refresh.run();
        });

        addIconToggleButton(floating, 4, fw, columns, "style_bold", toggleButtonBase(bold), null, new Component[]{tooltipBold}, click -> {
            String nextStyle = CanvasTextLayer.toggleStyleFlag(style, "bold");
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style={}", target, nextStyle);
            EditorChapterCommandClient.runChapterAction(player, state, "set_text_style", target, nextStyle, 0);
            refresh.run();
        });
        addIconToggleButton(floating, 5, fw, columns, "style_italic", toggleButtonBase(italic), null, new Component[]{tooltipItalic}, click -> {
            String nextStyle = CanvasTextLayer.toggleStyleFlag(style, "italic");
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style={}", target, nextStyle);
            EditorChapterCommandClient.runChapterAction(player, state, "set_text_style", target, nextStyle, 0);
            refresh.run();
        });
        addIconToggleButton(floating, 7, fw, columns, "style_underline", toggleButtonBase(underline), null, new Component[]{tooltipUnderline}, click -> {
            String nextStyle = CanvasTextLayer.toggleStyleFlag(style, "underline");
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style={}", target, nextStyle);
            EditorChapterCommandClient.runChapterAction(player, state, "set_text_style", target, nextStyle, 0);
            refresh.run();
        });
        addIconToggleButton(floating, 8, fw, columns, "style_strikethrough", toggleButtonBase(strikethrough), null, new Component[]{tooltipStrikethrough}, click -> {
            String nextStyle = CanvasTextLayer.toggleStyleFlag(style, "strikethrough");
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style={}", target, nextStyle);
            EditorChapterCommandClient.runChapterAction(player, state, "set_text_style", target, nextStyle, 0);
            refresh.run();
        });
        addIconToggleButton(floating, 9, fw, columns, "context_style", toggleButtonBase(!bold && !italic && !underline && !strikethrough && !quote && !spoiler), null, new Component[]{tooltipReset}, click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style=normal", target);
            EditorChapterCommandClient.runChapterAction(player, state, "set_text_style", target, "normal", 0);
            refresh.run();
        });
        addFontSizeControl(floating, state, player, target, fw, columns, fontSize, refresh);
        overlay.addWidget(floating);
    }

    private static int alignButtonBase(String currentAlign, String option) {
        String current = ClientChapterState.normalizeTextAlign(currentAlign);
        return current.equals(option) ? TabletColors.SUCCESS : TabletColors.SURFACE_PANEL_ALT;
    }

    private static int toggleButtonBase(boolean active) {
        return active ? TabletColors.SUCCESS : TabletColors.SURFACE_PANEL_ALT;
    }

    private static void addIconToggleButton(WidgetGroup parent, int index, int menuWidth, int columns, String iconName, int baseColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addIconToggleButton(parent, index, menuWidth, columns, iconName, baseColor, null, null, callback);
    }

    private static void addIconToggleButton(WidgetGroup parent, int index, int menuWidth, int columns, String iconName, int baseColor, Integer iconTint, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addIconToggleButton(parent, index, menuWidth, columns, iconName, baseColor, iconTint, null, callback);
    }

    private static void addIconToggleButton(WidgetGroup parent, int index, int menuWidth, int columns, String iconName, int baseColor, Integer iconTint, Component[] tooltips, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        TextStyleButtons.addTool(parent, index, menuWidth, columns, iconName, baseColor, iconTint, tooltips, callback);
    }

    private static void addFontSizeControl(WidgetGroup parent, TabletUiState state, Player player, String target, int menuWidth, int columns, int fontSize, Runnable refresh) {
        boolean open = target.equals(state.chapterPanel.chapterTextFontSizeFieldTarget);
        if (open) {
            TextStyleButtons.addFontSizeField(parent, 6, menuWidth, columns, fontSize, value -> {
                QuestsAndStuffMod.debugLog("[QnS:UI] chapter text size target={} size={}", target, value);
                EditorChapterCommandClient.runChapterAction(player, state, "set_text_size", target, String.valueOf(value), 0);
            }, () -> closeFontSizeField(state, refresh), () -> closeFontSizeField(state, refresh), () -> closeFontSizeField(state, refresh));
            return;
        }
        int baseColor = open || fontSize != CanvasTextLayer.DEFAULT_FONT_SIZE ? TabletColors.SUCCESS : TabletColors.SURFACE_PANEL_ALT;
        addIconToggleButton(parent, 6, menuWidth, columns, "size", baseColor, null, new Component[]{
                Component.translatable(TabletTranslationKeys.STYLE_TOOLTIP_FONT_SIZE).append(Component.literal(": " + fontSize))
        }, click -> {
            state.chapterPanel.chapterTextFontSizeFieldTarget = target;
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text font-size field target={} open=true", target);
            refresh.run();
        });
    }

    private static void closeFontSizeField(TabletUiState state, Runnable refresh) {
        state.chapterPanel.chapterTextFontSizeFieldTarget = "";
        refresh.run();
    }

    private static Component tooltip(String key) {
        return Component.translatable(key);
    }

}
