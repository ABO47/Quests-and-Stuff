package com.abo47.questsandstuff.forge.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

@Mixin(LootTable.class)
public abstract class LootTableMixin {
    @Inject(
            method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;",
            at = @At("RETURN")
    )
    private void questsandstuff$filterLockedDrops(LootParams params, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        ItemLockEnforcement.filterLoot(cir.getReturnValue(), params);
    }
}
