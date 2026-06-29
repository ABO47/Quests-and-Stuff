package com.abo47.questsandstuff.client.tablet.theme;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ShaderTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public final class SkinEditOverlay {
    private static final ResourceLocation GLOW_SHADER = new ResourceLocation("questsandstuff", "glow");
    private static final int DIM_COLOR = 0x60000000;
    private static final int GLOW_COLOR = ModColors.INTERACTIVE;
    private static final int LABEL_BG = 0xCC111111;
    private static final int LABEL_TEXT = ModColors.INTERACTIVE;

    private SkinEditOverlay() {
    }

    public static void draw(GuiGraphics graphics, WidgetGroup root, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        ColorRectTexture dim = new ColorRectTexture(DIM_COLOR);
        dim.draw(graphics, 0, 0, 0, 0, screenW, screenH);

        SkinEditTargetFinder.WidgetTarget target = SkinEditTargetFinder.find(root, mouseX, mouseY);
        if (target == null) return;

        Widget w = target.widget();
        int wx = w.getPositionX();
        int wy = w.getPositionY();
        int ww = w.getSizeWidth();
        int wh = w.getSizeHeight();

        List<Rectangle> ancestors = SkinEditTargetFinder.ancestorBounds(w, root);
        Rectangle clip = ancestorClip(w, ancestors);
        if (clip == null || clip.width <= 0 || clip.height <= 0) return;

        List<Rectangle> occluders = collectOccluders(w, root, clip);
        List<Rectangle> gaps = subtractOccluders(clip, occluders);
        if (gaps.isEmpty()) {
            gaps.add(new Rectangle(clip.x, clip.y, clip.width, clip.height));
        }

        ShaderTexture shader = ShaderTexture.createShader(GLOW_SHADER);
        float r = FastColor.ARGB32.red(GLOW_COLOR) / 255f;
        float g = FastColor.ARGB32.green(GLOW_COLOR) / 255f;
        float b = FastColor.ARGB32.blue(GLOW_COLOR) / 255f;

        for (Rectangle gap : gaps) {
            graphics.enableScissor(gap.x, gap.y, gap.x + gap.width, gap.y + gap.height);
            if (shader != null) {
                shader.setUniformCache(cache -> {
                    cache.glUniform4F("uGlowColor", r, g, b, 1f);
                });
                shader.draw(graphics, mouseX, mouseY, wx, wy, ww, wh);
            }
            graphics.disableScissor();
        }

        drawLabel(graphics, target, mouseX + 12, mouseY - 12);
    }

    private static Rectangle ancestorClip(Widget widget, List<Rectangle> ancestors) {
        int sx = widget.getPositionX();
        int sy = widget.getPositionY();
        int sw = widget.getSizeWidth();
        int sh = widget.getSizeHeight();
        for (Rectangle a : ancestors) {
            int ar = a.x + a.width;
            int ab = a.y + a.height;
            int r = sx + sw;
            int b = sy + sh;
            sx = Math.max(sx, a.x);
            sy = Math.max(sy, a.y);
            sw = Math.min(r, ar) - sx;
            sh = Math.min(b, ab) - sy;
            if (sw <= 0 || sh <= 0) return null;
        }
        return new Rectangle(sx, sy, sw, sh);
    }

    private static List<Widget> buildPathToRoot(Widget widget, WidgetGroup root) {
        List<Widget> path = new ArrayList<>();
        Widget cur = widget;
        while (cur != null) {
            path.add(cur);
            if (cur == root) break;
            cur = cur.getParent();
        }
        return path;
    }

    private static List<Rectangle> collectOccluders(Widget target, WidgetGroup root, Rectangle ancestorClip) {
        List<Rectangle> occluders = new ArrayList<>();
        List<Widget> path = buildPathToRoot(target, root);

        for (int level = 0; level < path.size() - 1; level++) {
            Widget node = path.get(level);
            WidgetGroup parent = (WidgetGroup) path.get(level + 1);
            int idx = parent.widgets.indexOf(node);

            for (int i = idx + 1; i < parent.widgets.size(); i++) {
                Widget sibling = parent.widgets.get(i);
                collectOccludingDescendants(sibling, ancestorClip, occluders);
            }
        }

        if (target instanceof WidgetGroup targetGroup) {
            for (Widget child : targetGroup.widgets) {
                if (child.isVisible() && occludes(child)) {
                    addClippedRect(child, ancestorClip, occluders);
                }
            }
        }

        return occluders;
    }

    private static void collectOccludingDescendants(Widget widget, Rectangle ancestorClip, List<Rectangle> occluders) {
        if (!widget.isVisible()) return;
        if (occludes(widget)) {
            addClippedRect(widget, ancestorClip, occluders);
        }
        if (widget instanceof WidgetGroup group) {
            for (Widget child : group.widgets) {
                collectOccludingDescendants(child, ancestorClip, occluders);
            }
        }
    }

    private static void addClippedRect(Widget widget, Rectangle ancestorClip, List<Rectangle> occluders) {
        int wx = Math.max(widget.getPositionX(), ancestorClip.x);
        int wy = Math.max(widget.getPositionY(), ancestorClip.y);
        int wr = Math.min(widget.getPositionX() + widget.getSizeWidth(), ancestorClip.x + ancestorClip.width);
        int wb = Math.min(widget.getPositionY() + widget.getSizeHeight(), ancestorClip.y + ancestorClip.height);
        int ww = wr - wx;
        int wh = wb - wy;
        if (ww > 0 && wh > 0) {
            occluders.add(new Rectangle(wx, wy, ww, wh));
        }
    }

    private static List<Rectangle> subtractOccluders(Rectangle area, List<Rectangle> occluders) {
        List<Rectangle> result = new ArrayList<>();
        result.add(new Rectangle(area.x, area.y, area.width, area.height));

        for (Rectangle o : occluders) {
            List<Rectangle> next = new ArrayList<>();
            for (Rectangle r : result) {
                int ox = Math.max(o.x, r.x);
                int oy = Math.max(o.y, r.y);
                int or = Math.min(o.x + o.width, r.x + r.width);
                int ob = Math.min(o.y + o.height, r.y + r.height);
                int ow = or - ox;
                int oh = ob - oy;
                if (ow <= 0 || oh <= 0) {
                    next.add(r);
                    continue;
                }
                if (oy > r.y) {
                    next.add(new Rectangle(r.x, r.y, r.width, oy - r.y));
                }
                if (ob < r.y + r.height) {
                    next.add(new Rectangle(r.x, ob, r.width, r.y + r.height - ob));
                }
                if (ox > r.x) {
                    next.add(new Rectangle(r.x, oy, ox - r.x, oh));
                }
                if (or < r.x + r.width) {
                    next.add(new Rectangle(or, oy, r.x + r.width - or, oh));
                }
            }
            result = next;
            if (result.isEmpty()) return result;
        }

        result.removeIf(rect -> rect.width <= 0 || rect.height <= 0);
        return result;
    }

    private static boolean occludes(Widget widget) {
        if (!widget.isVisible()) return false;
        IGuiTexture bg = widget.getBackgroundTexture();
        if (bg == null || bg.equals(IGuiTexture.EMPTY)) {
            if (widget instanceof WidgetGroup wg) {
                return SkinEditTargetFinder.hasCustomChrome(wg);
            }
            return false;
        }
        if (bg instanceof ColorRectTexture crt) {
            if (crt.color == ModColors.SURFACE_BASE || crt.color == ModColors.DEFAULT_SURFACE_BASE) {
                return false;
            }
        }
        return true;
    }

    private static void drawLabel(GuiGraphics graphics, SkinEditTargetFinder.WidgetTarget target, int x, int y) {
        String prefix = target.isBorder() ? "\u25A2 " : "\u2588 ";
        String text = prefix + target.label();
        Font font = Minecraft.getInstance().font;
        int tw = font.width(text);
        int pad = 3;
        int lx = Math.max(2, Math.min(x, Minecraft.getInstance().getWindow().getGuiScaledWidth() - tw - pad * 2 - 2));
        int ly = Math.max(2, Math.min(y, Minecraft.getInstance().getWindow().getGuiScaledHeight() - font.lineHeight - pad * 2 - 2));

        ColorRectTexture bgRect = new ColorRectTexture(LABEL_BG);
        bgRect.draw(graphics, 0, 0, lx, ly, tw + pad * 2, font.lineHeight + pad * 2);

        graphics.drawString(font, text, lx + pad, ly + pad, LABEL_TEXT, false);
    }
}
