package com.abo47.questsandstuff.client.tablet.editor;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.network.QuestNetwork;
import com.abo47.questsandstuff.network.editor.C2SEditorCommandPacket;
import com.abo47.questsandstuff.quest.QuestServices;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class EditorCanvasCommandClient {
    private EditorCanvasCommandClient() {
    }

    static void runCanvasMoveAction(Player player, TabletUiState state, Map<String, CanvasPoint> positions) {
        if (positions == null || positions.isEmpty()) {
            return;
        }
        String groupName = EditorChapterCommandClient.selectedGroupName(state);
        if (groupName.isBlank()) {
            return;
        }
        for (Map.Entry<String, CanvasPoint> entry : positions.entrySet()) {
            ClientQuestCache.setQuestPositionInGroupLocal(entry.getKey(), groupName, entry.getValue().x, entry.getValue().y);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Map<String, int[]> payload = new HashMap<>();
            for (Map.Entry<String, CanvasPoint> entry : positions.entrySet()) {
                payload.put(entry.getKey(), new int[]{entry.getValue().x, entry.getValue().y});
            }
            QuestServices.editor(serverPlayer.server).moveQuestsInGroup(serverPlayer, groupName, payload);
            return;
        }

        CompoundTag payload = new CompoundTag();
        payload.putString("group", groupName);
        ListTag moveTags = new ListTag();
        for (Map.Entry<String, CanvasPoint> entry : positions.entrySet()) {
            CompoundTag move = new CompoundTag();
            move.putString("quest", entry.getKey());
            move.putInt("x", entry.getValue().x);
            move.putInt("y", entry.getValue().y);
            moveTags.add(move);
        }
        payload.put("moves", moveTags);
        QuestNetwork.sendToServer(new C2SEditorCommandPacket("move_many", payload));
    }

    static void runCanvasScaleAction(Player player, TabletUiState state, Map<String, Float> scales) {
        if (scales == null || scales.isEmpty()) {
            return;
        }
        String groupName = EditorChapterCommandClient.selectedGroupName(state);
        if (groupName.isBlank()) {
            return;
        }
        Map<String, Float> normalized = new HashMap<>();
        for (Map.Entry<String, Float> entry : scales.entrySet()) {
            String questId = entry.getKey();
            Float rawScale = entry.getValue();
            if (questId == null || questId.isBlank() || rawScale == null || Float.isNaN(rawScale) || Float.isInfinite(rawScale)) {
                continue;
            }
            float scale = Math.max(0.5f, rawScale);
            normalized.put(questId, scale);
            ClientQuestCache.setQuestScaleInGroupLocal(questId, groupName, scale);
        }
        if (normalized.isEmpty()) {
            return;
        }

        if (player instanceof ServerPlayer serverPlayer) {
            QuestServices.editor(serverPlayer.server).scaleQuestsInGroup(serverPlayer, groupName, normalized);
            return;
        }

        CompoundTag payload = new CompoundTag();
        payload.putString("group", groupName);
        ListTag scaleTags = new ListTag();
        for (Map.Entry<String, Float> entry : normalized.entrySet()) {
            CompoundTag scaleTag = new CompoundTag();
            scaleTag.putString("quest", entry.getKey());
            scaleTag.putFloat("scale", entry.getValue());
            scaleTags.add(scaleTag);
        }
        payload.put("scales", scaleTags);
        QuestNetwork.sendToServer(new C2SEditorCommandPacket("scale_many", payload));
    }

    static void runCanvasCopyAction(Player player, String groupName, Set<String> questIds) {
        if (questIds == null || questIds.isEmpty()) {
            return;
        }
        String group = groupName == null ? "" : groupName.trim();
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] copy request group={} ids={} integratedServer={}", group, questIds, player instanceof ServerPlayer);
        ListTag quests = new ListTag();
        for (String questId : questIds) {
            String normalized = questId == null ? "" : questId.trim();
            if (!normalized.isBlank()) {
                quests.add(StringTag.valueOf(normalized));
            }
        }
        if (quests.isEmpty()) {
            return;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < quests.size(); i++) {
                ids.add(quests.getString(i));
            }
            QuestServices.editor(serverPlayer.server).copyQuestsToClipboard(serverPlayer, group, ids);
            return;
        }
        CompoundTag payload = new CompoundTag();
        payload.putString("group", group);
        payload.put("quests", quests);
        QuestNetwork.sendToServer(new C2SEditorCommandPacket("copy_many", payload));
    }

    static void runCanvasPasteClipboardAction(Player player, String groupName, int x, int y) {
        String group = groupName == null ? "" : groupName.trim();
        if (group.isBlank()) {
            return;
        }
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] paste request group={} anchor={},{} integratedServer={}", group, x, y, player instanceof ServerPlayer);
        if (player instanceof ServerPlayer serverPlayer) {
            QuestServices.editor(serverPlayer.server).pasteClipboardInGroup(serverPlayer, group, x, y);
            return;
        }
        CompoundTag payload = new CompoundTag();
        payload.putString("group", group);
        payload.putInt("x", x);
        payload.putInt("y", y);
        QuestNetwork.sendToServer(new C2SEditorCommandPacket("paste_clipboard", payload));
    }

    static void runCanvasPasteBlueprintAction(Player player, TabletUiState state, CanvasBlueprint blueprint, int x, int y) {
        String group = EditorChapterCommandClient.selectedGroupName(state);
        if (group.isBlank() || blueprint == null || blueprint.isEmpty()) {
            return;
        }
        QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] paste request group={} anchor={},{} entries={} integratedServer={}",
                group, x, y, blueprint.contentCount(), player instanceof ServerPlayer);
        if (player instanceof ServerPlayer serverPlayer) {
            QuestServices.editor(serverPlayer.server).pasteBlueprintInGroup(serverPlayer, group, x, y, blueprint);
            return;
        }
        CompoundTag payload = new CompoundTag();
        payload.putString("group", group);
        payload.putInt("x", x);
        payload.putInt("y", y);
        payload.put("blueprint", blueprint.toPacketTag());
        QuestNetwork.sendToServer(new C2SEditorCommandPacket(EditorCommandType.PASTE_BLUEPRINT.wireName(), payload));
    }

    static void runPrerequisiteAction(Player player, String questId, String prerequisiteId, boolean add) {
        if (questId == null || questId.isBlank() || prerequisiteId == null || prerequisiteId.isBlank() || questId.equals(prerequisiteId)) {
            return;
        }
        ClientQuestCache.setQuestPrerequisiteLocal(questId, prerequisiteId, add);
        if (player instanceof ServerPlayer serverPlayer) {
            QuestServices.editor(serverPlayer.server).setQuestPrerequisite(serverPlayer, questId, prerequisiteId, add);
            return;
        }
        CompoundTag payload = new CompoundTag();
        payload.putString("quest", questId);
        putPrerequisite(payload, prerequisiteId);
        QuestNetwork.sendToServer(new C2SEditorCommandPacket(
                add ? EditorCommandType.PREREQUISITE_ADD.wireName() : EditorCommandType.PREREQUISITE_REMOVE.wireName(),
                payload
        ));
    }

    static void runConnectionColorAction(Player player, String questId, String prerequisiteId, int color) {
        if (questId == null || questId.isBlank() || prerequisiteId == null || prerequisiteId.isBlank() || questId.equals(prerequisiteId)) {
            return;
        }
        ClientQuestCache.setConnectionColorLocal(questId, prerequisiteId, color);
        if (player instanceof ServerPlayer serverPlayer) {
            QuestServices.editor(serverPlayer.server).setConnectionColor(serverPlayer, questId, prerequisiteId, color);
            return;
        }
        CompoundTag payload = new CompoundTag();
        payload.putString("quest", questId);
        putPrerequisite(payload, prerequisiteId);
        payload.putInt("color", color);
        QuestNetwork.sendToServer(new C2SEditorCommandPacket("connection_color", payload));
    }

    static void runConnectionModeAction(Player player, String questId, String prerequisiteId, boolean gridMode) {
        if (questId == null || questId.isBlank() || prerequisiteId == null || prerequisiteId.isBlank() || questId.equals(prerequisiteId)) {
            return;
        }
        ClientQuestCache.setConnectionModeLocal(questId, prerequisiteId, gridMode);
        if (player instanceof ServerPlayer serverPlayer) {
            QuestServices.editor(serverPlayer.server).setConnectionMode(serverPlayer, questId, prerequisiteId, gridMode);
            return;
        }
        CompoundTag payload = new CompoundTag();
        payload.putString("quest", questId);
        putPrerequisite(payload, prerequisiteId);
        payload.putBoolean("grid", gridMode);
        QuestNetwork.sendToServer(new C2SEditorCommandPacket("connection_mode", payload));
    }

    static void runConnectionHiddenAction(Player player, String questId, String prerequisiteId, boolean hidden) {
        if (questId == null || questId.isBlank() || prerequisiteId == null || prerequisiteId.isBlank() || questId.equals(prerequisiteId)) {
            return;
        }
        ClientQuestCache.setConnectionHiddenLocal(questId, prerequisiteId, hidden);
        if (player instanceof ServerPlayer serverPlayer) {
            QuestServices.editor(serverPlayer.server).setConnectionHidden(serverPlayer, questId, prerequisiteId, hidden);
            return;
        }
        CompoundTag payload = new CompoundTag();
        payload.putString("quest", questId);
        putPrerequisite(payload, prerequisiteId);
        payload.putBoolean("hidden", hidden);
        QuestNetwork.sendToServer(new C2SEditorCommandPacket("connection_hidden", payload));
    }

    private static void putPrerequisite(CompoundTag payload, String prerequisiteId) {
        payload.putString(C2SEditorCommandPacket.PREREQUISITE_FIELD, prerequisiteId);
    }
}
