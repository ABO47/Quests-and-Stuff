package com.abo47.questsandstuff.fabric.mixin;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.inventory.RecipeBookMenu;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.abo47.questsandstuff.client.quest.lock.ClientBookFilter;

@Mixin(RecipeButton.class)
public abstract class RecipeButtonMixin {
    @Shadow
    private RecipeCollection collection;

    @Shadow
    private RecipeBook book;

    @Shadow
    private RecipeBookMenu<?> menu;

    @Unique
    private boolean questsandstuff$hasNothingToDisplay() {
        return !ClientBookFilter.hasDisplayableRecipes(collection, book, menu);
    }

    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$hideEmptyRecipeButton(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (!questsandstuff$hasNothingToDisplay()) {
            return;
        }
        ((AbstractWidget) (Object) this).visible = false;
        ci.cancel();
    }

    @Inject(method = "getTooltipText", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$emptyTooltipForEmptyButton(CallbackInfoReturnable<List<Component>> cir) {
        if (questsandstuff$hasNothingToDisplay()) {
            cir.setReturnValue(List.of());
        }
    }

    @Inject(method = "updateWidgetNarration", at = @At("HEAD"), cancellable = true)
    private void questsandstuff$skipNarrationForEmptyButton(NarrationElementOutput output, CallbackInfo ci) {
        if (questsandstuff$hasNothingToDisplay()) {
            ci.cancel();
        }
    }
}
