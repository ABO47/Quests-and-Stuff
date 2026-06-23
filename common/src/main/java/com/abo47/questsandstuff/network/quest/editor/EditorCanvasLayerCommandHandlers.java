package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadKeys;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadLimits;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

final class EditorCanvasLayerCommandHandlers {
    private EditorCanvasLayerCommandHandlers() {
    }

    static void register(EditorCommandRegistrar registrar) {
        registrar.register(EditorCommandType.CANVAS_EXCLUSIVE_CHOICE_PUT, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasExclusiveChoicePut);
        registrar.register(EditorCommandType.CANVAS_EXCLUSIVE_CHOICE_PUT_MANY, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasExclusiveChoicesPut);
        registrar.register(EditorCommandType.CANVAS_EXCLUSIVE_CHOICE_REMOVE, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasExclusiveChoiceRemove);
        registrar.register(EditorCommandType.CANVAS_IMAGE_PUT, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasImagePut);
        registrar.register(EditorCommandType.CANVAS_IMAGE_REMOVE, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasImageRemove);
        registrar.register(EditorCommandType.CANVAS_TEXT_PUT, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasTextPut);
        registrar.register(EditorCommandType.CANVAS_TEXT_REMOVE, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasTextRemove);
        registrar.register(EditorCommandType.CANVAS_LAYER_ORDER, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasLayerOrder);
    }

    private static void canvasExclusiveChoicePut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.putCanvasExclusiveChoice(
                player,
                EditorCommandPayloads.group(payload),
                CanvasLayerNbt.exclusiveChoiceFromTag(EditorCommandPayloads.compound(payload, EditorCommandPayloadKeys.EXCLUSIVE_CHOICE))
        );
    }

    private static void canvasExclusiveChoicesPut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.putCanvasExclusiveChoices(
                player,
                EditorCommandPayloads.group(payload),
                CanvasLayerNbt.exclusiveChoicesFromListTag(payload.getList(EditorCommandPayloadKeys.EXCLUSIVE_CHOICES, Tag.TAG_COMPOUND))
        );
    }

    private static void canvasExclusiveChoiceRemove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.removeCanvasExclusiveChoice(
                player,
                EditorCommandPayloads.group(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.ID)
        );
    }

    private static void canvasImagePut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.putCanvasImage(
                player,
                EditorCommandPayloads.group(payload),
                CanvasLayerNbt.imageFromTag(EditorCommandPayloads.compound(payload, EditorCommandPayloadKeys.IMAGE))
        );
    }

    private static void canvasImageRemove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.removeCanvasImage(
                player,
                EditorCommandPayloads.group(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.ID)
        );
    }

    private static void canvasTextPut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        CompoundTag text = EditorCommandPayloads.compound(payload, EditorCommandPayloadKeys.TEXT);
        if (EditorCommandPayloadLimits.exceedsLimit(text.getList(EditorCommandPayloadKeys.SPANS, Tag.TAG_COMPOUND), EditorCommandPayloadLimits.MAX_TEXT_SPANS)) {
            return;
        }
        editor.putCanvasText(player, EditorCommandPayloads.group(payload), CanvasLayerNbt.textFromTag(text));
    }

    private static void canvasTextRemove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.removeCanvasText(
                player,
                EditorCommandPayloads.group(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloadKeys.ID)
        );
    }

    private static void canvasLayerOrder(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        ListTag order = EditorCommandPayloads.order(payload);
        if (EditorCommandPayloadLimits.exceedsLimit(order, EditorCommandPayloadLimits.MAX_LAYER_ORDER_ENTRIES)) {
            return;
        }
        editor.setCanvasLayerOrder(player, EditorCommandPayloads.group(payload), EditorCommandPayloads.nonBlankStringsFrom(order));
    }
}
