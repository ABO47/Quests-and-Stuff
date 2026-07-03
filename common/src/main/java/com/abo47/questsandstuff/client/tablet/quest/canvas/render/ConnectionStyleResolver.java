package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMode;
import com.abo47.questsandstuff.quest.sync.SyncKeys;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.quest.sync.SyncKeys.Quest.CONNECTION_TEXTURES;
import static com.abo47.questsandstuff.quest.sync.SyncKeys.Quest.CONNECTION_TEXTURE_SPACINGS;

final class ConnectionStyleResolver {
    private ConnectionStyleResolver() {
    }

    static int connectionColor(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, CompoundTag target) {
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(SyncKeys.Quest.CONNECTION_COLORS, Tag.TAG_COMPOUND)) {
            CompoundTag colorsTag = target.getCompound(SyncKeys.Quest.CONNECTION_COLORS);
            if (colorsTag.contains(metadataKey, Tag.TAG_INT)) {
                return colorsTag.getInt(metadataKey);
            }
        }
        Map<String, Integer> colors = state.canvas.connectionColorsByGroup.get(chapter);
        if (colors == null) {
            return TabletColors.TEXT_SECONDARY;
        }
        return colors.getOrDefault(QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId), TabletColors.TEXT_SECONDARY);
    }

    static boolean isConnectionHidden(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, CompoundTag target) {
        if (isEcId(state, chapter, sourceQuestId) || isEcId(state, chapter, targetQuestId)) {
            return ecIsConnectionHidden(state, chapter, sourceQuestId, targetQuestId);
        }
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_LIST)) {
            ListTag hiddenTag = target.getList(SyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING);
            for (int i = 0; i < hiddenTag.size(); i++) {
                if (metadataKey.equals(hiddenTag.getString(i))) {
                    return true;
                }
            }
        }
        Set<String> hidden = state.canvas.hiddenConnectionsByGroup.get(chapter);
        return hidden != null && hidden.contains(QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId));
    }

    static boolean isConnectionDirect(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, CompoundTag target) {
        if (isEcId(state, chapter, sourceQuestId) || isEcId(state, chapter, targetQuestId)) {
            return ecIsConnectionDirect(state, chapter, sourceQuestId, targetQuestId);
        }
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(SyncKeys.Quest.CONNECTION_MODES, Tag.TAG_COMPOUND)) {
            CompoundTag modes = target.getCompound(SyncKeys.Quest.CONNECTION_MODES);
            if (modes.contains(metadataKey, Tag.TAG_STRING)) {
                return QuestConnectionMode.fromSerializedName(modes.getString(metadataKey)) != QuestConnectionMode.GRID;
            }
        }
        Set<String> grid = state.canvas.gridConnectionsByGroup.get(chapter);
        return grid == null || !grid.contains(QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId));
    }

    static String connectionTexture(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, CompoundTag target) {
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(CONNECTION_TEXTURES, Tag.TAG_COMPOUND)) {
            CompoundTag textures = target.getCompound(CONNECTION_TEXTURES);
            if (textures.contains(metadataKey, Tag.TAG_STRING)) {
                return textures.getString(metadataKey);
            }
        }
        Map<String, String> textures = state.canvas.connectionTexturesByGroup.get(chapter);
        if (textures == null) {
            return "";
        }
        return textures.getOrDefault(QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId), "");
    }

    static int connectionTextureSpacing(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, CompoundTag target) {
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(CONNECTION_TEXTURE_SPACINGS, Tag.TAG_COMPOUND)) {
            CompoundTag spacings = target.getCompound(CONNECTION_TEXTURE_SPACINGS);
            if (spacings.contains(metadataKey, Tag.TAG_INT)) {
                return Math.max(0, spacings.getInt(metadataKey));
            }
        }
        Map<String, Integer> spacings = state.canvas.connectionTextureSpacingsByGroup.get(chapter);
        if (spacings == null) {
            return 5;
        }
        return spacings.getOrDefault(QuestConnectionMetadata.connectionKey(sourceQuestId, targetQuestId), 5);
    }

    static QuestConnectionMetadata metadata(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, CompoundTag target) {
        boolean direct = isConnectionDirect(state, chapter, sourceQuestId, targetQuestId, target);
        return new QuestConnectionMetadata(
                sourceQuestId,
                targetQuestId,
                connectionColor(state, chapter, sourceQuestId, targetQuestId, target),
                direct ? QuestConnectionMode.DIRECT : QuestConnectionMode.GRID,
                isConnectionHidden(state, chapter, sourceQuestId, targetQuestId, target),
                connectionTexture(state, chapter, sourceQuestId, targetQuestId, target),
                connectionTextureSpacing(state, chapter, sourceQuestId, targetQuestId, target)
        );
    }

    static ConnectionRenderStyle style(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId, CompoundTag target) {
        return ConnectionRenderStyle.fromMetadata(metadata(state, chapter, sourceQuestId, targetQuestId, target));
    }

    static CanvasExclusiveChoice findEc(TabletUiState state, String chapter, String id) {
        return CanvasLayerMutations.findCanvasExclusiveChoice(state, chapter, id);
    }

    static boolean isEcId(TabletUiState state, String chapter, String id) {
        return findEc(state, chapter, id) != null;
    }

    static int ecConnectionColor(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        CanvasExclusiveChoice ec = findEc(state, chapter, sourceQuestId);
        if (ec != null) {
            return ec.connectionColors().getOrDefault(targetQuestId, TabletColors.TEXT_SECONDARY);
        }
        ec = findEc(state, chapter, targetQuestId);
        if (ec != null) {
            return ec.connectionColors().getOrDefault(sourceQuestId, TabletColors.TEXT_SECONDARY);
        }
        return TabletColors.TEXT_SECONDARY;
    }

    static boolean ecIsConnectionDirect(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        CanvasExclusiveChoice ec = findEc(state, chapter, sourceQuestId);
        if (ec != null) {
            String mode = ec.connectionModes().get(targetQuestId);
            return mode == null || QuestConnectionMode.fromSerializedName(mode) == QuestConnectionMode.DIRECT;
        }
        ec = findEc(state, chapter, targetQuestId);
        if (ec != null) {
            String mode = ec.connectionModes().get(sourceQuestId);
            return mode == null || QuestConnectionMode.fromSerializedName(mode) == QuestConnectionMode.DIRECT;
        }
        return true;
    }

    static boolean ecIsConnectionHidden(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        CanvasExclusiveChoice ec = findEc(state, chapter, sourceQuestId);
        if (ec != null) {
            return ec.hiddenConnections().contains(targetQuestId);
        }
        ec = findEc(state, chapter, targetQuestId);
        if (ec != null) {
            return ec.hiddenConnections().contains(sourceQuestId);
        }
        return false;
    }

    static String ecConnectionTexture(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        CanvasExclusiveChoice ec = findEc(state, chapter, sourceQuestId);
        if (ec != null) {
            return ec.connectionTextures().getOrDefault(targetQuestId, "");
        }
        ec = findEc(state, chapter, targetQuestId);
        if (ec != null) {
            return ec.connectionTextures().getOrDefault(sourceQuestId, "");
        }
        return "";
    }

    static int ecConnectionTextureSpacing(TabletUiState state, String chapter, String sourceQuestId, String targetQuestId) {
        CanvasExclusiveChoice ec = findEc(state, chapter, sourceQuestId);
        if (ec != null) {
            return ec.connectionTextureSpacings().getOrDefault(targetQuestId, 5);
        }
        ec = findEc(state, chapter, targetQuestId);
        if (ec != null) {
            return ec.connectionTextureSpacings().getOrDefault(sourceQuestId, 5);
        }
        return 5;
    }
}
