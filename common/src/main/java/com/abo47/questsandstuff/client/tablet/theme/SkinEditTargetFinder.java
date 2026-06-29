package com.abo47.questsandstuff.client.tablet.theme;

import com.lowdragmc.lowdraglib.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

final class SkinEditTargetFinder {
    static final int BORDER_PX = 1;

    record WidgetTarget(Widget widget, boolean isBorder, String label) {
    }

    private SkinEditTargetFinder() {
    }

    static WidgetTarget find(WidgetGroup root, int mouseX, int mouseY) {
        Widget hit = deepestAt(root, mouseX, mouseY);
        if (hit == null) return null;
        boolean isBorder = isOnBorder(hit, mouseX, mouseY);
        String label = resolveLabel(hit, isBorder);
        return new WidgetTarget(hit, isBorder, label);
    }

    private static Widget deepestAt(WidgetGroup group, int mouseX, int mouseY) {
        for (int i = group.widgets.size() - 1; i >= 0; i--) {
            Widget child = group.widgets.get(i);
            if (!child.isVisible()) continue;
            if (child instanceof WidgetGroup childGroup) {
                Widget found = deepestAt(childGroup, mouseX, mouseY);
                if (found != null) return found;
            }
            if (child.isMouseOverElement(mouseX, mouseY) && isTargetable(child)) {
                return child;
            }
        }
        if (group.isMouseOverElement(mouseX, mouseY) && isTargetable(group)) {
            return group;
        }
        return null;
    }

    private static boolean isTargetable(Widget widget) {
        if (isBackdropFill(widget)) return false;
        if (hasVisibleBackground(widget)) return true;
        if (widget instanceof WidgetGroup wg && hasCustomChrome(wg)) return true;
        return false;
    }

    private static boolean isBackdropFill(Widget widget) {
        IGuiTexture bg = widget.getBackgroundTexture();
        if (bg instanceof ColorRectTexture crt) {
            return crt.color == ModColors.SURFACE_BASE || crt.color == ModColors.DEFAULT_SURFACE_BASE;
        }
        return false;
    }

    private static boolean hasVisibleBackground(Widget widget) {
        IGuiTexture bg = widget.getBackgroundTexture();
        if (bg == null) return false;
        return !bg.equals(IGuiTexture.EMPTY);
    }

    private static boolean isOnBorder(Widget widget, int mouseX, int mouseY) {
        int x = widget.getPositionX();
        int y = widget.getPositionY();
        int w = widget.getSizeWidth();
        int h = widget.getSizeHeight();
        int mx = mouseX;
        int my = mouseY;
        int right = x + w;
        int bottom = y + h;
        boolean onLeft = mx >= x && mx < x + BORDER_PX;
        boolean onRight = mx >= right - BORDER_PX && mx < right;
        boolean onTop = my >= y && my < y + BORDER_PX;
        boolean onBottom = my >= bottom - BORDER_PX && my < bottom;
        return (onLeft || onRight) || (onTop || onBottom);
    }

    static List<Rectangle> ancestorBounds(Widget widget, WidgetGroup stopAt) {
        List<Rectangle> rects = new ArrayList<>();
        Widget cur = widget.getParent();
        while (cur != null && cur != stopAt) {
            rects.add(new Rectangle(cur.getPositionX(), cur.getPositionY(), cur.getSizeWidth(), cur.getSizeHeight()));
            cur = cur.getParent();
        }
        return rects;
    }

    private static String resolveLabel(Widget widget, boolean isBorder) {
        int fillC = extractFillColor(widget);
        int borderC = extractBorderColor(widget);
        if (fillC == 0 && borderC == 0 && widget instanceof WidgetGroup wg) {
            fillC = inheritedFill(wg);
            borderC = inheritedBorder(wg);
        }
        if (isBorder) {
            String slot = borderC != 0 ? matchSlotForColor(borderC, false) : null;
            if (slot != null) return slot;
            slot = fillC != 0 ? matchSlotForColor(fillC, true) : null;
            if (slot != null) return slot;
        } else {
            String slot = fillC != 0 ? matchSlotForColor(fillC, true) : null;
            if (slot != null) return slot;
            slot = borderC != 0 ? matchSlotForColor(borderC, false) : null;
            if (slot != null) return slot;
        }
        return widget.getClass().getSimpleName();
    }

    private static int inheritedFill(WidgetGroup group) {
        for (Widget child : group.widgets) {
            int c = extractFillColor(child);
            if (c != 0) return c;
        }
        return 0;
    }

    private static int inheritedBorder(WidgetGroup group) {
        for (Widget child : group.widgets) {
            int c = extractBorderColor(child);
            if (c != 0) return c;
        }
        return 0;
    }

    private static int extractFillColor(Widget widget) {
        IGuiTexture bg = widget.getBackgroundTexture();
        if (bg instanceof ColorRectTexture crt) {
            return crt.color;
        }
        if (bg instanceof GuiTextureGroup gtg) {
            for (IGuiTexture t : gtg.textures) {
                if (t instanceof ColorRectTexture crt) return crt.color;
            }
        }
        return 0;
    }

    private static int extractBorderColor(Widget widget) {
        IGuiTexture bg = widget.getBackgroundTexture();
        if (bg instanceof ColorBorderTexture cbt) {
            return cbt.color;
        }
        if (bg instanceof GuiTextureGroup gtg) {
            for (IGuiTexture t : gtg.textures) {
                if (t instanceof ColorBorderTexture cbt) return cbt.color;
            }
        }
        return 0;
    }

    static boolean hasCustomChrome(WidgetGroup widget) {
        try {
            return widget.getClass().getMethod("drawInBackground", GuiGraphics.class, int.class, int.class, float.class)
                    .getDeclaringClass() != WidgetGroup.class;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static String matchSlotForColor(int color, boolean fill) {
        if (fill) {
            if (color == ModColors.SURFACE_BASE || color == ModColors.DEFAULT_SURFACE_BASE) return "surface_base";
            if (color == ModColors.SURFACE_PANEL || color == ModColors.DEFAULT_SURFACE_PANEL) return "surface_panel";
            if (color == ModColors.SURFACE_PANEL_ALT || color == ModColors.DEFAULT_SURFACE_PANEL_ALT) return "surface_panel_alt";
            if (color == ModColors.SUCCESS || color == ModColors.DEFAULT_SUCCESS) return "success";
            if (color == ModColors.WARNING || color == ModColors.DEFAULT_WARNING) return "warning";
            if (color == ModColors.ERROR || color == ModColors.DEFAULT_ERROR) return "error";
            if (color == ModColors.INTERACTIVE || color == ModColors.DEFAULT_INTERACTIVE) return "interactive";
            if (color == ModColors.recessedSurface()) return "recessed_surface";
            if (color == ModColors.elevatedSurface()) return "elevated_surface";
        } else {
            if (color == ModColors.BORDER_BASE || color == ModColors.DEFAULT_BORDER_BASE) return "border_base";
            if (color == ModColors.BORDER_ACCENT || color == ModColors.DEFAULT_BORDER_ACCENT) return "border_accent";
            if (color == ModColors.subtleBorder()) return "subtle_border";
            if (color == ModColors.focusBorder()) return "focus_border";
        }
        return null;
    }
}
