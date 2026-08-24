package com.abo47.questsandstuff.forge.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
