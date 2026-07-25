package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimProtection;

@Mixin(LightningBolt.class)
public abstract class LightningBoltMixin extends Entity {
    private LightningBoltMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void questsandstuff$preventFire(CallbackInfo ci) {
        if (level() instanceof ServerLevel serverLevel) {
            if (ChunkClaimProtection.isProtectedChunk(serverLevel,
                    new ChunkPos(blockPosition()), false, true)) {
                discard();
            }
        }
    }
}
