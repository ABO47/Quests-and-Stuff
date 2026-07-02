package com.abo47.questsandstuff.client.tablet.quest.editor;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.ui.IntegratedServerActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class EditorCanvasCommandClient {
    private EditorCanvasCommandClient() {
    }

    public static void runCanvasMoveAction(Player player, TabletUiState state, Map<String, CanvasPoint> positions) {
        if (positions == null || positions.isEmpty()) {
            return;
        }
        String groupName = EditorGroupCommandClient.selectedGroupName(state);
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
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).moveQuestsInGroup(serverPlayer, groupName, moves),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.moveMany(groupName, moves);
                    EditorCommandSender.send(EditorCommandType.MOVE_MANY, payload);
                });
    }

    public static void runCanvasScaleAction(Player player, TabletUiState state, Map<String, Float> scales) {
        if (scales == null || scales.isEmpty()) {
            return;
        }
        String groupName = EditorGroupCommandClient.selectedGroupName(state);
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
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).scaleQuestsInGroup(serverPlayer, groupName, normalized),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.scaleMany(groupName, normalized);
                    EditorCommandSender.send(EditorCommandType.SCALE_MANY, payload);
                });
    }

    public static void runCanvasCopyAction(Player player, String groupName, Set<String> questIds) {
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
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).copyQuestsToClipboard(serverPlayer, group, normalizedQuestIds),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.copyMany(group, normalizedQuestIds);
                    EditorCommandSender.send(EditorCommandType.COPY_MANY, payload);
                });
    }

    public static void runCanvasPasteClipboardAction(Player player, String groupName, int x, int y) {
        String group = groupName == null ? "" : groupName.trim();
        if (group.isBlank()) {
            return;
        }
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] paste request group={} anchor={},{} integratedServer={}",
                group, x, y, IntegratedServerActions.canRunLocally(player));
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).pasteClipboardInGroup(serverPlayer, group, x, y),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.pasteClipboard(group, x, y);
                    EditorCommandSender.send(EditorCommandType.PASTE_CLIPBOARD, payload);
                });
    }

    public static void runCanvasPasteBlueprintAction(Player player, TabletUiState state, CanvasBlueprint blueprint, int x, int y) {
        String group = EditorGroupCommandClient.selectedGroupName(state);
        if (group.isBlank() || blueprint == null || blueprint.isEmpty()) {
            return;
        }
        QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] paste request group={} anchor={},{} entries={} integratedServer={}",
                group, x, y, blueprint.contentCount(), IntegratedServerActions.canRunLocally(player));
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).pasteBlueprintInGroup(serverPlayer, group, x, y, blueprint),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.pasteBlueprint(group, x, y, blueprint);
                    EditorCommandSender.send(EditorCommandType.PASTE_BLUEPRINT, payload);
                });
    }

    private static boolean invalidQuestPair(String questId, String prerequisiteId) {
        return questId == null || questId.isBlank() || prerequisiteId == null || prerequisiteId.isBlank() || questId.equals(prerequisiteId);
    }

    public static void runPrerequisiteAction(Player player, String questId, String prerequisiteId, boolean add) {
        if (invalidQuestPair(questId, prerequisiteId)) return;
        ClientQuestCache.setQuestPrerequisiteLocal(questId, prerequisiteId, add);
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestPrerequisite(serverPlayer, questId, prerequisiteId, add),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.prerequisite(questId, prerequisiteId);
                    EditorCommandSender.send(add ? EditorCommandType.PREREQUISITE_ADD : EditorCommandType.PREREQUISITE_REMOVE, payload);
                });
    }

    public static void runConnectionColorAction(Player player, String questId, String prerequisiteId, int color) {
        if (invalidQuestPair(questId, prerequisiteId)) return;
        ClientQuestCache.setConnectionColorLocal(questId, prerequisiteId, color);
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setConnectionColor(serverPlayer, questId, prerequisiteId, color),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.connectionColor(questId, prerequisiteId, color);
                    EditorCommandSender.send(EditorCommandType.CONNECTION_COLOR, payload);
                });
    }

    public static void runConnectionModeAction(Player player, String questId, String prerequisiteId, boolean gridMode) {
        if (invalidQuestPair(questId, prerequisiteId)) return;
        ClientQuestCache.setConnectionModeLocal(questId, prerequisiteId, gridMode);
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setConnectionMode(serverPlayer, questId, prerequisiteId, gridMode),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.connectionMode(questId, prerequisiteId, gridMode);
                    EditorCommandSender.send(EditorCommandType.CONNECTION_MODE, payload);
                });
    }

    public static void runConnectionHiddenAction(Player player, String questId, String prerequisiteId, boolean hidden) {
        if (invalidQuestPair(questId, prerequisiteId)) return;
        ClientQuestCache.setConnectionHiddenLocal(questId, prerequisiteId, hidden);
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setConnectionHidden(serverPlayer, questId, prerequisiteId, hidden),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.connectionHidden(questId, prerequisiteId, hidden);
                    EditorCommandSender.send(EditorCommandType.CONNECTION_HIDDEN, payload);
                });
    }

    public static void runConnectionTextureAction(Player player, String questId, String prerequisiteId, String texture) {
        if (invalidQuestPair(questId, prerequisiteId)) return;
        QuestsAndStuffMod.debugLog("[QnS:UI] runConnectionTextureAction quest={} prereq={} texture={} isServerPlayer={}", questId, prerequisiteId, texture, player instanceof net.minecraft.server.level.ServerPlayer);
        ClientQuestCache.setConnectionTextureLocal(questId, prerequisiteId, texture);
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setConnectionTexture(serverPlayer, questId, prerequisiteId, texture),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.connectionTexture(questId, prerequisiteId, texture);
                    EditorCommandSender.send(EditorCommandType.CONNECTION_TEXTURE, payload);
                });
    }

    public static void runConnectionTextureSpacingAction(Player player, String questId, String prerequisiteId, int spacing) {
        if (invalidQuestPair(questId, prerequisiteId)) return;
        ClientQuestCache.setConnectionTextureSpacingLocal(questId, prerequisiteId, spacing);
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setConnectionTextureSpacing(serverPlayer, questId, prerequisiteId, spacing),
                () -> {
                    CompoundTag payload = EditorCommandPayloads.connectionTextureSpacing(questId, prerequisiteId, spacing);
                    EditorCommandSender.send(EditorCommandType.CONNECTION_TEXTURE_SPACING, payload);
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

    @FunctionalInterface
    private interface EcConnectionAction {
        void run(String group, String ecId, String questId);
    }

    private static void runEcConnectionAction(TabletUiState state, String sourceId, String targetId, EcConnectionAction action) {
        String group = EditorGroupCommandClient.selectedGroupName(state);
        if (group.isBlank()) return;
        String ecId = resolveEcId(state, group, sourceId, targetId);
        if (ecId.isBlank()) return;
        String questId = resolveQuestId(ecId, sourceId, targetId);
        action.run(group, ecId, questId);
    }

    public static void runEcConnectionColorAction(Player player, TabletUiState state, String sourceId, String targetId, int color) {
        runEcConnectionAction(state, sourceId, targetId, (group, ecId, questId) ->
                ConnectionRenderer.setEcConnectionColor(state, group, ecId, questId, color));
    }

    public static void runEcConnectionModeAction(Player player, TabletUiState state, String sourceId, String targetId, boolean direct) {
        runEcConnectionAction(state, sourceId, targetId, (group, ecId, questId) ->
                ConnectionRenderer.setEcConnectionMode(state, group, ecId, questId, direct));
    }

    public static void runEcConnectionHiddenAction(Player player, TabletUiState state, String sourceId, String targetId, boolean hidden) {
        runEcConnectionAction(state, sourceId, targetId, (group, ecId, questId) ->
                ConnectionRenderer.setEcConnectionHidden(state, group, ecId, questId, hidden));
    }

    public static void runEcConnectionTextureAction(TabletUiState state, String sourceId, String targetId, String texture) {
        runEcConnectionAction(state, sourceId, targetId, (group, ecId, questId) ->
                ConnectionRenderer.setEcConnectionTexture(state, group, ecId, questId, texture));
    }

    public static void runEcConnectionTextureSpacingAction(TabletUiState state, String sourceId, String targetId, int spacing) {
        runEcConnectionAction(state, sourceId, targetId, (group, ecId, questId) ->
                ConnectionRenderer.setEcConnectionTextureSpacing(state, group, ecId, questId, spacing));
    }
}
