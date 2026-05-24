package com.abo47.questsandstuff.client.tablet.icons;

import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.model.CanvasModelPreviewRenderer;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public final class DisplayIconWidget extends WidgetGroup {
    private final String iconId;
    private final ItemStack stack;

    public DisplayIconWidget(int x, int y, int width, int height, String iconId) {
        super(x, y, width, height);
        this.iconId = iconId == null ? "" : iconId;
        this.stack = ItemStack.EMPTY;
    }

    public DisplayIconWidget(int x, int y, int width, int height, ItemStack stack) {
        super(x, y, width, height);
        this.iconId = "";
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!stack.isEmpty()) {
            new ItemStackTexture(stack).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            return;
        }
        ResourceTexture uiIcon = UiIconAtlas.iconTexture(iconId);
        if (uiIcon != null) {
            uiIcon.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            return;
        }
        if (CanvasModelPreviewRenderer.isModelAsset(iconId)
                && CanvasModelPreviewRenderer.renderModelAsset(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), iconId)) {
            return;
        }
        String entityId = EntityPreviewRenderer.entityId(iconId);
        if (!entityId.isBlank()) {
            int yaw = EntityPreviewRenderer.entityYaw(iconId);
            int spin = EntityPreviewRenderer.entitySpinSpeed(iconId);
            if (!EntityPreviewRenderer.renderEntityAsset(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), iconId, yaw, spin, partialTicks)) {
                QuestIconProvider.iconTexture(EntityPreviewRenderer.spawnEggIcon(entityId)).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            }
            return;
        }
        QuestIconProvider.iconTexture(iconId).draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
    }
}
