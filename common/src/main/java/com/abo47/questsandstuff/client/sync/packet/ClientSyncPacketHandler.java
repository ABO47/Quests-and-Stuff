package com.abo47.questsandstuff.client.sync.packet;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.hud.QuestCompletionNotificationOverlay;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.network.QuestNetwork;
import com.abo47.questsandstuff.network.runtime.C2SClaimAllRewardsPacket;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.List;

public final class ClientSyncPacketHandler {
    private ClientSyncPacketHandler() {
    }

    public static void handleFull(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        ClientQuestCache.acceptFullChunk(sequence, chunkIndex, chunkCount, payload);
        TabletUiFactory.refreshActiveTablet();
    }

    public static void handleDelta(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        ClientQuestCache.acceptDeltaChunk(sequence, chunkIndex, chunkCount, payload);
        TabletUiFactory.refreshActiveTablet();
    }

    public static void handleDescription(long sequence, int chunkIndex, int chunkCount, CompoundTag payload) {
        ClientQuestCache.acceptDescriptionChunk(sequence, chunkIndex, chunkCount, payload);
        TabletUiFactory.refreshActiveTablet();
    }

    public static void handleDisplayCache(long sequence, CompoundTag payload) {
        ClientQuestCache.applyDisplayCacheSync(sequence, payload);
    }

    public static void handlePinned(long sequence, List<String> pinned) {
        ClientQuestCache.applyPinnedSync(sequence, pinned);
    }

    public static void handleQuestEvent(long sequence, String eventType, String questId, String rewardId) {
        ClientQuestCache.applyQuestEvent(sequence, eventType, questId, rewardId);
        if ("quest_completed".equals(eventType)) {
            ClientQuestCache.noteQuestCompletedForChapterNotices(questId, TabletUiFactory.activeSelectedGroup());
            QuestCompletionNotificationOverlay.push(questId);
            if (QuestsAndStuffConfig.autoClaimRewardsEnabled() && questId != null && !questId.isBlank()) {
                QuestNetwork.sendToServer(new C2SClaimAllRewardsPacket(""));
                QuestsAndStuffMod.debugLog("[QnS:UI] global auto-claim triggered by quest={}", questId);
            }
            TabletUiFactory.refreshActiveTablet();
        }
    }

    public static void handleEditorMutation(long sequence, String action, String questId, CompoundTag questTag) {
        if ("paste_select".equals(action)) {
            QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] received paste_select group={} ids={}", questTag.getString("group"), questTag.getList("quests", Tag.TAG_STRING));
            TabletUiFactory.selectPastedQuests(questTag);
            return;
        }
        if ("add".equals(action)) {
            QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] received editor add quest={} prerequisites={}", questId, questTag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING));
        }
        ClientQuestCache.applyEditorMutation(sequence, action, questId, questTag);
        TabletUiFactory.refreshActiveTablet();
    }
}
