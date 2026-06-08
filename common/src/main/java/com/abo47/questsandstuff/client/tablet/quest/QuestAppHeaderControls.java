package com.abo47.questsandstuff.client.tablet.quest;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.TabletIconTextButton;
import com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.modal.ModalWindowManager;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.reward.QuestRewardClaimActions;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

final class QuestAppHeaderControls {
    private final TextFieldWidget chapterSearchField;
    private final TextFieldWidget searchField;
    private final WidgetGroup canvasHeaderSurface;
    private final TabletIconTextButton toolsButton;
    private final TabletIconTextButton settingsButton;
    private final TabletIconTextButton blueprintButton;
    private final TabletIconTextButton claimAllButton;
    private final TabletIconTextButton editorButton;
    private int toolsX;

    private QuestAppHeaderControls(
            TextFieldWidget chapterSearchField,
            TextFieldWidget searchField,
            WidgetGroup canvasHeaderSurface,
            TabletIconTextButton toolsButton,
            TabletIconTextButton settingsButton,
            TabletIconTextButton blueprintButton,
            TabletIconTextButton claimAllButton,
            TabletIconTextButton editorButton
    ) {
        this.chapterSearchField = chapterSearchField;
        this.searchField = searchField;
        this.canvasHeaderSurface = canvasHeaderSurface;
        this.toolsButton = toolsButton;
        this.settingsButton = settingsButton;
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
                () -> state.chapterSearch,
                Integer.MAX_VALUE,
                value -> {
                    state.chapterSearch = SearchFilter.normalizeUserInput(value);
                    state.chapterScroll = 0;
                    refresh.run();
                },
                focused -> state.chapterSearchFocused = focused
        );

        TextFieldWidget searchField = StyledTextFields.search(
                canvasHeaderX,
                topY,
                60,
                headerH,
                () -> state.search,
                Integer.MAX_VALUE,
                value -> {
                    state.search = SearchFilter.normalizeUserInput(value);
                    if (!state.search.isBlank()) {
                        CanvasRenderer.jumpToBestMatch(state);
                    }
                    refresh.run();
                },
                focused -> state.searchFocused = focused
        );

        WidgetGroup canvasHeaderSurface = new WidgetGroup(canvasHeaderX, topY, 60, headerH);
        canvasHeaderSurface.setBackground(Surfaces.fill(ModColors.SURFACE_PANEL));

        int toolsW = headerH;
        TabletIconTextButton toolsButton = headerButton(0, 0, toolsW, headerH, "tools", ModColors.INTERACTIVE, click -> {
            ToolMenuAnimation.toggleMain(state);
            refresh.run();
        });

        TabletIconTextButton settingsButton = headerButton(0, 0, toolsW, headerH, "settings-2", ModColors.INTERACTIVE, click -> toggleSettingsPanel(state, refresh));
        TabletIconTextButton blueprintButton = headerButton(0, 0, toolsW, headerH, "scroll", ModColors.WARNING, click -> {
            ToolMenuAnimation.closeMain(state);
            ContextMenuState.close(state);
            state.chapterMenuOpen = false;
            state.assetContextOpen = false;
            CanvasBlueprintController.openBlueprintLibrary(state);
            QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] library opened from main header");
            refresh.run();
        });
        TabletIconTextButton claimAllButton = headerButton(0, 0, toolsW, headerH, "claim_all", ModColors.INTERACTIVE, click -> {
            ToolMenuAnimation.closeMain(state);
            ContextMenuState.close(state);
            state.chapterMenuOpen = false;
            state.assetContextOpen = false;
            QuestRewardClaimActions.claimAll(player, "");
            refresh.run();
        });
        TabletIconTextButton editorButton = headerButton(0, 0, toolsW, headerH, "editor", ModColors.INTERACTIVE, click -> {
            if (!state.editorAvailable) {
                return;
            }
            state.editMode = !state.editMode;
            state.canEdit = state.editorAvailable && state.editMode;
            ContextMenuState.close(state);
            state.chapterMenuOpen = false;
            state.assetContextOpen = false;
            persistUiState(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] editor mode toggle enabled={}", state.editMode);
            refresh.run();
        });

        return new QuestAppHeaderControls(chapterSearchField, searchField, canvasHeaderSurface, toolsButton, settingsButton, blueprintButton, claimAllButton, editorButton);
    }

    TextFieldWidget chapterSearchField() {
        return chapterSearchField;
    }

    int toolsX() {
        return toolsX;
    }

    void syncFocus(TabletUiState state) {
        if (state.searchFocused) {
            searchField.setFocus(true);
        }
        if (state.chapterSearchFocused) {
            chapterSearchField.setFocus(true);
        }
    }

    void refreshSurfaces(TabletUiState state) {
        chapterSearchField.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
        searchField.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
        canvasHeaderSurface.setBackground(Surfaces.fill(ModColors.SURFACE_PANEL));
        toolsButton.visuals(headerVisuals(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE, ModColors.INTERACTIVE));
        boolean settingsActive = settingsActive(state);
        settingsButton.visuals(headerVisuals(settingsActive ? withAlpha(ModColors.SUCCESS, 38) : ModColors.SURFACE_PANEL_ALT, settingsActive ? ModColors.SUCCESS : ModColors.BORDER_BASE, settingsActive ? ModColors.SUCCESS : ModColors.INTERACTIVE));
        blueprintButton.visuals(headerVisuals(state.blueprintPlacementActive ? withAlpha(ModColors.WARNING, 38) : ModColors.SURFACE_PANEL_ALT, state.blueprintPlacementActive ? ModColors.WARNING : ModColors.BORDER_BASE, state.blueprintPlacementActive ? ModColors.WARNING : ModColors.INTERACTIVE));
        claimAllButton.visuals(headerVisuals(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE, ModColors.INTERACTIVE));
        editorButton.visuals(headerVisuals(withAlpha(state.editMode ? ModColors.SUCCESS : ModColors.ERROR, 38), state.editMode ? ModColors.SUCCESS : ModColors.ERROR, state.editMode ? ModColors.SUCCESS : ModColors.ERROR));
    }

    void layoutChapter(boolean chapterCollapsed, int dynamicListX, int dynamicListW, int chapterTopY, int chapterHeaderH) {
        chapterSearchField.setVisible(!chapterCollapsed);
        chapterSearchField.setSelfPosition(dynamicListX, chapterTopY);
        chapterSearchField.setSize(dynamicListW, chapterHeaderH);
    }

    void layoutCanvas(TabletUiState state, int headerX, int headerW, int topY, int headerH, int toolsW, int topGap) {
        toolsX = headerX + headerW - toolsW;
        boolean showEditorToggle = state.editorAvailable;
        boolean showBlueprintButton = state.canEdit && state.editMode;
        boolean showToolsButton = true;
        int editorX = showEditorToggle ? toolsX - topGap - toolsW : toolsX;
        int claimAllX = (showEditorToggle ? editorX : toolsX) - topGap - toolsW;
        int settingsX = claimAllX - topGap - toolsW;
        int blueprintX = showBlueprintButton ? settingsX - topGap - toolsW : settingsX;
        int searchX = headerX;
        int searchEnd = (showBlueprintButton ? blueprintX : settingsX) - topGap;
        int searchW = Math.max(60, searchEnd - searchX);
        canvasHeaderSurface.setSelfPosition(headerX, topY);
        canvasHeaderSurface.setSize(headerW, headerH);
        searchField.setSelfPosition(searchX, topY);
        searchField.setSize(searchW, headerH);
        layoutHeaderButton(editorButton, editorX, topY, toolsW, headerH, showEditorToggle, new Component[]{
                Component.translatable("ui.questsandstuff.tools.editor_toggle"),
                Component.translatable(state.editMode
                        ? "ui.questsandstuff.tools.editor_state_on"
                        : "ui.questsandstuff.tools.editor_state_off")
        });

        layoutHeaderButton(claimAllButton, claimAllX, topY, toolsW, headerH, true, new Component[]{
                TabletVocabulary.component(QuestVocabulary.CLAIM_ALL_REWARDS)
        });

        layoutHeaderButton(settingsButton, settingsX, topY, toolsW, headerH, true, new Component[]{
                Component.translatable("ui.questsandstuff.settings.button"),
                Component.translatable("ui.questsandstuff.settings.button_tooltip")
        });

        layoutHeaderButton(blueprintButton, blueprintX, topY, toolsW, headerH, showBlueprintButton, new Component[]{
                Component.translatable("ui.questsandstuff.blueprints.button"),
                Component.translatable("ui.questsandstuff.blueprints.button_tooltip")
        });

        layoutHeaderButton(toolsButton, toolsX, topY, toolsW, headerH, showToolsButton, null);
    }

    void addToCanvas(WidgetGroup canvasPanel) {
        canvasPanel.addWidget(canvasHeaderSurface);
        canvasPanel.addWidget(searchField);
        canvasPanel.addWidget(settingsButton);
        canvasPanel.addWidget(blueprintButton);
        canvasPanel.addWidget(claimAllButton);
        canvasPanel.addWidget(editorButton);
        canvasPanel.addWidget(toolsButton);
    }

    private static void toggleSettingsPanel(TabletUiState state, Runnable refresh) {
        if (settingsActive(state)) {
            ModalCloseActions.closeAll(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] settings panel toggle open=false");
            refresh.run();
            return;
        }
        ToolMenuAnimation.closeMain(state);
        ContextMenuState.close(state);
        state.chapterMenuOpen = false;
        state.assetContextOpen = false;
        ModalOpenActions.openSettingsPanel(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] settings panel toggle open=true");
        refresh.run();
    }

    private static boolean settingsActive(TabletUiState state) {
        return ModalStateQueries.isOpen(state, ModalWindowManager.ModalType.SETTINGS_PANEL) && !state.modalWindowClosing;
    }

    private static TabletIconTextButton headerButton(int x, int y, int width, int height, String icon, int accentColor, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        return TabletIconTextButton.icon(x, y, width, height, icon, headerVisuals(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE, accentColor), callback);
    }

    private static TabletIconTextButton.Visuals headerVisuals(int fill, int border, int accentColor) {
        return new TabletIconTextButton.Visuals(
                TabletIconTextButton.State.of(fill, border, accentColor),
                TabletIconTextButton.State.of(withAlpha(accentColor, 66), ModColors.BORDER_ACCENT, accentColor),
                TabletIconTextButton.State.of(withAlpha(accentColor, 90), accentColor, ModColors.TEXT_PRIMARY)
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
