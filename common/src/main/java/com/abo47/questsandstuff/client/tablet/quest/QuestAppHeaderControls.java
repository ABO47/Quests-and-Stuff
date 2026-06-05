package com.abo47.questsandstuff.client.tablet.quest;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.controls.HeaderIconWidget;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.quest.reward.QuestRewardClaimActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class QuestAppHeaderControls {
    private final TextFieldWidget chapterSearchField;
    private final TextFieldWidget searchField;
    private final WidgetGroup canvasHeaderSurface;
    private final WidgetGroup toolsBg;
    private final ButtonWidget toolsHit;
    private final HeaderIconWidget toolsIconWidget;
    private final WidgetGroup settingsBg;
    private final ButtonWidget settingsHit;
    private final HeaderIconWidget settingsIconWidget;
    private final WidgetGroup blueprintBg;
    private final ButtonWidget blueprintHit;
    private final HeaderIconWidget blueprintIconWidget;
    private final WidgetGroup claimAllBg;
    private final ButtonWidget claimAllHit;
    private final HeaderIconWidget claimAllIconWidget;
    private final WidgetGroup editorBg;
    private final ButtonWidget editorHit;
    private final HeaderIconWidget editorIconWidget;
    private int toolsX;

    private QuestAppHeaderControls(
            TextFieldWidget chapterSearchField,
            TextFieldWidget searchField,
            WidgetGroup canvasHeaderSurface,
            WidgetGroup toolsBg,
            ButtonWidget toolsHit,
            HeaderIconWidget toolsIconWidget,
            WidgetGroup settingsBg,
            ButtonWidget settingsHit,
            HeaderIconWidget settingsIconWidget,
            WidgetGroup blueprintBg,
            ButtonWidget blueprintHit,
            HeaderIconWidget blueprintIconWidget,
            WidgetGroup claimAllBg,
            ButtonWidget claimAllHit,
            HeaderIconWidget claimAllIconWidget,
            WidgetGroup editorBg,
            ButtonWidget editorHit,
            HeaderIconWidget editorIconWidget
    ) {
        this.chapterSearchField = chapterSearchField;
        this.searchField = searchField;
        this.canvasHeaderSurface = canvasHeaderSurface;
        this.toolsBg = toolsBg;
        this.toolsHit = toolsHit;
        this.toolsIconWidget = toolsIconWidget;
        this.settingsBg = settingsBg;
        this.settingsHit = settingsHit;
        this.settingsIconWidget = settingsIconWidget;
        this.blueprintBg = blueprintBg;
        this.blueprintHit = blueprintHit;
        this.blueprintIconWidget = blueprintIconWidget;
        this.claimAllBg = claimAllBg;
        this.claimAllHit = claimAllHit;
        this.claimAllIconWidget = claimAllIconWidget;
        this.editorBg = editorBg;
        this.editorHit = editorHit;
        this.editorIconWidget = editorIconWidget;
    }

    static QuestAppHeaderControls create(Player player, TabletUiState state, Runnable refresh, int contentInset, int chapterTopY, int chapterHeaderH, int initialChapterW, int topY, int headerH) {
        TextFieldWidget chapterSearchField = new TextFieldWidget(contentInset, chapterTopY, Math.max(24, initialChapterW - contentInset * 2), chapterHeaderH, () -> state.chapterSearch, value -> {
            state.chapterSearch = SearchFilter.normalizeUserInput(value);
            state.chapterScroll = 0;
            refresh.run();
        }) {
            @Override
            public void onFocusChanged(Widget lastFocus, Widget focus) {
                super.onFocusChanged(lastFocus, focus);
                state.chapterSearchFocused = isFocus();
            }
        };
        configureSearchField(chapterSearchField);

        TextFieldWidget searchField = new TextFieldWidget(contentInset, topY, 60, headerH, () -> state.search, value -> {
            state.search = SearchFilter.normalizeUserInput(value);
            if (!state.search.isBlank()) {
                CanvasRenderer.jumpToBestMatch(state);
            }
            refresh.run();
        }) {
            @Override
            public void onFocusChanged(Widget lastFocus, Widget focus) {
                super.onFocusChanged(lastFocus, focus);
                state.searchFocused = isFocus();
            }
        };
        configureSearchField(searchField);

        WidgetGroup canvasHeaderSurface = new WidgetGroup(contentInset, topY, 60, headerH);
        canvasHeaderSurface.setBackground(Surfaces.fill(ModColors.SURFACE_PANEL));

        int toolsW = headerH;
        WidgetGroup toolsBg = panel(0, 0, toolsW, headerH, ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE);
        ButtonWidget toolsHit = flatHitButton(0, 0, toolsW, headerH, click -> {
            ToolMenuAnimation.toggleMain(state);
            refresh.run();
        });
        toolsHit.setHoverTexture(Surfaces.bordered(withAlpha(ModColors.INTERACTIVE, 66), ModColors.BORDER_ACCENT));
        toolsHit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));

        int headerIconSize = Math.max(8, headerH - 4);
        HeaderIconWidget toolsIconWidget = new HeaderIconWidget(0, 0, headerIconSize, "tools.png");
        WidgetGroup settingsBg = panel(0, 0, toolsW, headerH, ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE);
        ButtonWidget settingsHit = flatHitButton(0, 0, toolsW, headerH, click -> toggleSettingsPanel(state, refresh));
        settingsHit.setHoverTexture(Surfaces.bordered(withAlpha(ModColors.INTERACTIVE, 66), ModColors.BORDER_ACCENT));
        settingsHit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        HeaderIconWidget settingsIconWidget = new HeaderIconWidget(0, 0, headerIconSize, "settings-2.png");
        WidgetGroup blueprintBg = panel(0, 0, toolsW, headerH, ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE);
        ButtonWidget blueprintHit = flatHitButton(0, 0, toolsW, headerH, click -> {
            ToolMenuAnimation.closeMain(state);
            state.contextMenuOpen = false;
            state.chapterMenuOpen = false;
            state.assetContextOpen = false;
            CanvasBlueprintController.openBlueprintLibrary(state);
            QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] library opened from main header");
            refresh.run();
        });
        blueprintHit.setHoverTexture(Surfaces.bordered(withAlpha(ModColors.INTERACTIVE, 66), ModColors.BORDER_ACCENT));
        blueprintHit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        HeaderIconWidget blueprintIconWidget = new HeaderIconWidget(0, 0, headerIconSize, "scroll.png");
        WidgetGroup claimAllBg = panel(0, 0, toolsW, headerH, ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE);
        ButtonWidget claimAllHit = flatHitButton(0, 0, toolsW, headerH, click -> {
            ToolMenuAnimation.closeMain(state);
            state.contextMenuOpen = false;
            state.chapterMenuOpen = false;
            state.assetContextOpen = false;
            QuestRewardClaimActions.claimAll(player, "");
            refresh.run();
        });
        claimAllHit.setHoverTexture(Surfaces.bordered(withAlpha(ModColors.INTERACTIVE, 66), ModColors.BORDER_ACCENT));
        claimAllHit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        HeaderIconWidget claimAllIconWidget = new HeaderIconWidget(0, 0, headerIconSize, "claim_all.png");
        WidgetGroup editorBg = panel(0, 0, toolsW, headerH, withAlpha(ModColors.SURFACE_PANEL_ALT, 164), ModColors.BORDER_BASE);
        ButtonWidget editorHit = flatHitButton(0, 0, toolsW, headerH, click -> {
            if (!state.editorAvailable) {
                return;
            }
            state.editMode = !state.editMode;
            state.canEdit = state.editorAvailable && state.editMode;
            state.contextMenuOpen = false;
            state.chapterMenuOpen = false;
            state.assetContextOpen = false;
            persistUiState(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] editor mode toggle enabled={}", state.editMode);
            refresh.run();
        });
        editorHit.setHoverTexture(Surfaces.bordered(withAlpha(ModColors.INTERACTIVE, 66), ModColors.BORDER_ACCENT));
        editorHit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 90)));
        HeaderIconWidget editorIconWidget = new HeaderIconWidget(0, 0, headerIconSize, "editor.png");

        return new QuestAppHeaderControls(chapterSearchField, searchField, canvasHeaderSurface, toolsBg, toolsHit, toolsIconWidget, settingsBg, settingsHit, settingsIconWidget, blueprintBg, blueprintHit, blueprintIconWidget, claimAllBg, claimAllHit, claimAllIconWidget, editorBg, editorHit, editorIconWidget);
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
        toolsBg.setBackground(Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE));
        boolean settingsActive = settingsActive(state);
        settingsBg.setBackground(Surfaces.bordered(settingsActive ? withAlpha(ModColors.SUCCESS, 38) : ModColors.SURFACE_PANEL_ALT, settingsActive ? ModColors.SUCCESS : ModColors.BORDER_BASE));
        blueprintBg.setBackground(Surfaces.bordered(state.blueprintPlacementActive ? withAlpha(ModColors.WARNING, 38) : ModColors.SURFACE_PANEL_ALT, state.blueprintPlacementActive ? ModColors.WARNING : ModColors.BORDER_BASE));
        claimAllBg.setBackground(Surfaces.bordered(ModColors.SURFACE_PANEL_ALT, ModColors.BORDER_BASE));
        editorBg.setBackground(Surfaces.bordered(withAlpha(state.editMode ? ModColors.SUCCESS : ModColors.ERROR, 38), state.editMode ? ModColors.SUCCESS : ModColors.ERROR));
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
        editorBg.setSelfPosition(editorX, topY);
        editorBg.setSize(toolsW, headerH);
        editorHit.setSelfPosition(editorX, topY);
        editorHit.setSize(toolsW, headerH);
        editorHit.setHoverTooltips(new Component[]{
                Component.translatable("ui.questsandstuff.tools.editor_toggle"),
                Component.translatable(state.editMode
                        ? "ui.questsandstuff.tools.editor_state_on"
                        : "ui.questsandstuff.tools.editor_state_off")
        });
        int editorIconSize = editorIconWidget.getSize().width;
        editorIconWidget.setSelfPosition(editorX + (toolsW - editorIconSize) / 2, topY + (headerH - editorIconSize) / 2);
        editorBg.setVisible(showEditorToggle);
        editorBg.setActive(showEditorToggle);
        editorHit.setVisible(showEditorToggle);
        editorHit.setActive(showEditorToggle);
        editorIconWidget.setVisible(showEditorToggle);
        editorIconWidget.setActive(false);

        claimAllBg.setSelfPosition(claimAllX, topY);
        claimAllBg.setSize(toolsW, headerH);
        claimAllHit.setSelfPosition(claimAllX, topY);
        claimAllHit.setSize(toolsW, headerH);
        claimAllHit.setHoverTooltips(new Component[]{
                TabletVocabulary.component(QuestVocabulary.CLAIM_ALL_REWARDS)
        });
        int claimAllIconSize = claimAllIconWidget.getSize().width;
        claimAllIconWidget.setSelfPosition(claimAllX + (toolsW - claimAllIconSize) / 2, topY + (headerH - claimAllIconSize) / 2);
        claimAllBg.setVisible(true);
        claimAllBg.setActive(true);
        claimAllHit.setVisible(true);
        claimAllHit.setActive(true);
        claimAllIconWidget.setVisible(true);
        claimAllIconWidget.setActive(false);

        settingsBg.setSelfPosition(settingsX, topY);
        settingsBg.setSize(toolsW, headerH);
        settingsHit.setSelfPosition(settingsX, topY);
        settingsHit.setSize(toolsW, headerH);
        settingsHit.setHoverTooltips(new Component[]{
                Component.translatable("ui.questsandstuff.settings.button"),
                Component.translatable("ui.questsandstuff.settings.button_tooltip")
        });
        int settingsIconSize = settingsIconWidget.getSize().width;
        settingsIconWidget.setSelfPosition(settingsX + (toolsW - settingsIconSize) / 2, topY + (headerH - settingsIconSize) / 2);
        settingsBg.setVisible(true);
        settingsBg.setActive(true);
        settingsHit.setVisible(true);
        settingsHit.setActive(true);
        settingsIconWidget.setVisible(true);
        settingsIconWidget.setActive(false);

        blueprintBg.setSelfPosition(blueprintX, topY);
        blueprintBg.setSize(toolsW, headerH);
        blueprintHit.setSelfPosition(blueprintX, topY);
        blueprintHit.setSize(toolsW, headerH);
        blueprintHit.setHoverTooltips(new Component[]{
                Component.translatable("ui.questsandstuff.blueprints.button"),
                Component.translatable("ui.questsandstuff.blueprints.button_tooltip")
        });
        int blueprintIconSize = blueprintIconWidget.getSize().width;
        blueprintIconWidget.setSelfPosition(blueprintX + (toolsW - blueprintIconSize) / 2, topY + (headerH - blueprintIconSize) / 2);
        blueprintBg.setVisible(showBlueprintButton);
        blueprintBg.setActive(showBlueprintButton);
        blueprintHit.setVisible(showBlueprintButton);
        blueprintHit.setActive(showBlueprintButton);
        blueprintIconWidget.setVisible(showBlueprintButton);
        blueprintIconWidget.setActive(false);

        toolsBg.setSelfPosition(toolsX, topY);
        toolsBg.setSize(toolsW, headerH);
        toolsHit.setSelfPosition(toolsX, topY);
        toolsHit.setSize(toolsW, headerH);
        int toolsIconSize = toolsIconWidget.getSize().width;
        toolsIconWidget.setSelfPosition(toolsX + (toolsW - toolsIconSize) / 2, topY + (headerH - toolsIconSize) / 2);
        toolsBg.setVisible(showToolsButton);
        toolsBg.setActive(showToolsButton);
        toolsHit.setVisible(showToolsButton);
        toolsHit.setActive(showToolsButton);
        toolsIconWidget.setVisible(showToolsButton);
        toolsIconWidget.setActive(false);
    }

    void addToCanvas(WidgetGroup canvasPanel) {
        canvasPanel.addWidget(canvasHeaderSurface);
        canvasPanel.addWidget(searchField);
        canvasPanel.addWidget(settingsBg);
        canvasPanel.addWidget(settingsHit);
        canvasPanel.addWidget(settingsIconWidget);
        canvasPanel.addWidget(blueprintBg);
        canvasPanel.addWidget(blueprintHit);
        canvasPanel.addWidget(blueprintIconWidget);
        canvasPanel.addWidget(claimAllBg);
        canvasPanel.addWidget(claimAllHit);
        canvasPanel.addWidget(claimAllIconWidget);
        canvasPanel.addWidget(editorBg);
        canvasPanel.addWidget(editorHit);
        canvasPanel.addWidget(editorIconWidget);
        canvasPanel.addWidget(toolsBg);
        canvasPanel.addWidget(toolsHit);
        canvasPanel.addWidget(toolsIconWidget);
    }

    private static void toggleSettingsPanel(TabletUiState state, Runnable refresh) {
        if (settingsActive(state)) {
            ModalCloseActions.closeAll(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] settings panel toggle open=false");
            refresh.run();
            return;
        }
        ToolMenuAnimation.closeMain(state);
        state.contextMenuOpen = false;
        state.chapterMenuOpen = false;
        state.assetContextOpen = false;
        ModalOpenActions.openSettingsPanel(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] settings panel toggle open=true");
        refresh.run();
    }

    private static boolean settingsActive(TabletUiState state) {
        return state.settingsPanelOpen && !state.modalWindowClosing;
    }

    private static void configureSearchField(TextFieldWidget field) {
        field.setClientSideWidget();
        field.setValidator(SearchFilter::normalizeUserInput);
        field.setBordered(false);
        field.setBackground(Surfaces.bordered(ModColors.SURFACE_BASE, ModColors.BORDER_BASE));
        field.setTextColor(ModColors.TEXT_PRIMARY);
    }
}
