package com.abo47.questsandstuff.network.quest.editor;

import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;

final class EditorTaskCommandHandlers {
    private EditorTaskCommandHandlers() {
    }

    static void register(Consumer<EditorCommandDescriptor> registrar) {
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.TASK_PUT, EditorCommandFamily.TASK, EditorTaskCommandHandlers::taskPut));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.TASK_REMOVE, EditorCommandFamily.TASK, EditorTaskCommandHandlers::taskRemove));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.TASK_MOVE, EditorCommandFamily.TASK, EditorTaskCommandHandlers::taskMove));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.REWARD_PUT, EditorCommandFamily.TASK, EditorTaskCommandHandlers::rewardPut));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.REWARD_REMOVE, EditorCommandFamily.TASK, EditorTaskCommandHandlers::rewardRemove));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.REWARD_MOVE, EditorCommandFamily.TASK, EditorTaskCommandHandlers::rewardMove));
    }

    private static void taskPut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String json = EditorCommandPayloads.json(payload);
        editor.putQuestTask(player, EditorCommandPayloads.quest(payload), json);
    }

    private static void taskRemove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.removeQuestTask(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.task(payload));
    }

    private static void taskMove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.moveQuestTask(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.task(payload),
                EditorCommandPayloads.integer(payload, EditorCommandPayloads.OFFSET)
        );
    }

    private static void rewardPut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String json = EditorCommandPayloads.json(payload);
        editor.putQuestReward(player, EditorCommandPayloads.quest(payload), json);
    }

    private static void rewardRemove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.removeQuestReward(player, EditorCommandPayloads.quest(payload), EditorCommandPayloads.reward(payload));
    }

    private static void rewardMove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.moveQuestReward(
                player,
                EditorCommandPayloads.quest(payload),
                EditorCommandPayloads.reward(payload),
                EditorCommandPayloads.integer(payload, EditorCommandPayloads.OFFSET)
        );
    }
}
