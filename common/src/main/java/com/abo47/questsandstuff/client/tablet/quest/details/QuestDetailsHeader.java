package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.controls.TabletIconTextButton;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.runtime.C2STogglePinPacket;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

final class QuestDetailsHeader {
    private QuestDetailsHeader() {
    }

    static int renderCanvasHeader(WidgetGroup canvasPanel, TabletUiState state, Player player, Runnable refresh, String questId, int viewportX, int viewportW) {
        WidgetGroup headerSurface = new WidgetGroup(viewportX, QuestDetailsWindow.TOP_Y, viewportW, QuestDetailsWindow.HEADER_H);
        headerSurface.setBackground(Surfaces.fill(ModColors.SURFACE_PANEL));
        canvasPanel.addWidget(headerSurface);

        int closeX = viewportX + viewportW - QuestDetailsWindow.TOOL_SIZE;
        int toolsX = closeX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE;
        boolean showEditor = QuestDetailsEditState.editorAvailable(state);
        int editorX = showEditor ? toolsX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE : toolsX;
        int navigationRightX = showEditor ? editorX : toolsX;
        int pinX = navigationRightX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE;
        int nextX = pinX - QuestDetailsWindow.HEADER_GAP - QuestDetailsWindow.TOOL_SIZE;
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
        boolean pinned = ClientQuestCache.pinned().contains(questId);
        addHeaderIconButton(canvasPanel, pinX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "window_pin", pinned ? ModColors.SUCCESS : ModColors.INTERACTIVE, pinned, click -> {
            ClientQuestCache.togglePinnedLocal(questId);
            ModNetwork.sendToServer(new C2STogglePinPacket(questId));
            QuestDetailsTransientState.closeContext(state);
            refresh.run();
        });
        addHeaderIconButton(canvasPanel, toolsX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "tools", state.questDetails.questDetailsToolsOpen ? ModColors.SUCCESS : ModColors.INTERACTIVE, state.questDetails.questDetailsToolsOpen, click -> {
            ToolMenuAnimation.toggleQuestDetails(state);
            QuestDetailsTransientState.closeContext(state);
            refresh.run();
        });
        if (showEditor) {
            addHeaderIconButton(canvasPanel, editorX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "editor", state.questDetails.questDetailsEditMode ? ModColors.SUCCESS : ModColors.ERROR, state.questDetails.questDetailsEditMode, click -> {
                if (!QuestDetailsEditState.toggle(state)) {
                    return;
                }
                ToolMenuAnimation.finishQuestDetails(state);
                if (!state.questDetails.questDetailsEditMode) {
                    QuestDetailsTransientState.closeFloatingPopups(state);
                    TextStyleSession.closeQuestDetails(state);
                    state.questDetails.questDetailsTitleFocused = false;
                    TextEditSession.closeQuestDetails(state, true);
                    if (questId.equals(state.questDetails.pendingQuestTitleChangeId)) {
                        state.questDetails.pendingQuestTitleChangeId = "";
                    }
                } else {
                    QuestDetailsTransientState.closeContext(state);
                }
                QuestsAndStuffMod.debugLog("[QnS:UI] quest details editor mode toggle enabled={}", state.questDetails.questDetailsEditMode);
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
        boolean framed = QuestDetailsEditState.canEdit(state);
        titleField.setBackground(framed
                ? Surfaces.bordered(ModColors.SURFACE_BASE, editing ? ModColors.INTERACTIVE : ModColors.BORDER_BASE)
                : Surfaces.transparentFill());
        titleField.setTextColor(ModColors.TEXT_PRIMARY);
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
        if (!QuestDetailsEditState.canEdit(state) || questId == null || questId.isBlank()) {
            return;
        }
        CompoundTag quest = ClientQuestCache.quest(questId);
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

    private static void addHeaderIconButton(WidgetGroup parent, int x, int y, int w, int h, String icon, int color, boolean active, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        int fill = active ? withAlpha(color, 38) : ModColors.SURFACE_PANEL_ALT;
        TabletIconTextButton.Visuals visuals = new TabletIconTextButton.Visuals(
                TabletIconTextButton.State.of(fill, active ? color : ModColors.BORDER_BASE, color),
                TabletIconTextButton.State.of(withAlpha(color, 66), ModColors.BORDER_ACCENT, color),
                TabletIconTextButton.State.of(withAlpha(color, 90), color, ModColors.TEXT_PRIMARY)
        );
        parent.addWidget(TabletIconTextButton.icon(x, y, w, h, icon, visuals, callback));
    }

}
