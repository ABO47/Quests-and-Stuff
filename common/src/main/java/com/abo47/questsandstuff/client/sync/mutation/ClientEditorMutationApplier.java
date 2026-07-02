package com.abo47.questsandstuff.client.sync.mutation;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientChapterState;
import com.abo47.questsandstuff.client.sync.state.ClientQuestState;
import com.abo47.questsandstuff.client.sync.packet.ClientSyncInbox;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
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
        if (QuestSyncKeys.EditorAction.REMOVE.equals(normalizedAction)) {
            ClientQuestState.removeQuest(normalizedId);
            return;
        }
        ensureGroupsFromQuestTag(normalizedAction, normalizedId, questTag);
        CompoundTag existing = ClientQuestState.mutableQuestOrCreate(normalizedId);
        existing.merge(questTag == null ? new CompoundTag() : questTag.copy());
    }

    private static void ensureGroupsFromQuestTag(String action, String questId, CompoundTag questTag) {
        if (questTag == null || !questTag.contains(QuestSyncKeys.Quest.GROUPS, Tag.TAG_COMPOUND)) {
            return;
        }
        CompoundTag groups = questTag.getCompound(QuestSyncKeys.Quest.GROUPS);
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
