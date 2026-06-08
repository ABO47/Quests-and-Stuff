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
        state.canvasImagesByGroup.clear();
        state.canvasImagesByGroup.putAll(ClientQuestCache.canvasImagesByGroup());
        state.canvasTextsByGroup.clear();
        state.canvasTextsByGroup.putAll(ClientQuestCache.canvasTextsByGroup());
        state.canvasLayerOrderByGroup.clear();
        state.canvasLayerOrderByGroup.putAll(ClientQuestCache.canvasLayerOrderByGroup());
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
            activeTabletState.selectedGroup = group;
            activeTabletState.groupDraft = group;
            activeTabletState.chapterDraftName = group;
            activeTabletState.recentlyCreatedGroups.remove(group);
            TabletPersistence.persistUiState(activeTabletState);
        }
        activeTabletState.canvasSelection.questIds().clear();
        activeTabletState.canvasSelection.setPrimaryImageId("");
        activeTabletState.canvasSelection.setPrimaryTextId("");
        activeTabletState.canvasSelection.imageIds().clear();
        activeTabletState.canvasSelection.textIds().clear();
        for (int i = 0; i < ids.size(); i++) {
            String questId = ids.getString(i);
            if (questId != null && !questId.isBlank()) {
                activeTabletState.canvasSelection.questIds().add(questId);
                activeTabletState.lastJumpQuest = questId;
            }
        }
        for (int i = 0; i < images.size(); i++) {
            String imageId = images.getString(i);
            if (imageId != null && !imageId.isBlank()) {
                activeTabletState.canvasSelection.imageIds().add(imageId);
                activeTabletState.canvasSelection.setPrimaryImageId(imageId);
            }
        }
        for (int i = 0; i < texts.size(); i++) {
            String textId = texts.getString(i);
            if (textId != null && !textId.isBlank()) {
                activeTabletState.canvasSelection.textIds().add(textId);
                activeTabletState.canvasSelection.setPrimaryTextId(textId);
            }
        }
        activeTabletState.canvasSelection.imageIds().addAll(activeTabletState.canvasClipboard.pendingPastedImageIds());
        activeTabletState.canvasSelection.textIds().addAll(activeTabletState.canvasClipboard.pendingPastedTextIds());
        String pendingImage = activeTabletState.canvasClipboard.lastPendingPastedImageId();
        String pendingText = activeTabletState.canvasClipboard.lastPendingPastedTextId();
        if (!pendingImage.isBlank()) {
            activeTabletState.canvasSelection.setPrimaryImageId(pendingImage);
        }
        if (!pendingText.isBlank()) {
            activeTabletState.canvasSelection.setPrimaryTextId(pendingText);
        }
        activeTabletState.canvasClipboard.clearPendingPastedLayers();
        activeTabletState.recentlyCreatedGroups.remove(EditorCommandClient.selectedGroupName(activeTabletState));
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] paste selection applied group={} quests={} images={} texts={}",
                EditorCommandClient.selectedGroupName(activeTabletState), activeTabletState.canvasSelection.questIds().size(), activeTabletState.canvasSelection.imageIds().size(), activeTabletState.canvasSelection.textIds().size());
        refreshActiveTablet();
    }
}
