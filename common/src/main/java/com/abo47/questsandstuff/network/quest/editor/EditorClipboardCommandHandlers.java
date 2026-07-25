package com.abo47.questsandstuff.network.quest.editor;

import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;

final class EditorClipboardCommandHandlers {
    private EditorClipboardCommandHandlers() {
    }

    static void register(Consumer<EditorCommandDescriptor> registrar) {
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.COPY_MANY, EditorCommandFamily.CLIPBOARD, EditorClipboardCommandHandlers::copyMany));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.PASTE_CLIPBOARD, EditorCommandFamily.CLIPBOARD, EditorClipboardCommandHandlers::pasteClipboard));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.PASTE_BLUEPRINT, EditorCommandFamily.CLIPBOARD, EditorClipboardCommandHandlers::pasteBlueprint));
    }

    private static void copyMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String chapter = EditorCommandPayloads.chapter(payload);
        editor.copyQuestsToClipboard(player, chapter, EditorCommandPayloads.questIds(payload));
    }

    private static void pasteClipboard(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String chapter = EditorCommandPayloads.chapter(payload);
        int x = EditorCommandPayloads.integer(payload, EditorCommandPayloads.X);
        int y = EditorCommandPayloads.integer(payload, EditorCommandPayloads.Y);
        editor.pasteClipboardInChapter(player, chapter, x, y);
    }

    private static void pasteBlueprint(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String chapter = EditorCommandPayloads.chapter(payload);
        int x = EditorCommandPayloads.integer(payload, EditorCommandPayloads.X);
        int y = EditorCommandPayloads.integer(payload, EditorCommandPayloads.Y);
        CanvasBlueprint blueprint = CanvasBlueprint.fromPacketTag(EditorCommandPayloads.compound(payload, EditorCommandPayloads.BLUEPRINT));
        if (blueprint.isEmpty()) {
            return;
        }
        editor.pasteBlueprintInChapter(player, chapter, x, y, blueprint);
    }
}
