package com.abo47.questsandstuff.quest.editor.command;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;

public record EditorCommand(EditorCommandType type, CompoundTag payload) {
    public EditorCommand {
        type = type == null ? EditorCommandType.UNKNOWN : type;
        payload = payload == null ? new CompoundTag() : payload;
        EditorCommandPayloadLimits.requireAllowed(type, payload);
    }

    public static EditorCommand decode(FriendlyByteBuf buf) {
        EditorCommandType type = EditorCommandType.fromWireName(buf.readUtf());
        CompoundTag payload = buf.readNbt(EditorCommandPayloadLimits.nbtAccounter());
        return new EditorCommand(type, payload == null ? new CompoundTag() : payload);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(type.wireName());
        buf.writeNbt(payload);
    }
}
