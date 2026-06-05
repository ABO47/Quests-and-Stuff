package com.abo47.questsandstuff.client.tablet.shell;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.UiThemeManager;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorControlPacket;
import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.clampGridSizeIndex;
import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.applyCanvasBgOpacityPercent;
import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.defaultGridOpacityPercent;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_MAX;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_W_MIN;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.ensureAssetsDirs;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.isChapterPanelCollapsed;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.readPersistedUiState;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.syncCanvasStateFromCache;

public final class TabletShellBootstrap {
    private TabletShellBootstrap() {
    }

    public static TabletUiState prepare(Player player) {
        UiThemeManager.activeThemeName();
        ensureAssetsDirs();
        TabletUiState state = new TabletUiState();
        state.editorAvailable = player.hasPermissions(2);
        readPersistedUiState(state);
        state.canEdit = state.editorAvailable && state.editMode;
        state.chapterPanelWidth = chapterPanelWidth(state);
        state.chapterPanelCollapsed = isChapterPanelCollapsed(state);
        if (!state.chapterPanelCollapsed) {
            state.chapterPanelLastExpandedWidth = Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, state.chapterPanelWidth));
        }
        keepSelectedGroupValid(state, false);
        state.groupDraft = selectedGroupName(state);
        state.chapterDraftName = state.groupDraft;
        state.gridSizeIndex = clampGridSizeIndex(state.gridSizeIndex);
        state.gridOpacityPercent = defaultGridOpacityPercent(state);
        state.toolsGridOpacityDraft = Integer.toString(state.gridOpacityPercent);
        applyCanvasBgOpacityPercent(state, state.canvasBgOpacityPercent);
        syncCanvasStateFromCache(state);
        TabletClientHooks.restoreRememberedWindow(state);
        return state;
    }

    public static Runnable undoAction(TabletUiState state, Player player) {
        return () -> {
            if (!canUseEditorHistory(state)) {
                return;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                QuestServices.editor(serverPlayer.server).undo(serverPlayer);
            } else {
                ModNetwork.sendToServer(new C2SEditorControlPacket("undo"));
            }
        };
    }

    public static Runnable redoAction(TabletUiState state, Player player) {
        return () -> {
            if (!canUseEditorHistory(state)) {
                return;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                QuestServices.editor(serverPlayer.server).redo(serverPlayer);
            } else {
                ModNetwork.sendToServer(new C2SEditorControlPacket("redo"));
            }
        };
    }

    private static boolean canUseEditorHistory(TabletUiState state) {
        return state != null && state.editorAvailable && (state.canEdit || QuestDetailsEditState.canEdit(state));
    }

    public static void keepSelectedGroupValid(TabletUiState state, boolean persist) {
        List<String> groups = ClientQuestCache.selectableGroupOrder(state.canEdit);
        if (groups.isEmpty()) {
            state.selectedGroup = "";
            return;
        }
        if (state.selectedGroup == null) {
            state.selectedGroup = groups.get(0);
            state.groupDraft = state.selectedGroup;
            state.chapterDraftName = state.selectedGroup;
            if (persist) {
                persistUiState(state);
            }
            return;
        }
        String selected = state.selectedGroup.trim();
        if (selected.isBlank() || groups.contains(selected)) {
            state.selectedGroup = selected;
            return;
        }
        if (state.recentlyCreatedGroups.contains(selected)) {
            ClientQuestCache.createGroupLocal(selected);
            state.selectedGroup = selected;
            state.groupDraft = selected;
            state.chapterDraftName = selected;
            if (persist) {
                persistUiState(state);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] preserved optimistic chapter selected={} groups={}", selected, ClientQuestCache.groupOrder());
            return;
        }
        state.selectedGroup = groups.get(0);
        state.groupDraft = state.selectedGroup;
        state.chapterDraftName = state.selectedGroup;
        if (persist) {
            persistUiState(state);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] selected chapter missing, reset to={} available={}", state.selectedGroup, groups);
    }
}
