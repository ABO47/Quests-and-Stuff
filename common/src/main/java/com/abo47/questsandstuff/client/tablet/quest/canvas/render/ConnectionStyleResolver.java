package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMode;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Map;
import java.util.Set;

final class ConnectionStyleResolver {
    private ConnectionStyleResolver() {
    }

    static int connectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return connectionColor(state, group, sourceQuestId, targetQuestId, ClientQuestCache.quest(targetQuestId));
    }

    static int connectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(QuestSyncKeys.Quest.CONNECTION_COLORS, Tag.TAG_COMPOUND)) {
            CompoundTag colorsTag = target.getCompound(QuestSyncKeys.Quest.CONNECTION_COLORS);
            if (colorsTag.contains(metadataKey, Tag.TAG_INT)) {
                return colorsTag.getInt(metadataKey);
            }
        }
        Map<String, Integer> colors = state.connectionColorsByGroup.get(group);
        if (colors == null) {
            return ModColors.TEXT_SECONDARY;
        }
        return colors.getOrDefault(QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId), ModColors.TEXT_SECONDARY);
    }

    static boolean isConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return isConnectionHidden(state, group, sourceQuestId, targetQuestId, ClientQuestCache.quest(targetQuestId));
    }

    static boolean isConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_LIST)) {
            ListTag hiddenTag = target.getList(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING);
            for (int i = 0; i < hiddenTag.size(); i++) {
                if (metadataKey.equals(hiddenTag.getString(i))) {
                    return true;
                }
            }
        }
        Set<String> hidden = state.hiddenConnectionsByGroup.get(group);
        return hidden != null && hidden.contains(QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId));
    }

    static boolean isConnectionDirect(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return isConnectionDirect(state, group, sourceQuestId, targetQuestId, ClientQuestCache.quest(targetQuestId));
    }

    static boolean isConnectionDirect(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(QuestSyncKeys.Quest.CONNECTION_MODES, Tag.TAG_COMPOUND)) {
            CompoundTag modes = target.getCompound(QuestSyncKeys.Quest.CONNECTION_MODES);
            if (modes.contains(metadataKey, Tag.TAG_STRING)) {
                return QuestConnectionMode.fromSerializedName(modes.getString(metadataKey)) != QuestConnectionMode.GRID;
            }
        }
        Set<String> grid = state.gridConnectionsByGroup.get(group);
        return grid == null || !grid.contains(QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId));
    }

    static QuestConnectionMetadata metadata(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return metadata(state, group, sourceQuestId, targetQuestId, ClientQuestCache.quest(targetQuestId));
    }

    static QuestConnectionMetadata metadata(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        boolean direct = isConnectionDirect(state, group, sourceQuestId, targetQuestId, target);
        return new QuestConnectionMetadata(
                sourceQuestId,
                targetQuestId,
                connectionColor(state, group, sourceQuestId, targetQuestId, target),
                direct ? QuestConnectionMode.DIRECT : QuestConnectionMode.GRID,
                isConnectionHidden(state, group, sourceQuestId, targetQuestId, target)
        );
    }

    static ConnectionRenderStyle style(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        return ConnectionRenderStyle.fromMetadata(metadata(state, group, sourceQuestId, targetQuestId, target));
    }
}
