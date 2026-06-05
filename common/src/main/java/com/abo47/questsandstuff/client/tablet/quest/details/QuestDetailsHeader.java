package com.abo47.questsandstuff.client.tablet.quest.details;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.InlineRenameField;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.icons.UiIconAtlas;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.runtime.C2STogglePinPacket;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
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
        addHeaderIconButton(canvasPanel, toolsX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "tools", state.questDetailsToolsOpen ? ModColors.SUCCESS : ModColors.INTERACTIVE, state.questDetailsToolsOpen, click -> {
            ToolMenuAnimation.toggleQuestDetails(state);
            QuestDetailsTransientState.closeContext(state);
            refresh.run();
        });
        if (showEditor) {
            addHeaderIconButton(canvasPanel, editorX, QuestDetailsWindow.TOP_Y, QuestDetailsWindow.TOOL_SIZE, QuestDetailsWindow.HEADER_H, "editor", state.questDetailsEditMode ? ModColors.SUCCESS : ModColors.ERROR, state.questDetailsEditMode, click -> {
                if (!QuestDetailsEditState.toggle(state)) {
                    return;
                }
                ToolMenuAnimation.finishQuestDetails(state);
                if (!state.questDetailsEditMode) {
                    QuestDetailsTransientState.closeFloatingPopups(state);
                    state.questDetailsTextStyleOpen = false;
                    state.questDetailsTitleFocused = false;
                    state.canvasTextEditOpen = false;
                    state.canvasTextEditTarget = "";
                    state.canvasTextEditDraft = "";
                    state.questDetailsTextEditTarget = "";
                    state.questDetailsTextEditDraft = "";
                    if (questId.equals(state.pendingQuestTitleChangeId)) {
                        state.pendingQuestTitleChangeId = "";
                    }
                } else {
                    QuestDetailsTransientState.closeContext(state);
                }
                QuestsAndStuffMod.debugLog("[QnS:UI] quest details editor mode toggle enabled={}", state.questDetailsEditMode);
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
        if (!state.questDetailsTitleFocused || !questId.equals(state.pendingQuestTitleChangeId)) {
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
                    state.pendingQuestTitleChangeId = "";
                    state.questTitleDraft = title;
                    state.questDetailsTitleFocused = false;
                    refresh.run();
                },
                () -> {
                    if (questId.equals(state.pendingQuestTitleChangeId)) {
                        commitQuestTitle(player, state, questId);
                        refresh.run();
                    }
                },
                focused -> {
                    if (focused && !questId.equals(state.pendingQuestTitleChangeId)) {
                        state.pendingQuestTitleChangeId = questId;
                        state.questTitleDraft = title;
                    }
                    state.questDetailsTitleFocused = focused;
                }
        );
        titleField.setClientSideWidget();
        titleField.setCurrentString(titleDraft(state));
        titleField.setMaxStringLength(80);
        titleField.setBordered(false);
        boolean editing = state.questDetailsTitleFocused && questId.equals(state.pendingQuestTitleChangeId);
        boolean framed = QuestDetailsEditState.canEdit(state);
        titleField.setBackground(framed
                ? Surfaces.bordered(ModColors.SURFACE_BASE, editing ? ModColors.INTERACTIVE : ModColors.BORDER_BASE)
                : Surfaces.fill(0x00000000));
        titleField.setTextColor(ModColors.TEXT_PRIMARY);
        titleField.setActive(framed);
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
        if (!QuestDetailsEditState.canEdit(state) || questId == null || questId.isBlank()) {
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
        state.pendingQuestTitleChangeId = "";
        state.questTitleDraft = title;
    }

    private static void addHeaderIconButton(WidgetGroup parent, int x, int y, int w, int h, String icon, int color, boolean active, java.util.function.Consumer<com.lowdragmc.lowdraglib.gui.util.ClickData> callback) {
        int fill = active ? withAlpha(color, 38) : ModColors.SURFACE_PANEL_ALT;
        parent.addWidget(panel(x, y, w, h, fill, active ? color : ModColors.BORDER_BASE));
        int iconSize = Math.min(ACTION_ICON_SIZE, Math.max(8, Math.min(w, h) - 4));
        parent.addWidget(new ImageWidget(x + (w - iconSize) / 2, y + (h - iconSize) / 2, iconSize, iconSize, () -> UiIconAtlas.iconTexture(icon)));
        var hit = flatHitButton(x, y, w, h, callback);
        hit.setHoverTexture(Surfaces.bordered(withAlpha(color, 66), ModColors.BORDER_ACCENT));
        hit.setClickedTexture(Surfaces.fill(withAlpha(color, 90)));
        parent.addWidget(hit);
    }

}
