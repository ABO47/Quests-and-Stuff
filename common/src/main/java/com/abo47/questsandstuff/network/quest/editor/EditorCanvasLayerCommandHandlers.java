package com.abo47.questsandstuff.network.quest.editor;

import java.util.function.Consumer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.editor.command.EditorCommandFamily;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloads;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.model.canvas.CanvasLayerNbtCodec;

final class EditorCanvasLayerCommandHandlers {
    private EditorCanvasLayerCommandHandlers() {
    }

    static void register(Consumer<EditorCommandDescriptor> registrar) {
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CANVAS_EXCLUSIVE_CHOICE_PUT, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasExclusiveChoicePut));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CANVAS_EXCLUSIVE_CHOICE_PUT_MANY, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasExclusiveChoicesPut));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CANVAS_EXCLUSIVE_CHOICE_REMOVE, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasExclusiveChoiceRemove));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.EC_CONNECTION_HIDDEN, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::ecConnectionHidden));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CANVAS_IMAGE_PUT, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasImagePut));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CANVAS_IMAGE_REMOVE, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasImageRemove));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CANVAS_TEXT_PUT, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasTextPut));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CANVAS_TEXT_REMOVE, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasTextRemove));
        registrar.accept(new EditorCommandDescriptor(EditorCommandType.CANVAS_LAYER_ORDER, EditorCommandFamily.CANVAS_LAYER, EditorCanvasLayerCommandHandlers::canvasLayerOrder));
    }

    private static void canvasExclusiveChoicePut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.putCanvasExclusiveChoice(
                player,
                EditorCommandPayloads.chapter(payload),
                CanvasLayerNbtCodec.exclusiveChoiceFromTag(EditorCommandPayloads.compound(payload, EditorCommandPayloads.EXCLUSIVE_CHOICE))
        );
    }

    private static void canvasExclusiveChoicesPut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.putCanvasExclusiveChoices(
                player,
                EditorCommandPayloads.chapter(payload),
                CanvasLayerNbtCodec.exclusiveChoicesFromListTag(payload.getList(EditorCommandPayloads.EXCLUSIVE_CHOICES, Tag.TAG_COMPOUND))
        );
    }

    private static void canvasExclusiveChoiceRemove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.removeCanvasExclusiveChoice(
                player,
                EditorCommandPayloads.chapter(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.ID)
        );
    }

    private static void ecConnectionHidden(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        String chapter = EditorCommandPayloads.chapter(payload);
        String sourceId = EditorCommandPayloads.string(payload, EditorCommandPayloads.ID);
        String targetId = EditorCommandPayloads.string(payload, EditorCommandPayloads.PREREQUISITE);
        boolean hidden = EditorCommandPayloads.bool(payload, EditorCommandPayloads.HIDDEN);
        if (sourceId.isBlank() || targetId.isBlank()) {
            return;
        }
        editor.ecConnectionHidden(player, chapter, sourceId, targetId, hidden);
    }

    private static void canvasImagePut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.putCanvasImage(
                player,
                EditorCommandPayloads.chapter(payload),
                CanvasLayerNbtCodec.imageFromTag(EditorCommandPayloads.compound(payload, EditorCommandPayloads.IMAGE))
        );
    }

    private static void canvasImageRemove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.removeCanvasImage(
                player,
                EditorCommandPayloads.chapter(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.ID)
        );
    }

    private static void canvasTextPut(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        CompoundTag text = EditorCommandPayloads.compound(payload, EditorCommandPayloads.TEXT);
        editor.putCanvasText(player, EditorCommandPayloads.chapter(payload), CanvasLayerNbtCodec.textFromTag(text));
    }

    private static void canvasTextRemove(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        editor.removeCanvasText(
                player,
                EditorCommandPayloads.chapter(payload),
                EditorCommandPayloads.string(payload, EditorCommandPayloads.ID)
        );
    }

    private static void canvasLayerOrder(ServerPlayer player, EditorSessionService editor, CompoundTag payload) {
        ListTag order = EditorCommandPayloads.order(payload);
        editor.setCanvasLayerOrder(player, EditorCommandPayloads.chapter(payload), EditorCommandPayloads.nonBlankStringsFrom(order));
    }
}
