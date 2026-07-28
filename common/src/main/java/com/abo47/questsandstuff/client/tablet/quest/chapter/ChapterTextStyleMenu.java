package com.abo47.questsandstuff.client.tablet.quest.chapter;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientChapterState;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.TextStyleButtons;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorChapterCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
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
        int columns = TextStyleButtons.columnsForWidth(fw);
        String target = state.chapterPanel.chapterTextMenuTarget;
        String align = ClientChapterState.normalizeTextAlign(ClientQuestStateFacade.chapterTextAlign(target));
        String style = ClientQuestStateFacade.chapterTextStyle(target);
        int textColor = ClientQuestStateFacade.chapterTextColor(target);
        boolean bold = CanvasTextLayer.hasStyleFlag(style, "bold");
        boolean italic = CanvasTextLayer.hasStyleFlag(style, "italic");
        boolean underline = CanvasTextLayer.hasStyleFlag(style, "underline");
        boolean strikethrough = CanvasTextLayer.hasStyleFlag(style, "strikethrough");
        int fontSize = ClientQuestStateFacade.chapterTextSize(target);
        boolean fontSizeFieldOpen = target.equals(state.chapterPanel.chapterTextFontSizeFieldTarget);

        QuestsAndStuffMod.debugLog("[QnS:UI] chapter text menu render x={} y={} w={} h={} cols={} align={} style={} target={}",
                overlayX, overlayY, fw, menuH, columns, align, style, target);
        int clickId = 0;
        TextStyleButtons.renderStyleMenu(
                overlay, overlayX, overlayY, fw, menuH, columns,
                align, textColor,
                bold, italic, underline, strikethrough, false, false,
                fontSize, fontSizeFieldOpen, false,
                TabletColors.SUCCESS,
                traceC(++clickId, "align_left", () -> {
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text align target={} align=left", target);
                    EditorChapterCommandClient.runChapterAction(player, state, "set_text_align", target, "left", 0);
                    refresh.run();
                }),
                traceC(++clickId, "align_center", () -> {
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text align target={} align=center", target);
                    EditorChapterCommandClient.runChapterAction(player, state, "set_text_align", target, "center", 0);
                    refresh.run();
                }),
                traceC(++clickId, "align_right", () -> {
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text align target={} align=right", target);
                    EditorChapterCommandClient.runChapterAction(player, state, "set_text_align", target, "right", 0);
                    refresh.run();
                }),
                traceC(++clickId, "color_open", () -> {
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text color open picker target={}", target);
                    ModalOpenActions.openColorPicker(state, target, textColor);
                    refresh.run();
                }),
                traceC(++clickId, "bold", () -> {
                    String nextStyle = CanvasTextLayer.toggleStyleFlag(style, "bold");
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style={}", target, nextStyle);
                    EditorChapterCommandClient.runChapterAction(player, state, "set_text_style", target, nextStyle, 0);
                    refresh.run();
                }),
                traceC(++clickId, "italic", () -> {
                    String nextStyle = CanvasTextLayer.toggleStyleFlag(style, "italic");
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style={}", target, nextStyle);
                    EditorChapterCommandClient.runChapterAction(player, state, "set_text_style", target, nextStyle, 0);
                    refresh.run();
                }),
                traceC(++clickId, "underline", () -> {
                    String nextStyle = CanvasTextLayer.toggleStyleFlag(style, "underline");
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style={}", target, nextStyle);
                    EditorChapterCommandClient.runChapterAction(player, state, "set_text_style", target, nextStyle, 0);
                    refresh.run();
                }),
                traceC(++clickId, "strikethrough", () -> {
                    String nextStyle = CanvasTextLayer.toggleStyleFlag(style, "strikethrough");
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style={}", target, nextStyle);
                    EditorChapterCommandClient.runChapterAction(player, state, "set_text_style", target, nextStyle, 0);
                    refresh.run();
                }),
                null,
                null,
                traceC(++clickId, "font_size_open", () -> {
                    state.chapterPanel.chapterTextFontSizeFieldTarget = target;
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text font-size field target={} open=true", target);
                    refresh.run();
                }),
                value -> {
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text size target={} size={}", target, value);
                    EditorChapterCommandClient.runChapterAction(player, state, "set_text_size", target, String.valueOf(value), 0);
                },
                null,
                traceC(++clickId, "font_size_commit", () -> closeFontSizeField(state, refresh)),
                traceC(++clickId, "font_size_cancel", () -> closeFontSizeField(state, refresh)),
                traceC(++clickId, "font_size_blur", () -> closeFontSizeField(state, refresh)),
                null,
                traceC(++clickId, "clear", () -> {
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text style target={} style=normal", target);
                    EditorChapterCommandClient.runChapterAction(player, state, "set_text_style", target, "normal", 0);
                    refresh.run();
                }),
                traceC(++clickId, "shell_bg", () -> {
                    QuestsAndStuffMod.debugLog("[QnS:UI] chapter text menu internal click target={}", target);
                    refresh.run();
                })
        );
    }

    private static Runnable traceC(int id, String name, Runnable action) {
        return () -> {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter style CLICK_{} #{} entered", name, id);
            action.run();
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter style CLICK_{} #{} exited", name, id);
        };
    }

    private static void closeFontSizeField(TabletUiState state, Runnable refresh) {
        state.chapterPanel.chapterTextFontSizeFieldTarget = "";
        refresh.run();
    }

}
