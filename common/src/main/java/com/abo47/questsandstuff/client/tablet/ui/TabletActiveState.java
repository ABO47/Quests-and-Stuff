package com.abo47.questsandstuff.client.tablet.ui;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.sync.packet.ClientSyncUiBridge;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        state.canvas.canvasExclusiveChoicesByGroup.clear();
        state.canvas.canvasExclusiveChoicesByGroup.putAll(ClientQuestCache.canvasExclusiveChoicesByGroup());
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
        ListTag ecs = payload == null ? new ListTag() : payload.getList("ecs", Tag.TAG_STRING);
        CompoundTag allocatedIds = payload == null ? new CompoundTag() : payload.getCompound("allocated_ids");
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

        if (!allocatedIds.isEmpty()) {
            Map<String, String> oldToNew = new HashMap<>();
            for (String oldId : allocatedIds.getAllKeys()) {
                oldToNew.put(oldId, allocatedIds.getString(oldId));
            }
            List<CanvasExclusiveChoice> existingEcs = activeTabletState.canvas.canvasExclusiveChoicesByGroup.get(group);
            List<CanvasExclusiveChoice> updatedEcs = new ArrayList<>();
            boolean anyUpdated = false;
            if (existingEcs != null) {
                for (CanvasExclusiveChoice ec : existingEcs) {
                    if (!activeTabletState.clipboard.canvasClipboard.pendingPastedEcIds().contains(ec.id())) {
                        updatedEcs.add(ec);
                        continue;
                    }
                    anyUpdated = true;
                    List<String> remappedConnections = remapQuestIds(ec.connectionQuestIds(), oldToNew);
                    List<String> remappedPrerequisites = remapQuestIds(ec.prerequisiteQuestIds(), oldToNew);
                    Map<String, Integer> remappedColors = remapColorKeys(ec.connectionColors(), oldToNew);
                    Map<String, String> remappedModes = remapStringKeys(ec.connectionModes(), oldToNew);
                    Map<String, String> remappedTextures = remapStringKeys(ec.connectionTextures(), oldToNew);
                    Map<String, Integer> remappedSpacings = remapIntKeys(ec.connectionTextureSpacings(), oldToNew);
                    Set<String> remappedHidden = remapStringSet(ec.hiddenConnections(), oldToNew);
                    updatedEcs.add(new CanvasExclusiveChoice(
                            ec.id(), ec.x(), ec.y(), ec.w(), ec.h(), ec.rotation(),
                            remappedConnections, remappedPrerequisites, ec.background(),
                            remappedColors, remappedModes, remappedTextures, remappedSpacings, remappedHidden
                    ));
                }
            }
            if (anyUpdated) {
                activeTabletState.canvas.canvasExclusiveChoicesByGroup.put(group, updatedEcs);
            }
            activeTabletState.canvas.canvasSelection.ecIds().addAll(activeTabletState.clipboard.canvasClipboard.pendingPastedEcIds());
            String pendingEc = activeTabletState.clipboard.canvasClipboard.lastPendingPastedEcId();
            if (!pendingEc.isBlank()) {
                activeTabletState.canvas.canvasSelection.setPrimaryEcId(pendingEc);
            }
        }

        if (!ecs.isEmpty()) {
            for (int i = 0; i < ecs.size(); i++) {
                String ecId = ecs.getString(i);
                if (ecId != null && !ecId.isBlank()) {
                    activeTabletState.canvas.canvasSelection.ecIds().add(ecId);
                    activeTabletState.canvas.canvasSelection.setPrimaryEcId(ecId);
                }
            }
        }

        activeTabletState.clipboard.canvasClipboard.clearPendingPastedLayers();
        activeTabletState.chapterPanel.recentlyCreatedGroups.remove(EditorCommandClient.selectedGroupName(activeTabletState));
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] paste selection applied group={} quests={} images={} texts={} ecs={}",
                EditorCommandClient.selectedGroupName(activeTabletState), activeTabletState.canvas.canvasSelection.questIds().size(), activeTabletState.canvas.canvasSelection.imageIds().size(), activeTabletState.canvas.canvasSelection.textIds().size(), activeTabletState.canvas.canvasSelection.ecIds().size());
        refreshActiveTablet();
    }

    private static List<String> remapQuestIds(List<String> questIds, Map<String, String> oldToNew) {
        List<String> result = new ArrayList<>();
        for (String id : questIds) {
            String mapped = oldToNew.get(id);
            result.add(mapped != null ? mapped : id);
        }
        return result;
    }

    private static Map<String, Integer> remapColorKeys(Map<String, Integer> map, Map<String, String> oldToNew) {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String mapped = oldToNew.get(entry.getKey());
            result.put(mapped != null ? mapped : entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static Map<String, String> remapStringKeys(Map<String, String> map, Map<String, String> oldToNew) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String mapped = oldToNew.get(entry.getKey());
            result.put(mapped != null ? mapped : entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static Map<String, Integer> remapIntKeys(Map<String, Integer> map, Map<String, String> oldToNew) {
        Map<String, Integer> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String mapped = oldToNew.get(entry.getKey());
            result.put(mapped != null ? mapped : entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static Set<String> remapStringSet(Set<String> set, Map<String, String> oldToNew) {
        Set<String> result = new HashSet<>();
        for (String value : set) {
            String mapped = oldToNew.get(value);
            result.add(mapped != null ? mapped : value);
        }
        return result;
    }
}
