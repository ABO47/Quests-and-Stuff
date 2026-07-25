package com.abo47.questsandstuff.fabric.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimProtection;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow
    @Final
    private ObjectArrayList<BlockPos> toBlow;

    @Shadow
    @Final
    private Level level;

    @Inject(method = "explode", at = @At("RETURN"))
    private void questsandstuff$filterProtectedBlocks(CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            toBlow.removeIf(pos -> ChunkClaimProtection.isProtectedChunk(
                    serverLevel, new ChunkPos(pos), true));
        }
    }
}
