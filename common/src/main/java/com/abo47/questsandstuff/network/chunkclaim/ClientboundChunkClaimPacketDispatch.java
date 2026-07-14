package com.abo47.questsandstuff.network.chunkclaim;

import com.abo47.questsandstuff.client.tablet.chunkclaimer.ClientChunkClaimCache;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.chunkclaim.ChunkClaimPacketHelper;
import net.minecraft.nbt.CompoundTag;

final class ClientboundChunkClaimPacketDispatch {
    private ClientboundChunkClaimPacketDispatch() {
    }

    static void handle(CompoundTag payload) {
        ChunkClaimPacketHelper.ChunkClaimSnapshot snapshot = ChunkClaimPacketHelper.fromPayload(payload);
        ClientChunkClaimCache.INSTANCE.set(snapshot.teamId(), snapshot.chunks());
        TabletUiFactory.refreshActiveTablet();
    }
}
