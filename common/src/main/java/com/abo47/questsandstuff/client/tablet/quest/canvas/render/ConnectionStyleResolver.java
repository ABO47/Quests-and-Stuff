package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMode;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.quest.sync.QuestSyncKeys.Quest.CONNECTION_TEXTURES;
import static com.abo47.questsandstuff.quest.sync.QuestSyncKeys.Quest.CONNECTION_TEXTURE_SPACINGS;

final class ConnectionStyleResolver {
    private ConnectionStyleResolver() {
    }

    static int connectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(QuestSyncKeys.Quest.CONNECTION_COLORS, Tag.TAG_COMPOUND)) {
            CompoundTag colorsTag = target.getCompound(QuestSyncKeys.Quest.CONNECTION_COLORS);
            if (colorsTag.contains(metadataKey, Tag.TAG_INT)) {
                return colorsTag.getInt(metadataKey);
            }
        }
        Map<String, Integer> colors = state.canvas.connectionColorsByGroup.get(group);
        if (colors == null) {
            return ModColors.TEXT_SECONDARY;
        }
        return colors.getOrDefault(QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId), ModColors.TEXT_SECONDARY);
    }

    static boolean isConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        if (isEcId(state, group, sourceQuestId) || isEcId(state, group, targetQuestId)) {
            return ecIsConnectionHidden(state, group, sourceQuestId, targetQuestId);
        }
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_LIST)) {
            ListTag hiddenTag = target.getList(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING);
            for (int i = 0; i < hiddenTag.size(); i++) {
                if (metadataKey.equals(hiddenTag.getString(i))) {
                    return true;
                }
            }
        }
        Set<String> hidden = state.canvas.hiddenConnectionsByGroup.get(group);
        return hidden != null && hidden.contains(QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId));
    }

    static boolean isConnectionDirect(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        if (isEcId(state, group, sourceQuestId) || isEcId(state, group, targetQuestId)) {
            return ecIsConnectionDirect(state, group, sourceQuestId, targetQuestId);
        }
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(QuestSyncKeys.Quest.CONNECTION_MODES, Tag.TAG_COMPOUND)) {
            CompoundTag modes = target.getCompound(QuestSyncKeys.Quest.CONNECTION_MODES);
            if (modes.contains(metadataKey, Tag.TAG_STRING)) {
                return QuestConnectionMode.fromSerializedName(modes.getString(metadataKey)) != QuestConnectionMode.GRID;
            }
        }
        Set<String> grid = state.canvas.gridConnectionsByGroup.get(group);
        return grid == null || !grid.contains(QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId));
    }

    static String connectionTexture(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(CONNECTION_TEXTURES, Tag.TAG_COMPOUND)) {
            CompoundTag textures = target.getCompound(CONNECTION_TEXTURES);
            if (textures.contains(metadataKey, Tag.TAG_STRING)) {
                return textures.getString(metadataKey);
            }
        }
        Map<String, String> textures = state.canvas.connectionTexturesByGroup.get(group);
        if (textures == null) {
            return "";
        }
        return textures.getOrDefault(QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId), "");
    }

    static int connectionTextureSpacing(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(CONNECTION_TEXTURE_SPACINGS, Tag.TAG_COMPOUND)) {
            CompoundTag spacings = target.getCompound(CONNECTION_TEXTURE_SPACINGS);
            if (spacings.contains(metadataKey, Tag.TAG_INT)) {
                return Math.max(0, spacings.getInt(metadataKey));
            }
        }
        Map<String, Integer> spacings = state.canvas.connectionTextureSpacingsByGroup.get(group);
        if (spacings == null) {
            return 5;
        }
        return spacings.getOrDefault(QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId), 5);
    }

    static QuestConnectionMetadata metadata(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        boolean direct = isConnectionDirect(state, group, sourceQuestId, targetQuestId, target);
        return new QuestConnectionMetadata(
                sourceQuestId,
                targetQuestId,
                connectionColor(state, group, sourceQuestId, targetQuestId, target),
                direct ? QuestConnectionMode.DIRECT : QuestConnectionMode.GRID,
                isConnectionHidden(state, group, sourceQuestId, targetQuestId, target),
                connectionTexture(state, group, sourceQuestId, targetQuestId, target),
                connectionTextureSpacing(state, group, sourceQuestId, targetQuestId, target)
        );
    }

    static ConnectionRenderStyle style(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        return ConnectionRenderStyle.fromMetadata(metadata(state, group, sourceQuestId, targetQuestId, target));
    }

    static CanvasExclusiveChoice findEc(TabletUiState state, String group, String id) {
        return CanvasLayerMutations.findCanvasExclusiveChoice(state, group, id);
    }

    static boolean isEcId(TabletUiState state, String group, String id) {
        return findEc(state, group, id) != null;
    }

    static int ecConnectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        CanvasExclusiveChoice ec = findEc(state, group, sourceQuestId);
        if (ec != null) {
            return ec.connectionColors().getOrDefault(targetQuestId, ModColors.TEXT_SECONDARY);
        }
        ec = findEc(state, group, targetQuestId);
        if (ec != null) {
            return ec.connectionColors().getOrDefault(sourceQuestId, ModColors.TEXT_SECONDARY);
        }
        return ModColors.TEXT_SECONDARY;
    }

    static boolean ecIsConnectionDirect(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        CanvasExclusiveChoice ec = findEc(state, group, sourceQuestId);
        if (ec != null) {
            String mode = ec.connectionModes().get(targetQuestId);
            return mode == null || QuestConnectionMode.fromSerializedName(mode) == QuestConnectionMode.DIRECT;
        }
        ec = findEc(state, group, targetQuestId);
        if (ec != null) {
            String mode = ec.connectionModes().get(sourceQuestId);
            return mode == null || QuestConnectionMode.fromSerializedName(mode) == QuestConnectionMode.DIRECT;
        }
        return true;
    }

    static boolean ecIsConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        CanvasExclusiveChoice ec = findEc(state, group, sourceQuestId);
        if (ec != null) {
            return ec.hiddenConnections().contains(targetQuestId);
        }
        ec = findEc(state, group, targetQuestId);
        if (ec != null) {
            return ec.hiddenConnections().contains(sourceQuestId);
        }
        return false;
    }

    static String ecConnectionTexture(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        CanvasExclusiveChoice ec = findEc(state, group, sourceQuestId);
        if (ec != null) {
            return ec.connectionTextures().getOrDefault(targetQuestId, "");
        }
        ec = findEc(state, group, targetQuestId);
        if (ec != null) {
            return ec.connectionTextures().getOrDefault(sourceQuestId, "");
        }
        return "";
    }

    static int ecConnectionTextureSpacing(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        CanvasExclusiveChoice ec = findEc(state, group, sourceQuestId);
        if (ec != null) {
            return ec.connectionTextureSpacings().getOrDefault(targetQuestId, 5);
        }
        ec = findEc(state, group, targetQuestId);
        if (ec != null) {
            return ec.connectionTextureSpacings().getOrDefault(sourceQuestId, 5);
        }
        return 5;
    }
}
