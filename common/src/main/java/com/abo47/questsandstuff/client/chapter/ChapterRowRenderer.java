package com.abo47.questsandstuff.client.chapter;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.CardDragGhosts;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

final class ChapterRowRenderer {
    private ChapterRowRenderer() {
    }

    static void addChapterRow(WidgetGroup chapterList, TabletUiState state, Runnable refresh, String group, int y, ChapterListMetrics.Layout layout, boolean collapsed) {
        boolean selected = group.equals(state.selectedGroup);
        String rowLabel = group.equals(state.pendingChapterRename) ? state.chapterDraftName : group;
        int baseFill = selected ? TabletUiFactory.withAlpha(ModColors.INTERACTIVE, 82) : ModColors.SURFACE_PANEL_ALT;
        int fill = TabletUiFactory.chapterBackgroundFill(ClientQuestCache.groupBackground(group), baseFill);
        int border = selected ? ModColors.BORDER_ACCENT : ModColors.BORDER_BASE;
        chapterList.addWidget(TabletUiFactory.panel(layout.cardX(), y, layout.cardW(), TabletUiFactory.CHAPTER_CARD_H, fill, border));

        IGuiTexture bgTexture = TabletUiFactory.chapterBackgroundTexture(ClientQuestCache.groupBackground(group));
        if (bgTexture != null) {
            chapterList.addWidget(new ImageWidget(layout.cardX() + 1, y + 1, layout.cardW() - 2, TabletUiFactory.CHAPTER_CARD_H - 2, bgTexture));
        }
        String groupIcon = ClientQuestCache.groupIcon(group);
        int iconDrawX = collapsed ? (layout.cardX() + Math.max(0, (layout.cardW() - 16) / 2)) : layout.iconX();
        if (groupIcon != null && !groupIcon.isBlank()) {
            chapterList.addWidget(new DisplayIconWidget(iconDrawX, y + 8, TabletUiFactory.CONTENT_ICON_SIZE, TabletUiFactory.CONTENT_ICON_SIZE, groupIcon));
        } else if (collapsed) {
            String initial = rowLabel == null || rowLabel.isBlank() ? "?" : rowLabel.substring(0, 1).toUpperCase();
            chapterList.addWidget(TabletUiFactory.label(iconDrawX + 5, y + 12, initial, ModColors.TEXT_PRIMARY));
        }
        int textW = Math.max(8, layout.cardW() - 26);
        if (!collapsed) {
            addChapterLabel(chapterList, group, rowLabel, y, layout.cardX(), textW);
        }
        addChapterSelectionHits(chapterList, state, refresh, group, y, layout, collapsed, textW);
        addChapterIconChangeHit(chapterList, state, refresh, group, iconDrawX, y + 8);
    }

    static void addRenameRow(WidgetGroup chapterList, TabletUiState state, Player player, Runnable refresh, String group, int y, ChapterListMetrics.Layout layout) {
        ChapterInlineRenameRows.add(
                chapterList,
                layout.cardX(),
                y,
                layout.cardW(),
                layout.iconX(),
                ClientQuestCache.groupIcon(group),
                () -> state.chapterDraftName,
                value -> state.chapterDraftName = ChapterRenameActions.sanitizeInlineTitle(value),
                value -> ChapterRenameActions.commitRename(player, state, refresh, group, value),
                () -> {
                    state.pendingChapterRename = "";
                    state.chapterDraftName = group;
                    refresh.run();
                }
        );
    }

    static void addDraftRow(WidgetGroup chapterList, TabletUiState state, Player player, Runnable refresh, int y, ChapterListMetrics.Layout layout) {
        ChapterInlineRenameRows.add(
                chapterList,
                layout.cardX(),
                y,
                layout.cardW(),
                layout.iconX(),
                "",
                () -> state.chapterDraftName,
                value -> state.chapterDraftName = ChapterRenameActions.sanitizeInlineTitle(value),
                value -> ChapterRenameActions.commitDraft(player, state, refresh, value),
                () -> {
                    state.pendingChapterRename = "";
                    state.chapterDraftName = state.selectedGroup;
                    refresh.run();
                }
        );
    }

    static int addGhostIfVisible(WidgetGroup chapterList, String group, int cardX, int y, int cardW, int listH, int rowStep) {
        if (y < listH && y + TabletUiFactory.CHAPTER_CARD_H > 0) {
            renderChapterGhost(chapterList, group, cardX, y, cardW);
        }
        return y + rowStep;
    }

    private static void addChapterLabel(WidgetGroup chapterList, String group, String rowLabel, int y, int cardX, int textW) {
        int textColor = ClientQuestCache.groupTextColor(group);
        String align = ClientQuestCache.groupTextAlign(group);
        String textStyle = ClientQuestCache.groupTextStyle(group);
        int textSize = ClientQuestCache.groupTextSize(group);
        String croppedLabel = SearchFilter.crop(rowLabel, 20);
        int labelW = chapterLabelWidth(croppedLabel, textStyle, textSize);
        int textX = switch (align) {
            case "left" -> cardX + 24;
            case "right" -> cardX + 24 + Math.max(0, textW - labelW);
            default -> cardX + 24 + Math.max(0, (textW - labelW) / 2);
        };
        chapterList.addWidget(chapterStyledLabel(textX, chapterTextY(y, textSize), croppedLabel, textColor, textStyle, textSize));
    }

    private static void addChapterSelectionHits(WidgetGroup chapterList, TabletUiState state, Runnable refresh, String group, int y, ChapterListMetrics.Layout layout, boolean collapsed, int textW) {
        final int rowY = y;
        chapterList.addWidget(TabletUiFactory.flatHitButton(collapsed ? layout.cardX() : layout.cardX() + 24, y + 8, collapsed ? layout.cardW() : textW, 16, click -> {
            selectChapter(state, group);
            state.chapterMenuTarget = group;
            state.chapterMenuX = 8;
            state.chapterMenuY = Math.max(4, Math.min(rowY, chapterList.getSize().height - 72));
            TabletUiFactory.persistUiState(state);
            refresh.run();
        }));
        chapterList.addWidget(TabletUiFactory.flatHitButton(layout.cardX(), y, layout.cardW(), TabletUiFactory.CHAPTER_CARD_H, click -> {
            selectChapter(state, group);
            TabletUiFactory.persistUiState(state);
            refresh.run();
        }));
    }

    private static void selectChapter(TabletUiState state, String group) {
        state.selectedGroup = group;
        state.groupDraft = group;
        state.chapterDraftName = group;
        state.pendingChapterRename = "";
        state.chapterTextMenuOpen = false;
        state.chapterTextMenuTarget = "";
        state.chapterTextFontSizeSliderTarget = "";
    }

    private static WidgetGroup chapterStyledLabel(int x, int y, String text, int color, String style, int fontSize) {
        Component component = styledChapterComponent(text, style);
        float scale = chapterFontScale(fontSize);
        int width = Math.max(1, Math.round(Minecraft.getInstance().font.width(component) * scale));
        int height = Math.max(1, Math.round(Minecraft.getInstance().font.lineHeight * scale));
        return new WidgetGroup(x, y, width, height) {
            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                graphics.pose().pushPose();
                graphics.pose().translate(getPositionX(), getPositionY(), 0.0f);
                graphics.pose().scale(scale, scale, 1.0f);
                graphics.drawString(Minecraft.getInstance().font, component, 0, 0, color, false);
                graphics.pose().popPose();
            }
        };
    }

    private static void renderChapterGhost(WidgetGroup chapterList, String group, int x, int y, int w) {
        CardDragGhosts.renderChapter(
                chapterList,
                x,
                y,
                w,
                TabletUiFactory.chapterBackgroundTexture(ClientQuestCache.groupBackground(group)),
                ClientQuestCache.groupIcon(group),
                group
        );
    }

    private static int chapterLabelWidth(String text, String style, int fontSize) {
        Component component = styledChapterComponent(text, style);
        return Math.max(1, Math.round(Minecraft.getInstance().font.width(component) * chapterFontScale(fontSize)));
    }

    private static int chapterTextY(int rowY, int fontSize) {
        int labelH = Math.max(1, Math.round(Minecraft.getInstance().font.lineHeight * chapterFontScale(fontSize)));
        return rowY + Math.max(2, (TabletUiFactory.CHAPTER_CARD_H - labelH) / 2);
    }

    private static float chapterFontScale(int fontSize) {
        int clamped = Math.max(CanvasTextLayer.MIN_FONT_SIZE, Math.min(18, fontSize));
        return Math.max(0.5f, clamped / (float) CanvasTextLayer.DEFAULT_FONT_SIZE);
    }

    private static Component styledChapterComponent(String text, String style) {
        String safe = text == null ? "" : text;
        return switch (CanvasTextLayer.normalizeStyle(style)) {
            case "bold" -> Component.literal(safe).withStyle(ChatFormatting.BOLD);
            case "italic" -> Component.literal(safe).withStyle(ChatFormatting.ITALIC);
            case "bold_italic" -> Component.literal(safe).withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC);
            default -> Component.literal(safe);
        };
    }

    private static void addChapterIconChangeHit(WidgetGroup parent, TabletUiState state, Runnable refresh, String group, int x, int y) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        EntityIconControls.addChangeIconHit(parent, state, refresh, x, y, TabletUiFactory.CONTENT_ICON_SIZE, () -> {
            selectChapter(state, group);
            state.chapterMenuOpen = false;
            state.chapterDragPending = false;
            state.chapterDragActive = false;
            state.chapterDragName = "";
            state.chapterDragTargetIndex = -1;
            state.modalChapterTarget = group;
            state.modalQuestTarget = "";
            state.modalCanvasBackgroundTarget = "";
            state.modalCanvasImageTarget = "";
            state.modalCanvasEntityTarget = "";
            state.iconSearch = "";
            state.iconTagMode = false;
            state.iconAllItemsMode = false;
            state.iconEntityMode = false;
            state.iconScroll = 0;
            EntityIconControls.openIconPicker(state, EntityIconControls.IconPickerTarget.chapter(group));
            TabletUiFactory.persistUiState(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter icon picker open target={}", group);
        });
    }
}
