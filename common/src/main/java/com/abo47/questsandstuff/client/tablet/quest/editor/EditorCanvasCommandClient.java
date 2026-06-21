package com.abo47.questsandstuff.client.tablet.quest.editor;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.actions.IntegratedServerActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.QuestServices;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import net.minecraft.nbt.CompoundTag;
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
        Map<String, int[]> moves = new HashMap<>();
        for (Map.Entry<String, CanvasPoint> entry : positions.entrySet()) {
            ClientQuestCache.setQuestPositionInGroupLocal(entry.getKey(), groupName, entry.getValue().x, entry.getValue().y);
            moves.put(entry.getKey(), new int[]{entry.getValue().x, entry.getValue().y});
        }
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServices.editor(serverPlayer.server).moveQuestsInGroup(serverPlayer, groupName, moves),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.moveMany(groupName, moves);
                    EditorCommandSender.send(EditorCommandType.MOVE_MANY, payload);
                });
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

        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServices.editor(serverPlayer.server).scaleQuestsInGroup(serverPlayer, groupName, normalized),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.scaleMany(groupName, normalized);
                    EditorCommandSender.send(EditorCommandType.SCALE_MANY, payload);
                });
    }

    static void runCanvasCopyAction(Player player, String groupName, Set<String> questIds) {
        if (questIds == null || questIds.isEmpty()) {
            return;
        }
        String group = groupName == null ? "" : groupName.trim();
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] copy request group={} ids={} integratedServer={}",
                group, questIds, IntegratedServerActions.canRunLocally(player));
        Set<String> normalizedQuestIds = new HashSet<>();
        for (String questId : questIds) {
            String normalized = questId == null ? "" : questId.trim();
            if (!normalized.isBlank()) {
                normalizedQuestIds.add(normalized);
            }
        }
        if (normalizedQuestIds.isEmpty()) {
            return;
        }
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServices.editor(serverPlayer.server).copyQuestsToClipboard(serverPlayer, group, normalizedQuestIds),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.copyMany(group, normalizedQuestIds);
                    EditorCommandSender.send(EditorCommandType.COPY_MANY, payload);
                });
    }

    static void runCanvasPasteClipboardAction(Player player, String groupName, int x, int y) {
        String group = groupName == null ? "" : groupName.trim();
        if (group.isBlank()) {
            return;
        }
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] paste request group={} anchor={},{} integratedServer={}",
                group, x, y, IntegratedServerActions.canRunLocally(player));
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServices.editor(serverPlayer.server).pasteClipboardInGroup(serverPlayer, group, x, y),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.pasteClipboard(group, x, y);
                    EditorCommandSender.send(EditorCommandType.PASTE_CLIPBOARD, payload);
                });
    }

    static void runCanvasPasteBlueprintAction(Player player, TabletUiState state, CanvasBlueprint blueprint, int x, int y) {
        String group = EditorChapterCommandClient.selectedGroupName(state);
        if (group.isBlank() || blueprint == null || blueprint.isEmpty()) {
            return;
        }
        QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] paste request group={} anchor={},{} entries={} integratedServer={}",
                group, x, y, blueprint.contentCount(), IntegratedServerActions.canRunLocally(player));
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServices.editor(serverPlayer.server).pasteBlueprintInGroup(serverPlayer, group, x, y, blueprint),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.pasteBlueprint(group, x, y, blueprint);
                    EditorCommandSender.send(EditorCommandType.PASTE_BLUEPRINT, payload);
                });
    }

    static void runPrerequisiteAction(Player player, String questId, String prerequisiteId, boolean add) {
        if (questId == null || questId.isBlank() || prerequisiteId == null || prerequisiteId.isBlank() || questId.equals(prerequisiteId)) {
            return;
        }
        ClientQuestCache.setQuestPrerequisiteLocal(questId, prerequisiteId, add);
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServices.editor(serverPlayer.server).setQuestPrerequisite(serverPlayer, questId, prerequisiteId, add),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.prerequisite(questId, prerequisiteId);
                    EditorCommandSender.send(add ? EditorCommandType.PREREQUISITE_ADD : EditorCommandType.PREREQUISITE_REMOVE, payload);
                });
    }

    static void runConnectionColorAction(Player player, String questId, String prerequisiteId, int color) {
        if (questId == null || questId.isBlank() || prerequisiteId == null || prerequisiteId.isBlank() || questId.equals(prerequisiteId)) {
            return;
        }
        ClientQuestCache.setConnectionColorLocal(questId, prerequisiteId, color);
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServices.editor(serverPlayer.server).setConnectionColor(serverPlayer, questId, prerequisiteId, color),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.connectionColor(questId, prerequisiteId, color);
                    EditorCommandSender.send(EditorCommandType.CONNECTION_COLOR, payload);
                });
    }

    static void runConnectionModeAction(Player player, String questId, String prerequisiteId, boolean gridMode) {
        if (questId == null || questId.isBlank() || prerequisiteId == null || prerequisiteId.isBlank() || questId.equals(prerequisiteId)) {
            return;
        }
        ClientQuestCache.setConnectionModeLocal(questId, prerequisiteId, gridMode);
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServices.editor(serverPlayer.server).setConnectionMode(serverPlayer, questId, prerequisiteId, gridMode),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.connectionMode(questId, prerequisiteId, gridMode);
                    EditorCommandSender.send(EditorCommandType.CONNECTION_MODE, payload);
                });
    }

    static void runConnectionHiddenAction(Player player, String questId, String prerequisiteId, boolean hidden) {
        if (questId == null || questId.isBlank() || prerequisiteId == null || prerequisiteId.isBlank() || questId.equals(prerequisiteId)) {
            return;
        }
        ClientQuestCache.setConnectionHiddenLocal(questId, prerequisiteId, hidden);
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServices.editor(serverPlayer.server).setConnectionHidden(serverPlayer, questId, prerequisiteId, hidden),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.connectionHidden(questId, prerequisiteId, hidden);
                    EditorCommandSender.send(EditorCommandType.CONNECTION_HIDDEN, payload);
                });
    }

    private static String resolveEcId(TabletUiState state, String group, String idA, String idB) {
        if (CanvasLayerMutations.findCanvasExclusiveChoice(state, group, idA) != null) return idA;
        if (CanvasLayerMutations.findCanvasExclusiveChoice(state, group, idB) != null) return idB;
        return "";
    }

    private static String resolveQuestId(String ecId, String idA, String idB) {
        return idA.equals(ecId) ? idB : idA;
    }

    static void runEcConnectionColorAction(Player player, TabletUiState state, String sourceId, String targetId, int color) {
        String group = EditorChapterCommandClient.selectedGroupName(state);
        if (group.isBlank()) return;
        String ecId = resolveEcId(state, group, sourceId, targetId);
        if (ecId.isBlank()) return;
        String questId = resolveQuestId(ecId, sourceId, targetId);
        ConnectionRenderer.setEcConnectionColor(state, group, ecId, questId, color);
    }

    static void runEcConnectionModeAction(Player player, TabletUiState state, String sourceId, String targetId, boolean direct) {
        String group = EditorChapterCommandClient.selectedGroupName(state);
        if (group.isBlank()) return;
        String ecId = resolveEcId(state, group, sourceId, targetId);
        if (ecId.isBlank()) return;
        String questId = resolveQuestId(ecId, sourceId, targetId);
        ConnectionRenderer.setEcConnectionMode(state, group, ecId, questId, direct);
    }
}
