package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.controls.ToggleSwitchWidget;
import com.abo47.questsandstuff.client.tablet.screen.TabletClientHooks;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class TabletSettingsModal {
    private static final int PAD = 8;
    private static final int TAB_Y = 22;
    private static final int TAB_H = 20;
    private static final int TAB_GAP = 4;
    private static final int LIST_Y = 50;
    private static final int ROW_H = 26;
    private static final int ROW_INSET = 4;
    private static final int SWITCH_GAP = 8;
    private static final int TAB_THEMES = 0;
    private static final int TAB_CANVAS = 1;
    private static final int TAB_HUD = 2;
    private static final int TAB_ANIMATIONS = 3;
    private static final int TAB_DEBUG = 4;
    private static final List<SettingTab> TABS = List.of(
            new SettingTab(TAB_THEMES, "themes", "ui.questsandstuff.settings.tab_themes"),
            new SettingTab(TAB_CANVAS, "canvas", "ui.questsandstuff.settings.tab_canvas"),
            new SettingTab(TAB_HUD, "hud", "ui.questsandstuff.settings.tab_hud"),
            new SettingTab(TAB_ANIMATIONS, "animations", "ui.questsandstuff.settings.tab_animations"),
            new SettingTab(TAB_DEBUG, "debug", "ui.questsandstuff.settings.tab_debug")
    );

    private TabletSettingsModal() {
    }

    public static void rebuild(WidgetGroup modal, TabletUiState state, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, TabletModalPanel.tr("ui.questsandstuff.settings.title"), w, state, refresh);
        state.settingsTab = activeTab(state.settingsTab);
        addTabs(modal, state, refresh, w);

        int listX = PAD;
        int listW = Math.max(32, w - PAD * 2);
        int listH = Math.max(1, h - LIST_Y - PAD);
        if (state.settingsTab == TAB_THEMES) {
            TabletThemePickerModal.addSettingsThemeList(
                    modal,
                    state,
                    refresh,
                    listX,
                    LIST_Y,
                    listW,
                    listH,
                    TabletModalPanel.tr("ui.questsandstuff.settings.themes_empty")
            );
            return;
        }
        PickerListPanel.add(
                modal,
                listX,
                LIST_Y,
                listW,
                listH,
                ROW_H,
                options(state.settingsTab, state),
                TabletModalPanel.tr("ui.questsandstuff.settings.empty"),
                ScrollState.bind(
                        () -> state.settingsScroll,
                        value -> state.settingsScroll = value,
                        () -> state.settingsScrollDragging,
                        dragging -> state.settingsScrollDragging = dragging
                ),
                2,
                refresh,
                (list, option, index, rowY, rowW) -> renderOptionRow(list, option, rowY, rowW, refresh)
        );
    }

    private static int activeTab(int tab) {
        if (tab == TAB_THEMES || tab == TAB_CANVAS || tab == TAB_HUD || tab == TAB_ANIMATIONS || tab == TAB_DEBUG) {
            return tab;
        }
        return TAB_THEMES;
    }

    private static void addTabs(WidgetGroup modal, TabletUiState state, Runnable refresh, int w) {
        int totalW = Math.max(1, w - PAD * 2);
        int count = Math.max(1, TABS.size());
        int available = Math.max(count, totalW - TAB_GAP * (count - 1));
        int tabW = Math.max(1, available / count);
        int remainder = Math.max(0, available - tabW * count);
        int tabX = PAD;
        for (int i = 0; i < count; i++) {
            SettingTab tab = TABS.get(i);
            int currentW = tabW + (i < remainder ? 1 : 0);
            addTab(modal, state, refresh, tabX, currentW, tab);
            tabX += currentW + TAB_GAP;
        }
    }

    private static void addTab(WidgetGroup modal, TabletUiState state, Runnable refresh, int x, int w, SettingTab tab) {
        boolean active = state.settingsTab == tab.id();
        int tabY = active ? TAB_Y : TAB_Y + 3;
        int tabH = active ? TAB_H : TAB_H - 3;
        int fill = active ? withAlpha(ModColors.SURFACE_BASE, 250) : withAlpha(ModColors.SURFACE_PANEL_ALT, 142);
        int border = active ? ModColors.BORDER_ACCENT : ModColors.BORDER_BASE;
        addTabShadow(modal, x, tabY, w, tabH, active);
        modal.addWidget(panel(x, tabY, w, tabH, fill, border));
        modal.addWidget(label(x + 8, tabY + 6, SearchFilter.crop(TabletModalPanel.tr(tab.labelKey()), Math.max(8, (w - 16) / 6)), active ? ModColors.TEXT_PRIMARY : ModColors.TEXT_MUTED));
        ButtonWidget hit = flatHitButton(x, tabY, w, tabH, click -> {
            if (state.settingsTab == tab.id()) {
                return;
            }
            state.settingsTab = tab.id();
            state.settingsScroll = 0;
            state.settingsScrollDragging = false;
            state.themeScroll = 0;
            state.themeScrollDragging = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] settings tab selected tab={}", tab.logName());
            refresh.run();
        });
        hit.setHoverTexture(Surfaces.bordered(withAlpha(ModColors.INTERACTIVE, active ? 72 : 42), ModColors.BORDER_ACCENT));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 82)));
        hit.setHoverTooltips(Component.translatable(tab.labelKey()));
        modal.addWidget(hit);
    }

    private static void addTabShadow(WidgetGroup modal, int x, int y, int w, int h, boolean active) {
        WidgetGroup cast = new WidgetGroup(x + 3, y + 4, w, h);
        cast.setBackground(Surfaces.fill(withAlpha(ModColors.SURFACE_BASE, active ? 126 : 78)));
        modal.addWidget(cast);

        WidgetGroup soft = new WidgetGroup(x + 1, y + 2, w, h);
        soft.setBackground(Surfaces.fill(withAlpha(ModColors.SURFACE_PANEL, active ? 58 : 34)));
        modal.addWidget(soft);
    }

    private static List<SettingOption> options(int tab, TabletUiState state) {
        if (tab == TAB_DEBUG) {
            return debugOptions();
        }
        if (tab == TAB_CANVAS) {
            return canvasOptions(state);
        }
        if (tab == TAB_HUD) {
            return hudOptions();
        }
        return animationOptions();
    }

    private static List<SettingOption> canvasOptions(TabletUiState state) {
        return List.of(
                new SettingOption(
                        "fullScreenMode",
                        "ui.questsandstuff.settings.full_screen_mode",
                        "ui.questsandstuff.settings.full_screen_mode_desc",
                        QuestsAndStuffConfig::fullScreenModeEnabled,
                        enabled -> setFullScreenMode(state, enabled),
                        false,
                        false
                ),
                new SettingOption(
                        "minimap",
                        "ui.questsandstuff.settings.minimap",
                        "ui.questsandstuff.settings.minimap_desc",
                        QuestsAndStuffConfig::minimapEnabled,
                        QuestsAndStuffConfig::setMinimapEnabled,
                        false,
                        false
                ),
                new SettingOption(
                        "readOnlyCanvasFocus",
                        "ui.questsandstuff.settings.read_only_canvas_focus",
                        "ui.questsandstuff.settings.read_only_canvas_focus_desc",
                        QuestsAndStuffConfig::readOnlyCanvasFocusEnabled,
                        QuestsAndStuffConfig::setReadOnlyCanvasFocusEnabled,
                        false,
                        false
                ),
                new SettingOption(
                        "questEffectIcons",
                        "ui.questsandstuff.settings.quest_effect_icons",
                        "ui.questsandstuff.settings.quest_effect_icons_desc",
                        QuestsAndStuffConfig::questEffectIconsEnabled,
                        QuestsAndStuffConfig::setQuestEffectIconsEnabled,
                        false,
                        false
                )
        );
    }

    private static List<SettingOption> hudOptions() {
        return List.of(
                new SettingOption(
                        "completionHud",
                        "ui.questsandstuff.settings.completion_hud",
                        "ui.questsandstuff.settings.completion_hud_desc",
                        QuestsAndStuffConfig::completionHudEnabled,
                        QuestsAndStuffConfig::setCompletionHudEnabled,
                        false,
                        false
                ),
                new SettingOption(
                        "completionHudSound",
                        "ui.questsandstuff.settings.completion_hud_sound",
                        "ui.questsandstuff.settings.completion_hud_sound_desc",
                        QuestsAndStuffConfig::completionHudSoundEnabled,
                        QuestsAndStuffConfig::setCompletionHudSoundEnabled,
                        false,
                        false
                ),
                new SettingOption(
                        "completionHudDurationMs",
                        "ui.questsandstuff.settings.completion_hud_duration",
                        "ui.questsandstuff.settings.completion_hud_duration_desc",
                        QuestsAndStuffConfig::completionHudDurationMs,
                        QuestsAndStuffConfig::setCompletionHudDurationMs,
                        QuestsAndStuffConfig.MIN_COMPLETION_HUD_DURATION_MS,
                        QuestsAndStuffConfig.MAX_COMPLETION_HUD_DURATION_MS,
                        5,
                        false
                )
        );
    }

    private static void setFullScreenMode(TabletUiState state, boolean enabled) {
        QuestsAndStuffConfig.setFullScreenModeEnabled(enabled);
        TabletClientHooks.applyQuestTabletLayoutMode(state);
    }

    private static List<SettingOption> animationOptions() {
        return List.of(
                new SettingOption(
                        "uiAnimations",
                        "ui.questsandstuff.settings.ui_animations",
                        "ui.questsandstuff.settings.ui_animations_desc",
                        QuestsAndStuffConfig::uiAnimationsEnabled,
                        QuestsAndStuffConfig::setUiAnimationsEnabled,
                        false,
                        false
                ),
                new SettingOption(
                        "contextMenuAnimations",
                        "ui.questsandstuff.settings.context_menu_animations",
                        "ui.questsandstuff.settings.context_menu_animations_desc",
                        QuestsAndStuffConfig::contextMenuAnimationSettingEnabled,
                        QuestsAndStuffConfig::setContextMenuAnimationsEnabled,
                        false,
                        true
                ),
                new SettingOption(
                        "toolsMenuAnimations",
                        "ui.questsandstuff.settings.tools_menu_animations",
                        "ui.questsandstuff.settings.tools_menu_animations_desc",
                        QuestsAndStuffConfig::toolsMenuAnimationSettingEnabled,
                        QuestsAndStuffConfig::setToolsMenuAnimationsEnabled,
                        false,
                        true
                ),
                new SettingOption(
                        "minimapAnimations",
                        "ui.questsandstuff.settings.minimap_animations",
                        "ui.questsandstuff.settings.minimap_animations_desc",
                        QuestsAndStuffConfig::minimapAnimationSettingEnabled,
                        QuestsAndStuffConfig::setMinimapAnimationsEnabled,
                        false,
                        true
                ),
                new SettingOption(
                        "questWindowAnimations",
                        "ui.questsandstuff.settings.quest_window_animations",
                        "ui.questsandstuff.settings.quest_window_animations_desc",
                        QuestsAndStuffConfig::questWindowAnimationSettingEnabled,
                        QuestsAndStuffConfig::setQuestWindowAnimationsEnabled,
                        false,
                        true
                ),
                new SettingOption(
                        "popupWindowAnimations",
                        "ui.questsandstuff.settings.popup_window_animations",
                        "ui.questsandstuff.settings.popup_window_animations_desc",
                        QuestsAndStuffConfig::popupWindowAnimationSettingEnabled,
                        QuestsAndStuffConfig::setPopupWindowAnimationsEnabled,
                        false,
                        true
                ),
                new SettingOption(
                        "connectionAnimations",
                        "ui.questsandstuff.settings.connection_animations",
                        "ui.questsandstuff.settings.connection_animations_desc",
                        QuestsAndStuffConfig::connectionAnimationSettingEnabled,
                        QuestsAndStuffConfig::setConnectionAnimationsEnabled,
                        false,
                        true
                ),
                new SettingOption(
                        "chapterSwitchAnimations",
                        "ui.questsandstuff.settings.chapter_switch_animations",
                        "ui.questsandstuff.settings.chapter_switch_animations_desc",
                        QuestsAndStuffConfig::chapterSwitchAnimationSettingEnabled,
                        QuestsAndStuffConfig::setChapterSwitchAnimationsEnabled,
                        false,
                        true
                )
        );
    }

    private static List<SettingOption> debugOptions() {
        return List.of(
                new SettingOption(
                        "debugLogging",
                        "ui.questsandstuff.settings.debug_logging",
                        "ui.questsandstuff.settings.debug_logging_desc",
                        QuestsAndStuffConfig::debugLoggingEnabled,
                        QuestsAndStuffConfig::setDebugLoggingEnabled,
                        false,
                        false
                )
        );
    }

    private static void renderOptionRow(WidgetGroup list, SettingOption option, int rowY, int rowW, Runnable refresh) {
        if (option.number()) {
            renderNumberOptionRow(list, option, rowY, rowW, refresh);
            return;
        }
        boolean enabled = option.enabled();
        int rowX = ROW_INSET;
        int rowH = ROW_H - ROW_INSET;
        int cardW = Math.max(1, rowW - ROW_INSET * 2);
        int fill = enabled ? withAlpha(ModColors.SUCCESS, 28) : withAlpha(ModColors.SURFACE_PANEL_ALT, 180);
        int border = enabled ? withAlpha(ModColors.SUCCESS, 170) : ModColors.BORDER_BASE;
        list.addWidget(panel(rowX, rowY, cardW, rowH, fill, border));

        Component[] tooltips = tooltips(option);
        ButtonWidget hit = flatHitButton(rowX, rowY, cardW, rowH, click -> toggle(option, refresh));
        hit.setHoverTexture(Surfaces.transparentBorder(ModColors.BORDER_ACCENT));
        hit.setClickedTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 64)));
        hit.setHoverTooltips(tooltips);

        int switchX = Math.max(rowX + 104, rowX + cardW - ToggleSwitchWidget.DEFAULT_WIDTH - SWITCH_GAP);
        int textW = Math.max(16, switchX - rowX - 14);
        int crop = Math.max(14, textW / 6);
        int titleColor = enabled ? ModColors.TEXT_PRIMARY : ModColors.TEXT_SECONDARY;
        list.addWidget(label(rowX + 8, rowY + 7, SearchFilter.crop(TabletModalPanel.tr(option.labelKey()), crop), titleColor));
        list.addWidget(new ToggleSwitchWidget(
                option.id(),
                switchX,
                rowY + 3,
                ToggleSwitchWidget.DEFAULT_WIDTH,
                ToggleSwitchWidget.DEFAULT_HEIGHT,
                option::enabled,
                value -> setOption(option, value),
                refresh,
                tooltips
        ));
        list.addWidget(hit);
    }

    private static void renderNumberOptionRow(WidgetGroup list, SettingOption option, int rowY, int rowW, Runnable refresh) {
        int rowX = ROW_INSET;
        int rowH = ROW_H - ROW_INSET;
        int cardW = Math.max(1, rowW - ROW_INSET * 2);
        list.addWidget(panel(rowX, rowY, cardW, rowH, withAlpha(ModColors.SURFACE_PANEL_ALT, 180), ModColors.BORDER_BASE));

        Component[] tooltips = tooltips(option);
        int unitW = 18;
        int fieldW = 54;
        int fieldX = Math.max(rowX + 104, rowX + cardW - fieldW - unitW - SWITCH_GAP);
        int textW = Math.max(16, fieldX - rowX - 14);
        int crop = Math.max(14, textW / 6);
        list.addWidget(label(rowX + 8, rowY + 7, SearchFilter.crop(TabletModalPanel.tr(option.labelKey()), crop), ModColors.TEXT_SECONDARY));

        final TextFieldWidget[] fieldRef = new TextFieldWidget[1];
        Runnable commit = () -> {
            int next = parseNumber(fieldRef[0], option.intValue(), option.min(), option.max());
            if (next == option.intValue()) {
                if (fieldRef[0] != null) {
                    fieldRef[0].setCurrentString(Integer.toString(next));
                }
                return;
            }
            option.setIntValue(next);
            QuestsAndStuffMod.debugLog("[QnS:UI] settings number {}={}", option.id(), next);
            refresh.run();
        };
        TextFieldWidget field = StyledTextFields.numberField(
                fieldX,
                rowY + 4,
                fieldW,
                14,
                option.intValue(),
                option.min(),
                option.max(),
                option.maxLength(),
                raw -> {
                },
                commit,
                () -> {
                },
                commit
        );
        field.setHoverTooltips(tooltips);
        fieldRef[0] = field;
        list.addWidget(field);
        list.addWidget(label(fieldX + fieldW + 4, rowY + 7, TabletModalPanel.tr("ui.questsandstuff.settings.duration_unit_ms"), ModColors.TEXT_MUTED));
    }

    private static void toggle(SettingOption option, Runnable refresh) {
        boolean from = option.enabled();
        boolean to = !from;
        ToggleSwitchWidget.beginAnimation(option.id(), from, to);
        setOption(option, to);
        refresh.run();
    }

    private static void setOption(SettingOption option, boolean enabled) {
        if (option.enabled() == enabled) {
            return;
        }
        option.setEnabled(enabled);
        QuestsAndStuffMod.debugLog("[QnS:UI] settings toggle {}={}", option.id(), enabled);
    }

    private static int parseNumber(TextFieldWidget field, int fallback, int min, int max) {
        if (field == null || field.getRawCurrentString() == null || field.getRawCurrentString().isBlank()) {
            return Math.max(min, Math.min(max, fallback));
        }
        try {
            return Math.max(min, Math.min(max, Integer.parseInt(field.getRawCurrentString().trim())));
        } catch (NumberFormatException ignored) {
            return Math.max(min, Math.min(max, fallback));
        }
    }

    private static Component[] tooltips(SettingOption option) {
        if (!option.requiresGlobalAnimation()) {
            return new Component[]{
                    Component.translatable(option.labelKey()).withStyle(ChatFormatting.WHITE),
                    Component.translatable(option.descriptionKey()).withStyle(ChatFormatting.GRAY),
                    restartTooltip(option)
            };
        }
        return new Component[]{
                Component.translatable(option.labelKey()).withStyle(ChatFormatting.WHITE),
                Component.translatable(option.descriptionKey()).withStyle(ChatFormatting.GRAY),
                Component.translatable("ui.questsandstuff.settings.requires_global").withStyle(ChatFormatting.DARK_GRAY),
                restartTooltip(option)
        };
    }

    private static Component restartTooltip(SettingOption option) {
        return Component.translatable(option.restartRequired()
                        ? "ui.questsandstuff.settings.restart_required"
                        : "ui.questsandstuff.settings.restart_not_required")
                .withStyle(option.restartRequired() ? ChatFormatting.YELLOW : ChatFormatting.GREEN);
    }

    private record SettingOption(
            String id,
            String labelKey,
            String descriptionKey,
            SettingOptionKind kind,
            BooleanSupplier getter,
            Consumer<Boolean> setter,
            IntSupplier intGetter,
            IntConsumer intSetter,
            int min,
            int max,
            int maxLength,
            boolean restartRequired,
            boolean requiresGlobalAnimation
    ) {
        SettingOption(
                String id,
                String labelKey,
                String descriptionKey,
                BooleanSupplier getter,
                Consumer<Boolean> setter,
                boolean restartRequired,
                boolean requiresGlobalAnimation
        ) {
            this(
                    id,
                    labelKey,
                    descriptionKey,
                    SettingOptionKind.TOGGLE,
                    getter,
                    setter,
                    null,
                    null,
                    0,
                    0,
                    0,
                    restartRequired,
                    requiresGlobalAnimation
            );
        }

        SettingOption(
                String id,
                String labelKey,
                String descriptionKey,
                IntSupplier intGetter,
                IntConsumer intSetter,
                int min,
                int max,
                int maxLength,
                boolean restartRequired
        ) {
            this(
                    id,
                    labelKey,
                    descriptionKey,
                    SettingOptionKind.NUMBER,
                    null,
                    null,
                    intGetter,
                    intSetter,
                    min,
                    max,
                    maxLength,
                    restartRequired,
                    false
            );
        }

        boolean number() {
            return kind == SettingOptionKind.NUMBER;
        }

        boolean enabled() {
            return getter.getAsBoolean();
        }

        void setEnabled(boolean enabled) {
            setter.accept(enabled);
        }

        int intValue() {
            return intGetter.getAsInt();
        }

        void setIntValue(int value) {
            intSetter.accept(value);
        }
    }

    private record SettingTab(int id, String logName, String labelKey) {
    }

    private enum SettingOptionKind {
        TOGGLE,
        NUMBER
    }
}
