package com.abo47.questsandstuff.client.tablet.theme.skin;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;

public final class SkinEditTargetResolver {
    private static final Map<Class<?>, Boolean> CUSTOM_CHROME_CACHE = new HashMap<>();

    private SkinEditTargetResolver() {
    }

    public static String findTargetKeyAt(WidgetGroup root, int mouseX, int mouseY) {
        Widget qdl = SkinAnchorRegistry.findByKey("quest_details_layer");
        if (qdl != null && qdl instanceof WidgetGroup qdlGroup) {
            Widget hit = deepestAt(qdlGroup, mouseX, mouseY);
            if (hit != null && hit != qdl) {
                String key = stableKeyFor(hit);
                if (!isCanvasPanelKey(key)) return key;
                return null;
            }
        }
        Widget hit = deepestAt(root, mouseX, mouseY);
        if (hit != null && hit != root) {
            String key = stableKeyFor(hit);
            if (!isCanvasPanelKey(key)) return key;
            return null;
        }
        if (root.isMouseOverElement(mouseX, mouseY) && isTargetable(root)) {
            return "root";
        }
        return null;
    }

    private static boolean isCanvasPanelKey(String key) {
        return key != null && ("quests_canvas_background".equals(key) || "quest_details_canvas_background".equals(key));
    }

    private static Widget deepestAt(WidgetGroup group, int mouseX, int mouseY) {
        Widget groupMatch = null;
        if (group.isMouseOverElement(mouseX, mouseY) && isTargetable(group)) {
            groupMatch = group;
        }

        for (int i = group.widgets.size() - 1; i >= 0; i--) {
            Widget child = group.widgets.get(i);
            if (!child.isVisible()) continue;
            boolean childCoversMouse = child.isMouseOverElement(mouseX, mouseY);
            if (child instanceof WidgetGroup childGroup) {
                Widget found = deepestAt(childGroup, mouseX, mouseY);
                if (found != null) return found;
            }
            if (childCoversMouse && isTargetable(child)) {
                return child;
            }
        }

        return groupMatch;
    }

    public static Widget widgetForKey(WidgetGroup root, String targetKey) {
        if (targetKey == null || targetKey.isBlank()) return null;
        Widget registered = SkinAnchorRegistry.findByKey(targetKey);
        if (registered != null) return registered;
        for (Widget w : SkinAnchorRegistry.sharedWidgetsFor(targetKey)) {
            return w;
        }
        for (Widget w : root.widgets) {
            Widget found = searchForKey(w, targetKey);
            if (found != null) return found;
        }
        return null;
    }

    private static Widget searchForKey(Widget widget, String targetKey) {
        String key = stableKeyFor(widget);
        if (key != null && key.equals(targetKey)) return widget;
        if (widget instanceof WidgetGroup group) {
            for (Widget child : group.widgets) {
                Widget found = searchForKey(child, targetKey);
                if (found != null) return found;
            }
        }
        return null;
    }

    public static String stableKeyFor(Widget widget) {
        String registered = SkinAnchorRegistry.keyFor(widget);
        if (registered != null) return registered;
        return buildPathKey(widget);
    }

    private static String buildPathKey(Widget widget) {
        List<Widget> path = new ArrayList<>();
        Widget cur = widget;
        while (cur != null) {
            path.add(cur);
            cur = cur.getParent();
        }

        StringBuilder sb = new StringBuilder();
        for (int i = path.size() - 1; i >= 0; i--) {
            if (sb.length() > 0) sb.append("/");
            Widget w = path.get(i);
            String name = w.getClass().getSimpleName();
            if (name.isEmpty()) name = w.getClass().getName().replace('.', '_');

            WidgetGroup parent = w.getParent();
            if (parent != null) {
                int count = 0;
                int idx = 0;
                for (Widget sibling : parent.widgets) {
                    if (sibling.getClass() == w.getClass()) {
                        if (sibling == w) idx = count;
                        count++;
                    }
                }
                if (count > 1) {
                    name += "[" + idx + "]";
                }
            }
            sb.append(name);
        }
        return sb.toString();
    }

    public static boolean isTargetable(Widget widget) {
        if (isSkinExcluded(widget)) return false;
        if (hasVisibleBackground(widget)) return true;
        return SkinAnchorRegistry.keyFor(widget) != null;
    }

    private static boolean isSkinExcluded(Widget widget) {
        Widget cur = widget;
        while (cur != null) {
            if (cur instanceof CanvasViewport) return true;
            cur = cur.getParent();
        }
        String simpleName = widget.getClass().getSimpleName();
        if ("SourceOriginRevealWidget".equals(simpleName)) return true;
        if ("ImageWidget".equals(simpleName)) return true;
        if ("DisplayIconWidget".equals(simpleName)) return true;
        if ("QuestDetailsDescriptionCanvas".equals(simpleName)) return true;
        if ("TabletHomeOverviewPanel".equals(simpleName)) return true;
        if ("CollapsedChapterTileWidget".equals(simpleName)) return true;
        if ("ChapterCompletionNoticeWidget".equals(simpleName)) return true;
        if ("InlineRenameField".equals(simpleName)) return true;
        if (isInsideChapterCardArea(widget)) return true;
        return false;
    }

    private static boolean isInsideChapterCardArea(Widget widget) {
        Widget cur = widget.getParent();
        while (cur != null) {
            if ("TabletScissoredWidgetGroup".equals(cur.getClass().getSimpleName())) return true;
            cur = cur.getParent();
        }
        return false;
    }

    private static boolean hasVisibleBackground(Widget widget) {
        IGuiTexture bg = widget.getBackgroundTexture();
        if (bg == null) return false;
        if (bg.equals(IGuiTexture.EMPTY)) return false;
        if (bg instanceof ColorRectTexture crt && (crt.color >>> 24) == 0) return false;
        return true;
    }

    public static String resolveSharedKey(Widget widget) {
        String selfKey = SkinAnchorRegistry.keyFor(widget);
        if (selfKey != null && !SkinOverrideKey.isSharedKey(selfKey)) return null;
        Widget cur = widget;
        while (cur != null) {
            String registeredKey = SkinAnchorRegistry.keyFor(cur);
            if (registeredKey != null && SkinOverrideKey.isSharedKey(registeredKey)) {
                return registeredKey;
            }
            cur = cur.getParent();
        }
        return null;
    }

    public static boolean hasCustomChrome(Widget widget) {
        Class<?> cls = widget.getClass();
        return CUSTOM_CHROME_CACHE.computeIfAbsent(cls, k -> {
            try {
                if (k.getMethod("drawInBackground", GuiGraphics.class, int.class, int.class, float.class)
                        .getDeclaringClass() != WidgetGroup.class) {
                    return true;
                }
            } catch (NoSuchMethodException e) {
            }
            try {
                if (k.getMethod("drawInForeground", GuiGraphics.class, int.class, int.class, float.class)
                        .getDeclaringClass() != WidgetGroup.class) {
                    return true;
                }
            } catch (NoSuchMethodException e) {
            }
            return false;
        });
    }

    public static List<Rectangle> ancestorBounds(Widget widget, WidgetGroup stopAt) {
        List<Rectangle> rects = new ArrayList<>();
        Widget cur = widget.getParent();
        while (cur != null && cur != stopAt) {
            rects.add(new Rectangle(cur.getPositionX(), cur.getPositionY(),
                    cur.getSizeWidth(), cur.getSizeHeight()));
            cur = cur.getParent();
        }
        rects.add(new Rectangle(stopAt.getPositionX(), stopAt.getPositionY(),
                stopAt.getSizeWidth(), stopAt.getSizeHeight()));
        return rects;
    }

    /**
     * Find bounds of all target-widgets nested inside {@code widget} (not including widget itself).
     * Recursion stops as soon as a target is found along a branch, since a target's own
     * bounds already contain whatever is nested further inside it.
     */
    public static List<Rectangle> nestedTargetBounds(Widget widget, WidgetGroup root) {
        List<Rectangle> out = new ArrayList<>();
        if (widget instanceof WidgetGroup group) {
            for (Widget child : group.widgets) {
                collectNestedTargets(child, out);
            }
        }
        return out;
    }

    private static void collectNestedTargets(Widget widget, List<Rectangle> out) {
        if (!widget.isVisible()) return;

        if (isTargetable(widget)) {
            out.add(new Rectangle(widget.getPositionX(), widget.getPositionY(),
                    widget.getSizeWidth(), widget.getSizeHeight()));
            return;
        }

        if (widget instanceof WidgetGroup group) {
            boolean hasBackground = hasVisibleBackground(widget);
            boolean hasChrome = hasCustomChrome(widget);
            for (Widget child : group.widgets) {
                collectNestedTargets(child, out);
            }
            if (out.isEmpty() && (hasBackground || hasChrome)) {
                out.add(new Rectangle(widget.getPositionX(), widget.getPositionY(),
                        widget.getSizeWidth(), widget.getSizeHeight()));
            }
        } else if (hasVisibleBackground(widget) || (widget.getSizeWidth() > 0 && widget.getSizeHeight() > 0)) {
            out.add(new Rectangle(widget.getPositionX(), widget.getPositionY(),
                    widget.getSizeWidth(), widget.getSizeHeight()));
        }
    }
}
