package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
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
    }

    static void refreshActiveTablet() {
        activeTabletRefresh.run();
    }

    static String activeSelectedGroup() {
        return activeTabletState == null ? "" : TabletEditorActions.selectedGroupName(activeTabletState);
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
        activeTabletState.selectedQuestIds.clear();
        activeTabletState.selectedCanvasImageId = "";
        activeTabletState.selectedCanvasTextId = "";
        activeTabletState.selectedCanvasImageIds.clear();
        activeTabletState.selectedCanvasTextIds.clear();
        for (int i = 0; i < ids.size(); i++) {
            String questId = ids.getString(i);
            if (questId != null && !questId.isBlank()) {
                activeTabletState.selectedQuestIds.add(questId);
                activeTabletState.lastJumpQuest = questId;
            }
        }
        for (int i = 0; i < images.size(); i++) {
            String imageId = images.getString(i);
            if (imageId != null && !imageId.isBlank()) {
                activeTabletState.selectedCanvasImageIds.add(imageId);
                activeTabletState.selectedCanvasImageId = imageId;
            }
        }
        for (int i = 0; i < texts.size(); i++) {
            String textId = texts.getString(i);
            if (textId != null && !textId.isBlank()) {
                activeTabletState.selectedCanvasTextIds.add(textId);
                activeTabletState.selectedCanvasTextId = textId;
            }
        }
        activeTabletState.selectedCanvasImageIds.addAll(activeTabletState.pendingPastedCanvasImageIds);
        activeTabletState.selectedCanvasTextIds.addAll(activeTabletState.pendingPastedCanvasTextIds);
        String pendingImage = activeTabletState.pendingPastedCanvasImageIds.stream().reduce((first, second) -> second).orElse("");
        String pendingText = activeTabletState.pendingPastedCanvasTextIds.stream().reduce((first, second) -> second).orElse("");
        if (!pendingImage.isBlank()) {
            activeTabletState.selectedCanvasImageId = pendingImage;
        }
        if (!pendingText.isBlank()) {
            activeTabletState.selectedCanvasTextId = pendingText;
        }
        activeTabletState.pendingPastedCanvasImageIds.clear();
        activeTabletState.pendingPastedCanvasTextIds.clear();
        activeTabletState.recentlyCreatedGroups.remove(TabletEditorActions.selectedGroupName(activeTabletState));
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] paste selection applied group={} quests={} images={} texts={}",
                TabletEditorActions.selectedGroupName(activeTabletState), activeTabletState.selectedQuestIds.size(), activeTabletState.selectedCanvasImageIds.size(), activeTabletState.selectedCanvasTextIds.size());
        refreshActiveTablet();
    }
}
