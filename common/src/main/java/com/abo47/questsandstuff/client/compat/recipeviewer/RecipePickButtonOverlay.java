package com.abo47.questsandstuff.client.compat.recipeviewer;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;

import com.abo47.questsandstuff.client.tablet.icons.IconAtlas;
import com.abo47.questsandstuff.client.tablet.theme.render.GlowShaderHelper;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.theme.tokens.UiThemeTokens;

public final class RecipePickButtonOverlay {
    public static final int BUTTON_SIZE = 11;
    public static final int BUTTON_GAP = 2;
    private static final int MIN_NATIVE_BUTTON_SIZE = 8;
    private static final int MAX_NATIVE_BUTTON_SIZE = 24;

    private RecipePickButtonOverlay() {
    }

    public static void draw(GuiGraphics graphics, int mouseX, int mouseY, Rect2i button) {
        if (button == null) {
            return;
        }
        RenderSystem.disableScissor();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        boolean hovered = contains(button, mouseX, mouseY);
        int border = TabletColors.subtleBorder();
        int fill = UiThemeTokens.withAlpha(TabletColors.SURFACE_PANEL_ALT, 218);
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 350.0F);
        try {
            SurfaceFactory.fill(border).draw(graphics, 0, 0, button.getX(), button.getY(), button.getWidth(), button.getHeight());
            SurfaceFactory.fill(fill).draw(graphics, 0, 0, button.getX() + 1, button.getY() + 1, button.getWidth() - 2, button.getHeight() - 2);
            if (hovered) {
                GlowShaderHelper.drawGlow(graphics, 0, 0, button.getX() + 1, button.getY() + 1, button.getWidth() - 2, button.getHeight() - 2);
            }
            IGuiTexture icon = IconAtlas.iconTexture("add");
            if (icon != null) {
                int iconSize = centeredIconSize(button);
                int iconX = button.getX() + (button.getWidth() - iconSize) / 2;
                int iconY = button.getY() + (button.getHeight() - iconSize) / 2;
                icon.draw(graphics, mouseX, mouseY, iconX, iconY, iconSize, iconSize);
            }
        } finally {
            graphics.pose().popPose();
        }
    }

    public static void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY, Rect2i button) {
        if (button != null && contains(button, mouseX, mouseY)) {
            RenderSystem.disableScissor();
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 950.0F);
            try {
                graphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("ui.questsandstuff.recipe_viewer.pick_recipe"), mouseX, mouseY);
            } finally {
                graphics.pose().popPose();
            }
        }
    }

    public static Rect2i buttonAbove(Rect2i nativeButton) {
        if (nativeButton == null) {
            return null;
        }
        int width = nativeSize(nativeButton.getWidth());
        int height = nativeSize(nativeButton.getHeight());
        int x = nativeButton.getX() + (nativeButton.getWidth() - width) / 2;
        int y = nativeButton.getY() - height - BUTTON_GAP;
        return screenClamped(new Rect2i(x, y, width, height));
    }

    public static Rect2i pickButtonAboveRightStack(Rect2i recipeBounds, List<Rect2i> blockers) {
        Rect2i anchor = topRightStackBlocker(recipeBounds, blockers);
        if (anchor != null) {
            return buttonAbove(anchor);
        }
        return pickButtonRect(recipeBounds, blockers);
    }

    public static Rect2i pickButtonRect(Rect2i recipeBounds, List<Rect2i> blockers) {
        if (recipeBounds == null) {
            return null;
        }
        int size = BUTTON_SIZE;
        int gap = BUTTON_GAP;
        int rightOfBlockers = recipeBounds.getX() + recipeBounds.getWidth() + gap;
        if (blockers != null) {
            for (Rect2i blocker : blockers) {
                if (blocker != null && overlaps(verticalBand(recipeBounds), blocker)) {
                    rightOfBlockers = Math.max(rightOfBlockers, blocker.getX() + blocker.getWidth() + gap);
                }
            }
        }
        int bottomY = recipeBounds.getY() + recipeBounds.getHeight() - size - gap;
        int topY = recipeBounds.getY() + gap;
        Rect2i[] candidates = {
                screenClamped(new Rect2i(rightOfBlockers, bottomY, size, size)),
                screenClamped(new Rect2i(recipeBounds.getX() + recipeBounds.getWidth() + gap, bottomY, size, size)),
                screenClamped(new Rect2i(rightOfBlockers, topY, size, size)),
                screenClamped(new Rect2i(recipeBounds.getX() + recipeBounds.getWidth() - size - gap, bottomY, size, size)),
                screenClamped(new Rect2i(recipeBounds.getX() + gap, bottomY, size, size)),
                screenClamped(new Rect2i(recipeBounds.getX() + recipeBounds.getWidth() - size - gap, topY, size, size))
        };
        for (Rect2i candidate : candidates) {
            if (!overlapsAny(candidate, blockers)) {
                return candidate;
            }
        }
        return candidates[0];
    }

    public static boolean contains(Rect2i rect, double mouseX, double mouseY) {
        return rect != null
                && mouseX >= rect.getX()
                && mouseY >= rect.getY()
                && mouseX < rect.getX() + rect.getWidth()
                && mouseY < rect.getY() + rect.getHeight();
    }

    public static boolean overlaps(Rect2i left, Rect2i right) {
        return left != null
                && right != null
                && left.getX() < right.getX() + right.getWidth()
                && left.getX() + left.getWidth() > right.getX()
                && left.getY() < right.getY() + right.getHeight()
                && left.getY() + left.getHeight() > right.getY();
    }

    private static boolean overlapsAny(Rect2i candidate, List<Rect2i> blockers) {
        if (blockers == null || blockers.isEmpty()) {
            return false;
        }
        for (Rect2i blocker : blockers) {
            if (overlaps(candidate, blocker)) {
                return true;
            }
        }
        return false;
    }

    private static Rect2i topRightStackBlocker(Rect2i recipeBounds, List<Rect2i> blockers) {
        if (recipeBounds == null || blockers == null || blockers.isEmpty()) {
            return null;
        }
        int minX = recipeBounds.getX() + recipeBounds.getWidth() / 2;
        int maxX = recipeBounds.getX() + recipeBounds.getWidth() + BUTTON_SIZE * 5;
        int minY = recipeBounds.getY() - BUTTON_SIZE * 2;
        int maxY = recipeBounds.getY() + recipeBounds.getHeight() + BUTTON_SIZE * 2;
        int stackX = Integer.MIN_VALUE;
        for (Rect2i blocker : blockers) {
            if (isSmallRightSideBlocker(blocker, minX, maxX, minY, maxY)) {
                stackX = Math.max(stackX, blocker.getX());
            }
        }
        if (stackX == Integer.MIN_VALUE) {
            return null;
        }
        Rect2i best = null;
        for (Rect2i blocker : blockers) {
            if (!isSmallRightSideBlocker(blocker, minX, maxX, minY, maxY)
                    || Math.abs(blocker.getX() - stackX) > BUTTON_GAP) {
                continue;
            }
            if (best == null
                    || blocker.getY() < best.getY()
                    || blocker.getY() == best.getY() && blocker.getX() < best.getX()) {
                best = blocker;
            }
        }
        return best;
    }

    private static boolean isSmallRightSideBlocker(Rect2i blocker, int minX, int maxX, int minY, int maxY) {
        return blocker != null
                && blocker.getWidth() <= BUTTON_SIZE * 3
                && blocker.getHeight() <= BUTTON_SIZE * 3
                && blocker.getX() >= minX
                && blocker.getX() <= maxX
                && blocker.getY() + blocker.getHeight() >= minY
                && blocker.getY() <= maxY;
    }

    private static Rect2i verticalBand(Rect2i bounds) {
        return new Rect2i(0, bounds.getY() - BUTTON_SIZE, Minecraft.getInstance().getWindow().getGuiScaledWidth(), bounds.getHeight() + BUTTON_SIZE * 2);
    }

    private static Rect2i screenClamped(Rect2i rect) {
        Minecraft minecraft = Minecraft.getInstance();
        int min = BUTTON_GAP;
        int maxX = minecraft.getWindow().getGuiScaledWidth() - rect.getWidth() - BUTTON_GAP;
        int maxY = minecraft.getWindow().getGuiScaledHeight() - rect.getHeight() - BUTTON_GAP;
        return new Rect2i(clamp(rect.getX(), min, maxX), clamp(rect.getY(), min, maxY), rect.getWidth(), rect.getHeight());
    }

    private static int centeredIconSize(Rect2i button) {
        int control = Math.max(1, Math.min(button.getWidth(), button.getHeight()));
        int size = clamp(Math.round(control * 0.56F), 4, 10);
        if (((control - size) & 1) != 0) {
            int larger = size + 1;
            int smaller = size - 1;
            if (larger <= 10) {
                size = larger;
            } else if (smaller >= 4) {
                size = smaller;
            }
        }
        return size;
    }

    private static int nativeSize(int size) {
        return clamp(size, MIN_NATIVE_BUTTON_SIZE, MAX_NATIVE_BUTTON_SIZE);
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }
}
