package com.abo47.questsandstuff.chunkclaim;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.quest.QuestServiceRegistry;
import com.abo47.questsandstuff.team.model.TeamData;
import com.abo47.questsandstuff.team.TeamManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;

import java.util.UUID;

public final class ChunkClaimProtection {
    private ChunkClaimProtection() {
    }

    private static UUID teamOf(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        var server = player.getServer();
        if (server == null) {
            return null;
        }
        TeamManager manager = new TeamManager(player.serverLevel(), QuestServiceRegistry.engine(server));
        TeamData team = manager.getTeam(player);
        return team == null ? null : team.teamId();
    }

    private static boolean claimedByOther(UUID actorTeam, ServerLevel level, ChunkPos pos) {
        UUID owner = QuestServiceRegistry.chunkClaims(level.getServer())
                .ownerTeamIdOf(level.dimension().location(), pos.x, pos.z);
        if (owner == null) {
            return false;
        }
        return !owner.equals(actorTeam);
    }

    public static boolean allowedBreakPlace(ServerPlayer actor, ServerLevel level, BlockPos pos) {
        if (actor != null && actor.hasPermissions(2)) {
            return true;
        }
        if (!QuestsAndStuffConfig.chunkClaimProtectBreakPlace()) {
            return true;
        }
        return !claimedByOther(teamOf(actor), level, new ChunkPos(pos));
    }

    public static boolean allowedInteract(ServerPlayer actor, ServerLevel level, BlockPos pos) {
        if (actor != null && actor.hasPermissions(2)) {
            return true;
        }
        if (!QuestsAndStuffConfig.chunkClaimProtectInteraction()) {
            return true;
        }
        return !claimedByOther(teamOf(actor), level, new ChunkPos(pos));
    }

    public static boolean allowedPvp(ServerPlayer attacker, ServerPlayer target, ServerLevel level, BlockPos targetPos) {
        if (attacker != null && attacker.hasPermissions(2)) {
            return true;
        }
        if (!QuestsAndStuffConfig.chunkClaimProtectPvp()) {
            return true;
        }
        ChunkPos pos = new ChunkPos(targetPos);
        UUID owner = QuestServiceRegistry.chunkClaims(level.getServer())
                .ownerTeamIdOf(level.dimension().location(), pos.x, pos.z);
        if (owner == null) {
            return true;
        }
        return owner.equals(teamOf(attacker));
    }

    public static boolean isProtectedChunk(ServerLevel level, ChunkPos pos, boolean explosions) {
        if (!explosions) {
            return false;
        }
        if (!QuestsAndStuffConfig.chunkClaimProtectExplosions()) {
            return false;
        }
        return QuestServiceRegistry.chunkClaims(level.getServer())
                .ownerTeamIdOf(level.dimension().location(), pos.x, pos.z) != null;
    }

    public static boolean isProtectedChunk(ServerLevel level, ChunkPos pos, boolean explosions, boolean mobGriefing) {
        if (!explosions && !mobGriefing) {
            return false;
        }
        if (explosions && isProtectedChunk(level, pos, true)) {
            return true;
        }
        if (mobGriefing && QuestsAndStuffConfig.chunkClaimProtectMobGriefing()) {
            return QuestServiceRegistry.chunkClaims(level.getServer())
                    .ownerTeamIdOf(level.dimension().location(), pos.x, pos.z) != null;
        }
        return false;
    }
}
