package com.abo47.questsandstuff.fabric.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.quest.lock.ClientCookingLocks;
import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin {
    @Inject(method = "placeFood", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$blockLockedCampfireFood(Entity entity, ItemStack stack, int cookTime, CallbackInfoReturnable<Boolean> cir) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        Level level = ((CampfireBlockEntity) (Object) this).getLevel();
        boolean deny;
        if (level != null && !level.isClientSide) {
            deny = ItemLockEnforcement.campfireOutputLocked(level, stack);
        } else {
            deny = ClientCookingLocks.campfireOutputBlocked(stack);
        }
        if (deny) {
            QuestsAndStuffMod.debugLog("[QnS:Lock] blocked locked campfire ingredient {}", stack.getItem());
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "cookTick", at = @At("HEAD"), cancellable = true)
    private static void questsandstuff$freezeLockedCampfireCooking(
            Level level,
            BlockPos pos,
            BlockState state,
            CampfireBlockEntity campfire,
            CallbackInfo ci) {
        if (level == null || level.isClientSide || !ItemLockEnforcement.locksActive()) {
            return;
        }
        for (ItemStack cooking : campfire.getItems()) {
            if (cooking.isEmpty()) {
                continue;
            }
            if (ItemLockEnforcement.cookingOutputLockedFromEveryone(level, cooking)) {
                ci.cancel();
                return;
            }
        }
    }
}
