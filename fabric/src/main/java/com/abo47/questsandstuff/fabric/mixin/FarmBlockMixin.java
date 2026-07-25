package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimProtection;

@Mixin(FarmBlock.class)
public abstract class FarmBlockMixin {
    @Inject(method = "fallOn", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$preventTrampling(Level level, BlockState state,
            BlockPos pos, Entity entity, float fallDistance, CallbackInfo ci) {
        if (!(entity instanceof Player)
                && level instanceof ServerLevel serverLevel) {
            if (ChunkClaimProtection.isProtectedChunk(serverLevel,
                    new ChunkPos(pos), false, true)) {
                ci.cancel();
            }
        }
    }
}
