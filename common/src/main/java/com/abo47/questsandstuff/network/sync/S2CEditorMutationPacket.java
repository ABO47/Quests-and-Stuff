package com.abo47.questsandstuff.network.sync;

import com.abo47.questsandstuff.network.QuestPacketContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


public record S2CEditorMutationPacket(long sequence, String action, String questId, CompoundTag questTag) {
    public static S2CEditorMutationPacket decode(FriendlyByteBuf buf) {
        long sequence = buf.readLong();
        String action = buf.readUtf();
        String questId = buf.readUtf();
        CompoundTag tag = buf.readNbt();
        return new S2CEditorMutationPacket(sequence, action, questId, tag == null ? new CompoundTag() : tag);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(sequence);
        buf.writeUtf(action == null ? "" : action);
        buf.writeUtf(questId == null ? "" : questId);
        buf.writeNbt(questTag == null ? new CompoundTag() : questTag);
    }

    public void handle(QuestPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleEditorMutation(sequence, action, questId, questTag));
    }
}
