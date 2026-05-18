package com.abo47.questsandstuff.client.canvas.hit;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.client.canvas.model.EdgeHit;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.render.CanvasElementSelectionSlot;
import com.abo47.questsandstuff.client.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.FONT_SIZE_SLIDER_POPOVER_GAP;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.FONT_SIZE_SLIDER_POPOVER_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.selectedGroupName;

public final class CanvasHitTester {
    private CanvasHitTester() {
    }

    public static QuestCardLayout hitTestCard(List<QuestCardLayout> cards, int x, int y) {
        for (int i = cards.size() - 1; i >= 0; i--) {
            QuestCardLayout card = cards.get(i);
            if (card.containsScreen(x, y)) {
                return card;
            }
        }
        return null;
    }

    public static CanvasImageLayer hitTestCanvasImage(TabletUiState state, int x, int y) {
        String group = selectedGroupName(state);
        List<CanvasImageLayer> images = orderedCanvasImages(state, group);
        for (int i = images.size() - 1; i >= 0; i--) {
            CanvasImageLayer image = images.get(i);
            int sw = CanvasGeometry.screenSpan(state, image.w());
            int sh = CanvasGeometry.screenSpan(state, image.h());
            double[] local = canvasImageLocalScreenPoint(state, image, x, y);
            if (local[0] >= 0 && local[0] <= sw && local[1] >= 0 && local[1] <= sh) {
                return image;
            }
        }
        return null;
    }

    public static CanvasImageLayer hitTestSelectedCanvasImageControls(TabletUiState state, int x, int y) {
        if (state.selectedCanvasImageId.isBlank()) {
            return null;
        }
        CanvasImageLayer image = state.canvasImagesByGroup.getOrDefault(selectedGroupName(state), List.of()).stream()
                .filter(entry -> entry.id().equals(state.selectedCanvasImageId))
                .findFirst()
                .orElse(null);
        if (image == null) {
            return null;
        }
        if (isCanvasImageResizeHandleHit(state, image, x, y) || isCanvasImageRotateHandleHit(state, image, x, y)) {
            return image;
        }
        int sw = CanvasGeometry.screenSpan(state, image.w());
        int sh = CanvasGeometry.screenSpan(state, image.h());
        double[] local = canvasImageLocalScreenPoint(state, image, x, y);
        return local[0] >= -3 && local[0] <= sw + 3 && local[1] >= -3 && local[1] <= sh + 3 ? image : null;
    }

    public static EdgeHit hitTestEdge(TabletUiState state, List<QuestCardLayout> cards, Map<String, QuestCardLayout> byQuestId, int x, int y) {
        String group = selectedGroupName(state);
        int tolerance = 4;
        for (QuestCardLayout quest : cards) {
            if (!quest.tag().getBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD)) {
                continue;
            }
            ListTag prerequisites = quest.tag().getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
            for (int i = 0; i < prerequisites.size(); i++) {
                String prerequisiteId = prerequisites.getString(i);
                QuestCardLayout prerequisite = byQuestId.get(prerequisiteId);
                if (prerequisite == null) {
                    continue;
                }
                if (ConnectionRenderer.isConnectionHidden(state, group, prerequisiteId, quest.questId()) && !state.canEdit) {
                    continue;
                }

                List<CanvasPoint> path = ConnectionRenderer.connectionPath(
                        state,
                        0,
                        0,
                        prerequisite.centerX(),
                        prerequisite.centerY(),
                        quest.centerX(),
                        quest.centerY(),
                        ConnectionRenderer.isConnectionDirect(state, group, prerequisiteId, quest.questId())
                );
                if (nearPath(x, y, path, tolerance)) {
                    return new EdgeHit(prerequisiteId, quest.questId());
                }
            }
        }
        return null;
    }

    public static CanvasTextLayer hitTestCanvasText(TabletUiState state, int x, int y) {
        String group = selectedGroupName(state);
        List<CanvasTextLayer> texts = orderedCanvasTexts(state, group);
        for (int i = texts.size() - 1; i >= 0; i--) {
            CanvasTextLayer text = texts.get(i);
            int sw = CanvasGeometry.screenSpan(state, text.w());
            int sh = CanvasGeometry.screenSpan(state, text.h());
            double[] local = canvasTextLocalScreenPoint(state, text, x, y);
            if (local[0] >= 0 && local[0] <= sw && local[1] >= 0 && local[1] <= sh) {
                return text;
            }
        }
        return null;
    }

    public static CanvasTextLayer hitTestSelectedCanvasTextControls(TabletUiState state, int x, int y) {
        if (state.selectedCanvasTextId.isBlank()) {
            return null;
        }
        CanvasTextLayer text = state.canvasTextsByGroup.getOrDefault(selectedGroupName(state), List.of()).stream()
                .filter(entry -> entry.id().equals(state.selectedCanvasTextId))
                .findFirst()
                .orElse(null);
        if (text == null) {
            return null;
        }
        if (isCanvasTextResizeHandleHit(state, text, x, y) || isCanvasTextRotateHandleHit(state, text, x, y)) {
            return text;
        }
        int sw = CanvasGeometry.screenSpan(state, text.w());
        int sh = CanvasGeometry.screenSpan(state, text.h());
        double[] local = canvasTextLocalScreenPoint(state, text, x, y);
        return local[0] >= -3 && local[0] <= sw + 3 && local[1] >= -3 && local[1] <= sh + 3 ? text : null;
    }

    public static boolean isCanvasTextResizeHandleHit(TabletUiState state, CanvasTextLayer text, int x, int y) {
        return CanvasElementSelectionSlot.resizeHandleHit(state, text.x(), text.y(), text.w(), text.h(), text.rotation(), x, y);
    }

    public static boolean isCanvasTextRotateHandleHit(TabletUiState state, CanvasTextLayer text, int x, int y) {
        return CanvasElementSelectionSlot.rotateHandleHit(state, text.x(), text.y(), text.w(), text.h(), text.rotation(), x, y);
    }

    public static double[] canvasTextLocalScreenPoint(TabletUiState state, CanvasTextLayer text, int x, int y) {
        int sx = CanvasGeometry.screenX(state, text.x());
        int sy = CanvasGeometry.screenY(state, text.y());
        int sw = CanvasGeometry.screenSpan(state, text.w());
        int sh = CanvasGeometry.screenSpan(state, text.h());
        double cx = sx + sw / 2.0;
        double cy = sy + sh / 2.0;
        double dx = x - cx;
        double dy = y - cy;
        double radians = Math.toRadians(-text.rotation());
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double localX = dx * cos - dy * sin + sw / 2.0;
        double localY = dx * sin + dy * cos + sh / 2.0;
        return new double[]{localX, localY};
    }

    public static int[] canvasTextMenuBounds(TabletUiState state, CanvasTextLayer text, int viewportW, int viewportH, int toolCount) {
        int sx = CanvasGeometry.screenX(state, text.x());
        int sy = CanvasGeometry.screenY(state, text.y());
        int sh = CanvasGeometry.screenSpan(state, text.h());
        int buttonW = 18;
        int usableColumns = Math.max(1, (Math.max(1, viewportW) - 12) / buttonW);
        int columns = Math.max(4, Math.min(toolCount, usableColumns));
        int rows = Math.max(1, (toolCount + columns - 1) / columns);
        int menuW = columns * buttonW + 4;
        int menuH = rows * 16 + 4;
        int x = Math.max(4, Math.min(sx, viewportW - menuW - 4));
        int below = sy + sh + 3;
        int above = sy - menuH - 3;
        int y = below + menuH <= viewportH - 4
                ? below
                : above >= 4
                ? above
                : Math.max(4, Math.min(below, viewportH - menuH - 4));
        return new int[]{x, y, menuW, menuH, buttonW, columns};
    }

    public static int[] canvasTextFontSizeSliderBounds(TabletUiState state, CanvasTextLayer text, int viewportW, int viewportH, int toolCount) {
        int[] menu = canvasTextMenuBounds(state, text, viewportW, viewportH, toolCount);
        int buttonW = menu[4];
        int columns = menu[5];
        int sliderX = menu[0] + toolX(7, columns, buttonW);
        int sliderY = menu[1] + toolY(7, columns) + 16 + FONT_SIZE_SLIDER_POPOVER_GAP;
        return new int[]{sliderX, sliderY, buttonW, FONT_SIZE_SLIDER_POPOVER_H};
    }

    public static boolean isCanvasImageResizeHandleHit(TabletUiState state, CanvasImageLayer image, int x, int y) {
        return CanvasElementSelectionSlot.resizeHandleHit(state, image.x(), image.y(), image.w(), image.h(), image.rotation(), x, y);
    }

    public static boolean isCanvasImageRotateHandleHit(TabletUiState state, CanvasImageLayer image, int x, int y) {
        return CanvasElementSelectionSlot.rotateHandleHit(state, image.x(), image.y(), image.w(), image.h(), image.rotation(), x, y);
    }

    public static double[] canvasImageLocalScreenPoint(TabletUiState state, CanvasImageLayer image, int x, int y) {
        int sx = CanvasGeometry.screenX(state, image.x());
        int sy = CanvasGeometry.screenY(state, image.y());
        int sw = CanvasGeometry.screenSpan(state, image.w());
        int sh = CanvasGeometry.screenSpan(state, image.h());
        double cx = sx + sw / 2.0;
        double cy = sy + sh / 2.0;
        double dx = x - cx;
        double dy = y - cy;
        double radians = Math.toRadians(-image.rotation());
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double localX = dx * cos - dy * sin + sw / 2.0;
        double localY = dx * sin + dy * cos + sh / 2.0;
        return new double[]{localX, localY};
    }

    private static List<CanvasImageLayer> orderedCanvasImages(TabletUiState state, String group) {
        List<CanvasImageLayer> images = new ArrayList<>(state.canvasImagesByGroup.getOrDefault(group, List.of()));
        List<String> order = state.canvasLayerOrderByGroup.getOrDefault(group, List.of());
        images.sort(Comparator.comparingInt(image -> CanvasLayerOrdering.layerIndex(order, CanvasLayerOrdering.imageKey(image.id()))));
        return images;
    }

    private static List<CanvasTextLayer> orderedCanvasTexts(TabletUiState state, String group) {
        List<CanvasTextLayer> texts = new ArrayList<>(state.canvasTextsByGroup.getOrDefault(group, List.of()));
        List<String> order = state.canvasLayerOrderByGroup.getOrDefault(group, List.of());
        texts.sort(Comparator.comparingInt(text -> CanvasLayerOrdering.layerIndex(order, CanvasLayerOrdering.textKey(text.id()))));
        return texts;
    }

    private static boolean nearHorizontal(int x, int y, int lineY, int x1, int x2, int tolerance) {
        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        return x >= minX - tolerance && x <= maxX + tolerance && Math.abs(y - lineY) <= tolerance;
    }

    private static boolean nearVertical(int x, int y, int lineX, int y1, int y2, int tolerance) {
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        return y >= minY - tolerance && y <= maxY + tolerance && Math.abs(x - lineX) <= tolerance;
    }

    private static boolean nearPath(int x, int y, List<CanvasPoint> path, int tolerance) {
        for (int i = 0; i + 1 < path.size(); i++) {
            CanvasPoint a = path.get(i);
            CanvasPoint b = path.get(i + 1);
            if (a.y == b.y && nearHorizontal(x, y, a.y, a.x, b.x, tolerance)) {
                return true;
            }
            if (a.x == b.x && nearVertical(x, y, a.x, a.y, b.y, tolerance)) {
                return true;
            }
            if (a.x != b.x && a.y != b.y && nearSegment(x, y, a.x, a.y, b.x, b.y, tolerance)) {
                return true;
            }
        }
        return false;
    }

    private static boolean nearSegment(int px, int py, int x1, int y1, int x2, int y2, int tolerance) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;
        if (lenSq <= 0.0) {
            return Math.abs(px - x1) <= tolerance && Math.abs(py - y1) <= tolerance;
        }
        double t = ((px - x1) * dx + (py - y1) * dy) / lenSq;
        t = Math.max(0.0, Math.min(1.0, t));
        double closestX = x1 + t * dx;
        double closestY = y1 + t * dy;
        double distX = px - closestX;
        double distY = py - closestY;
        return distX * distX + distY * distY <= tolerance * tolerance;
    }

    private static float clampZoom(float zoom) {
        if (Float.isNaN(zoom) || Float.isInfinite(zoom)) {
            return 1.0f;
        }
        return Math.max(0.5f, Math.min(3.0f, zoom));
    }

    private static int toolX(int index, int columns, int buttonWidth) {
        return 2 + (index % Math.max(1, columns)) * buttonWidth;
    }

    private static int toolY(int index, int columns) {
        return 2 + (index / Math.max(1, columns)) * 16;
    }
}
