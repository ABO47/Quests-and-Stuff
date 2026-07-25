package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.chunkclaim.ChunkClaimProtection;

@Mixin(BlockItem.class)
public abstract class BlockItemMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$preventPlacement(BlockPlaceContext blockPlaceContext,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (blockPlaceContext.getPlayer() instanceof ServerPlayer player
                && blockPlaceContext.getLevel() instanceof ServerLevel level) {
            if (!ChunkClaimProtection.allowedBreakPlace(player, level,
                    blockPlaceContext.getClickedPos())) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}
