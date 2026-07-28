package com.abo47.questsandstuff.client.sync.mutation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.packet.ClientSyncInbox;
import com.abo47.questsandstuff.client.sync.state.ClientChapterState;
import com.abo47.questsandstuff.client.sync.state.ClientQuestState;
import com.abo47.questsandstuff.quest.sync.SyncKeys;

public final class ClientEditorMutationApplier {
    private ClientEditorMutationApplier() {
    }

    public static void apply(long sequence, String action, String questId, CompoundTag questTag) {
        if (!ClientSyncInbox.acceptEditorMutationSequence(sequence)) {
            return;
        }
        String normalizedAction = action == null ? "" : action;
        String normalizedId = questId == null ? "" : questId;
        if (normalizedId.isBlank()) {
            return;
        }
        if (SyncKeys.EditorAction.REMOVE.equals(normalizedAction)) {
            ClientQuestState.removeQuest(normalizedId);
            return;
        }
        ensureChaptersFromQuestTag(normalizedAction, normalizedId, questTag);
        CompoundTag existing = ClientQuestState.mutableQuestOrCreate(normalizedId);
        existing.merge(questTag == null ? new CompoundTag() : questTag.copy());
    }

    private static void ensureChaptersFromQuestTag(String action, String questId, CompoundTag questTag) {
        if (questTag == null || !questTag.contains(SyncKeys.Quest.CHAPTERS, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag groups = questTag.getCompound(SyncKeys.Quest.CHAPTERS);
        for (String rawGroup : groups.getAllKeys()) {
            String chapter = ClientChapterState.normalizeChapter(rawGroup);
            if (chapter.isBlank()) {
                continue;
            }
            boolean missing = !ClientChapterState.containsChapter(chapter);
            ClientQuestMutator.createChapterLocal(chapter);
            if (missing && ClientChapterState.containsChapter(chapter)) {
                QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] editor mutation added missing chapter action={} quest={} chapter={}", action, questId, chapter);
            }
        }
    }
}
