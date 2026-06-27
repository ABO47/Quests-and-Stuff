package com.abo47.questsandstuff.client.tablet.teams;

import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

import java.util.UUID;

import static com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome.drawRectOutline;

public final class PlayerFaceTexture implements IGuiTexture {
    private final UUID uuid;

    public PlayerFaceTexture(UUID uuid, String name) {
        this.uuid = uuid;
    }

    @Override
    public void draw(GuiGraphics graphics, int mouseX, int mouseY, float x, float y, int width, int height) {
        ResourceLocation skin = resolveSkin(uuid);
        IGuiTexture texture = new com.lowdragmc.lowdraglib.gui.texture.ResourceTexture(skin);
        texture.draw(graphics, 0, 0, (int) x, (int) y, width, height);
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
