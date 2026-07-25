package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimProtection;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin {
    @Inject(method = "checkWalls", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$preventBlockDestruction(AABB area,
            CallbackInfoReturnable<Boolean> cir) {
        EnderDragon dragon = (EnderDragon) (Object) this;
        if (dragon.level() instanceof ServerLevel serverLevel) {
            if (ChunkClaimProtection.isProtectedChunk(serverLevel,
                    new ChunkPos(dragon.blockPosition()), false, true)) {
                cir.setReturnValue(false);
            }
        }
    }
}
