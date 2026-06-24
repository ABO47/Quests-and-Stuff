package com.abo47.questsandstuff.client.tablet.teams;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

public final class PlayerFaceTexture implements IGuiTexture {
    private final UUID uuid;

    public PlayerFaceTexture(UUID uuid, String name) {
        this.uuid = uuid;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        ResourceLocation skin = resolveSkin(uuid);
        graphics.blit(skin, (int) x, (int) y, width, height, 8.0f, 8.0f, 8, 8, 64, 64);
    }

    private static ResourceLocation resolveSkin(UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null) {
            PlayerInfo playerInfo = mc.getConnection().getPlayerInfo(uuid);
            if (playerInfo != null) {
                ResourceLocation tex = playerInfo.getSkinLocation();
                if (tex != null) {
                    return tex;
                }
            }
        }
        return DefaultPlayerSkin.getDefaultSkin(uuid);
    }
}
