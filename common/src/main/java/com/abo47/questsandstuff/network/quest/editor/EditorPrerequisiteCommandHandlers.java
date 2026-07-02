package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadKeys;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadReader;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

final class EditorPrerequisiteCommandHandlers {
    private EditorPrerequisiteCommandHandlers() {
    }

    static void register(EditorCommandRegistrar registrar) {
        registrar.register(EditorCommandType.PREREQUISITE_ADD, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::addPrerequisite);
        registrar.register(EditorCommandType.PREREQUISITE_REMOVE, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::removePrerequisite);
        registrar.register(EditorCommandType.CONNECTION_COLOR, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionColor);
        registrar.register(EditorCommandType.CONNECTION_MODE, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionMode);
        registrar.register(EditorCommandType.CONNECTION_HIDDEN, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionHidden);
        registrar.register(EditorCommandType.CONNECTION_TEXTURE, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionTexture);
        registrar.register(EditorCommandType.CONNECTION_TEXTURE_MANY, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionTextures);
        registrar.register(EditorCommandType.CONNECTION_TEXTURE_SPACING, EditorCommandFamily.PREREQUISITE, EditorPrerequisiteCommandHandlers::connectionTextureSpacing);
    }

    private static void addPrerequisite(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestPrerequisite(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.prerequisite(payload), true);
    }

    private static void removePrerequisite(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestPrerequisite(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.prerequisite(payload), false);
    }

    private static void connectionColor(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setConnectionColor(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.prerequisite(payload),
                EditorCommandPayloads.integer(payload, EditorCommandPayloads.COLOR)
        );
    }

    private static void connectionMode(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setConnectionMode(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.prerequisite(payload),
                EditorCommandPayloads.bool(payload, EditorCommandPayloads.GRID)
        );
    }

    private static void connectionHidden(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setConnectionHidden(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.prerequisite(payload),
                EditorCommandPayloads.bool(payload, EditorCommandPayloads.HIDDEN)
        );
    }

    private static void connectionTexture(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setConnectionTexture(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.prerequisite(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.TEXTURE)
        );
    }

    private static void connectionTextures(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        Map<String, Map<String, String>> questTextures = new HashMap<>();
        ListTag list = payload.getList(EditorCommandPayloads.TEXTURES, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            String quest = EditorCommandPayloads.string(entry, EditorCommandPayloads.QUEST);
            String prerequisite = EditorCommandPayloads.string(entry, EditorCommandPayloads.PREREQUISITE);
            String texture = EditorCommandPayloads.string(entry, EditorCommandPayloads.TEXTURE);
            if (quest.isBlank() || prerequisite.isBlank()) continue;
            questTextures.computeIfAbsent(quest, k -> new HashMap<>()).put(prerequisite, texture == null ? "" : texture);
        }
        QuestsAndStuffMod.debugLog("[QnS:Editor] CONNECTION_TEXTURE_MANY handler quests={} entries={}", questTextures.size(), list.size());
        editor.setConnectionTextures(player, questTextures);
    }

    private static void connectionTextureSpacing(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setConnectionTextureSpacing(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.prerequisite(payload),
                EditorCommandPayloads.integer(payload, EditorCommandPayloads.SPACING)
        );
    }
}
