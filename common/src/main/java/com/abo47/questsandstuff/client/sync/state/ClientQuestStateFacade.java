package com.abo47.questsandstuff.client.sync.state;

import com.abo47.questsandstuff.client.sync.packet.ClientSyncInbox;
import com.abo47.questsandstuff.client.sync.packet.ClientSyncUiBridge;
import com.abo47.questsandstuff.client.sync.mutation.ClientQuestLocalMutations;
import com.abo47.questsandstuff.client.sync.packet.ClientSyncPayloadApplier;
import com.abo47.questsandstuff.client.sync.packet.ClientRawSyncStore;
import com.abo47.questsandstuff.client.sync.mutation.ClientEditorMutationApplier;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClientQuestStateFacade {
    private ClientQuestStateFacade() {
    }

    public static void resetStateForTests() {
        ClientQuestState.reset();
        ClientChapterState.reset();
        ClientCanvasLayerState.reset();
        ClientDisplayState.reset();
        ClientRawSyncStore.reset();
        ClientSyncInbox.reset();
        ClientSyncUiBridge.resetForTests();
    }

    public static void applyFullSync(CompoundTag payload) {
        ClientSyncPayloadApplier.applyFullSync(payload);
    }

    public static void applyDeltaSync(CompoundTag payload) {
        ClientSyncPayloadApplier.applyDeltaSync(payload);
    }

    public static void acceptFullChunk(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        ClientSyncInbox.acceptFullChunk(sequence, chunkIndex, chunkCount, payload);
    }

    public static void acceptDeltaChunk(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        ClientSyncInbox.acceptDeltaChunk(sequence, chunkIndex, chunkCount, payload);
    }

    public static void applyPinnedSync(long sequence, List<String> pinnedList) {
        if (ClientSyncInbox.acceptPinnedSequence(sequence)) {
            ClientSyncPayloadApplier.applyPinnedSync(pinnedList);
        }
    }

    public static void applyDescriptionSync(CompoundTag payload) {
        ClientSyncPayloadApplier.applyDescriptionSync(payload);
    }

    public static void acceptDescriptionChunk(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        ClientSyncInbox.acceptDescriptionChunk(sequence, chunkIndex, chunkCount, payload);
    }

    public static void applyDisplayCacheSync(long sequence, CompoundTag payload) {
        if (ClientSyncInbox.acceptDisplayCacheSequence(sequence)) {
            ClientDisplayState.applyDisplayCacheSync(payload);
        }
    }

    public static void applyQuestEvent(long sequence, String eventType, String questId, String rewardId) {
        if (ClientSyncInbox.acceptEventSequence(sequence)) {
            ClientDisplayState.applyQuestEvent(eventType, questId, rewardId);
        }
    }

    public static void applyEditorMutation(long sequence, String action, String questId, CompoundTag questTag) {
        ClientEditorMutationApplier.apply(sequence, action, questId, questTag);
    }

    public static Map<String, CompoundTag> quests() {
        return ClientQuestState.questSnapshot();
    }

    public static List<Map.Entry<String, CompoundTag>> questEntries() {
        return ClientQuestState.questEntries();
    }

    public static Set<String> questIds() {
        return ClientQuestState.questIdsSnapshot();
    }

    public static List<String> questIdsInGroup(String group) {
        if (group == null || group.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, CompoundTag> entry : questEntries()) {
            CompoundTag groups = entry.getValue().getCompound(QuestSyncKeys.Quest.GROUPS);
            if (groups.contains(group)) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public static boolean containsQuest(String questId) {
        return ClientQuestState.containsQuest(questId);
    }

    public static CompoundTag quest(String questId) {
        return ClientQuestState.questCopy(questId);
    }

    public static CompoundTag questTasks(String questId) {
        return ClientQuestState.questSectionCopy(questId, QuestSyncKeys.Quest.TASKS);
    }

    public static CompoundTag questRewards(String questId) {
        return ClientQuestState.questSectionCopy(questId, QuestSyncKeys.Quest.REWARDS);
    }

    public static Set<String> pinned() {
        return ClientQuestState.pinnedSnapshot();
    }

    public static void togglePinnedLocal(String questId) {
        ClientQuestState.togglePinned(questId);
    }

    public static Map<String, String> advancementDisplays() {
        return ClientDisplayState.advancementDisplays();
    }

    public static Map<String, String> lootTableDisplays() {
        return ClientDisplayState.lootTableDisplays();
    }

    public static Map<String, String> biomeDisplays() {
        return ClientDisplayState.biomeDisplays();
    }

    public static List<String> recentEvents() {
        return ClientDisplayState.recentEvents();
    }

    public static void noteQuestCompletedForChapterNotices(String questId, String currentGroup) {
        ClientDisplayState.noteQuestCompleted(quest(questId), currentGroup);
    }

    public static boolean groupHasCompletionNotice(String group) {
        return ClientDisplayState.chapterHasCompletionNotice(group);
    }

    public static void clearGroupCompletionNotice(String group) {
        ClientDisplayState.clearChapterCompletionNotice(group);
    }

    public static List<String> groupOrder() {
        return ClientChapterState.groupOrderSnapshot();
    }

    public static String groupIcon(String group) {
        return ClientChapterState.groupIcon(group);
    }

    public static String groupBackground(String group) {
        return ClientChapterState.groupBackground(group);
    }

    public static String groupCanvasBackground(String group) {
        return ClientChapterState.groupCanvasBackground(group);
    }

    public static String groupTextAlign(String group) {
        return ClientChapterState.groupTextAlign(group);
    }

    public static int groupTextColor(String group) {
        return ClientChapterState.groupTextColor(group);
    }

    public static String groupTextStyle(String group) {
        return ClientChapterState.groupTextStyle(group);
    }

    public static int groupTextSize(String group) {
        return ClientChapterState.groupTextSize(group);
    }

    public static boolean groupLockUntilUnlocked(String group) {
        return ClientChapterState.groupLockUntilUnlocked(group);
    }

    public static boolean groupHideUntilUnlocked(String group) {
        return ClientChapterState.groupHideUntilUnlocked(group);
    }

    public static boolean groupLockedPreview(String group) {
        return ClientQuestPreviewChecker.groupLocked(group);
    }

    public static boolean groupHiddenPreview(String group) {
        return ClientQuestPreviewChecker.groupHidden(group);
    }

    public static boolean groupOpenablePreview(String group) {
        return ClientQuestPreviewChecker.groupOpenable(group);
    }

    public static List<String> selectableGroupOrder(boolean canEdit) {
        return ClientQuestPreviewChecker.selectableGroupOrder(canEdit);
    }

    public static List<String> visibleGroupOrder(boolean canEdit) {
        return ClientQuestPreviewChecker.visibleGroupOrder(canEdit);
    }

    public static boolean questLockedPreview(CompoundTag quest) {
        return ClientQuestPreviewChecker.questLocked(quest);
    }

    public static boolean questHiddenPreview(CompoundTag quest) {
        return ClientQuestPreviewChecker.questHidden(quest);
    }

    public static Map<String, List<CanvasImageLayer>> canvasImagesByGroup() {
        return ClientCanvasLayerState.imagesByGroup();
    }

    public static Map<String, List<CanvasTextLayer>> canvasTextsByGroup() {
        return ClientCanvasLayerState.textsByGroup();
    }

    public static Map<String, List<CanvasExclusiveChoice>> canvasExclusiveChoicesByGroup() {
        return ClientCanvasLayerState.exclusiveChoicesByGroup();
    }

    public static Map<String, List<String>> canvasLayerOrderByGroup() {
        return ClientCanvasLayerState.layerOrderByGroup();
    }

    public static List<CanvasImageLayer> canvasImages(String group) {
        return ClientCanvasLayerState.images(group);
    }

    public static List<CanvasTextLayer> canvasTexts(String group) {
        return ClientCanvasLayerState.texts(group);
    }

    public static List<CanvasExclusiveChoice> canvasExclusiveChoices(String group) {
        return ClientCanvasLayerState.exclusiveChoices(group);
    }

    public static List<String> canvasLayerOrder(String group) {
        return ClientCanvasLayerState.layerOrder(group);
    }

    public static void createGroupLocal(String group) {
        ClientQuestLocalMutations.createGroupLocal(group);
    }

    public static void renameGroupLocal(String from, String to) {
        ClientQuestLocalMutations.renameGroupLocal(from, to);
    }

    public static void deleteGroupLocal(String group) {
        ClientQuestLocalMutations.deleteGroupLocal(group);
    }

    public static void moveGroupLocal(String group, int offset) {
        ClientQuestLocalMutations.moveGroupLocal(group, offset);
    }

    public static void moveGroupToIndexLocal(String group, int targetIndex) {
        ClientQuestLocalMutations.moveGroupToIndexLocal(group, targetIndex);
    }

    public static void setGroupIconLocal(String group, String icon) {
        ClientQuestLocalMutations.setGroupIconLocal(group, icon);
    }

    public static void setGroupBackgroundLocal(String group, String background) {
        ClientQuestLocalMutations.setGroupBackgroundLocal(group, background);
    }

    public static void setGroupCanvasBackgroundLocal(String group, String background) {
        ClientQuestLocalMutations.setGroupCanvasBackgroundLocal(group, background);
    }

    public static void setGroupTextAlignLocal(String group, String align) {
        ClientQuestLocalMutations.setGroupTextAlignLocal(group, align);
    }

    public static void setGroupTextColorLocal(String group, int color) {
        ClientQuestLocalMutations.setGroupTextColorLocal(group, color);
    }

    public static void setGroupTextStyleLocal(String group, String style) {
        ClientQuestLocalMutations.setGroupTextStyleLocal(group, style);
    }

    public static void setGroupTextSizeLocal(String group, int size) {
        ClientQuestLocalMutations.setGroupTextSizeLocal(group, size);
    }

    public static void setGroupLockUntilUnlockedLocal(String group, boolean lockUntilUnlocked) {
        ClientQuestLocalMutations.setGroupLockUntilUnlockedLocal(group, lockUntilUnlocked);
    }

    public static void setGroupHideUntilUnlockedLocal(String group, boolean hideUntilUnlocked) {
        ClientQuestLocalMutations.setGroupHideUntilUnlockedLocal(group, hideUntilUnlocked);
    }

    public static void putCanvasExclusiveChoiceLocal(String group, CanvasExclusiveChoice ec) {
        ClientQuestLocalMutations.putCanvasExclusiveChoiceLocal(group, ec);
    }

    public static void removeCanvasExclusiveChoiceLocal(String group, String ecId) {
        ClientQuestLocalMutations.removeCanvasExclusiveChoiceLocal(group, ecId);
    }

    public static void putCanvasImageLocal(String group, CanvasImageLayer image) {
        ClientQuestLocalMutations.putCanvasImageLocal(group, image);
    }

    public static void removeCanvasImageLocal(String group, String imageId) {
        ClientQuestLocalMutations.removeCanvasImageLocal(group, imageId);
    }

    public static void putCanvasTextLocal(String group, CanvasTextLayer text) {
        ClientQuestLocalMutations.putCanvasTextLocal(group, text);
    }

    public static void removeCanvasTextLocal(String group, String textId) {
        ClientQuestLocalMutations.removeCanvasTextLocal(group, textId);
    }

    public static void setCanvasLayerOrderLocal(String group, List<String> order) {
        ClientQuestLocalMutations.setCanvasLayerOrderLocal(group, order);
    }

    public static int completedCount() {
        return ClientQuestLocalMutations.completedCount();
    }

    public static int totalCount() {
        return ClientQuestLocalMutations.totalCount();
    }

    public static void setQuestDisplayLocal(String questId, String title, String subtitle) {
        ClientQuestLocalMutations.setQuestDisplayLocal(questId, title, subtitle);
    }

    public static void setQuestDescriptionLocal(String questId, List<String> description) {
        ClientQuestLocalMutations.setQuestDescriptionLocal(questId, description);
    }

    public static void setQuestIconLocal(String questId, String icon) {
        ClientQuestLocalMutations.setQuestIconLocal(questId, icon);
    }

    public static void setQuestRepeatableLocal(String questId, boolean enabled) {
        ClientQuestLocalMutations.setQuestRepeatableLocal(questId, enabled);
    }

    public static void setQuestHiddenModeLocal(String questId, String hiddenMode) {
        ClientQuestLocalMutations.setQuestHiddenModeLocal(questId, hiddenMode);
    }

    public static void setQuestVisualHiddenLocal(String questId, boolean hidden) {
        ClientQuestLocalMutations.setQuestVisualHiddenLocal(questId, hidden);
    }

    public static void setQuestCompletionSoundLocal(String questId, String sound) {
        ClientQuestLocalMutations.setQuestCompletionSoundLocal(questId, sound);
    }

    public static void setQuestCompletionSoundVolumeLocal(String questId, int volume) {
        ClientQuestLocalMutations.setQuestCompletionSoundVolumeLocal(questId, volume);
    }

    public static void setQuestCompletionHudBackgroundLocal(String questId, String background) {
        ClientQuestLocalMutations.setQuestCompletionHudBackgroundLocal(questId, background);
    }

    public static void setQuestBackgroundLocal(String questId, String background, boolean grayscale) {
        ClientQuestLocalMutations.setQuestBackgroundLocal(questId, background, grayscale);
    }

    public static void resetQuestProgressLocal(String questId) {
        ClientQuestLocalMutations.resetQuestProgressLocal(questId);
    }

    public static void setQuestClaimedLocal(String questId, boolean claimed) {
        ClientQuestLocalMutations.setQuestClaimedLocal(questId, claimed);
    }

    public static void putQuestTaskJsonLocal(String questId, String taskJson) {
        ClientQuestLocalMutations.putQuestTaskJsonLocal(questId, taskJson);
    }

    public static void putQuestRewardJsonLocal(String questId, String rewardJson) {
        ClientQuestLocalMutations.putQuestRewardJsonLocal(questId, rewardJson);
    }

    public static void setQuestPrerequisiteLocal(String questId, String prerequisiteId, boolean add) {
        ClientQuestLocalMutations.setQuestPrerequisiteLocal(questId, prerequisiteId, add);
    }

    public static void setConnectionColorLocal(String questId, String prerequisiteId, int color) {
        ClientQuestLocalMutations.setConnectionColorLocal(questId, prerequisiteId, color);
    }

    public static void setConnectionModeLocal(String questId, String prerequisiteId, boolean gridMode) {
        ClientQuestLocalMutations.setConnectionModeLocal(questId, prerequisiteId, gridMode);
    }

    public static void setConnectionHiddenLocal(String questId, String prerequisiteId, boolean hidden) {
        ClientQuestLocalMutations.setConnectionHiddenLocal(questId, prerequisiteId, hidden);
    }

    public static void setConnectionTextureLocal(String questId, String prerequisiteId, String texture) {
        ClientQuestLocalMutations.setConnectionTextureLocal(questId, prerequisiteId, texture);
    }

    public static void setConnectionTextureSpacingLocal(String questId, String prerequisiteId, int spacing) {
        ClientQuestLocalMutations.setConnectionTextureSpacingLocal(questId, prerequisiteId, spacing);
    }

    public static void setQuestPositionInGroupLocal(String questId, String group, int x, int y) {
        ClientQuestLocalMutations.setQuestPositionInGroupLocal(questId, group, x, y);
    }

    public static void setQuestScaleInGroupLocal(String questId, String group, float scale) {
        ClientQuestLocalMutations.setQuestScaleInGroupLocal(questId, group, scale);
    }

    public static void createEditorQuestLocal(String questId, String group, int x, int y, String title) {
        ClientQuestLocalMutations.createEditorQuestLocal(questId, group, x, y, title);
    }

    public static void copyQuestLocal(String sourceQuestId, String newQuestId, String group, int x, int y, float scale, Map<String, String> copiedIds) {
        ClientQuestLocalMutations.copyQuestLocal(sourceQuestId, newQuestId, group, x, y, scale, copiedIds);
    }

    public static void copyQuestSnapshotLocal(CompoundTag sourceSnapshot, String sourceQuestId, String newQuestId, String group, int x, int y, float scale, Map<String, String> copiedIds) {
        ClientQuestLocalMutations.copyQuestSnapshotLocal(sourceSnapshot, sourceQuestId, newQuestId, group, x, y, scale, copiedIds);
    }

    public static void remapCopiedQuestPrerequisitesLocal(Map<String, String> copiedIds, Map<String, CompoundTag> snapshots) {
        ClientQuestLocalMutations.remapCopiedQuestPrerequisitesLocal(copiedIds, snapshots);
    }

    public static void removeQuestLocal(String questId) {
        ClientQuestLocalMutations.removeQuestLocal(questId);
    }

    public static String summaryLine() {
        return completedCount() + " / " + totalCount() + " completed";
    }
}
