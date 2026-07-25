package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.level.ChunkPos;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimProtection;

@Mixin(RemoveBlockGoal.class)
public abstract class RemoveBlockGoalMixin {
    @Final
    @Shadow
    private Mob removerMob;

    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$preventDoorBreaking(CallbackInfoReturnable<Boolean> cir) {
        if (removerMob.level() instanceof ServerLevel serverLevel) {
            if (ChunkClaimProtection.isProtectedChunk(serverLevel,
                    new ChunkPos(removerMob.blockPosition()), false, true)) {
                cir.setReturnValue(false);
            }
        }
    }
}
