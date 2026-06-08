package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.sync.packet.ClientSyncUiBridge;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

final class TabletActiveState {
    private static Runnable activeTabletRefresh = () -> {
    };
    private static TabletUiState activeTabletState;

    private TabletActiveState() {
    }

    static void setActiveTabletRefresh(Runnable refresh) {
        activeTabletRefresh = refresh == null ? () -> {
        } : refresh;
    }

    static void setActiveTabletState(TabletUiState state) {
        activeTabletState = state;
        ClientSyncUiBridge.registerTabletCallbacks(
                TabletActiveState::refreshActiveTablet,
                TabletActiveState::syncActiveCanvasStateFromCache,
                TabletActiveState::activeSelectedGroup,
                TabletActiveState::selectPastedQuests);
    }

    static void refreshActiveTablet() {
        activeTabletRefresh.run();
    }

    static String activeSelectedGroup() {
        return activeTabletState == null ? "" : EditorCommandClient.selectedGroupName(activeTabletState);
    }

    static void syncCanvasStateFromCache(TabletUiState state) {
        if (state == null) {
            return;
        }
        state.canvas.canvasImagesByGroup.clear();
        state.canvas.canvasImagesByGroup.putAll(ClientQuestCache.canvasImagesByGroup());
        state.canvas.canvasTextsByGroup.clear();
        state.canvas.canvasTextsByGroup.putAll(ClientQuestCache.canvasTextsByGroup());
        state.canvas.canvasLayerOrderByGroup.clear();
        state.canvas.canvasLayerOrderByGroup.putAll(ClientQuestCache.canvasLayerOrderByGroup());
    }

    static void syncActiveCanvasStateFromCache() {
        if (activeTabletState == null) {
            return;
        }
        syncCanvasStateFromCache(activeTabletState);
        refreshActiveTablet();
    }

    static void selectPastedQuests(ListTag ids) {
        CompoundTag payload = new CompoundTag();
        payload.put("quests", ids == null ? new ListTag() : ids.copy());
        selectPastedQuests(payload);
    }

    static void selectPastedQuests(CompoundTag payload) {
        ListTag ids = payload == null ? new ListTag() : payload.getList("quests", Tag.TAG_STRING);
        ListTag images = payload == null ? new ListTag() : payload.getList("images", Tag.TAG_STRING);
        ListTag texts = payload == null ? new ListTag() : payload.getList("texts", Tag.TAG_STRING);
        if (activeTabletState == null) {
            refreshActiveTablet();
            return;
        }
        String group = payload == null ? "" : payload.getString("group").trim();
        if (!group.isBlank()) {
            ClientQuestCache.createGroupLocal(group);
            activeTabletState.root.selectedGroup = group;
            activeTabletState.chapterPanel.groupDraft = group;
            activeTabletState.chapterPanel.chapterDraftName = group;
            activeTabletState.chapterPanel.recentlyCreatedGroups.remove(group);
            TabletPersistence.persistUiState(activeTabletState);
        }
        activeTabletState.canvas.canvasSelection.questIds().clear();
        activeTabletState.canvas.canvasSelection.setPrimaryImageId("");
        activeTabletState.canvas.canvasSelection.setPrimaryTextId("");
        activeTabletState.canvas.canvasSelection.imageIds().clear();
        activeTabletState.canvas.canvasSelection.textIds().clear();
        for (int i = 0; i < ids.size(); i++) {
            String questId = ids.getString(i);
            if (questId != null && !questId.isBlank()) {
                activeTabletState.canvas.canvasSelection.questIds().add(questId);
                activeTabletState.chapterPanel.lastJumpQuest = questId;
            }
        }
        for (int i = 0; i < images.size(); i++) {
            String imageId = images.getString(i);
            if (imageId != null && !imageId.isBlank()) {
                activeTabletState.canvas.canvasSelection.imageIds().add(imageId);
                activeTabletState.canvas.canvasSelection.setPrimaryImageId(imageId);
            }
        }
        for (int i = 0; i < texts.size(); i++) {
            String textId = texts.getString(i);
            if (textId != null && !textId.isBlank()) {
                activeTabletState.canvas.canvasSelection.textIds().add(textId);
                activeTabletState.canvas.canvasSelection.setPrimaryTextId(textId);
            }
        }
        activeTabletState.canvas.canvasSelection.imageIds().addAll(activeTabletState.clipboard.canvasClipboard.pendingPastedImageIds());
        activeTabletState.canvas.canvasSelection.textIds().addAll(activeTabletState.clipboard.canvasClipboard.pendingPastedTextIds());
        String pendingImage = activeTabletState.clipboard.canvasClipboard.lastPendingPastedImageId();
        String pendingText = activeTabletState.clipboard.canvasClipboard.lastPendingPastedTextId();
        if (!pendingImage.isBlank()) {
            activeTabletState.canvas.canvasSelection.setPrimaryImageId(pendingImage);
        }
        if (!pendingText.isBlank()) {
            activeTabletState.canvas.canvasSelection.setPrimaryTextId(pendingText);
        }
        activeTabletState.clipboard.canvasClipboard.clearPendingPastedLayers();
        activeTabletState.chapterPanel.recentlyCreatedGroups.remove(EditorCommandClient.selectedGroupName(activeTabletState));
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] paste selection applied group={} quests={} images={} texts={}",
                EditorCommandClient.selectedGroupName(activeTabletState), activeTabletState.canvas.canvasSelection.questIds().size(), activeTabletState.canvas.canvasSelection.imageIds().size(), activeTabletState.canvas.canvasSelection.textIds().size());
        refreshActiveTablet();
    }
}
