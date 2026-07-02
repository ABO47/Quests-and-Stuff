package com.abo47.questsandstuff.client.tablet.shell;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.ui.IntegratedServerActions;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.codec.UiThemeManager;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorControlPacket;
import com.abo47.questsandstuff.quest.QuestServices;
import net.minecraft.world.entity.player.Player;

import java.util.List;

import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.clampGridSizeIndex;
import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.applyCanvasBgOpacityPercent;
import static com.abo47.questsandstuff.client.tablet.layout.TabletGridControls.defaultGridOpacityPercent;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W_MAX;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_W_MIN;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterPanelWidth;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.ensureAssetsDirs;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.isChapterPanelCollapsed;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.readPersistedUiState;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.readPersistedSkinState;
import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.syncCanvasStateFromCache;

public final class TabletShellBootstrap {
    private TabletShellBootstrap() {
    }

    public static TabletUiState prepare(Player player) {
        UiThemeManager.activeThemeName();
        ensureAssetsDirs();
        TabletUiState state = new TabletUiState();
        state.root.editorAvailable = player.hasPermissions(2);
        readPersistedUiState(state);
        readPersistedSkinState(state);
        state.root.canEdit = state.root.editorAvailable && state.root.editMode;
        state.chapterPanel.chapterPanelWidth = chapterPanelWidth(state);
        state.chapterPanel.chapterPanelCollapsed = isChapterPanelCollapsed(state);
        if (!state.chapterPanel.chapterPanelCollapsed) {
            state.chapterPanel.chapterPanelLastExpandedWidth = Math.max(CHAPTER_W_MIN, Math.min(CHAPTER_W_MAX, state.chapterPanel.chapterPanelWidth));
        }
        keepSelectedGroupValid(state, false);
        state.chapterPanel.groupDraft = selectedGroupName(state);
        state.chapterPanel.chapterDraftName = state.chapterPanel.groupDraft;
        state.canvas.gridSizeIndex = clampGridSizeIndex(state.canvas.gridSizeIndex);
        state.canvas.gridOpacityPercent = defaultGridOpacityPercent(state);
        state.canvas.toolsGridOpacityDraft = Integer.toString(state.canvas.gridOpacityPercent);
        applyCanvasBgOpacityPercent(state, state.canvas.canvasBgOpacityPercent);
        syncCanvasStateFromCache(state);
        return state;
    }

    public static Runnable undoAction(TabletUiState state, Player player) {
        return () -> {
            if (!canUseEditorHistory(state)) {
                return;
            }
            IntegratedServerActions.run(
                    player,
                    serverPlayer -> QuestServices.editor(serverPlayer.server).undo(serverPlayer),
                    () -> ModNetwork.sendToServer(new C2SEditorControlPacket("undo")));
        };
    }

    public static Runnable redoAction(TabletUiState state, Player player) {
        return () -> {
            if (!canUseEditorHistory(state)) {
                return;
            }
            IntegratedServerActions.run(
                    player,
                    serverPlayer -> QuestServices.editor(serverPlayer.server).redo(serverPlayer),
                    () -> ModNetwork.sendToServer(new C2SEditorControlPacket("redo")));
        };
    }

    private static boolean canUseEditorHistory(TabletUiState state) {
        return state != null && state.root.editorAvailable && (state.root.canEdit || QuestDetailsEditState.canEdit(state));
    }

    public static void keepSelectedGroupValid(TabletUiState state, boolean persist) {
        List<String> groups = ClientQuestCache.selectableGroupOrder(state.root.canEdit);
        if (groups.isEmpty()) {
            state.root.selectedGroup = "";
            return;
        }
        if (state.root.selectedGroup == null) {
            state.root.selectedGroup = groups.get(0);
            state.chapterPanel.groupDraft = state.root.selectedGroup;
            state.chapterPanel.chapterDraftName = state.root.selectedGroup;
            if (persist) {
                persistUiState(state);
            }
            return;
        }
        String selected = state.root.selectedGroup.trim();
        if (selected.isBlank() || groups.contains(selected)) {
            state.root.selectedGroup = selected;
            return;
        }
        if (state.chapterPanel.recentlyCreatedGroups.contains(selected)) {
            ClientQuestCache.createGroupLocal(selected);
            state.root.selectedGroup = selected;
            state.chapterPanel.groupDraft = selected;
            state.chapterPanel.chapterDraftName = selected;
            if (persist) {
                persistUiState(state);
            }
            QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] preserved optimistic chapter selected={} groups={}", selected, ClientQuestCache.groupOrder());
            return;
        }
        state.root.selectedGroup = groups.get(0);
        state.chapterPanel.groupDraft = state.root.selectedGroup;
        state.chapterPanel.chapterDraftName = state.root.selectedGroup;
        if (persist) {
            persistUiState(state);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] selected chapter missing, reset to={} available={}", state.root.selectedGroup, groups);
    }
}
