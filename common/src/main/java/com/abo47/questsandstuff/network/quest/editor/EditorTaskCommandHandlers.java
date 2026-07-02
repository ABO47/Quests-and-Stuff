package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadKeys;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadLimits;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadReader;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

final class EditorTaskCommandHandlers {
    private EditorTaskCommandHandlers() {
    }

    static void register(EditorCommandRegistrar registrar) {
        registrar.register(EditorCommandType.TASK_PUT, EditorCommandFamily.TASK, EditorTaskCommandHandlers::taskPut);
        registrar.register(EditorCommandType.TASK_REMOVE, EditorCommandFamily.TASK, EditorTaskCommandHandlers::taskRemove);
        registrar.register(EditorCommandType.TASK_MOVE, EditorCommandFamily.TASK, EditorTaskCommandHandlers::taskMove);
        registrar.register(EditorCommandType.REWARD_PUT, EditorCommandFamily.TASK, EditorTaskCommandHandlers::rewardPut);
        registrar.register(EditorCommandType.REWARD_REMOVE, EditorCommandFamily.TASK, EditorTaskCommandHandlers::rewardRemove);
        registrar.register(EditorCommandType.REWARD_MOVE, EditorCommandFamily.TASK, EditorTaskCommandHandlers::rewardMove);
    }

    private static void taskPut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String json = EditorCommandPayloads.json(payload);
        if (EditorCommandPayloads.exceedsLength(json, EditorCommandPayloads.MAX_EDITOR_JSON_LENGTH)) {
            return;
        }
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
        if (EditorCommandPayloads.exceedsLength(json, EditorCommandPayloads.MAX_EDITOR_JSON_LENGTH)) {
            return;
        }
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
