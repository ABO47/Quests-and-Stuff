package com.abo47.questsandstuff.client.tablet.icons;

import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.model.CanvasModelPreviewRenderer;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public final class DisplayIconWidget extends WidgetGroup {
    private final String iconId;
    private final ItemStack stack;
    private final ItemStackTexture stackTexture;

    public DisplayIconWidget(int x, int y, int width, int height, String iconId) {
        super(x, y, width, height);
        this.iconId = iconId == null ? "" : iconId;
        this.stack = ItemStack.EMPTY;
        this.stackTexture = null;
    }

    public DisplayIconWidget(int x, int y, int width, int height, ItemStack stack) {
        super(x, y, width, height);
        this.iconId = "";
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.stackTexture = this.stack.isEmpty() ? null : new ScopedItemStackTexture(this.stack);
    }

    @Override
    public void drawInBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (!stack.isEmpty()) {
            stackTexture.draw(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
            return;
        }
        drawIcon(graphics, mouseX, mouseY, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), iconId, partialTicks, 255);
    }

    public static void drawIcon(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int width, int height, String iconId, float partialTicks, int alpha) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        if (safeAlpha <= 0 || width <= 0 || height <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, safeAlpha / 255.0f);
        try {
            drawIconContent(graphics, mouseX, mouseY, x, y, width, height, iconId, partialTicks);
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private static void drawIconContent(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int width, int height, String iconId, float partialTicks) {
        String safeIconId = iconId == null ? "" : iconId;
        ResourceTexture uiIcon = UiIconAtlas.iconTexture(safeIconId);
        if (uiIcon != null) {
            uiIcon.draw(graphics, mouseX, mouseY, x, y, width, height);
            return;
        }
        if (CanvasModelPreviewRenderer.isModelAsset(safeIconId)
                && CanvasModelPreviewRenderer.renderModelAsset(graphics, x, y, width, height, safeIconId)) {
            return;
        }
        if (FluidIconCodec.isFluidIcon(safeIconId) && renderFluidIcon(graphics, x, y, width, height, safeIconId)) {
            return;
        }
        String entityId = EntityPreviewRenderer.entityId(safeIconId);
        if (!entityId.isBlank()) {
            int yaw = EntityPreviewRenderer.entityYaw(safeIconId);
            int spin = EntityPreviewRenderer.entitySpinSpeed(safeIconId);
            if (!EntityPreviewRenderer.renderEntityAsset(graphics, x, y, width, height, safeIconId, yaw, spin, partialTicks)) {
                QuestIconProvider.iconTexture(EntityPreviewRenderer.spawnEggIcon(entityId)).draw(graphics, mouseX, mouseY, x, y, width, height);
            }
            return;
        }
        QuestIconProvider.iconTexture(safeIconId).draw(graphics, mouseX, mouseY, x, y, width, height);
    }

    private static boolean renderFluidIcon(GuiGraphics graphics, int x, int y, int width, int height, String iconId) {
        Fluid fluid = FluidIconCodec.fluidFromIcon(iconId);
        if (fluid == Fluids.EMPTY) {
            return false;
        }
        int inset = Math.max(0, Math.min(width, height) / 12);
        DrawerHelper.drawFluidForGui(
                graphics,
                FluidStack.create(fluid, 1000),
                x + inset,
                y + inset,
                Math.max(1, width - inset * 2),
                Math.max(1, height - inset * 2)
        );
        return true;
    }
}
