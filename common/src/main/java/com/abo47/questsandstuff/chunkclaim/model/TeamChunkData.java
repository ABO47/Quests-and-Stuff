package com.abo47.questsandstuff.chunkclaim.model;

import java.util.List;

public record TeamChunkData(List<ClaimedChunk> chunks) {
    public TeamChunkData {
        chunks = List.copyOf(chunks);
    }
}
