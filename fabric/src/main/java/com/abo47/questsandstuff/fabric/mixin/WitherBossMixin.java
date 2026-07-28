package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.ChunkPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimProtection;

@Mixin(WitherBoss.class)
public abstract class WitherBossMixin {
    @Shadow
    private int destroyBlocksTick;

    @Inject(method = "customServerAiStep", at = @At("HEAD"))
    private void questsandstuff$preventBlockDestruction(CallbackInfo ci) {
        WitherBoss wither = (WitherBoss) (Object) this;
        if (wither.level() instanceof ServerLevel serverLevel) {
            if (ChunkClaimProtection.isProtectedChunk(serverLevel,
                    new ChunkPos(wither.blockPosition()), false, true)) {
                destroyBlocksTick = 20;
            }
        }
    }
}
