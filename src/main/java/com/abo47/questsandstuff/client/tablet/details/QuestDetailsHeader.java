package com.abo47.questsandstuff.client.tablet.details;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.tools.ToolMenuAnimation;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ACTION_ICON_SIZE;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class QuestDetailsHeader {
    private QuestDetailsHeader() {
    }

    static int renderCanvasHeader(WidgetGroup canvasPanel, TabletUiState state, Player player, Runnable refresh, String questId, int viewportX, int viewportW) {
        WidgetGroup headerSurface = new WidgetGroup(viewportX, QuestDetailsWindow.TOP_Y, viewportW, QuestDetailsWindow.HEADER_H);
        headerSurface.setBackground(Surfaces.fill(ModColors.SURFACE_PANEL));
        canvasPanel.addWidget(headerSurface);

        int closeX = viewportX + viewportW - QuestDetailsWindow.TOOL_SIZE;
        int toolsX = closeX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE;
        boolean showEditor = state.editorAvailable;
        int editorX = showEditor ? toolsX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE : toolsX;
        int settingsX = (showEditor ? editorX : toolsX) - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE;
        int nextX = settingsX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE;
        int previousX = nextX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE;
        int titleW = Math.max(24, previousX - QuestDetailsWindow.HEADER_GAP - viewportX);
        addQuestTitleField(canvasPanel, state, player, refresh, questId, viewportX, QuestDetailsWindow.TOP_Y, titleW, QuestDetailsWindow.HEADER_H);
        addHeaderIconButton(canvasPanel, previousX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "back", ModColors.INTERACTIVE, false, click -> {
            QuestDetailsWindow.openAdjacentQuest(state, questId, -1);
            ToolMenuAnimation.finishQuestDetails(state);
            QuestDetailsTransientState.closeContext(state);
            refresh.run();
        });
        addHeaderIconButton(canvasPanel, nextX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "chevron-right", ModColors.INTERACTIVE, false, click -> {
            QuestDetailsWindow.openAdjacentQuest(state, questId, 1);
            ToolMenuAnimation.finishQuestDetails(state);
            QuestDetailsTransientState.closeContext(state);
            refresh.run();
        });
        addHeaderIconButton(canvasPanel, toolsX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "tools", state.questDetailsToolsOpen ? ModColors.SUCCESS : ModColors.INTERACTIVE, state.questDetailsToolsOpen, click -> {
            ToolMenuAnimation.toggleQuestDetails(state);
            QuestDetailsTransientState.closeContext(state);
            refresh.run();
        });
        addHeaderIconButton(canvasPanel, settingsX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "settings-2", state.settingsPanelOpen ? ModColors.SUCCESS : ModColors.INTERACTIVE, state.settingsPanelOpen, new Component[]{
                Component.translatable("ui.questsandstuff.settings.button"),
                Component.translatable("ui.questsandstuff.settings.button_tooltip")
        }, click -> toggleSettingsPanel(state, refresh));
        if (showEditor) {
            addHeaderIconButton(canvasPanel, editorX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "editor", state.questDetailsEditMode ? ModColors.SUCCESS : ModColors.ERROR, state.questDetailsEditMode, click -> {
                if (!state.canEdit) {
                    return;
                }
                state.questDetailsEditMode = !state.questDetailsEditMode;
                ToolMenuAnimation.finishQuestDetails(state);
                QuestDetailsTransientState.closeContext(state);
                state.questDetailsTextStyleOpen = false;
                refresh.run();
            });
        }
        addHeaderIconButton(canvasPanel, closeX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "close", ModColors.ERROR, false, click -> {
            QuestDetailsWindow.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details close quest={}", questId);
            refresh.run();
        });
        return toolsX;
    }

    private static void addQuestTitleField(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, int x, int y, int w, int h) {
        CompoundTag quest = ClientQuestCache.quest(questId);
        String title = quest == null ? "" : quest.getString("title");
        if (!state.questDetailsTitleFocused || !questId.equals(state.pendingQuestRenameId)) {
            state.questTitleDraft = title;
        }
        InlineRenameField titleField = new InlineRenameField(
                x,
                y,
                w,
                h,
                () -> titleDraft(state),
                value -> state.questTitleDraft = sanitizeTitleDraft(value),
                () -> {
                    commitQuestTitle(player, state, questId);
                    state.questDetailsTitleFocused = false;
                    refresh.run();
                },
                () -> {
                    state.pendingQuestRenameId = "";
                    state.questTitleDraft = title;
                    state.questDetailsTitleFocused = false;
                    refresh.run();
                },
                () -> {
                    if (questId.equals(state.pendingQuestRenameId)) {
                        commitQuestTitle(player, state, questId);
                        refresh.run();
                    }
                },
                focused -> {
                    if (focused && !questId.equals(state.pendingQuestRenameId)) {
                        state.pendingQuestRenameId = questId;
                        state.questTitleDraft = title;
                    }
                    state.questDetailsTitleFocused = focused;
                }
        );
        titleField.setClientSideWidget();
        titleField.setCurrentString(titleDraft(state));
        titleField.setMaxStringLength(80);
        titleField.setBordered(false);
        boolean editing = state.questDetailsTitleFocused && questId.equals(state.pendingQuestRenameId);
        boolean framed = state.canEdit && state.questDetailsEditMode;
        titleField.setBackground(framed
                ? Surfaces.bordered(ModColors.SURFACE_BASE, editing ? ModColors.INTERACTIVE : ModColors.BORDER_BASE)
                : Surfaces.fill(0x00000000));
        titleField.setTextColor(ModColors.TEXT_PRIMARY);
        if (editing) {
            titleField.setFocus(true);
        }
        parent.addWidget(titleField);
    }

    private static String titleDraft(TabletUiState state) {
        return state.questTitleDraft == null ? "" : state.questTitleDraft;
    }

    private static String sanitizeTitleDraft(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }

    private static void commitQuestTitle(Player player, TabletUiState state, String questId) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestCache.quest(questId);
        if (quest == null) {
            return;
        }
        String oldTitle = quest.getString("title");
        String title = sanitizeTitleDraft(state.questTitleDraft).trim();
        if (player != null && !title.equals(oldTitle)) {
            EditorCommandClient.updateQuestDisplay(player, questId, title, quest.getString("subtitle"));
        }
        state.pendingQuestRenameId = "";
        state.questTitleDraft = title;
    }

    private static void addHeaderIconButton(WidgetGroup parent, int x, int y, int w, int h, String icon, int color, boolean active, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        addHeaderIconButton(parent, x, y, w, h, icon, color, active, null, callback);
    }

    private static void addHeaderIconButton(WidgetGroup parent, int x, int y, int w, int h, String icon, int color, boolean active, Component[] tooltips, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        int fill = active ? withAlpha(color, 38) : ModColors.SURFACE_PANEL_ALT;
        parent.addWidget(panel(x, y, w, h, fill, active ? color : ModColors.BORDER_BASE));
        int iconSize = Math.min(ACTION_ICON_SIZE, Math.max(8, Math.min(w, h) - 4));
        parent.addWidget(new ImageWidget(x + (w - iconSize) / 2, y + (h - iconSize) / 2, iconSize, iconSize, () -> UiIconAtlas.iconTexture(icon)));
        var hit = flatHitButton(x, y, w, h, callback);
        hit.setHoverTexture(Surfaces.bordered(withAlpha(color, 66), ModColors.BORDER_ACCENT));
        hit.setClickedTexture(Surfaces.fill(withAlpha(color, 90)));
        if (tooltips != null) {
            hit.setHoverTooltips(tooltips);
        }
        parent.addWidget(hit);
    }

    private static void toggleSettingsPanel(TabletUiState state, Runnable refresh) {
        if (state.settingsPanelOpen) {
            ModalCloseActions.closeAll(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details settings toggle open=false");
            refresh.run();
            return;
        }
        ToolMenuAnimation.closeQuestDetails(state);
        state.questDetailsTextStyleOpen = false;
        QuestDetailsTransientState.closeContext(state);
        ModalOpenActions.openSettingsPanel(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details settings toggle open=true");
        refresh.run();
    }
}
