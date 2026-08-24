package com.abo47.questsandstuff.forge.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.client.quest.lock.ClientCookingLocks;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

@Mixin(CampfireBlock.class)
public abstract class CampfireBlockMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$blockLockedCampfireUse(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (level == null || player == null) {
            return;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) {
            return;
        }
        boolean deny = level.isClientSide
                ? ClientCookingLocks.campfireOutputBlocked(held)
                : ItemLockEnforcement.campfireOutputLocked(level, held);
        if (deny) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
