package com.abo47.questsandstuff.network.chunkclaim;

import net.minecraft.nbt.CompoundTag;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimPacketHelper;
import com.abo47.questsandstuff.client.tablet.chunkclaimer.ChunkClaimerHeaderControls;
import com.abo47.questsandstuff.client.tablet.chunkclaimer.ClientChunkClaimCache;

final class ClientboundChunkClaimPacketDispatch {
    private ClientboundChunkClaimPacketDispatch() {
    }

    static void handle(CompoundTag payload) {
        ClientChunkClaimCache.INSTANCE.setAll(ChunkClaimPacketHelper.decodeClaims(payload));
        ChunkClaimerHeaderControls.onSync();
    }
}
