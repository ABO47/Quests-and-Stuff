package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimProtection;

@Mixin(Entity.class)
public abstract class EntityMayInteractMixin {
    @Inject(method = "mayInteract", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$denyMobGriefing(Level level, BlockPos pos,
            CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof Player)
                && level instanceof ServerLevel serverLevel) {
            if (ChunkClaimProtection.isProtectedChunk(
                    serverLevel, new ChunkPos(pos), false, true)) {
                cir.setReturnValue(false);
            }
        }
    }
}
