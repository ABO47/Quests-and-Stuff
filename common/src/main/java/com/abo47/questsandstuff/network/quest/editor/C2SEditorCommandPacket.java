package com.abo47.questsandstuff.network.quest.editor;

import com.abo47.questsandstuff.network.ModPacketContext;
import com.abo47.questsandstuff.quest.editor.command.EditorCommand;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandPayloadKeys;
import com.abo47.questsandstuff.quest.editor.command.EditorCommandType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public record C2SEditorCommandPacket(EditorCommand command) {
    public static final String PREREQUISITE_FIELD = EditorCommandPayloadKeys.PREREQUISITE;

    public C2SEditorCommandPacket(String action, CompoundTag payload) {
        this(new EditorCommand(EditorCommandType.fromWireName(action), payload));
    }

    public C2SEditorCommandPacket(EditorCommandType type, CompoundTag payload) {
        this(new EditorCommand(type, payload));
    }

    public static C2SEditorCommandPacket decode(FriendlyByteBuf buf) {
        return new C2SEditorCommandPacket(EditorCommand.decode(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        command.encode(buf);
    }

    public void handle(ModPacketContext context) {
        ServerPlayer player = context.sender();
        if (EditorPacketGuard.canEdit(player)) {
            context.enqueueWork(() -> EditorCommandDispatcher.dispatch(player, command));
        }
    }
}
