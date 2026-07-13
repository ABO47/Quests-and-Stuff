package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.controls.TabletIconTextButton;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.skin.SkinAnchorRegistry;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.runtime.C2STogglePinPacket;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

final class QuestDetailsHeader {
    private QuestDetailsHeader() {
    }

    static int renderCanvasHeader(WidgetGroup canvasPanel, TabletUiState state, Player player, Runnable refresh, String questId, int viewportX, int viewportW) {
        int closeX = viewportX + viewportW - QuestDetailsWindow.TOOL_SIZE;
        int toolsX = closeX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE;
        boolean showEditor = QuestDetailsEditController.editorAvailable(state);
        int editorX = showEditor ? toolsX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE : toolsX;
        int navigationRightX = showEditor ? editorX : toolsX;
        int pinX = navigationRightX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE;
        int nextX = pinX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE;
        int previousX = nextX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE;
        int titleW = Math.max(24, previousX - QuestDetailsWindow.HEADER_GAP - viewportX);
        addQuestTitleField(canvasPanel, state, player, refresh, questId, viewportX, QuestDetailsWindow.TOP_Y, titleW, QuestDetailsWindow.HEADER_H);
        SkinAnchorRegistry.register("quest_details_back", addHeaderIconButton(canvasPanel, previousX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "back", TabletColors.INTERACTIVE, false, click -> {
            QuestDetailsWindow.openAdjacentQuest(state, questId, -1);
            ToolMenuAnimation.finishQuestDetails(state);
            QuestDetailsTransientManager.closeContext(state);
            refresh.run();
        }));
        SkinAnchorRegistry.register("quest_details_forward", addHeaderIconButton(canvasPanel, nextX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "chevron-right", TabletColors.INTERACTIVE, false, click -> {
            QuestDetailsWindow.openAdjacentQuest(state, questId, 1);
            ToolMenuAnimation.finishQuestDetails(state);
            QuestDetailsTransientManager.closeContext(state);
            refresh.run();
        }));
        boolean pinned = ClientQuestStateFacade.pinned().contains(questId);
        SkinAnchorRegistry.register("quest_details_pin", addHeaderIconButton(canvasPanel, pinX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "window_pin", pinned ? TabletColors.SUCCESS : TabletColors.INTERACTIVE, pinned, click -> {
            ClientQuestStateFacade.togglePinnedLocal(questId);
            ModNetwork.sendToServer(new C2STogglePinPacket(questId));
            QuestDetailsTransientManager.closeContext(state);
            refresh.run();
        }));
        SkinAnchorRegistry.register("quest_details_tools", addHeaderIconButton(canvasPanel, toolsX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "tools", state.questDetails.questDetailsToolsOpen ? TabletColors.SUCCESS : TabletColors.INTERACTIVE, state.questDetails.questDetailsToolsOpen, click -> {
            ToolMenuAnimation.toggleQuestDetails(state);
            QuestDetailsTransientManager.closeContext(state);
            refresh.run();
        }));
        if (showEditor) {
            SkinAnchorRegistry.register("quest_details_editor", addHeaderIconButton(canvasPanel, editorX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "editor", state.questDetails.questDetailsEditMode ? TabletColors.SUCCESS : TabletColors.ERROR, state.questDetails.questDetailsEditMode, click -> {
                if (!QuestDetailsEditController.toggle(state)) {
                    return;
                }
                ToolMenuAnimation.finishQuestDetails(state);
                if (!state.questDetails.questDetailsEditMode) {
                    QuestDetailsTransientManager.closeFloatingPopups(state);
                    TextStyleSession.closeQuestDetails(state);
                    state.questDetails.questDetailsTitleFocused = false;
                    TextEditSession.closeQuestDetails(state, true);
                    if (questId.equals(state.questDetails.pendingQuestTitleChangeId)) {
                        state.questDetails.pendingQuestTitleChangeId = "";
                    }
                } else {
                    QuestDetailsTransientManager.closeContext(state);
                }
                QuestsAndStuffMod.debugLog("[QnS:UI] quest details editor mode toggle enabled={}", state.questDetails.questDetailsEditMode);
                refresh.run();
            }));
        }
        SkinAnchorRegistry.register("quest_details_close", addHeaderIconButton(canvasPanel, closeX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "close", TabletColors.ERROR, false, click -> {
            QuestDetailsWindow.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details close quest={}", questId);
            refresh.run();
        }));
        return toolsX;
    }

    private static void addQuestTitleField(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, String questId, int x, int y, int w, int h) {
        CompoundTag quest = ClientQuestStateFacade.quest(questId);
        String title = quest == null ? "" : quest.getString("title");
        if (!state.questDetails.questDetailsTitleFocused || !questId.equals(state.questDetails.pendingQuestTitleChangeId)) {
            state.questDetails.questTitleDraft = title;
        }
        InlineRenameField titleField = new InlineRenameField(
                x,
                y,
                w,
                h,
                () -> titleDraft(state),
                value -> state.questDetails.questTitleDraft = sanitizeTitleDraft(value),
                () -> {
                    commitQuestTitle(player, state, questId);
                    state.questDetails.questDetailsTitleFocused = false;
                    refresh.run();
                },
                () -> {
                    state.questDetails.pendingQuestTitleChangeId = "";
                    state.questDetails.questTitleDraft = title;
                    state.questDetails.questDetailsTitleFocused = false;
                    refresh.run();
                },
                () -> {
                    if (questId.equals(state.questDetails.pendingQuestTitleChangeId)) {
                        commitQuestTitle(player, state, questId);
                        refresh.run();
                    }
                },
                focused -> {
                    if (focused && !questId.equals(state.questDetails.pendingQuestTitleChangeId)) {
                        state.questDetails.pendingQuestTitleChangeId = questId;
                        state.questDetails.questTitleDraft = title;
                    }
                    state.questDetails.questDetailsTitleFocused = focused;
                }
        );
        titleField.setClientSideWidget();
        titleField.setCurrentString(titleDraft(state));
        titleField.setMaxStringLength(80);
        titleField.setBordered(false);
        boolean editing = state.questDetails.questDetailsTitleFocused && questId.equals(state.questDetails.pendingQuestTitleChangeId);
        boolean framed = QuestDetailsEditController.canEdit(state);
        titleField.setBackground(framed
                ? SurfaceFactory.bordered(TabletColors.SURFACE_BASE, editing ? TabletColors.INTERACTIVE : TabletColors.BORDER_BASE)
                : SurfaceFactory.transparentFill());
        titleField.setTextColor(TabletColors.TEXT_PRIMARY);
        titleField.setActive(framed);
        if (editing) {
            titleField.setFocus(true);
        }
        parent.addWidget(titleField);
    }

    private static String titleDraft(TabletUiState state) {
        return state.questDetails.questTitleDraft == null ? "" : state.questDetails.questTitleDraft;
    }

    private static String sanitizeTitleDraft(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }

    private static void commitQuestTitle(Player player, TabletUiState state, String questId) {
        if (!QuestDetailsEditController.canEdit(state) || questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestStateFacade.quest(questId);
        if (quest == null) {
            return;
        }
        String oldTitle = quest.getString("title");
        String title = sanitizeTitleDraft(state.questDetails.questTitleDraft).trim();
        if (player != null && !title.equals(oldTitle)) {
            EditorQuestCommandClient.updateQuestDisplay(player, questId, title, quest.getString("subtitle"));
        }
        state.questDetails.pendingQuestTitleChangeId = "";
        state.questDetails.questTitleDraft = title;
    }

    static ButtonWidget addHeaderIconButton(WidgetGroup parent, int x, int y, int w, int h, String icon, int color, boolean active, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        int fill = TabletColors.SURFACE_PANEL_ALT;
        TabletIconTextButton.Visuals visuals = new TabletIconTextButton.Visuals(
                TabletIconTextButton.State.of(fill, active ? color : TabletColors.BORDER_BASE, color),
                TabletIconTextButton.State.of(TabletColors.hoverFill(color), TabletColors.BORDER_ACCENT, color),
                TabletIconTextButton.State.of(TabletColors.pressedFill(color), color, TabletColors.TEXT_PRIMARY),
                active ? color : -1
        );
        ButtonWidget btn = TabletIconTextButton.icon(x, y, w, h, icon, visuals, callback);
        parent.addWidget(btn);
        return btn;
    }

}
