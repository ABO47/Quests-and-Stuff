package com.abo47.questsandstuff.client.tablet.icons;

import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

public final class DisplayIconWidget extends WidgetGroup {
    private final String iconId;

    public DisplayIconWidget(int x, int y, int width, int height, String iconId) {
        super(x, y, width, height);
        this.iconId = iconId == null ? "" : iconId;
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
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
