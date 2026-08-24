package com.abo47.questsandstuff.client.sync.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.lock.ClientItemLocks;
import com.abo47.questsandstuff.quest.sync.SyncKeys;
import com.abo47.questsandstuff.util.naming.QuestIdentity;

public final class ClientQuestState {
    private static final Map<String, CompoundTag> QUESTS = new HashMap<>();
    private static final Set<String> PINNED = new TreeSet<>();
    private static String dragReorderQuestId;
    private static String dragReorderEntryId;
    private static int dragReorderOffset;
    private static boolean dragReorderIsTask;

    private ClientQuestState() {
    }

    public static void reset() {
        QUESTS.clear();
        PINNED.clear();
        ClientItemLocks.invalidate();
    }

    public static void clearQuests() {
        QUESTS.clear();
        ClientItemLocks.invalidate();
    }

    public static void putQuest(String questId, CompoundTag quest) {
        String normalized = normalizeQuestId(questId);
        if (normalized.isBlank() || quest == null) {
            return;
        }
        QUESTS.put(normalized, quest.copy());
        ClientItemLocks.invalidate();
        if (normalized.equals(dragReorderQuestId) && dragReorderEntryId != null && !dragReorderEntryId.isBlank()) {
            if (dragReorderIsTask) {
                moveTask(dragReorderQuestId, dragReorderEntryId, dragReorderOffset);
            } else {
                moveReward(dragReorderQuestId, dragReorderEntryId, dragReorderOffset);
            }
            dragReorderQuestId = null;
            dragReorderEntryId = null;
            dragReorderOffset = 0;
        }
    }

    public static void setDragReorderPending(String questId, String entryId, int offset, boolean isTask) {
        dragReorderQuestId = normalizeQuestId(questId);
        dragReorderEntryId = entryId;
        dragReorderOffset = offset;
        dragReorderIsTask = isTask;
    }

    public static void clearDragReorderPending() {
        dragReorderQuestId = null;
        dragReorderEntryId = null;
        dragReorderOffset = 0;
    }

    public static CompoundTag questCopy(String questId) {
        CompoundTag quest = QUESTS.get(normalizeQuestId(questId));
        return quest == null ? new CompoundTag() : quest.copy();
    }

    public static CompoundTag questSectionCopy(String questId, String section) {
        CompoundTag quest = QUESTS.get(normalizeQuestId(questId));
        return quest == null || section == null ? new CompoundTag() : quest.getCompound(section).copy();
    }

    public static CompoundTag mutableQuest(String questId) {
        return QUESTS.get(normalizeQuestId(questId));
    }

    public static CompoundTag mutableQuestOrCreate(String questId) {
        String normalized = normalizeQuestId(questId);
        if (normalized.isBlank()) {
            return new CompoundTag();
        }
        return QUESTS.computeIfAbsent(normalized, ignored -> new CompoundTag());
    }

    public static boolean containsQuest(String questId) {
        return QUESTS.containsKey(normalizeQuestId(questId));
    }

    public static List<Map.Entry<String, CompoundTag>> questEntries() {
        List<Map.Entry<String, CompoundTag>> entries = new ArrayList<>(QUESTS.size());
        for (Map.Entry<String, CompoundTag> entry : QUESTS.entrySet()) {
            entries.add(Map.entry(entry.getKey(), entry.getValue()));
        }
        return List.copyOf(entries);
    }

    public static Set<String> questIdsSnapshot() {
        return Collections.unmodifiableSet(new TreeSet<>(QUESTS.keySet()));
    }

    public static boolean removeQuest(String questId) {
        return QUESTS.remove(normalizeQuestId(questId)) != null;
    }

    public static void forEachQuest(Consumer<CompoundTag> consumer) {
        if (consumer == null) {
            return;
        }
        QUESTS.values().forEach(consumer);
    }

    public static void forEachQuestEntry(BiConsumer<String, CompoundTag> consumer) {
        if (consumer == null) {
            return;
        }
        QUESTS.forEach(consumer);
    }

    public static Map<String, CompoundTag> questSnapshot() {
        Map<String, CompoundTag> copy = new HashMap<>();
        for (Map.Entry<String, CompoundTag> entry : QUESTS.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return Collections.unmodifiableMap(copy);
    }

    public static Set<String> pinnedSnapshot() {
        return Collections.unmodifiableSet(new TreeSet<>(PINNED));
    }

    public static void setPinned(Iterable<String> questIds) {
        PINNED.clear();
        if (questIds == null) {
            return;
        }
        for (String questId : questIds) {
            String normalized = normalizeQuestId(questId);
            if (!normalized.isBlank()) {
                PINNED.add(normalized);
            }
        }
    }

    public static void removePinned(String questId) {
        PINNED.remove(normalizeQuestId(questId));
    }

    public static void togglePinned(String questId) {
        String normalized = normalizeQuestId(questId);
        if (normalized.isBlank()) {
            return;
        }
        if (!PINNED.add(normalized)) {
            PINNED.remove(normalized);
        }
    }

    public static boolean moveTask(String questId, String taskId, int offset) {
        CompoundTag quest = QUESTS.get(normalizeQuestId(questId));
        if (quest == null || taskId == null || taskId.isBlank() || offset == 0) {
            QuestsAndStuffMod.debugLog("[QnS:UI] moveTask skip quest={} task={} offset={} exist={}", questId, taskId, offset, quest != null);
            return false;
        }
        ListTag order = quest.getList(SyncKeys.Quest.TASKS_ORDER, Tag.TAG_STRING);
        if (order == null || order.isEmpty()) {
            QuestsAndStuffMod.debugLog("[QnS:UI] moveTask no order quest={} task={}", questId, taskId);
            return false;
        }
        int fromIndex = -1;
        for (int i = 0; i < order.size(); i++) {
            if (order.getString(i).equals(taskId)) {
                fromIndex = i;
                break;
            }
        }
        if (fromIndex < 0) {
            QuestsAndStuffMod.debugLog("[QnS:UI] moveTask id not found quest={} task={} order={}", questId, taskId, order);
            return false;
        }
        int target = Math.max(0, Math.min(order.size() - 1, fromIndex + offset));
        if (target == fromIndex) {
            return false;
        }
        String id = order.getString(fromIndex);
        QuestsAndStuffMod.debugLog("[QnS:UI] moveTask before quest={} task={} order={}", questId, taskId, order);
        if (target > fromIndex) {
            for (int i = fromIndex; i < target; i++) {
                order.setTag(i, StringTag.valueOf(order.getString(i + 1)));
            }
        } else {
            for (int i = fromIndex; i > target; i--) {
                order.setTag(i, StringTag.valueOf(order.getString(i - 1)));
            }
        }
        order.setTag(target, StringTag.valueOf(id));
        quest.put(SyncKeys.Quest.TASKS_ORDER, order);
        QuestsAndStuffMod.debugLog("[QnS:UI] moveTask after quest={} task={} order={}", questId, taskId, order);
        return true;
    }

    public static boolean moveReward(String questId, String rewardId, int offset) {
        CompoundTag quest = QUESTS.get(normalizeQuestId(questId));
        if (quest == null || rewardId == null || rewardId.isBlank() || offset == 0) {
            return false;
        }
        ListTag order = quest.getList(SyncKeys.Quest.REWARDS_ORDER, Tag.TAG_STRING);
        if (order == null || order.isEmpty()) {
            return false;
        }
        int fromIndex = -1;
        for (int i = 0; i < order.size(); i++) {
            if (order.getString(i).equals(rewardId)) {
                fromIndex = i;
                break;
            }
        }
        if (fromIndex < 0) {
            return false;
        }
        int target = Math.max(0, Math.min(order.size() - 1, fromIndex + offset));
        if (target == fromIndex) {
            return false;
        }
        String id = order.getString(fromIndex);
        if (target > fromIndex) {
            for (int i = fromIndex; i < target; i++) {
                order.setTag(i, StringTag.valueOf(order.getString(i + 1)));
            }
        } else {
            for (int i = fromIndex; i > target; i--) {
                order.setTag(i, StringTag.valueOf(order.getString(i - 1)));
            }
        }
        order.setTag(target, StringTag.valueOf(id));
        quest.put(SyncKeys.Quest.REWARDS_ORDER, order);
        return true;
    }

    public static int completedCount() {
        int count = 0;
        for (CompoundTag quest : QUESTS.values()) {
            if (quest.getBoolean(SyncKeys.Quest.COMPLETED)) {
                count++;
            }
        }
        return count;
    }

    public static int totalCount() {
        return QUESTS.size();
    }

    private static String normalizeQuestId(String questId) {
        return QuestIdentity.questId(questId);
    }
}
