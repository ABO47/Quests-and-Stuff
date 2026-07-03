package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
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
        String chapter = EditorCommandPayloads.chapter(payload);
        Map<String, int[]> moves = new HashMap<>();
        ListTag moveTags = EditorCommandPayloads.moves(payload);
        if (EditorCommandPayloads.exceedsLimit(moveTags, EditorCommandPayloads.MAX_BULK_EDIT_ENTRIES)) {
            return;
        }
        for (int i = 0; i < moveTags.size(); i++) {
            CompoundTag moveTag = moveTags.getCompound(i);
            String questId = moveTag.getString(EditorCommandPayloads.QUEST);
            moves.put(questId, new int[]{
                    moveTag.getInt(EditorCommandPayloads.X),
                    moveTag.getInt(EditorCommandPayloads.Y)
            });
        }
        editor.moveQuestsInChapter(player, group, moves);
    }

    private static void scaleMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String chapter = EditorCommandPayloads.chapter(payload);
        Map<String, Float> scales = new HashMap<>();
        ListTag scaleTags = EditorCommandPayloads.scales(payload);
        if (EditorCommandPayloads.exceedsLimit(scaleTags, EditorCommandPayloads.MAX_BULK_EDIT_ENTRIES)) {
            return;
        }
        for (int i = 0; i < scaleTags.size(); i++) {
            CompoundTag scaleTag = scaleTags.getCompound(i);
            String questId = scaleTag.getString(EditorCommandPayloads.QUEST);
            scales.put(questId, scaleTag.getFloat(EditorCommandPayloads.SCALE));
        }
        editor.scaleQuestsInChapter(player, group, scales);
    }
}
