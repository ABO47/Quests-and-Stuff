package com.abo47.questsandstuff.client.tablet.teams;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PlayerFaceTexture implements IGuiTexture {
    private static final Map<String, ResourceLocation> CACHE = new HashMap<>();

    private final String profileKey;

    public PlayerFaceTexture(UUID uuid, String name) {
        this.profileKey = uuid.toString() + "|" + (name == null ? "" : name);
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        ResourceLocation skin = CACHE.computeIfAbsent(profileKey, k -> {
            String[] parts = k.split("\\|", 2);
            UUID uuid = UUID.fromString(parts[0]);
            return Minecraft.getInstance().getSkinManager().getInsecureSkinLocation(
                    new com.mojang.authlib.GameProfile(uuid, parts.length > 1 ? parts[1] : ""));
        });
        if (skin == null) {
            String[] parts = profileKey.split("\\|", 2);
            UUID uuid = UUID.fromString(parts[0]);
            skin = DefaultPlayerSkin.getDefaultSkin(uuid);
        }
        graphics.blit(skin, (int) x, (int) y, width, height, 8.0f, 8.0f, 8, 8, 64, 64);
    }
}
