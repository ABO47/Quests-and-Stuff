package com.abo47.questsandstuff.network.quest.editor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;

final class EditorQuestCommandHandlers {
    private EditorQuestCommandHandlers() {
    }

    static void register(EditorCommandRegistrar registrar) {
        registrar.register(EditorCommandType.QUEST_ICON, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::questIcon);
        registrar.register(EditorCommandType.QUEST_REPEATABLE, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::questRepeatable);
        registrar.register(EditorCommandType.QUEST_HIDDEN_MODE, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::questHiddenMode);
        registrar.register(EditorCommandType.QUEST_VISUAL_HIDDEN, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::questVisualHidden);
        registrar.register(EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::completionSound);
        registrar.register(EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_MANY, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::completionSoundMany);
        registrar.register(EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_VOLUME, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::completionSoundVolume);
        registrar.register(EditorCommandType.QUEST_CHANGE_COMPLETION_SOUND_VOLUME_MANY, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::completionSoundVolumeMany);
        registrar.register(EditorCommandType.QUEST_COMPLETION_HUD_BACKGROUND, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::completionHudBackground);
        registrar.register(EditorCommandType.QUEST_COMPLETION_HUD_BACKGROUND_MANY, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::completionHudBackgroundMany);
        registrar.register(EditorCommandType.QUEST_BACKGROUND, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::questBackground);
        registrar.register(EditorCommandType.QUEST_BACKGROUND_MANY, EditorCommandFamily.QUEST, EditorQuestCommandHandlers::questBackgroundMany);
    }

    private static void questIcon(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestIcon(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.ICON)
        );
    }

    private static void questRepeatable(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestRepeatable(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.bool(payload, EditorCommandPayloads.ENABLED)
        );
    }

    private static void questHiddenMode(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestHiddenMode(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.MODE)
        );
    }

    private static void questVisualHidden(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestVisualHidden(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.bool(payload, EditorCommandPayloads.HIDDEN)
        );
    }

    private static void completionSound(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestCompletionSound(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.SOUND)
        );
    }

    private static void completionSoundMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestCompletionSound(
                player,
                EditorCommandPayloads.questIds(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.SOUND)
        );
    }

    private static void completionSoundVolume(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestCompletionSoundVolume(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.integer(payload, EditorCommandPayloads.VOLUME)
        );
    }

    private static void completionSoundVolumeMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestCompletionSoundVolume(
                player,
                EditorCommandPayloads.questIds(payload),
                EditorCommandPayloads.integer(payload, EditorCommandPayloads.VOLUME)
        );
    }

    private static void completionHudBackground(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestCompletionHudBackground(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.BACKGROUND)
        );
    }

    private static void completionHudBackgroundMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestCompletionHudBackground(
                player,
                EditorCommandPayloads.questIds(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.BACKGROUND)
        );
    }

    private static void questBackground(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestBackground(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.BACKGROUND),
                EditorCommandPayloads.bool(payload, EditorCommandPayloads.GRAYSCALE)
        );
    }

    private static void questBackgroundMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.setQuestBackground(
                player,
                EditorCommandPayloads.questIds(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.BACKGROUND),
                EditorCommandPayloads.bool(payload, EditorCommandPayloads.GRAYSCALE)
        );
    }
}
