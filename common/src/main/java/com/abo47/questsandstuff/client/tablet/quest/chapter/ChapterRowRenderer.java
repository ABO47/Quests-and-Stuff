package com.abo47.questsandstuff.client.tablet.quest.chapter;

import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.CardDragGhosts;
import com.abo47.questsandstuff.client.tablet.controls.TabletTextTextures;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorChapterCommandClient;
import com.abo47.questsandstuff.client.tablet.controls.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.TextTextureWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

final class ChapterRowRenderer {
    private static final int COLLAPSED_TILE_SIZE = 28;
    private static final int COLLAPSED_ICON_SIZE = 16;
    private static final int NOTICE_ICON_SIZE = 9;
    private static final int FONT_LINE_HEIGHT = 9;

    private ChapterRowRenderer() {
    }

    static void addChapterRow(WidgetGroup chapterList, TabletUiState state, Runnable refresh, String chapter, int y, ChapterListMetrics.Layout layout, boolean collapsed) {
        boolean lockedPreview = ClientQuestStateFacade.chapterLockedPreview(chapter);
        boolean selected = chapter.equals(TabletStateQueries.selectedChapterName(state));
        String rowLabel = chapter.equals(state.canvas.pendingChapterRename) ? state.chapterPanel.chapterDraftName : chapter;
        if (collapsed) {
            addCollapsedChapterRow(chapterList, chapter, rowLabel, y, layout, selected);
            if (lockedPreview) {
                renderLockedFilter(chapterList, layout.cardX(), collapsedTileY(y), layout.cardW(), COLLAPSED_TILE_SIZE);
            }
            addCompletionNotice(chapterList, chapter, collapsedIconX(layout) - 2, collapsedIconY(y) - 5);
            addChapterSelectionHits(chapterList, state, refresh, chapter, y, layout, true, layout.cardW());
            addChapterIconChangeHit(chapterList, state, refresh, chapter, collapsedIconX(layout), collapsedIconY(y));
            return;
        }

        int fill = TabletUiFactory.chapterBackgroundFill(ClientQuestStateFacade.chapterBackground(chapter), TabletColors.SURFACE_PANEL_ALT);
        int border = selected ? TabletColors.BORDER_ACCENT : TabletColors.BORDER_BASE;
        chapterList.addWidget(TabletUiFactory.panel(layout.cardX(), y, layout.cardW(), TabletUiFactory.CHAPTER_CARD_H, fill, border));

        IGuiTexture bgTexture = TabletUiFactory.chapterBackgroundTexture(ClientQuestStateFacade.chapterBackground(chapter));
        if (bgTexture != null) {
            chapterList.addWidget(new ImageWidget(layout.cardX(), y, layout.cardW(), TabletUiFactory.CHAPTER_CARD_H, bgTexture));
        }
        if (selected) {
            chapterList.addWidget(new ImageWidget(layout.cardX(), y, layout.cardW(), TabletUiFactory.CHAPTER_CARD_H, SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, 82))));
        }
        String chapterIcon = ClientQuestStateFacade.chapterIcon(chapter);
        int iconDrawX = layout.iconX();
        if (chapterIcon != null && !chapterIcon.isBlank()) {
            chapterList.addWidget(new DisplayIconWidget(iconDrawX, y + 8, TabletUiFactory.CONTENT_ICON_SIZE, TabletUiFactory.CONTENT_ICON_SIZE, chapterIcon));
        }
        int textW = Math.max(8, layout.cardW() - 26);
        addChapterLabel(chapterList, chapter, rowLabel, y, layout.cardX(), textW);
        if (lockedPreview) {
            renderLockedFilter(chapterList, layout.cardX(), y, layout.cardW(), TabletUiFactory.CHAPTER_CARD_H);
        }
        addCompletionNotice(chapterList, chapter, iconDrawX - 2, y + 3);
        addChapterSelectionHits(chapterList, state, refresh, chapter, y, layout, collapsed, textW);
        addChapterIconChangeHit(chapterList, state, refresh, chapter, iconDrawX, y + 8);
    }

    static void addRenameRow(WidgetGroup chapterList, TabletUiState state, Player player, Runnable refresh, String chapter, int y, ChapterListMetrics.Layout layout) {
        ChapterInlineRenameRows.add(
                chapterList,
                layout.cardX(),
                y,
                layout.cardW(),
                layout.iconX(),
                ClientQuestStateFacade.chapterIcon(chapter),
                () -> state.chapterPanel.chapterDraftName,
                value -> state.chapterPanel.chapterDraftName = ChapterRenameActions.sanitizeInlineTitle(value),
                value -> ChapterRenameActions.commitRename(player, state, refresh, chapter, value),
                () -> {
                    state.canvas.pendingChapterRename = "";
                    state.chapterPanel.chapterDraftName = chapter;
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
                () -> state.chapterPanel.chapterDraftName,
                value -> state.chapterPanel.chapterDraftName = ChapterRenameActions.sanitizeInlineTitle(value),
                value -> ChapterRenameActions.commitDraft(player, state, refresh, value),
                () -> {
                    state.canvas.pendingChapterRename = "";
                    state.chapterPanel.chapterDraftName = state.root.selectedChapter;
                    refresh.run();
                }
        );
    }

    static int addGhostIfVisible(WidgetGroup chapterList, String chapter, int cardX, int y, int cardW, int listH, int rowStep) {
        if (y < listH && y + TabletUiFactory.CHAPTER_CARD_H > 0) {
            renderChapterGhost(chapterList, chapter, cardX, y, cardW);
        }
        return y + rowStep;
    }

    private static void addChapterLabel(WidgetGroup chapterList, String chapter, String rowLabel, int y, int cardX, int textW) {
        int textColor = ClientQuestStateFacade.chapterTextColor(chapter);
        String align = ClientQuestStateFacade.chapterTextAlign(chapter);
        String textStyle = ClientQuestStateFacade.chapterTextStyle(chapter);
        int textSize = ClientQuestStateFacade.chapterTextSize(chapter);
        chapterList.addWidget(chapterStyledLabel(cardX + 24, chapterTextY(y, textSize), textW, chapterLabelHeight(textSize), rowLabel, textColor, textStyle, textSize, chapterTextType(align)));
    }

    private static void addCollapsedChapterRow(WidgetGroup chapterList, String chapter, String rowLabel, int y, ChapterListMetrics.Layout layout, boolean selected) {
        String chapterIcon = ClientQuestStateFacade.chapterIcon(chapter);
        String initial = "";
        if (chapterIcon == null || chapterIcon.isBlank()) {
            initial = rowLabel == null || rowLabel.isBlank() ? "?" : rowLabel.substring(0, 1).toUpperCase();
        }
        int tileY = collapsedTileY(y);
        chapterList.addWidget(new CollapsedChapterTileWidget(layout.cardX(), tileY, layout.cardW(), COLLAPSED_TILE_SIZE, rowLabel, selected));
        if (!initial.isBlank()) {
            chapterList.addWidget(TabletTextTextures.literal(layout.cardX(), tileY, layout.cardW(), COLLAPSED_TILE_SIZE, initial, selected ? TabletColors.TEXT_PRIMARY : TabletColors.TEXT_MUTED, TextTexture.TextType.HIDE));
        }
        if (chapterIcon != null && !chapterIcon.isBlank()) {
            chapterList.addWidget(new DisplayIconWidget(collapsedIconX(layout), collapsedIconY(y), COLLAPSED_ICON_SIZE, COLLAPSED_ICON_SIZE, chapterIcon));
        }
    }

    private static void addChapterSelectionHits(WidgetGroup chapterList, TabletUiState state, Runnable refresh, String chapter, int y, ChapterListMetrics.Layout layout, boolean collapsed, int textW) {
        final int rowY = y;
        var menuHit = TabletUiFactory.flatHitButton(collapsed ? layout.cardX() : layout.cardX() + 24, collapsed ? collapsedTileY(y) : y + 8, collapsed ? layout.cardW() : textW, collapsed ? COLLAPSED_TILE_SIZE : 16, click -> {
            if (!canOpenChapter(state, chapter)) {
                return;
            }
            selectChapter(state, chapter);
            state.chapterPanel.chapterMenuTarget = chapter;
            state.chapterPanel.chapterMenuX = 8;
            state.chapterPanel.chapterMenuY = Math.max(4, Math.min(rowY, chapterList.getSize().height - 72));
            TabletUiFactory.persistUiState(state);
            refresh.run();
        });
        menuHit.setHoverTooltips(new Component[]{Component.literal(chapter)});
        chapterList.addWidget(menuHit);
        var rowHit = TabletUiFactory.flatHitButton(layout.cardX(), collapsed ? collapsedTileY(y) : y, layout.cardW(), collapsed ? COLLAPSED_TILE_SIZE : TabletUiFactory.CHAPTER_CARD_H, click -> {
            if (!canOpenChapter(state, chapter)) {
                return;
            }
            selectChapter(state, chapter);
            TabletUiFactory.persistUiState(state);
            refresh.run();
        });
        rowHit.setHoverTooltips(new Component[]{Component.literal(chapter)});
        chapterList.addWidget(rowHit);
    }

    private static boolean canOpenChapter(TabletUiState state, String chapter) {
        return state != null && (state.root.canEdit || ClientQuestStateFacade.chapterOpenablePreview(chapter));
    }

    private static void selectChapter(TabletUiState state, String chapter) {
        state.root.selectedChapter = chapter;
        state.chapterPanel.chapterDraft = chapter;
        state.chapterPanel.chapterDraftName = chapter;
        state.canvas.pendingChapterRename = "";
        state.chapterPanel.chapterTextMenuOpen = false;
        state.chapterPanel.chapterTextMenuTarget = "";
        state.chapterPanel.chapterTextFontSizeFieldTarget = "";
        ClientQuestStateFacade.clearChapterCompletionNotice(chapter);
    }

    private static WidgetGroup chapterStyledLabel(int x, int y, int width, int height, String text, int color, String style, int fontSize, TextTexture.TextType type) {
        float scale = chapterFontScale(fontSize);
        int innerW = Math.max(1, Math.round(width / scale));
        int innerH = Math.max(1, Math.round(height / scale));
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        TextTextureWidget label = TabletTextTextures.literal(
                Math.max(0, (width - innerW) / 2),
                Math.max(0, (height - innerH) / 2),
                innerW,
                innerH,
                styledChapterText(text, style),
                color,
                type
        );
        label.textureStyle(texture -> texture.scale(scale));
        group.addWidget(label);
        return group;
    }

    private static void renderChapterGhost(WidgetGroup chapterList, String chapter, int x, int y, int w) {
        CardDragGhosts.renderChapter(
                chapterList,
                x,
                y,
                w,
                TabletUiFactory.chapterBackgroundTexture(ClientQuestStateFacade.chapterBackground(chapter)),
                ClientQuestStateFacade.chapterIcon(chapter),
                chapter
        );
    }

    private static void renderLockedFilter(WidgetGroup chapterList, int x, int y, int w, int h) {
        WidgetGroup filter = new WidgetGroup(x, y, w, h);
        filter.setBackground(com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.fill(withAlpha(TabletColors.SURFACE_BASE, 150)));
        chapterList.addWidget(filter);
    }

    private static void addCompletionNotice(WidgetGroup chapterList, String chapter, int x, int y) {
        if (!ClientQuestStateFacade.chapterHasCompletionNotice(chapter)) {
            return;
        }
        chapterList.addWidget(new ChapterCompletionNoticeWidget(x, y, NOTICE_ICON_SIZE, NOTICE_ICON_SIZE));
    }

    private static int chapterTextY(int rowY, int fontSize) {
        int labelH = chapterLabelHeight(fontSize);
        return rowY + Math.max(2, (TabletUiFactory.CHAPTER_CARD_H - labelH) / 2);
    }

    private static int chapterLabelHeight(int fontSize) {
        return Math.max(1, Math.round(FONT_LINE_HEIGHT * chapterFontScale(fontSize)));
    }

    private static float chapterFontScale(int fontSize) {
        int clamped = CanvasTextLayer.clampFontSize(fontSize);
        return Math.max(0.1f, clamped / (float) CanvasTextLayer.DEFAULT_FONT_SIZE);
    }

    private static int collapsedTileY(int rowY) {
        return rowY + Math.max(0, (TabletUiFactory.CHAPTER_COLLAPSED_ROW_STEP - COLLAPSED_TILE_SIZE) / 2);
    }

    private static int collapsedIconX(ChapterListMetrics.Layout layout) {
        return layout.cardX() + Math.max(0, (layout.cardW() - COLLAPSED_ICON_SIZE) / 2);
    }

    private static int collapsedIconY(int rowY) {
        return rowY + Math.max(0, (TabletUiFactory.CHAPTER_COLLAPSED_ROW_STEP - COLLAPSED_ICON_SIZE) / 2);
    }

    private static TextTexture.TextType chapterTextType(String align) {
        return switch (align == null ? "" : align) {
            case "left" -> TextTexture.TextType.LEFT_HIDE;
            case "right" -> TextTexture.TextType.RIGHT_HIDE;
            default -> TextTexture.TextType.HIDE;
        };
    }

    private static String styledChapterText(String text, String style) {
        String safe = text == null ? "" : text;
        return switch (CanvasTextLayer.normalizeStyle(style)) {
            case "bold" -> ChatFormatting.BOLD + safe;
            case "italic" -> ChatFormatting.ITALIC + safe;
            case "bold_italic" -> ChatFormatting.BOLD.toString() + ChatFormatting.ITALIC + safe;
            default -> safe;
        };
    }

    private static void addChapterIconChangeHit(WidgetGroup parent, TabletUiState state, Runnable refresh, String chapter, int x, int y) {
        if (!EditorChapterCommandClient.canManageChapters(state)) {
            return;
        }
        EntityIconControls.addChangeIconHit(parent, state, refresh, x, y, TabletUiFactory.CONTENT_ICON_SIZE, () -> {
            selectChapter(state, chapter);
            state.chapterPanel.chapterMenuOpen = false;
            state.chapterPanel.chapterDragPending = false;
            state.chapterPanel.chapterDragActive = false;
            state.chapterPanel.chapterDragName = "";
            state.chapterPanel.chapterDragTargetIndex = -1;
            EntityIconControls.openIconPicker(state, EntityIconControls.IconPickerTarget.chapter(chapter));
            TabletUiFactory.persistUiState(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter icon picker open target={}", chapter);
        });
    }

    private static final class CollapsedChapterTileWidget extends WidgetGroup {
        private final String label;
        private final boolean selected;

        private CollapsedChapterTileWidget(int x, int y, int width, int height, String label, boolean selected) {
            super(x, y, width, height);
            this.label = label == null ? "" : label;
            this.selected = selected;
            setHoverTooltips(new Component[]{Component.literal(this.label)});
        }

        @Override
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            int x = getPositionX();
            int y = getPositionY();
            int w = getSizeWidth();
            int h = getSizeHeight();
            boolean hovered = isMouseOverElement(mouseX, mouseY);
            if (selected || hovered) {
                int fill = selected ? withAlpha(TabletColors.INTERACTIVE, 108) : withAlpha(TabletColors.INTERACTIVE, 44);
                SurfaceFactory.fill(fill).draw(graphics, 0, 0, x, y, w, h);
            }
        }
    }

    private static final class ChapterCompletionNoticeWidget extends WidgetGroup {
        private ChapterCompletionNoticeWidget(int x, int y, int width, int height) {
            super(x, y, width, height);
        }

        @Override
        public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
            IGuiTexture texture = IconAtlas.iconTexture("chapter_notice");
            if (texture == null) {
                return;
            }
            long time = System.currentTimeMillis();
            int bounce = Math.round((float) Math.sin(time / 180.0) * 2.0f);
            texture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY() + bounce, getSizeWidth(), getSizeHeight());
        }
    }
}
