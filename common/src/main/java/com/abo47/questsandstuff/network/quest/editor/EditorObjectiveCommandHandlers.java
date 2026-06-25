package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadKeys;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadLimits;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadReader;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

final class EditorObjectiveCommandHandlers {
    private EditorObjectiveCommandHandlers() {
    }

    static void register(EditorCommandRegistrar registrar) {
        registrar.register(EditorCommandType.TASK_PUT, EditorCommandFamily.OBJECTIVE, EditorObjectiveCommandHandlers::taskPut);
        registrar.register(EditorCommandType.TASK_REMOVE, EditorCommandFamily.OBJECTIVE, EditorObjectiveCommandHandlers::taskRemove);
        registrar.register(EditorCommandType.TASK_MOVE, EditorCommandFamily.OBJECTIVE, EditorObjectiveCommandHandlers::taskMove);
        registrar.register(EditorCommandType.REWARD_PUT, EditorCommandFamily.OBJECTIVE, EditorObjectiveCommandHandlers::rewardPut);
        registrar.register(EditorCommandType.REWARD_REMOVE, EditorCommandFamily.OBJECTIVE, EditorObjectiveCommandHandlers::rewardRemove);
        registrar.register(EditorCommandType.REWARD_MOVE, EditorCommandFamily.OBJECTIVE, EditorObjectiveCommandHandlers::rewardMove);
    }

    private static void taskPut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String json = EditorCommandPayloadReader.json(payload);
        if (EditorCommandPayloadLimits.exceedsLength(json, EditorCommandPayloadLimits.MAX_EDITOR_JSON_LENGTH)) {
            return;
        }
        editor.putQuestTask(player, EditorCommandPayloadReader.quest(payload), json);
    }

    private static void taskRemove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.removeQuestTask(player, EditorCommandPayloadReader.quest(payload), EditorCommandPayloadReader.task(payload));
    }

    private static void taskMove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.moveQuestTask(
                player,
                EditorCommandPayloadReader.quest(payload),
                EditorCommandPayloadReader.task(payload),
                EditorCommandPayloadReader.integer(payload, EditorCommandPayloadKeys.OFFSET)
        );
    }

    private static void rewardPut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String json = EditorCommandPayloadReader.json(payload);
        if (EditorCommandPayloadLimits.exceedsLength(json, EditorCommandPayloadLimits.MAX_EDITOR_JSON_LENGTH)) {
            return;
        }
        editor.putQuestReward(player, EditorCommandPayloadReader.quest(payload), json);
    }

    private static void rewardRemove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.removeQuestReward(player, EditorCommandPayloadReader.quest(payload), EditorCommandPayloadReader.reward(payload));
    }

    private static void rewardMove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.moveQuestReward(
                player,
                EditorCommandPayloadReader.quest(payload),
                EditorCommandPayloadReader.reward(payload),
                EditorCommandPayloadReader.integer(payload, EditorCommandPayloadKeys.OFFSET)
        );
    }
}
