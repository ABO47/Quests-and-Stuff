package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadKeys;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadLimits;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadReader;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

final class EditorClipboardCommandHandlers {
    private EditorClipboardCommandHandlers() {
    }

    static void register(EditorCommandRegistrar registrar) {
        registrar.register(EditorCommandType.COPY_MANY, EditorCommandFamily.CLIPBOARD, EditorClipboardCommandHandlers::copyMany);
        registrar.register(EditorCommandType.PASTE_CLIPBOARD, EditorCommandFamily.CLIPBOARD, EditorClipboardCommandHandlers::pasteClipboard);
        registrar.register(EditorCommandType.PASTE_BLUEPRINT, EditorCommandFamily.CLIPBOARD, EditorClipboardCommandHandlers::pasteBlueprint);
    }

    private static void copyMany(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String group = EditorCommandPayloads.group(payload);
        ListTag questTags = EditorCommandPayloads.list(payload, EditorCommandPayloads.QUESTS, Tag.TAG_STRING);
        if (EditorCommandPayloads.exceedsLimit(questTags, EditorCommandPayloads.MAX_BULK_EDIT_ENTRIES)) {
            return;
        }
        editor.copyQuestsToClipboard(player, group, EditorCommandPayloads.questIds(payload));
    }

    private static void pasteClipboard(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String group = EditorCommandPayloads.group(payload);
        int x = EditorCommandPayloads.integer(payload, EditorCommandPayloads.X);
        int y = EditorCommandPayloads.integer(payload, EditorCommandPayloads.Y);
        editor.pasteClipboardInGroup(player, group, x, y);
    }

    private static void pasteBlueprint(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String group = EditorCommandPayloads.group(payload);
        int x = EditorCommandPayloads.integer(payload, EditorCommandPayloads.X);
        int y = EditorCommandPayloads.integer(payload, EditorCommandPayloads.Y);
        CanvasBlueprint blueprint = CanvasBlueprint.fromPacketTag(EditorCommandPayloads.compound(payload, EditorCommandPayloads.BLUEPRINT));
        if (blueprint.isEmpty()) {
            return;
        }
        editor.pasteBlueprintInGroup(player, group, x, y, blueprint);
    }
}
