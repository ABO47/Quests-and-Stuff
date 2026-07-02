package com.abo47.questsandstuff.client.tablet.quest.editor;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.ui.IntegratedServerActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.network.ModNetwork;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorAddQuestPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorRemoveQuestPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SEditorUpdateQuestPacket;
import com.abo47.questsandstuff.network.quest.editor.C2SResetQuestPacket;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.util.naming.QuestNaming;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EditorQuestCommandClient {
    private static final int MAX_DESCRIPTION_LINES = 256;
    private static final int MAX_DESCRIPTION_LINE_LENGTH = 16384;

    private EditorQuestCommandClient() {
    }

    public static void runQuestIconAction(Player player, String questId, String icon) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        String normalizedIcon = icon == null || icon.isBlank() ? "minecraft:book" : icon.trim();
        if (normalizedQuestId.isBlank()) {
            return;
        }
        ClientQuestCache.setQuestIconLocal(normalizedQuestId, normalizedIcon);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest icon picked quest={} icon={}", normalizedQuestId, normalizedIcon);
        CompoundTag payload = EditorCommandPayloads.questIcon(normalizedQuestId, normalizedIcon);
        EditorCommandSender.run(player, EditorCommandType.QUEST_ICON, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestIcon(serverPlayer, normalizedQuestId, normalizedIcon));
    }

    public static void setQuestHiddenMode(Player player, String questId, String mode) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        String normalizedMode = EditorCommandSender.value(mode);
        if (normalizedQuestId.isBlank() || normalizedMode.isBlank()) {
            return;
        }
        ClientQuestCache.setQuestHiddenModeLocal(normalizedQuestId, normalizedMode);
        CompoundTag payload = EditorCommandPayloads.questHiddenMode(normalizedQuestId, normalizedMode);
        EditorCommandSender.run(player, EditorCommandType.QUEST_HIDDEN_MODE, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestHiddenMode(serverPlayer, normalizedQuestId, normalizedMode));
    }

    public static void setQuestVisualHidden(Player player, String questId, boolean hidden) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        if (normalizedQuestId.isBlank()) {
            return;
        }
        ClientQuestCache.setQuestVisualHiddenLocal(normalizedQuestId, hidden);
        CompoundTag payload = EditorCommandPayloads.questVisualHidden(normalizedQuestId, hidden);
        EditorCommandSender.run(player, EditorCommandType.QUEST_VISUAL_HIDDEN, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestVisualHidden(serverPlayer, normalizedQuestId, hidden));
    }

    public static void setQuestCompletionSound(Player player, String questId, String sound) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        String normalizedSound = sound == null || sound.isBlank() ? "minecraft:ui.toast.challenge_complete" : sound.trim();
        if (normalizedQuestId.isBlank()) {
            return;
        }
        ClientQuestCache.setQuestCompletionSoundLocal(normalizedQuestId, normalizedSound);
        CompoundTag payload = EditorCommandPayloads.completionSound(normalizedQuestId, normalizedSound);
        EditorCommandSender.run(player, EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestCompletionSound(serverPlayer, normalizedQuestId, normalizedSound));
    }

    public static void setQuestCompletionSound(Player player, Set<String> questIds, String sound) {
        Set<String> targets = normalizedQuestIds(questIds);
        String normalizedSound = sound == null || sound.isBlank() ? QuestDisplay.DEFAULT_COMPLETION_SOUND : sound.trim();
        if (targets.isEmpty()) {
            return;
        }
        for (String questId : targets) {
            ClientQuestCache.setQuestCompletionSoundLocal(questId, normalizedSound);
        }
        CompoundTag payload = EditorCommandPayloads.completionSoundMany(targets, normalizedSound);
        EditorCommandSender.run(player, EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_MANY, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestCompletionSound(serverPlayer, targets, normalizedSound));
    }

    public static void setQuestCompletionSoundVolume(Player player, String questId, int volume) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        if (normalizedQuestId.isBlank()) {
            return;
        }
        int normalizedVolume = QuestDisplay.normalizeCompletionSoundVolume(volume);
        ClientQuestCache.setQuestCompletionSoundVolumeLocal(normalizedQuestId, normalizedVolume);
        CompoundTag payload = EditorCommandPayloads.completionSoundVolume(normalizedQuestId, normalizedVolume);
        EditorCommandSender.run(player, EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_VOLUME, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestCompletionSoundVolume(serverPlayer, normalizedQuestId, normalizedVolume));
    }

    public static void setQuestCompletionSoundVolume(Player player, Set<String> questIds, int volume) {
        Set<String> targets = normalizedQuestIds(questIds);
        if (targets.isEmpty()) {
            return;
        }
        int normalizedVolume = QuestDisplay.normalizeCompletionSoundVolume(volume);
        for (String questId : targets) {
            ClientQuestCache.setQuestCompletionSoundVolumeLocal(questId, normalizedVolume);
        }
        CompoundTag payload = EditorCommandPayloads.completionSoundVolumeMany(targets, normalizedVolume);
        EditorCommandSender.run(player, EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_VOLUME_MANY, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestCompletionSoundVolume(serverPlayer, targets, normalizedVolume));
    }

    public static void setQuestCompletionHudBackground(Player player, String questId, String background) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        String normalizedBackground = QuestDisplay.normalizeCompletionHudBackground(background);
        if (normalizedQuestId.isBlank()) {
            return;
        }
        ClientQuestCache.setQuestCompletionHudBackgroundLocal(normalizedQuestId, normalizedBackground);
        CompoundTag payload = EditorCommandPayloads.completionHudBackground(normalizedQuestId, normalizedBackground);
        EditorCommandSender.run(player, EditorCommandType.QUEST_COMPLETION_HUD_BACKGROUND, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestCompletionHudBackground(serverPlayer, normalizedQuestId, normalizedBackground));
    }

    public static void setQuestCompletionHudBackground(Player player, Set<String> questIds, String background) {
        Set<String> targets = normalizedQuestIds(questIds);
        String normalizedBackground = QuestDisplay.normalizeCompletionHudBackground(background);
        if (targets.isEmpty()) {
            return;
        }
        for (String questId : targets) {
            ClientQuestCache.setQuestCompletionHudBackgroundLocal(questId, normalizedBackground);
        }
        CompoundTag payload = EditorCommandPayloads.completionHudBackgroundMany(targets, normalizedBackground);
        EditorCommandSender.run(player, EditorCommandType.QUEST_COMPLETION_HUD_BACKGROUND_MANY, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestCompletionHudBackground(serverPlayer, targets, normalizedBackground));
    }

    public static void setQuestBackground(Player player, String questId, String background, boolean grayscale) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        String normalizedBackground = QuestDisplay.normalizeQuestBackground(background);
        if (normalizedQuestId.isBlank()) {
            return;
        }
        ClientQuestCache.setQuestBackgroundLocal(normalizedQuestId, normalizedBackground, grayscale);
        CompoundTag payload = EditorCommandPayloads.questBackground(normalizedQuestId, normalizedBackground, grayscale);
        EditorCommandSender.run(player, EditorCommandType.QUEST_BACKGROUND, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestBackground(serverPlayer, normalizedQuestId, normalizedBackground, grayscale));
    }

    public static void setQuestBackground(Player player, Set<String> questIds, String background, boolean grayscale) {
        Set<String> targets = normalizedQuestIds(questIds);
        String normalizedBackground = QuestDisplay.normalizeQuestBackground(background);
        if (targets.isEmpty()) {
            return;
        }
        for (String questId : targets) {
            ClientQuestCache.setQuestBackgroundLocal(questId, normalizedBackground, grayscale);
        }
        CompoundTag payload = EditorCommandPayloads.questBackgroundMany(targets, normalizedBackground, grayscale);
        EditorCommandSender.run(player, EditorCommandType.QUEST_BACKGROUND_MANY, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestBackground(serverPlayer, targets, normalizedBackground, grayscale));
    }

    public static void runRemoveQuestAction(Player player, String questId) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).removeQuest(serverPlayer, questId),
                () -> {
                    ClientQuestCache.removeQuestLocal(questId);
                    ModNetwork.sendToServer(new C2SEditorRemoveQuestPacket(questId));
                });
    }

    public static String predictNextQuestId(TabletUiState state) {
        return QuestNaming.nextQuestId(EditorGroupCommandClient.selectedGroupName(state), ClientQuestCache.questIds());
    }

    public static void addQuestAt(Player player, TabletUiState state, int logicalX, int logicalY, String title) {
        String group = EditorGroupCommandClient.selectedGroupName(state);
        if (group.isBlank()) {
            return;
        }
        int[] position = findNearestFreeCell(state, group, logicalX, logicalY);
        logicalX = position[0];
        logicalY = position[1];
        String predictedId = predictNextQuestId(state);
        String normalizedTitle = title == null ? "" : title.trim();

        int targetX = logicalX;
        int targetY = logicalY;
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).addQuest(serverPlayer, group, predictedId, targetX, targetY, normalizedTitle),
                () -> {
                    ClientQuestCache.createEditorQuestLocal(predictedId, group, targetX, targetY, normalizedTitle);
                    ModNetwork.sendToServer(new C2SEditorAddQuestPacket(group, predictedId, targetX, targetY, normalizedTitle));
                });

        state.canvas.canvasSelection.questIds().clear();
        state.canvas.canvasSelection.questIds().add(predictedId);
        state.chapterPanel.lastJumpQuest = predictedId;
    }

    public static void beginQuestTitleChange(TabletUiState state, String questId) {
        if (questId == null || questId.isBlank()) {
            return;
        }
        if (!ClientQuestCache.containsQuest(questId)) {
            return;
        }
        CompoundTag quest = ClientQuestCache.quest(questId);
        state.questDetails.pendingQuestTitleChangeId = questId;
        state.questDetails.questTitleDraft = quest.getString("title");
        QuestsAndStuffMod.debugLog("[QnS:UI] quest title change begin id={} title={}", questId, state.questDetails.questTitleDraft);
    }

    public static void cancelQuestTitleChange(TabletUiState state) {
        if (state.questDetails.pendingQuestTitleChangeId.isBlank()) {
            return;
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] quest title change cancel id={}", state.questDetails.pendingQuestTitleChangeId);
        state.questDetails.pendingQuestTitleChangeId = "";
        state.questDetails.questTitleDraft = "";
    }

    public static boolean commitQuestTitleChange(Player player, TabletUiState state) {
        String questId = state.questDetails.pendingQuestTitleChangeId;
        if (questId.isBlank()) {
            return false;
        }
        if (!ClientQuestCache.containsQuest(questId)) {
            cancelQuestTitleChange(state);
            return false;
        }
        CompoundTag quest = ClientQuestCache.quest(questId);
        String oldTitle = quest.getString("title");
        String subtitle = quest.getString("subtitle");
        String title = sanitizeQuestTitle(state.questDetails.questTitleDraft, oldTitle);
        if (!title.equals(oldTitle)) {
            runQuestDisplayAction(player, questId, title, subtitle);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest title change commit id={} from={} to={}", questId, oldTitle, title);
        }
        state.questDetails.pendingQuestTitleChangeId = "";
        state.questDetails.questTitleDraft = "";
        return true;
    }

    public static void putQuestTaskJson(Player player, String questId, String taskJson) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        if (normalizedQuestId.isBlank() || taskJson == null || taskJson.isBlank()) {
            return;
        }
        CompoundTag payload = EditorCommandPayloads.taskPut(normalizedQuestId, taskJson);
        EditorCommandSender.run(player, EditorCommandType.TASK_PUT, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).putQuestTask(serverPlayer, normalizedQuestId, taskJson));
    }

    public static void removeQuestTask(Player player, String questId, String taskId) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        String normalizedTaskId = EditorCommandSender.id(taskId);
        if (normalizedQuestId.isBlank() || normalizedTaskId.isBlank()) {
            return;
        }
        CompoundTag payload = EditorCommandPayloads.taskRemove(normalizedQuestId, normalizedTaskId);
        EditorCommandSender.run(player, EditorCommandType.TASK_REMOVE, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).removeQuestTask(serverPlayer, normalizedQuestId, normalizedTaskId));
        resetQuestProgress(player, normalizedQuestId);
    }

    public static void resetQuestProgress(Player player, String questId) {
        String normalizedQuestId = questId == null ? "" : questId.trim();
        if (normalizedQuestId.isBlank()) {
            return;
        }
        ClientQuestCache.resetQuestProgressLocal(normalizedQuestId);
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.engine(serverPlayer.server).resetQuest(serverPlayer, normalizedQuestId),
                () -> ModNetwork.sendToServer(new C2SResetQuestPacket(normalizedQuestId)));
    }

    public static void moveQuestTask(Player player, String questId, String taskId, int offset) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        String normalizedTaskId = EditorCommandSender.id(taskId);
        if (normalizedQuestId.isBlank() || normalizedTaskId.isBlank() || offset == 0) {
            return;
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] quest task drag drop quest={} task={} offset={}", normalizedQuestId, normalizedTaskId, offset);
        CompoundTag payload = EditorCommandPayloads.taskMove(normalizedQuestId, normalizedTaskId, offset);
        EditorCommandSender.run(player, EditorCommandType.TASK_MOVE, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).moveQuestTask(serverPlayer, normalizedQuestId, normalizedTaskId, offset));
    }

    public static void putQuestRewardJson(Player player, String questId, String rewardJson) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        if (normalizedQuestId.isBlank() || rewardJson == null || rewardJson.isBlank()) {
            return;
        }
        CompoundTag payload = EditorCommandPayloads.rewardPut(normalizedQuestId, rewardJson);
        EditorCommandSender.run(player, EditorCommandType.REWARD_PUT, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).putQuestReward(serverPlayer, normalizedQuestId, rewardJson));
    }

    public static void removeQuestReward(Player player, String questId, String rewardId) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        String normalizedRewardId = EditorCommandSender.id(rewardId);
        if (normalizedQuestId.isBlank() || normalizedRewardId.isBlank()) {
            return;
        }
        CompoundTag payload = EditorCommandPayloads.rewardRemove(normalizedQuestId, normalizedRewardId);
        EditorCommandSender.run(player, EditorCommandType.REWARD_REMOVE, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).removeQuestReward(serverPlayer, normalizedQuestId, normalizedRewardId));
    }

    public static void moveQuestReward(Player player, String questId, String rewardId, int offset) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        String normalizedRewardId = EditorCommandSender.id(rewardId);
        if (normalizedQuestId.isBlank() || normalizedRewardId.isBlank() || offset == 0) {
            return;
        }
        QuestsAndStuffMod.debugLog("[QnS:UI] quest reward drag drop quest={} reward={} offset={}", normalizedQuestId, normalizedRewardId, offset);
        CompoundTag payload = EditorCommandPayloads.rewardMove(normalizedQuestId, normalizedRewardId, offset);
        EditorCommandSender.run(player, EditorCommandType.REWARD_MOVE, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).moveQuestReward(serverPlayer, normalizedQuestId, normalizedRewardId, offset));
    }

    public static void updateQuestDisplay(Player player, String questId, String title, String subtitle) {
        runQuestDisplayAction(player, questId, title, subtitle);
    }

    public static void setQuestRepeatable(Player player, String questId, boolean enabled) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        if (normalizedQuestId.isBlank()) {
            return;
        }
        ClientQuestCache.setQuestRepeatableLocal(normalizedQuestId, enabled);
        CompoundTag payload = EditorCommandPayloads.questRepeatable(normalizedQuestId, enabled);
        EditorCommandSender.run(player, EditorCommandType.QUEST_REPEATABLE, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).setQuestRepeatable(serverPlayer, normalizedQuestId, enabled));
    }

    public static void updateQuestDescription(Player player, String questId, List<String> description) {
        String normalizedQuestId = EditorCommandSender.id(questId);
        if (normalizedQuestId.isBlank()) {
            return;
        }
        List<String> safeDescription = description == null ? List.of() : description.stream()
                .filter(line -> line != null)
                .map(EditorQuestCommandClient::limitDescriptionLine)
                .limit(MAX_DESCRIPTION_LINES)
                .toList();
        ClientQuestCache.setQuestDescriptionLocal(normalizedQuestId, safeDescription);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest description save quest={} lines={}", normalizedQuestId, safeDescription.size());
        CompoundTag payload = EditorCommandPayloads.description(normalizedQuestId, safeDescription);
        EditorCommandSender.run(player, EditorCommandType.DESCRIPTION_PUT, payload,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).updateQuestDescription(serverPlayer, normalizedQuestId, safeDescription));
    }

    private static String limitDescriptionLine(String line) {
        return line.length() > MAX_DESCRIPTION_LINE_LENGTH ? line.substring(0, MAX_DESCRIPTION_LINE_LENGTH) : line;
    }

    private static Set<String> normalizedQuestIds(Collection<String> questIds) {
        Set<String> targets = new LinkedHashSet<>();
        if (questIds == null) {
            return targets;
        }
        for (String questId : questIds) {
            String normalized = EditorCommandSender.id(questId);
            if (!normalized.isBlank()) {
                targets.add(normalized);
            }
        }
        return targets;
    }

    private static int[] findNearestFreeCell(TabletUiState state, String group, int startX, int startY) {
        int step = CanvasGeometry.gridSize(state);
        int x = TabletUiFactory.snapToGrid(state, startX);
        int y = TabletUiFactory.snapToGrid(state, startY);
        if (!isOccupied(group, x, y)) {
            return new int[]{x, y};
        }
        for (int i = 1; i <= 64; i++) {
            int right = x + i * step;
            if (!isOccupied(group, right, y)) {
                return new int[]{right, y};
            }
            int down = y + i * step;
            if (!isOccupied(group, x, down)) {
                return new int[]{x, down};
            }
            int left = x - i * step;
            if (!isOccupied(group, left, y)) {
                return new int[]{left, y};
            }
            int up = y - i * step;
            if (!isOccupied(group, x, up)) {
                return new int[]{x, up};
            }
        }
        return new int[]{x, y};
    }

    private static boolean isOccupied(String group, int x, int y) {
        for (var entry : ClientQuestCache.questEntries()) {
            CompoundTag quest = entry.getValue();
            CompoundTag groups = quest.getCompound("groups");
            if (!groups.contains(group)) {
                continue;
            }
            CompoundTag view = groups.getCompound(group);
            if (view.getInt("x") == x && view.getInt("y") == y) {
                return true;
            }
        }
        return false;
    }

    private static String sanitizeQuestTitle(String value, String fallback) {
        String base = value == null ? "" : value.trim().replace('\n', ' ').replace('\r', ' ');
        if (base.length() > 64) {
            base = base.substring(0, 64);
        }
        if (!base.isBlank()) {
            return base;
        }
        return fallback == null ? "" : fallback.trim();
    }

    private static void runQuestDisplayAction(Player player, String questId, String title, String subtitle) {
        ClientQuestCache.setQuestDisplayLocal(questId, title, subtitle);
        IntegratedServerActions.run(
                player,
                serverPlayer -> QuestServiceRegistry.editor(serverPlayer.server).updateQuestDisplay(serverPlayer, questId, title, subtitle),
                () -> ModNetwork.sendToServer(new C2SEditorUpdateQuestPacket(
                        questId,
                        title == null ? "" : title,
                        subtitle == null ? "" : subtitle)));
    }
}
