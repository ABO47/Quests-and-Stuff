package com.abo47.questsandstuff.chunkclaim;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.chunkclaim.model.TeamChunkData;
import com.abo47.questsandstuff.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class ChunkClaimService {
    private final MinecraftServer server;

    public ChunkClaimService(MinecraftServer server) {
        this.server = server;
    }

    private ChunkClaimSavedData data() {
        return ChunkClaimSavedData.get(server);
    }

    public enum ClaimResult {
        OK,
        ALREADY_CLAIMED,
        LIMIT_REACHED,
        NOT_CLAIMED
    }

    public ClaimResult claim(UUID teamId, ResourceLocation dim, int x, int z) {
        if (data().isClaimed(teamId, dim, x, z)) {
            return ClaimResult.ALREADY_CLAIMED;
        }
        if (data().countClaimed(teamId) >= QuestsAndStuffConfig.chunkClaimMaxClaimedChunks()) {
            return ClaimResult.LIMIT_REACHED;
        }
        if (data().claim(teamId, dim, x, z)) {
            return ClaimResult.OK;
        }
        return ClaimResult.ALREADY_CLAIMED;
    }

    public ClaimResult unclaim(UUID teamId, ResourceLocation dim, int x, int z) {
        if (!data().isClaimed(teamId, dim, x, z)) {
            return ClaimResult.NOT_CLAIMED;
        }
        data().unclaim(teamId, dim, x, z);
        ServerLevel level = levelFor(dim);
        if (level != null) {
            Services.platform().setForceChunk(level, new ChunkPos(x, z), false);
        }
        return ClaimResult.OK;
    }

    public ClaimResult setForceLoaded(UUID teamId, ResourceLocation dim, int x, int z, boolean force) {
        if (!data().isClaimed(teamId, dim, x, z)) {
            return ClaimResult.NOT_CLAIMED;
        }
        boolean currently = data().isForceLoaded(teamId, dim, x, z);
        if (force && !currently) {
            if (data().countForceLoaded(teamId) >= QuestsAndStuffConfig.chunkClaimMaxForceLoadedChunks()) {
                return ClaimResult.LIMIT_REACHED;
            }
        }
        if (data().setForce(teamId, dim, x, z, force)) {
            applyForceLoad(teamId, dim, x, z, force);
            return ClaimResult.OK;
        }
        return ClaimResult.NOT_CLAIMED;
    }

    public void applyForceLoad(UUID teamId, ResourceLocation dim, int x, int z, boolean forced) {
        ServerLevel level = levelFor(dim);
        if (level != null) {
            Services.platform().setForceChunk(level, new ChunkPos(x, z), forced);
        }
    }

    public void applyAllForceLoads() {
        data().forEachForceChunk((teamId, chunk) -> applyForceLoad(teamId, chunk.dimension(), chunk.x(), chunk.z(), true));
    }

    public void suppressFire() {
        if (!QuestsAndStuffConfig.chunkClaimProtectFire()) {
            return;
        }
        data().forEachClaimed((teamId, chunk) -> {
            ServerLevel level = levelFor(chunk.dimension());
            if (level == null) {
                return;
            }
            int baseX = chunk.x() << 4;
            int baseZ = chunk.z() << 4;
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (int lx = 0; lx < 16; lx++) {
                for (int lz = 0; lz < 16; lz++) {
                    for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                        mutable.set(baseX + lx, y, baseZ + lz);
                        if (level.getBlockState(mutable).getBlock() == Blocks.FIRE) {
                            BlockState below = level.getBlockState(mutable.below());
                            try {
                                java.lang.reflect.Method m = FireBlock.class.getDeclaredMethod("getIgniteOdds", BlockState.class);
                                m.setAccessible(true);
                                int odds = (int) m.invoke(Blocks.FIRE, below);
                                if (odds > 0) {
                                    level.removeBlock(mutable, false);
                                }
                            } catch (Exception igniteError) {
                                level.removeBlock(mutable, false);
                            }
                        }
                    }
                }
            }
        });
    }

    public TeamChunkData claims(UUID teamId) {
        return data().claims(teamId);
    }

    public boolean isClaimed(UUID teamId, ResourceLocation dim, int x, int z) {
        return data().isClaimed(teamId, dim, x, z);
    }

    public boolean isForceLoaded(UUID teamId, ResourceLocation dim, int x, int z) {
        return data().isForceLoaded(teamId, dim, x, z);
    }

    public UUID ownerTeamIdOf(ResourceLocation dim, int x, int z) {
        return data().ownerTeamIdOf(dim, x, z);
    }

    public int countClaimed(UUID teamId) {
        return data().countClaimed(teamId);
    }

    public int countForceLoaded(UUID teamId) {
        return data().countForceLoaded(teamId);
    }

    public boolean removeTeam(UUID teamId) {
        return data().removeTeam(teamId);
    }

    private ServerLevel levelFor(ResourceLocation dim) {
        return server.getLevel(net.minecraft.resources.ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION, dim));
    }
}
