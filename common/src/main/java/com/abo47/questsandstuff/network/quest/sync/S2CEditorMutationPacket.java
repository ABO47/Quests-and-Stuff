package com.abo47.questsandstuff.network.quest.sync;

import com.abo47.questsandstuff.network.ModPacketContext;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;


public record S2CEditorMutationPacket(long sequence, String action, String questId, CompoundTag questTag) {
    public static S2CEditorMutationPacket decode(FriendlyByteBuf buf) {
        long sequence = buf.readLong();
        String action = buf.readUtf();
        String questId = buf.readUtf();
        return new S2CEditorMutationPacket(sequence, action, questId, SyncPacketPayloadLimits.readNbt(buf));
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeLong(sequence);
        buf.writeUtf(action == null ? "" : action);
        buf.writeUtf(questId == null ? "" : questId);
        buf.writeNbt(questTag == null ? new CompoundTag() : questTag);
    }

    public void handle(ModPacketContext context) {
        context.enqueueWork(() -> ClientboundSyncPacketDispatch.handleEditorMutation(sequence, action, questId, questTag));
    }
}
