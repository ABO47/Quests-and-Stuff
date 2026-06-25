package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadKeys;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadLimits;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadReader;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

final class EditorCanvasCommandHandlers {
    private EditorCanvasCommandHandlers() {
    }

    static void register(EditorCommandRegistrar registrar) {
        registrar.register(EditorCommandType.MOVE_MANY, EditorCommandFamily.CANVAS, EditorCanvasCommandHandlers::moveMany);
        registrar.register(EditorCommandType.SCALE_MANY, EditorCommandFamily.CANVAS, EditorCanvasCommandHandlers::scaleMany);
    }

    private static void moveMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String group = EditorCommandPayloadReader.group(payload);
        Map<String, int[]> moves = new HashMap<>();
        ListTag moveTags = EditorCommandPayloadReader.moves(payload);
        if (EditorCommandPayloadLimits.exceedsLimit(moveTags, EditorCommandPayloadLimits.MAX_BULK_EDIT_ENTRIES)) {
            return;
        }
        for (int i = 0; i < moveTags.size(); i++) {
            CompoundTag moveTag = moveTags.getCompound(i);
            String questId = moveTag.getString(EditorCommandPayloadKeys.QUEST);
            moves.put(questId, new int[]{
                    moveTag.getInt(EditorCommandPayloadKeys.X),
                    moveTag.getInt(EditorCommandPayloadKeys.Y)
            });
        }
        editor.moveQuestsInGroup(player, group, moves);
    }

    private static void scaleMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String group = EditorCommandPayloadReader.group(payload);
        Map<String, Float> scales = new HashMap<>();
        ListTag scaleTags = EditorCommandPayloadReader.scales(payload);
        if (EditorCommandPayloadLimits.exceedsLimit(scaleTags, EditorCommandPayloadLimits.MAX_BULK_EDIT_ENTRIES)) {
            return;
        }
        for (int i = 0; i < scaleTags.size(); i++) {
            CompoundTag scaleTag = scaleTags.getCompound(i);
            String questId = scaleTag.getString(EditorCommandPayloadKeys.QUEST);
            scales.put(questId, scaleTag.getFloat(EditorCommandPayloadKeys.SCALE));
        }
        editor.scaleQuestsInGroup(player, group, scales);
    }
}
