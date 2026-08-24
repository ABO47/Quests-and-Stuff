package com.abo47.questsandstuff.quest.runtime.lock;

import java.util.Set;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;

import com.abo47.questsandstuff.QuestsAndStuffMod;

public final class LockedRecipeSerializer implements RecipeSerializer<LockedCraftingRecipe> {
    public static final LockedRecipeSerializer INSTANCE = new LockedRecipeSerializer();
    public static final ResourceLocation ID =
            ResourceLocation.tryBuild(QuestsAndStuffMod.MODID, "locked");

    private static final Set<String> WARNED_DECODE =
            java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<>());

    private LockedRecipeSerializer() {
    }

    @Override
    public LockedCraftingRecipe fromJson(ResourceLocation id, JsonObject json) {
        JsonObject innerJson = GsonHelper.getAsJsonObject(json, "recipe");
        Recipe<?> inner = RecipeManager.fromJson(id, innerJson);
        if (!(inner instanceof CraftingRecipe crafting)) {
            throw new JsonSyntaxException(
                    "Gated recipes only support crafting table recipes: " + id);
        }
        return new LockedCraftingRecipe(crafting);
    }

    @Override
    public LockedCraftingRecipe fromNetwork(ResourceLocation outerId, FriendlyByteBuf buf) {
        ResourceLocation innerSerializerId = buf.readResourceLocation();
        ResourceLocation innerId = buf.readResourceLocation();
        RecipeSerializer<?> innerSerializer = BuiltInRegistries.RECIPE_SERIALIZER.get(innerSerializerId);
        if (innerSerializer == null) {
            if (WARNED_DECODE.add(innerSerializerId.toString())) {
                QuestsAndStuffMod.LOGGER.warn(
                        "[QnS:Lock] unknown wrapped recipe serializer {} during client sync",
                        innerSerializerId);
            }
            throw new IllegalArgumentException("Unknown wrapped recipe serializer " + innerSerializerId);
        }
        Recipe<?> inner = readInner(innerSerializer, innerId, buf);
        if (!(inner instanceof CraftingRecipe crafting)) {
            throw new IllegalArgumentException(
                    "Wrapped non-crafting recipe over network: " + innerSerializerId);
        }
        return new LockedCraftingRecipe(crafting);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buf, LockedCraftingRecipe recipe) {
        writeInner(buf, recipe.inner());
    }

    private static <T extends Recipe<?>> T readInner(RecipeSerializer<T> serializer, ResourceLocation id, FriendlyByteBuf buf) {
        return serializer.fromNetwork(id, buf);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void writeInner(FriendlyByteBuf buf, Recipe<?> inner) {
        RecipeSerializer serializer = (RecipeSerializer) inner.getSerializer();
        ResourceLocation serializerId = BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer);
        if (serializerId == null) {
            throw new IllegalArgumentException("Unregistered serializer for gated recipe " + inner.getId());
        }
        buf.writeResourceLocation(serializerId);
        buf.writeResourceLocation(inner.getId());
        serializer.toNetwork(buf, inner);
    }
}
