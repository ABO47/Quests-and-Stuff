package com.abo47.questsandstuff.client.tablet.quest.group;

import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientChapterState;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorGroupCommandClient;
import com.abo47.questsandstuff.client.tablet.controls.TextStyleButtons;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

final class GroupTextStyleMenu {
    private GroupTextStyleMenu() {
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
        String align = ClientQuestCache.groupTextAlign(target);
        String style = ClientQuestCache.groupTextStyle(target);
        int fontSize = ClientQuestCache.groupTextSize(target);
        boolean bold = CanvasTextLayer.hasStyleFlag(style, "bold");
        boolean italic = CanvasTextLayer.hasStyleFlag(style, "italic");
        int columns = TextStyleButtons.columnsForWidth(fw);

        addIconToggleButton(floating, 0, fw, columns, "style_align_left", alignButtonBase(align, "left"), click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text align target={} align=left", target);
            EditorGroupCommandClient.runGroupAction(player, state, "set_text_align", target, "left", 0);
            refresh.run();
        });
        addIconToggleButton(floating, 1, fw, columns, "style_align_center", alignButtonBase(align, "center"), click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text align target={} align=center", target);
            EditorGroupCommandClient.runGroupAction(player, state, "set_text_align", target, "center", 0);
            refresh.run();
        });
        addIconToggleButton(floating, 2, fw, columns, "style_align_right", alignButtonBase(align, "right"), click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text align target={} align=right", target);
            EditorGroupCommandClient.runGroupAction(player, state, "set_text_align", target, "right", 0);
            refresh.run();
        });
        addIconToggleButton(floating, 3, fw, columns, "style_color", ModColors.SURFACE_PANEL_ALT, ClientQuestCache.groupTextColor(target), click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text color open picker target={}", target);
            ModalOpenActions.openColorPicker(state, target, ClientQuestCache.groupTextColor(target));
            refresh.run();
        });

        addIconToggleButton(floating, 4, fw, columns, "context_style", toggleButtonBase(!bold && !italic), click -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style=normal", target);
            EditorGroupCommandClient.runGroupAction(player, state, "set_text_style", target, "normal", 0);
            refresh.run();
        });
        addIconToggleButton(floating, 5, fw, columns, "style_bold", toggleButtonBase(bold), click -> {
            String nextStyle = CanvasTextLayer.styleFromFlags(!bold, italic);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style={}", target, nextStyle);
            EditorGroupCommandClient.runGroupAction(player, state, "set_text_style", target, nextStyle, 0);
            refresh.run();
        });
        addIconToggleButton(floating, 6, fw, columns, "style_italic", toggleButtonBase(italic), click -> {
            String nextStyle = CanvasTextLayer.styleFromFlags(bold, !italic);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style={}", target, nextStyle);
            EditorGroupCommandClient.runGroupAction(player, state, "set_text_style", target, nextStyle, 0);
            refresh.run();
        });
        addFontSizeControl(floating, state, player, target, fw, columns, fontSize, refresh);
        overlay.addWidget(floating);
    }

    private static int alignButtonBase(String currentAlign, String option) {
        String current = ClientChapterState.normalizeTextAlign(currentAlign);
        return current.equals(option) ? ModColors.SUCCESS : ModColors.SURFACE_PANEL_ALT;
    }

    private static int toggleButtonBase(boolean active) {
        return active ? ModColors.SUCCESS : ModColors.SURFACE_PANEL_ALT;
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
            TextStyleButtons.addFontSizeField(parent, 7, menuWidth, columns, fontSize, value -> {
                QuestsAndStuffMod.debugLog("[QnS:UI] chapter text size target={} size={}", target, value);
                EditorGroupCommandClient.runGroupAction(player, state, "set_text_size", target, String.valueOf(value), 0);
            }, () -> closeFontSizeField(state, refresh), () -> closeFontSizeField(state, refresh), () -> closeFontSizeField(state, refresh));
            return;
        }
        int baseColor = open || fontSize != CanvasTextLayer.DEFAULT_FONT_SIZE ? ModColors.SUCCESS : ModColors.SURFACE_PANEL_ALT;
        addIconToggleButton(parent, 7, menuWidth, columns, "size", baseColor, null, new Component[]{
                Component.literal("Font size: " + fontSize)
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

}
