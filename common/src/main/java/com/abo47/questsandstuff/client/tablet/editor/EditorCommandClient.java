package com.abo47.questsandstuff.client.tablet.editor;

import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EditorCommandClient {
    private EditorCommandClient() {
    }

    public static void cycleGroup(TabletUiState state, int dir) {
        EditorChapterCommandClient.cycleGroup(state, dir);
    }

    public static String selectedGroupName(TabletUiState state) {
        return EditorChapterCommandClient.selectedGroupName(state);
    }

    public static boolean canEditGroups(TabletUiState state) {
        return EditorChapterCommandClient.canEditGroups(state);
    }

    public static boolean canManageGroups(TabletUiState state) {
        return EditorChapterCommandClient.canManageGroups(state);
    }

    public static String resolveGroupDraft(TabletUiState state, String fallback) {
        return EditorChapterCommandClient.resolveGroupDraft(state, fallback);
    }

    public static String nextGroupName() {
        return EditorChapterCommandClient.nextGroupName();
    }

    public static String nextGroupName(String baseName) {
        return EditorChapterCommandClient.nextGroupName(baseName);
    }

    public static String uniqueGroupName(String preferred, String excludeCurrent) {
        return EditorChapterCommandClient.uniqueGroupName(preferred, excludeCurrent);
    }

    public static String nextRenamedGroup(String source) {
        return EditorChapterCommandClient.nextRenamedGroup(source);
    }

    public static String sanitizeGroupName(String value) {
        return EditorChapterCommandClient.sanitizeGroupName(value);
    }

    public static void runGroupAction(Player player, TabletUiState state, String action, String group, String value, int offset) {
        EditorChapterCommandClient.runGroupAction(player, state, action, group, value, offset);
    }

    public static void runCanvasMoveAction(Player player, TabletUiState state, Map<String, CanvasPoint> positions) {
        EditorCanvasCommandClient.runCanvasMoveAction(player, state, positions);
    }

    public static void runCanvasScaleAction(Player player, TabletUiState state, Map<String, Float> scales) {
        EditorCanvasCommandClient.runCanvasScaleAction(player, state, scales);
    }

    public static void runCanvasCopyAction(Player player, String groupName, Set<String> questIds) {
        EditorCanvasCommandClient.runCanvasCopyAction(player, groupName, questIds);
    }

    public static void runCanvasPasteClipboardAction(Player player, String groupName, int x, int y) {
        EditorCanvasCommandClient.runCanvasPasteClipboardAction(player, groupName, x, y);
    }

    public static void runPrerequisiteAction(Player player, String questId, String prerequisiteId, boolean add) {
        EditorCanvasCommandClient.runPrerequisiteAction(player, questId, prerequisiteId, add);
    }

    public static void runConnectionColorAction(Player player, String questId, String prerequisiteId, int color) {
        EditorCanvasCommandClient.runConnectionColorAction(player, questId, prerequisiteId, color);
    }

    public static void runConnectionModeAction(Player player, String questId, String prerequisiteId, boolean gridMode) {
        EditorCanvasCommandClient.runConnectionModeAction(player, questId, prerequisiteId, gridMode);
    }

    public static void runConnectionHiddenAction(Player player, String questId, String prerequisiteId, boolean hidden) {
        EditorCanvasCommandClient.runConnectionHiddenAction(player, questId, prerequisiteId, hidden);
    }

    public static void runQuestIconAction(Player player, String questId, String icon) {
        EditorQuestCommandClient.runQuestIconAction(player, questId, icon);
    }

    public static void setQuestHiddenMode(Player player, String questId, String mode) {
        EditorQuestCommandClient.setQuestHiddenMode(player, questId, mode);
    }

    public static void setQuestVisualHidden(Player player, String questId, boolean hidden) {
        EditorQuestCommandClient.setQuestVisualHidden(player, questId, hidden);
    }

    public static void setQuestCompletionSound(Player player, String questId, String sound) {
        EditorQuestCommandClient.setQuestCompletionSound(player, questId, sound);
    }

    public static void setQuestCompletionSoundVolume(Player player, String questId, int volume) {
        EditorQuestCommandClient.setQuestCompletionSoundVolume(player, questId, volume);
    }

    public static void setQuestBackground(Player player, String questId, String background, boolean grayscale) {
        EditorQuestCommandClient.setQuestBackground(player, questId, background, grayscale);
    }

    public static void runRemoveQuestAction(Player player, String questId) {
        EditorQuestCommandClient.runRemoveQuestAction(player, questId);
    }

    public static String predictNextQuestId(TabletUiState state) {
        return EditorQuestCommandClient.predictNextQuestId(state);
    }

    public static void addQuestAt(Player player, TabletUiState state, int logicalX, int logicalY, String title) {
        EditorQuestCommandClient.addQuestAt(player, state, logicalX, logicalY, title);
    }

    public static void beginQuestTitleChange(TabletUiState state, String questId) {
        EditorQuestCommandClient.beginQuestTitleChange(state, questId);
    }

    public static void cancelQuestTitleChange(TabletUiState state) {
        EditorQuestCommandClient.cancelQuestTitleChange(state);
    }

    public static boolean commitQuestTitleChange(Player player, TabletUiState state) {
        return EditorQuestCommandClient.commitQuestTitleChange(player, state);
    }

    public static void putQuestTaskJson(Player player, String questId, String taskJson) {
        EditorQuestCommandClient.putQuestTaskJson(player, questId, taskJson);
    }

    public static void removeQuestTask(Player player, String questId, String taskId) {
        EditorQuestCommandClient.removeQuestTask(player, questId, taskId);
    }

    public static void resetQuestProgress(Player player, String questId) {
        EditorQuestCommandClient.resetQuestProgress(player, questId);
    }

    public static void moveQuestTask(Player player, String questId, String taskId, int offset) {
        EditorQuestCommandClient.moveQuestTask(player, questId, taskId, offset);
    }

    public static void putQuestRewardJson(Player player, String questId, String rewardJson) {
        EditorQuestCommandClient.putQuestRewardJson(player, questId, rewardJson);
    }

    public static void removeQuestReward(Player player, String questId, String rewardId) {
        EditorQuestCommandClient.removeQuestReward(player, questId, rewardId);
    }

    public static void moveQuestReward(Player player, String questId, String rewardId, int offset) {
        EditorQuestCommandClient.moveQuestReward(player, questId, rewardId, offset);
    }

    public static void updateQuestDisplay(Player player, String questId, String title, String subtitle) {
        EditorQuestCommandClient.updateQuestDisplay(player, questId, title, subtitle);
    }

    public static void setQuestAutoClaim(Player player, String questId, boolean enabled) {
        EditorQuestCommandClient.setQuestAutoClaim(player, questId, enabled);
    }

    public static void setQuestRepeatable(Player player, String questId, boolean enabled) {
        EditorQuestCommandClient.setQuestRepeatable(player, questId, enabled);
    }

    public static void updateQuestDescription(Player player, String questId, List<String> description) {
        EditorQuestCommandClient.updateQuestDescription(player, questId, description);
    }
}
