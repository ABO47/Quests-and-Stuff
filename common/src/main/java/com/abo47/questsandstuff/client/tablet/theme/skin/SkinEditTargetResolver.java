package com.abo47.questsandstuff.client.tablet.theme.skin;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.client.gui.GuiGraphics;

public final class SkinEditTargetResolver {
    private static final Map<Class<?>, Boolean> CUSTOM_CHROME_CACHE = new HashMap<>();

    private SkinEditTargetResolver() {
    }

    public static String findTargetKeyAt(WidgetGroup root, int mouseX, int mouseY) {
        Widget qdl = SkinAnchorRegistry.findByKey("quest_details_layer");
        if (qdl != null && qdl.isVisible() && qdl.isMouseOverElement(mouseX, mouseY) && qdl instanceof WidgetGroup qdlGroup) {
            Widget hit = deepestAt(qdlGroup, mouseX, mouseY);
            if (hit != null) return stableKeyFor(hit);
        }
        Widget hit = deepestAt(root, mouseX, mouseY);
        if (hit != null) return stableKeyFor(hit);
        if (root.isMouseOverElement(mouseX, mouseY) && isTargetable(root)) {
            return "root";
        }
        return null;
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
        if (SkinAnchorRegistry.keyFor(widget) != null) return true;
        return false;
    }

    private static boolean isSkinExcluded(Widget widget) {
        Widget cur = widget;
        while (cur != null) {
            if (cur instanceof CanvasViewport) return true;
            cur = cur.getParent();
        }
        String simpleName = widget.getClass().getSimpleName();
        if ("ImageWidget".equals(simpleName)) return true;
        if ("DisplayIconWidget".equals(simpleName)) return true;
        if ("QuestDetailsDescriptionCanvas".equals(simpleName)) return true;
        if ("TabletHomeOverviewPanel".equals(simpleName)) return true;
        if ("CollapsedChapterTileWidget".equals(simpleName)) return true;
        if ("ChapterCompletionNoticeWidget".equals(simpleName)) return true;
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
        return !bg.equals(IGuiTexture.EMPTY);
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
}
