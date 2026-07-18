package com.abo47.questsandstuff.client.sync.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.client.sync.mutation.ClientEditorMutationApplier;
import com.abo47.questsandstuff.client.sync.mutation.ClientQuestMutator;
import com.abo47.questsandstuff.client.sync.packet.ClientSyncInbox;
import com.abo47.questsandstuff.client.sync.packet.ClientSyncPayloadApplier;
import com.abo47.questsandstuff.client.sync.packet.ClientSyncUiBridge;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.sync.SyncKeys;

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

    public static List<String> questIdsInChapter(String chapter) {
        if (chapter == null || chapter.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, CompoundTag> entry : questEntries()) {
            CompoundTag groups = entry.getValue().getCompound(SyncKeys.Quest.CHAPTERS);
            if (groups.contains(chapter)) {
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
        return ClientQuestState.questSectionCopy(questId, SyncKeys.Quest.TASKS);
    }

    public static CompoundTag questRewards(String questId) {
        return ClientQuestState.questSectionCopy(questId, SyncKeys.Quest.REWARDS);
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

    public static void noteQuestCompletedForChapterNotices(String questId, String currentChapter) {
        ClientDisplayState.noteQuestCompleted(quest(questId), currentChapter);
    }

    public static boolean chapterHasCompletionNotice(String chapter) {
        return ClientDisplayState.chapterHasCompletionNotice(chapter);
    }

    public static void clearChapterCompletionNotice(String chapter) {
        ClientDisplayState.clearChapterCompletionNotice(chapter);
    }

    public static List<String> chapterOrder() {
        return ClientChapterState.chapterOrderSnapshot();
    }

    public static String chapterIcon(String chapter) {
        return ClientChapterState.chapterIcon(chapter);
    }

    public static String chapterBackground(String chapter) {
        return ClientChapterState.chapterBackground(chapter);
    }

    public static String chapterCanvasBackground(String chapter) {
        return ClientChapterState.chapterCanvasBackground(chapter);
    }

    public static String chapterTextAlign(String chapter) {
        return ClientChapterState.chapterTextAlign(chapter);
    }

    public static int chapterTextColor(String chapter) {
        return ClientChapterState.chapterTextColor(chapter);
    }

    public static String chapterTextStyle(String chapter) {
        return ClientChapterState.chapterTextStyle(chapter);
    }

    public static int chapterTextSize(String chapter) {
        return ClientChapterState.chapterTextSize(chapter);
    }

    public static boolean chapterLockUntilUnlocked(String chapter) {
        return ClientChapterState.chapterLockUntilUnlocked(chapter);
    }

    public static boolean chapterHideUntilUnlocked(String chapter) {
        return ClientChapterState.chapterHideUntilUnlocked(chapter);
    }

    public static boolean chapterLockedPreview(String chapter) {
        return ClientQuestPreviewChecker.chapterLocked(chapter);
    }

    public static boolean chapterHiddenPreview(String chapter) {
        return ClientQuestPreviewChecker.chapterHidden(chapter);
    }

    public static boolean chapterOpenablePreview(String chapter) {
        return ClientQuestPreviewChecker.chapterOpenable(chapter);
    }

    public static List<String> selectableChapterOrder(boolean canEdit) {
        return ClientQuestPreviewChecker.selectableChapterOrder(canEdit);
    }

    public static List<String> visibleChapterOrder(boolean canEdit) {
        return ClientQuestPreviewChecker.visibleChapterOrder(canEdit);
    }

    public static boolean questLockedPreview(CompoundTag quest) {
        return ClientQuestPreviewChecker.questLocked(quest);
    }

    public static boolean questHiddenPreview(CompoundTag quest) {
        return ClientQuestPreviewChecker.questHidden(quest);
    }

    public static Map<String, List<CanvasImageLayer>> canvasImagesByChapter() {
        return ClientCanvasLayerState.imagesByChapter();
    }

    public static Map<String, List<CanvasTextLayer>> canvasTextsByChapter() {
        return ClientCanvasLayerState.textsByChapter();
    }

    public static Map<String, List<CanvasExclusiveChoice>> canvasExclusiveChoicesByChapter() {
        return ClientCanvasLayerState.exclusiveChoicesByChapter();
    }

    public static Map<String, List<String>> canvasLayerOrderByChapter() {
        return ClientCanvasLayerState.layerOrderByChapter();
    }

    public static List<CanvasImageLayer> canvasImages(String chapter) {
        return ClientCanvasLayerState.images(chapter);
    }

    public static List<CanvasTextLayer> canvasTexts(String chapter) {
        return ClientCanvasLayerState.texts(chapter);
    }

    public static List<CanvasExclusiveChoice> canvasExclusiveChoices(String chapter) {
        return ClientCanvasLayerState.exclusiveChoices(chapter);
    }

    public static List<String> canvasLayerOrder(String chapter) {
        return ClientCanvasLayerState.layerOrder(chapter);
    }

    public static void createChapterLocal(String chapter) {
        ClientQuestMutator.createChapterLocal(chapter);
    }

    public static void renameChapterLocal(String from, String to) {
        ClientQuestMutator.renameChapterLocal(from, to);
    }

    public static void deleteChapterLocal(String chapter) {
        ClientQuestMutator.deleteChapterLocal(chapter);
    }

    public static void moveChapterLocal(String chapter, int offset) {
        ClientQuestMutator.moveChapterLocal(chapter, offset);
    }

    public static void moveChapterToIndexLocal(String chapter, int targetIndex) {
        ClientQuestMutator.moveChapterToIndexLocal(chapter, targetIndex);
    }

    public static void setChapterIconLocal(String chapter, String icon) {
        ClientQuestMutator.setChapterIconLocal(chapter, icon);
    }

    public static void setChapterBackgroundLocal(String chapter, String background) {
        ClientQuestMutator.setChapterBackgroundLocal(chapter, background);
    }

    public static void setChapterCanvasBackgroundLocal(String chapter, String background) {
        ClientQuestMutator.setChapterCanvasBackgroundLocal(chapter, background);
    }

    public static void setChapterTextAlignLocal(String chapter, String align) {
        ClientQuestMutator.setChapterTextAlignLocal(chapter, align);
    }

    public static void setChapterTextColorLocal(String chapter, int color) {
        ClientQuestMutator.setChapterTextColorLocal(chapter, color);
    }

    public static void setChapterTextStyleLocal(String chapter, String style) {
        ClientQuestMutator.setChapterTextStyleLocal(chapter, style);
    }

    public static void setChapterTextSizeLocal(String chapter, int size) {
        ClientQuestMutator.setChapterTextSizeLocal(chapter, size);
    }

    public static void setChapterLockUntilUnlockedLocal(String chapter, boolean lockUntilUnlocked) {
        ClientQuestMutator.setChapterLockUntilUnlockedLocal(chapter, lockUntilUnlocked);
    }

    public static void setChapterHideUntilUnlockedLocal(String chapter, boolean hideUntilUnlocked) {
        ClientQuestMutator.setChapterHideUntilUnlockedLocal(chapter, hideUntilUnlocked);
    }

    public static void putCanvasExclusiveChoiceLocal(String chapter, CanvasExclusiveChoice ec) {
        ClientQuestMutator.putCanvasExclusiveChoiceLocal(chapter, ec);
    }

    public static void removeCanvasExclusiveChoiceLocal(String chapter, String ecId) {
        ClientQuestMutator.removeCanvasExclusiveChoiceLocal(chapter, ecId);
    }

    public static void putCanvasImageLocal(String chapter, CanvasImageLayer image) {
        ClientQuestMutator.putCanvasImageLocal(chapter, image);
    }

    public static void removeCanvasImageLocal(String chapter, String imageId) {
        ClientQuestMutator.removeCanvasImageLocal(chapter, imageId);
    }

    public static void putCanvasTextLocal(String chapter, CanvasTextLayer text) {
        ClientQuestMutator.putCanvasTextLocal(chapter, text);
    }

    public static void removeCanvasTextLocal(String chapter, String textId) {
        ClientQuestMutator.removeCanvasTextLocal(chapter, textId);
    }

    public static void setCanvasLayerOrderLocal(String chapter, List<String> order) {
        ClientQuestMutator.setCanvasLayerOrderLocal(chapter, order);
    }

    public static int completedCount() {
        return ClientQuestMutator.completedCount();
    }

    public static int totalCount() {
        return ClientQuestMutator.totalCount();
    }

    public static void setQuestDisplayLocal(String questId, String title, String subtitle) {
        ClientQuestMutator.setQuestDisplayLocal(questId, title, subtitle);
    }

    public static void setQuestDescriptionLocal(String questId, List<String> description) {
        ClientQuestMutator.setQuestDescriptionLocal(questId, description);
    }

    public static void setQuestIconLocal(String questId, String icon) {
        ClientQuestMutator.setQuestIconLocal(questId, icon);
    }

    public static void setQuestRepeatableLocal(String questId, boolean enabled) {
        ClientQuestMutator.setQuestRepeatableLocal(questId, enabled);
    }

    public static void setQuestHiddenModeLocal(String questId, String hiddenMode) {
        ClientQuestMutator.setQuestHiddenModeLocal(questId, hiddenMode);
    }

    public static void setQuestVisualHiddenLocal(String questId, boolean hidden) {
        ClientQuestMutator.setQuestVisualHiddenLocal(questId, hidden);
    }

    public static void setQuestCompletionSoundLocal(String questId, String sound) {
        ClientQuestMutator.setQuestCompletionSoundLocal(questId, sound);
    }

    public static void setQuestCompletionSoundVolumeLocal(String questId, int volume) {
        ClientQuestMutator.setQuestCompletionSoundVolumeLocal(questId, volume);
    }

    public static void setQuestCompletionHudBackgroundLocal(String questId, String background) {
        ClientQuestMutator.setQuestCompletionHudBackgroundLocal(questId, background);
    }

    public static void setQuestBackgroundLocal(String questId, String background, boolean grayscale) {
        ClientQuestMutator.setQuestBackgroundLocal(questId, background, grayscale);
    }

    public static void resetQuestProgressLocal(String questId) {
        ClientQuestMutator.resetQuestProgressLocal(questId);
    }

    public static void setQuestClaimedLocal(String questId, boolean claimed) {
        ClientQuestMutator.setQuestClaimedLocal(questId, claimed);
    }

    public static void putQuestTaskJsonLocal(String questId, String taskJson) {
        ClientQuestMutator.putQuestTaskJsonLocal(questId, taskJson);
    }

    public static void putQuestRewardJsonLocal(String questId, String rewardJson) {
        ClientQuestMutator.putQuestRewardJsonLocal(questId, rewardJson);
    }

    public static void setQuestPrerequisiteLocal(String questId, String prerequisiteId, boolean add) {
        ClientQuestMutator.setQuestPrerequisiteLocal(questId, prerequisiteId, add);
    }

    public static void setConnectionColorLocal(String questId, String prerequisiteId, int color) {
        ClientQuestMutator.setConnectionColorLocal(questId, prerequisiteId, color);
    }

    public static void setConnectionModeLocal(String questId, String prerequisiteId, boolean gridMode) {
        ClientQuestMutator.setConnectionModeLocal(questId, prerequisiteId, gridMode);
    }

    public static void setConnectionHiddenLocal(String questId, String prerequisiteId, boolean hidden) {
        ClientQuestMutator.setConnectionHiddenLocal(questId, prerequisiteId, hidden);
    }

    public static void setConnectionTextureLocal(String questId, String prerequisiteId, String texture) {
        ClientQuestMutator.setConnectionTextureLocal(questId, prerequisiteId, texture);
    }

    public static void setConnectionTextureSpacingLocal(String questId, String prerequisiteId, int spacing) {
        ClientQuestMutator.setConnectionTextureSpacingLocal(questId, prerequisiteId, spacing);
    }

    public static void setQuestPositionInChapterLocal(String questId, String chapter, int x, int y) {
        ClientQuestMutator.setQuestPositionInChapterLocal(questId, chapter, x, y);
    }

    public static void setQuestScaleInChapterLocal(String questId, String chapter, float scale) {
        ClientQuestMutator.setQuestScaleInChapterLocal(questId, chapter, scale);
    }

    public static void createEditorQuestLocal(String questId, String chapter, int x, int y, String title) {
        ClientQuestMutator.createEditorQuestLocal(questId, chapter, x, y, title);
    }

    public static void copyQuestLocal(String sourceQuestId, String newQuestId, String chapter, int x, int y, float scale, Map<String, String> copiedIds) {
        ClientQuestMutator.copyQuestLocal(sourceQuestId, newQuestId, chapter, x, y, scale, copiedIds);
    }

    public static void copyQuestSnapshotLocal(CompoundTag sourceSnapshot, String sourceQuestId, String newQuestId, String chapter, int x, int y, float scale, Map<String, String> copiedIds) {
        ClientQuestMutator.copyQuestSnapshotLocal(sourceSnapshot, sourceQuestId, newQuestId, chapter, x, y, scale, copiedIds);
    }

    public static void remapCopiedQuestPrerequisitesLocal(Map<String, String> copiedIds, Map<String, CompoundTag> snapshots) {
        ClientQuestMutator.remapCopiedQuestPrerequisitesLocal(copiedIds, snapshots);
    }

    public static void removeQuestLocal(String questId) {
        ClientQuestMutator.removeQuestLocal(questId);
    }

    public static String summaryLine() {
        return completedCount() + " / " + totalCount() + " completed";
    }
}
