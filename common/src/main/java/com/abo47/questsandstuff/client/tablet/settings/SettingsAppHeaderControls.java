package com.abo47.questsandstuff.client.tablet.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.modal.SettingsTabDescriptor;
import com.abo47.questsandstuff.client.tablet.modal.SettingsTabDescriptors;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens.*;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.HEADER_H;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.panel;

final class SettingsAppHeaderControls {
    private static final int TAB_H = GRID_20;
    private static final int TAB_GAP = GRID_4;
    private static final int SEARCH_INSET = 9;
    private static final int TAB_ANIM_MS = 200;
    private static final int TAB_ENLARGE = GRID_4;

    private final TabletUiState state;
    private final Runnable refresh;
    private final TextFieldWidget searchField;
    private final WidgetGroup tabLayer;

    private SettingsAppHeaderControls(TabletUiState state, Runnable refresh, TextFieldWidget searchField, WidgetGroup tabLayer) {
        this.state = state;
        this.refresh = refresh;
        this.searchField = searchField;
        this.tabLayer = tabLayer;
    }

    static SettingsAppHeaderControls create(TabletUiState state, Runnable refresh, int headerTop, int bodyW) {
        state.settings.tabAnimationStartMs = System.currentTimeMillis();
        TextFieldWidget searchField = StyledTextFields.search(
                SEARCH_INSET, headerTop + TAB_H + TAB_ENLARGE + TAB_GAP, Math.max(40, bodyW - SEARCH_INSET * 2), HEADER_H,
                () -> state.settings.search, Integer.MAX_VALUE,
                value -> {
                    state.settings.search = SearchFilter.normalizeUserInput(value);
                    refresh.run();
                },
                focused -> {}
        );
        WidgetGroup tabLayer = new WidgetGroup(0, headerTop, bodyW, TAB_H + TAB_ENLARGE);
        return new SettingsAppHeaderControls(state, refresh, searchField, tabLayer);
    }

    TextFieldWidget searchField() {
        return searchField;
    }

    WidgetGroup tabLayer() {
        return tabLayer;
    }

    void layout(int headerTop, int bodyW) {
        tabLayer.clearAllWidgets();
        tabLayer.setSelfPosition(0, headerTop);
        tabLayer.setSize(bodyW, TAB_H + TAB_ENLARGE);
        int tabAreaW = Math.max(1, bodyW - SEARCH_INSET * 2);
        int tabX = SEARCH_INSET;
        int count = Math.max(1, SettingsTabDescriptors.all().size());
        int totalGap = TAB_GAP * (count - 1);
        int tabW = Math.max(1, (tabAreaW - totalGap) / count);
        int remainder = Math.max(0, (tabAreaW - totalGap) - tabW * count);
        for (int i = 0; i < count; i++) {
            SettingsTabDescriptor tab = SettingsTabDescriptors.all().get(i);
            int currentW = tabW + (i < remainder ? 1 : 0);
            tabLayer.addWidget(makeTabButton(tab, tabX, currentW));
            tabX += currentW + TAB_GAP;
        }
        searchField.setSelfPosition(SEARCH_INSET, headerTop + TAB_H + TAB_ENLARGE + TAB_GAP);
        searchField.setSize(Math.max(40, bodyW - SEARCH_INSET * 2), HEADER_H);
    }

    void addTo(WidgetGroup mainPanel) {
        mainPanel.addWidget(tabLayer);
        mainPanel.addWidget(searchField);
    }

    private WidgetGroup makeTabButton(SettingsTabDescriptor tab, int x, int w) {
        boolean active = state.settings.currentTab == tab.id();
        int h = TAB_H;
        int fill = active ? withAlpha(TabletColors.SURFACE_BASE, 250) : withAlpha(TabletColors.SURFACE_PANEL_ALT, 142);
        int border = active ? TabletColors.BORDER_ACCENT : TabletColors.BORDER_BASE;
        WidgetGroup container = new WidgetGroup(x, 0, w, h + TAB_ENLARGE);
        WidgetGroup bg = active ? enlargedTabBg(w, fill, border) : panel(0, TAB_ENLARGE, w, h, fill, border);
        LabelWidget text = label(8, TAB_ENLARGE + 6, SearchFilter.crop(I18n.get(tab.labelKey()), fontWidth(I18n.get(tab.labelKey()), Math.max(8, w - 16))), active ? TabletColors.TEXT_PRIMARY : TabletColors.TEXT_MUTED);
        ButtonWidget hit = active
                ? flatHitButton(0, 0, w, h + TAB_ENLARGE, click -> selectTab(tab))
                : flatHitButton(0, TAB_ENLARGE, w, h, click -> selectTab(tab));
        hit.setHoverTexture(GlowShaderHelper.hoverGlow());
        hit.setClickedTexture(SurfaceFactory.fill(withAlpha(TabletColors.INTERACTIVE, 82)));
        hit.setHoverTooltips(Component.translatable(tab.labelKey()));
        container.addWidget(bg);
        container.addWidget(text);
        container.addWidget(hit);
        return container;
    }

    private WidgetGroup enlargedTabBg(int w, int fill, int border) {
        return new WidgetGroup(0, 0, w, TAB_H) {
            @Override
            public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                long elapsed = System.currentTimeMillis() - state.settings.tabAnimationStartMs;
                float t = Math.max(0f, Math.min(1f, (float) elapsed / (float) TAB_ANIM_MS));
                float eased = 1f - (1f - t) * (1f - t) * (1f - t);
                int grow = (int) (TAB_ENLARGE * eased);
                SurfaceFactory.bordered(fill, border).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY() + TAB_ENLARGE - grow, getSizeWidth(), TAB_H + grow);
            }
        };
    }

    private void selectTab(SettingsTabDescriptor tab) {
        if (tab == null || state.settings.currentTab == tab.id()) {
            return;
        }
        state.settings.currentTab = tab.id();
        state.settings.scroll = 0;
        state.settings.scrollDragging = false;
        state.modal.themeScroll = 0;
        state.modal.themeScrollDragging = false;
        state.settings.tabAnimationStartMs = System.currentTimeMillis();
        QuestsAndStuffMod.debugLog("[QnS:UI] settings tab selected tab={}", tab.logName());
        refresh.run();
    }

    private static int fontWidth(String text, int maxWidth) {
        if (text == null) {
            return 0;
        }
        int width = Minecraft.getInstance().font.width(text);
        if (width <= maxWidth) {
            return text.length();
        }
        int fit = text.length();
        while (fit > 0 && Minecraft.getInstance().font.width(text.substring(0, fit) + "...") > maxWidth) {
            fit--;
        }
        return Math.max(1, fit);
    }
}
