package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientChapterState;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;
import com.abo47.questsandstuff.client.sync.packet.ClientSyncInbox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

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
        if ("remove".equals(normalizedAction)) {
            ClientQuestState.removeQuest(normalizedId);
            return;
        }
        ensureGroupsFromQuestTag(normalizedAction, normalizedId, questTag);
        CompoundTag existing = ClientQuestState.mutableQuestOrCreate(normalizedId);
        existing.merge(questTag == null ? new CompoundTag() : questTag.copy());
    }

    private static void ensureGroupsFromQuestTag(String action, String questId, CompoundTag questTag) {
        if (questTag == null || !questTag.contains("groups", Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag groups = questTag.getCompound("groups");
        for (String rawGroup : groups.getAllKeys()) {
            String group = ClientChapterState.normalizeGroup(rawGroup);
            if (group.isBlank()) {
                continue;
            }
            boolean missing = !ClientChapterState.containsGroup(group);
            ClientQuestLocalMutations.createGroupLocal(group);
            if (missing && ClientChapterState.containsGroup(group)) {
                QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] editor mutation added missing chapter action={} quest={} group={}", action, questId, group);
            }
        }
    }
}
