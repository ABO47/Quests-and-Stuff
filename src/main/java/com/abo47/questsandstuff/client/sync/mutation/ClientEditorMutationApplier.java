package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestState;

import com.abo47.questsandstuff.client.sync.packet.ClientSyncInbox;

import com.abo47.questsandstuff.client.sync.cache.ClientChapterState;

import com.abo47.questsandstuff.QuestsAndStuffMod;
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
            ClientQuestState.QUESTS.remove(normalizedId);
            return;
        }
        ensureGroupsFromQuestTag(normalizedAction, normalizedId, questTag);
        CompoundTag existing = ClientQuestState.QUESTS.getOrDefault(normalizedId, new CompoundTag());
        existing.merge(questTag == null ? new CompoundTag() : questTag.copy());
        ClientQuestState.QUESTS.put(normalizedId, existing);
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
            boolean missing = !ClientChapterState.GROUP_ORDER.contains(group);
            ClientQuestLocalMutations.createGroupLocal(group);
            if (missing && ClientChapterState.GROUP_ORDER.contains(group)) {
                QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] editor mutation added missing chapter action={} quest={} group={}", action, questId, group);
            }
        }
    }
}
