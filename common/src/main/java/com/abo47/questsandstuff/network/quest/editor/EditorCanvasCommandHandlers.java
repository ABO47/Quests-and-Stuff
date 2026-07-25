package com.abo47.questsandstuff.network.quest.editor;

import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;

final class EditorCanvasCommandHandlers {
    private EditorCanvasCommandHandlers() {
    }

    static void register(Consumer<EditorCommandDescriptor> registrar) {
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.MOVE_MANY, EditorCommandFamily.CANVAS, EditorCanvasCommandHandlers::moveMany));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.SCALE_MANY, EditorCommandFamily.CANVAS, EditorCanvasCommandHandlers::scaleMany));
    }

    private static void moveMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String chapter = EditorCommandPayloads.chapter(payload);
        Map<String, int[]> moves = EditorCommandPayloads.moveMap(payload);
        editor.moveQuestsInChapter(player, chapter, moves);
    }

    private static void scaleMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String chapter = EditorCommandPayloads.chapter(payload);
        Map<String, Float> scales = EditorCommandPayloads.scaleMap(payload);
        editor.scaleQuestsInChapter(player, chapter, scales);
    }
}
