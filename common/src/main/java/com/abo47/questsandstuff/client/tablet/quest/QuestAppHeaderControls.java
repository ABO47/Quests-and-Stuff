package com.abo47.questsandstuff.client.tablet.quest;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.TabletIconTextButton;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.persistUiState;

final class QuestAppHeaderControls {
    private final TextFieldWidget chapterSearchField;
    private final TextFieldWidget searchField;
    private final TabletIconTextButton toolsButton;
    private final TabletIconTextButton blueprintButton;
    private final TabletIconTextButton claimAllButton;
    private final TabletIconTextButton editorButton;
    private int toolsX;

    private QuestAppHeaderControls(
            TextFieldWidget chapterSearchField,
            TextFieldWidget searchField,
            TabletIconTextButton toolsButton,
            TabletIconTextButton blueprintButton,
            TabletIconTextButton claimAllButton,
            TabletIconTextButton editorButton
    ) {
        this.chapterSearchField = chapterSearchField;
        this.searchField = searchField;
        this.toolsButton = toolsButton;
        this.blueprintButton = blueprintButton;
        this.claimAllButton = claimAllButton;
        this.editorButton = editorButton;
    }

    static QuestAppHeaderControls create(Player player, TabletUiState state, Runnable refresh, int chapterInset, int chapterTopY, int chapterHeaderH, int initialChapterW, int canvasHeaderX, int topY, int headerH) {
        TextFieldWidget chapterSearchField = StyledTextFields.search(
                chapterInset,
                chapterTopY,
                Math.max(24, initialChapterW - chapterInset * 2),
                chapterHeaderH,
                () -> state.chapterPanel.chapterSearch,
                Integer.MAX_VALUE,
                value -> {
                    state.chapterPanel.chapterSearch = SearchFilter.normalizeUserInput(value);
                    state.chapterPanel.chapterScroll = 0;
                    refresh.run();
                },
                focused -> state.chapterPanel.chapterSearchFocused = focused
        );

        TextFieldWidget searchField = StyledTextFields.search(
                canvasHeaderX,
                topY,
                60,
                headerH,
                () -> state.root.search,
                Integer.MAX_VALUE,
                value -> {
                    state.root.search = SearchFilter.normalizeUserInput(value);
                    if (!state.root.search.isBlank()) {
                        CanvasRenderer.jumpToBestMatch(state);
                    }
                    refresh.run();
                },
                focused -> state.root.searchFocused = focused
        );

        int toolsW = headerH;
        TabletIconTextButton toolsButton = headerButton(0, 0, toolsW, headerH, "tools", TabletColors.INTERACTIVE, click -> {
            ToolMenuAnimation.toggleMain(state);
            refresh.run();
        });

        TabletIconTextButton blueprintButton = headerButton(0, 0, toolsW, headerH, "scroll", TabletColors.WARNING, click -> {
            ToolMenuAnimation.closeMain(state);
            ContextMenuController.close(state);
            state.chapterPanel.chapterMenuOpen = false;
            state.pickers.assetContextOpen = false;
            CanvasBlueprintController.openBlueprintLibrary(state);
            QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] library opened from main header");
            refresh.run();
        });
        TabletIconTextButton claimAllButton = headerButton(0, 0, toolsW, headerH, "claim_all", TabletColors.INTERACTIVE, click -> {
            ToolMenuAnimation.closeMain(state);
            ContextMenuController.close(state);
            state.chapterPanel.chapterMenuOpen = false;
            state.pickers.assetContextOpen = false;
            RewardClaimHandler.claimAll(player, "");
            refresh.run();
        });
        TabletIconTextButton editorButton = headerButton(0, 0, toolsW, headerH, "editor", TabletColors.INTERACTIVE, click -> {
            if (!state.root.editorAvailable) {
                return;
            }
            state.root.editMode = !state.root.editMode;
            state.root.canEdit = state.root.editorAvailable && state.root.editMode;
            ContextMenuController.close(state);
            state.chapterPanel.chapterMenuOpen = false;
            state.pickers.assetContextOpen = false;
            persistUiState(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] editor mode toggle enabled={}", state.root.editMode);
            refresh.run();
        });

        return new QuestAppHeaderControls(chapterSearchField, searchField, toolsButton, blueprintButton, claimAllButton, editorButton);
    }

    TextFieldWidget chapterSearchField() {
        return chapterSearchField;
    }

    TextFieldWidget searchField() {
        return searchField;
    }

    TabletIconTextButton toolsButton() {
        return toolsButton;
    }

    TabletIconTextButton blueprintButton() {
        return blueprintButton;
    }

    TabletIconTextButton claimAllButton() {
        return claimAllButton;
    }

    TabletIconTextButton editorButton() {
        return editorButton;
    }

    int toolsX() {
        return toolsX;
    }

    void syncFocus(TabletUiState state) {
        if (state.root.searchFocused) {
            searchField.setFocus(true);
        }
        if (state.chapterPanel.chapterSearchFocused) {
            chapterSearchField.setFocus(true);
        }
    }

    void refreshSurfaceFactory(TabletUiState state) {
        chapterSearchField.setBackground(SurfaceFactory.bordered(TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE));
        chapterSearchField.setTextColor(TabletColors.TEXT_PRIMARY);
        searchField.setBackground(SurfaceFactory.bordered(TabletColors.SURFACE_BASE, TabletColors.BORDER_BASE));
        searchField.setTextColor(TabletColors.TEXT_PRIMARY);
        toolsButton.visuals(headerVisuals(TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_BASE, TabletColors.INTERACTIVE));
        blueprintButton.visuals(headerVisuals(TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_BASE, TabletColors.INTERACTIVE));
        claimAllButton.visuals(headerVisuals(TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_BASE, TabletColors.INTERACTIVE));
        editorButton.visuals(editorVisuals(state.root.editMode));
    }

    void layoutChapter(boolean chapterCollapsed, int dynamicListX, int dynamicListW, int chapterTopY, int chapterHeaderH) {
        chapterSearchField.setVisible(!chapterCollapsed);
        chapterSearchField.setSelfPosition(dynamicListX, chapterTopY);
        chapterSearchField.setSize(dynamicListW, chapterHeaderH);
    }

    void layoutCanvas(TabletUiState state, int headerX, int headerW, int topY, int headerH, int toolsW, int topGap) {
        toolsX = headerX + headerW - toolsW;
        boolean showEditorToggle = state.root.editorAvailable;
        boolean showBlueprintButton = state.root.canEdit && state.root.editMode;
        boolean showToolsButton = true;
        int editorX = showEditorToggle ? toolsX - topGap - toolsW : toolsX;
        int claimAllX = (showEditorToggle ? editorX : toolsX) - topGap - toolsW;
        int blueprintX = showBlueprintButton ? claimAllX - topGap - toolsW : claimAllX;
        int searchX = headerX;
        int searchEnd = (showBlueprintButton ? blueprintX : claimAllX) - topGap;
        int searchW = Math.max(60, searchEnd - searchX);
        searchField.setSelfPosition(searchX, topY);
        searchField.setSize(searchW, headerH);
        layoutHeaderButton(editorButton, editorX, topY, toolsW, headerH, showEditorToggle, new Component[]{
                Component.translatable("ui.questsandstuff.tools.editor_toggle"),
                Component.translatable(state.root.editMode
                        ? "ui.questsandstuff.tools.editor_state_on"
                        : "ui.questsandstuff.tools.editor_state_off")
        });

        layoutHeaderButton(claimAllButton, claimAllX, topY, toolsW, headerH, true, new Component[]{
                TabletTranslationKeys.component(QuestTranslationKeys.CLAIM_ALL_REWARDS)
        });

        layoutHeaderButton(blueprintButton, blueprintX, topY, toolsW, headerH, showBlueprintButton, new Component[]{
                Component.translatable("ui.questsandstuff.blueprints.button"),
                Component.translatable("ui.questsandstuff.blueprints.button_tooltip")
        });

        layoutHeaderButton(toolsButton, toolsX, topY, toolsW, headerH, showToolsButton, null);
    }

    void addToCanvas(WidgetGroup canvasPanel) {
        canvasPanel.addWidget(searchField);
        canvasPanel.addWidget(blueprintButton);
        canvasPanel.addWidget(claimAllButton);
        canvasPanel.addWidget(editorButton);
        canvasPanel.addWidget(toolsButton);
    }

    private static TabletIconTextButton headerButton(int x, int y, int width, int height, String icon, int accentColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return TabletIconTextButton.icon(x, y, width, height, icon, headerVisuals(TabletColors.SURFACE_PANEL_ALT, TabletColors.BORDER_BASE, accentColor), callback);
    }

    private static TabletIconTextButton.Visuals headerVisuals(int fill, int border, int accentColor) {
        return new TabletIconTextButton.Visuals(
                TabletIconTextButton.State.of(fill, border, accentColor),
                TabletIconTextButton.State.of(withAlpha(accentColor, 66), TabletColors.BORDER_ACCENT, accentColor),
                TabletIconTextButton.State.of(withAlpha(accentColor, 90), accentColor, TabletColors.TEXT_PRIMARY)
        );
    }

    private static TabletIconTextButton.Visuals editorVisuals(boolean editMode) {
        int accent = editMode ? TabletColors.SUCCESS : TabletColors.ERROR;
        return new TabletIconTextButton.Visuals(
                TabletIconTextButton.State.of(TabletColors.SURFACE_PANEL_ALT, accent, accent),
                TabletIconTextButton.State.of(TabletColors.hoverFill(accent), TabletColors.BORDER_ACCENT, accent),
                TabletIconTextButton.State.of(TabletColors.pressedFill(accent), accent, TabletColors.TEXT_PRIMARY),
                editMode ? TabletColors.SUCCESS : -1
        );
    }

    private static void layoutHeaderButton(TabletIconTextButton button, int x, int y, int width, int height, boolean visible, Component[] tooltips) {
        button.setSelfPosition(x, y);
        button.setSize(width, height);
        button.tooltips(tooltips);
        button.setVisible(visible);
        button.setActive(visible);
    }

}
