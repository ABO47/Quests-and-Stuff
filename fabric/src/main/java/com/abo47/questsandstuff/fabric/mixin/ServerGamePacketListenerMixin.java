package com.abo47.questsandstuff.fabric.mixin;

import java.util.Optional;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.inventory.RecipeBookMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.abo47.questsandstuff.quest.runtime.lock.ItemLockEnforcement;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {
    @Shadow
    public ServerPlayer player;

    @Inject(method = "handlePlaceRecipe", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$blockLockedPlacement(ServerboundPlaceRecipePacket packet, CallbackInfo ci) {
        if (!(this.player.containerMenu instanceof RecipeBookMenu)) {
            return;
        }
        Optional<? extends Recipe<?>> holder = this.player.server.getRecipeManager().byKey(packet.getRecipe());
        if (holder.isEmpty()) {
            return;
        }
        Recipe<?> recipe = holder.get();
        ItemStack result = recipe.getResultItem(this.player.level().registryAccess());
        if (result == null || result.isEmpty()) {
            return;
        }
        if (ItemLockEnforcement.isLocked(this.player, result)) {
            ci.cancel();
            this.player.containerMenu.broadcastChanges();
        }
    }
}
