package com.abo47.questsandstuff.client.tablet.quest.canvas.hit;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.client.tablet.controls.TextStyleButtons;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.EdgeHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementSelectionSlot;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;

public final class CanvasHitTester {
    private static final int TEXT_MENU_MARGIN = 4;

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
            CanvasImageLayer image = CanvasLayerMutations.effectiveCanvasImage(state, images.get(i));
            CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
            double[] local = canvasImageLocalScreenPoint(state, image, x, y);
            if (local[0] >= 0 && local[0] <= box.width() && local[1] >= 0 && local[1] <= box.height()) {
                return image;
            }
        }
        return null;
    }

    public static CanvasImageLayer hitTestSelectedCanvasImageControls(TabletUiState state, int x, int y) {
        if (state.canvas.canvasSelection.primaryImageId().isBlank()) {
            return null;
        }
        CanvasImageLayer image = state.canvas.canvasImagesByGroup.getOrDefault(selectedGroupName(state), List.of()).stream()
                .filter(entry -> entry.id().equals(state.canvas.canvasSelection.primaryImageId()))
                .findFirst()
                .orElse(null);
        if (image == null) {
            return null;
        }
        image = CanvasLayerMutations.effectiveCanvasImage(state, image);
        boolean gizmoSupported = CanvasTransformGizmo.supports(image.asset());
        if (gizmoSupported && CanvasTransformGizmo.controlHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch(), x, y)) {
            return image;
        }
        if (!gizmoSupported && (isCanvasImageResizeHandleHit(state, image, x, y) || isCanvasImageRotateHandleHit(state, image, x, y))) {
            return image;
        }
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
        double[] local = canvasImageLocalScreenPoint(state, image, x, y);
        return local[0] >= -3 && local[0] <= box.width() + 3 && local[1] >= -3 && local[1] <= box.height() + 3 ? image : null;
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
                if (ConnectionRenderer.isConnectionHidden(state, group, prerequisiteId, quest.questId(), quest.tag()) && !state.root.canEdit) {
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
                        ConnectionRenderer.isConnectionDirect(state, group, prerequisiteId, quest.questId(), quest.tag())
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
            CanvasTextLayer text = CanvasLayerMutations.effectiveCanvasText(state, texts.get(i));
            CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
            double[] local = canvasTextLocalScreenPoint(state, text, x, y);
            if (local[0] >= 0 && local[0] <= box.width() && local[1] >= 0 && local[1] <= box.height()) {
                return text;
            }
        }
        return null;
    }

    public static CanvasTextLayer hitTestSelectedCanvasTextControls(TabletUiState state, int x, int y) {
        if (state.canvas.canvasSelection.primaryTextId().isBlank()) {
            return null;
        }
        CanvasTextLayer text = state.canvas.canvasTextsByGroup.getOrDefault(selectedGroupName(state), List.of()).stream()
                .filter(entry -> entry.id().equals(state.canvas.canvasSelection.primaryTextId()))
                .findFirst()
                .orElse(null);
        if (text == null) {
            return null;
        }
        text = CanvasLayerMutations.effectiveCanvasText(state, text);
        if (isCanvasTextResizeHandleHit(state, text, x, y) || isCanvasTextRotateHandleHit(state, text, x, y)) {
            return text;
        }
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
        double[] local = canvasTextLocalScreenPoint(state, text, x, y);
        return local[0] >= -3 && local[0] <= box.width() + 3 && local[1] >= -3 && local[1] <= box.height() + 3 ? text : null;
    }

    public static boolean isCanvasTextResizeHandleHit(TabletUiState state, CanvasTextLayer text, int x, int y) {
        return CanvasElementSelectionSlot.resizeHandleHit(state, text.x(), text.y(), text.w(), text.h(), text.rotation(), x, y);
    }

    public static boolean isCanvasTextRotateHandleHit(TabletUiState state, CanvasTextLayer text, int x, int y) {
        return CanvasElementSelectionSlot.rotateHandleHit(state, text.x(), text.y(), text.w(), text.h(), text.rotation(), x, y);
    }

    public static double[] canvasTextLocalScreenPoint(TabletUiState state, CanvasTextLayer text, int x, int y) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
        double dx = x - box.centerX();
        double dy = y - box.centerY();
        double radians = Math.toRadians(-text.rotation());
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double localX = dx * cos - dy * sin - box.left();
        double localY = dx * sin + dy * cos - box.top();
        return new double[]{localX, localY};
    }

    public static int[] canvasTextMenuBounds(TabletUiState state, CanvasTextLayer text, int viewportW, int viewportH, int toolCount) {
        int menuW = TextStyleButtons.menuWidthForAvailable(Math.max(1, viewportW) - TEXT_MENU_MARGIN * 2);
        int columns = TextStyleButtons.columnsForWidth(menuW);
        int menuH = TextStyleButtons.menuHeightForColumns(columns);
        int buttonW = TextStyleButtons.buttonWidth(menuW, columns);
        CanvasTextLayer drawText = CanvasLayerMutations.effectiveCanvasText(state, text);
        int[] textBounds = rotatedTextScreenBounds(state, drawText);
        MenuCandidate best = bestMenuCandidate(textBounds, viewportW, viewportH, menuW, menuH);
        int x = best.x();
        int y = best.y();
        return new int[]{x, y, menuW, menuH, buttonW, columns};
    }

    public static boolean isCanvasTextOwnerHit(TabletUiState state, CanvasTextLayer text, int x, int y) {
        if (state == null || text == null) {
            return false;
        }
        CanvasTextLayer drawText = CanvasLayerMutations.effectiveCanvasText(state, text);
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBox(state, drawText.x(), drawText.y(), drawText.w(), drawText.h(), drawText.rotation());
        double[] local = canvasTextLocalScreenPoint(state, drawText, x, y);
        return local[0] >= -3 && local[0] <= box.width() + 3 && local[1] >= -3 && local[1] <= box.height() + 3;
    }

    public static boolean isCanvasImageResizeHandleHit(TabletUiState state, CanvasImageLayer image, int x, int y) {
        return CanvasElementSelectionSlot.resizeHandleHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), x, y);
    }

    public static boolean isCanvasImageRotateHandleHit(TabletUiState state, CanvasImageLayer image, int x, int y) {
        return CanvasElementSelectionSlot.rotateHandleHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), x, y);
    }

    public static double[] canvasImageLocalScreenPoint(TabletUiState state, CanvasImageLayer image, int x, int y) {
        CanvasElementGeometry.Box box = CanvasElementGeometry.screenBoxAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
        double dx = x - box.centerX();
        double dy = y - box.centerY();
        double radians = Math.toRadians(-image.rotation());
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        double localX = dx * cos - dy * sin - box.left();
        double localY = dx * sin + dy * cos - box.top();
        return new double[]{localX, localY};
    }

    private static List<CanvasImageLayer> orderedCanvasImages(TabletUiState state, String group) {
        List<CanvasImageLayer> images = new ArrayList<>(state.canvas.canvasImagesByGroup.getOrDefault(group, List.of()));
        List<String> order = state.canvas.canvasLayerOrderByGroup.getOrDefault(group, List.of());
        Map<String, Integer> indexes = CanvasLayerOrdering.indexMap(order);
        images.sort(Comparator.comparingInt(image -> CanvasLayerOrdering.layerIndex(indexes, CanvasLayerOrdering.imageKey(image.id()))));
        return images;
    }

    private static List<CanvasTextLayer> orderedCanvasTexts(TabletUiState state, String group) {
        List<CanvasTextLayer> texts = new ArrayList<>(state.canvas.canvasTextsByGroup.getOrDefault(group, List.of()));
        List<String> order = state.canvas.canvasLayerOrderByGroup.getOrDefault(group, List.of());
        Map<String, Integer> indexes = CanvasLayerOrdering.indexMap(order);
        texts.sort(Comparator.comparingInt(text -> CanvasLayerOrdering.layerIndex(indexes, CanvasLayerOrdering.textKey(text.id()))));
        return texts;
    }

    private static int[] rotatedTextScreenBounds(TabletUiState state, CanvasTextLayer text) {
        return CanvasElementGeometry.screenBounds(state, text.x(), text.y(), text.w(), text.h(), text.rotation());
    }

    private static MenuCandidate bestMenuCandidate(int[] avoidBounds, int viewportW, int viewportH, int menuW, int occupiedH) {
        int safeViewportW = Math.max(1, viewportW);
        int safeViewportH = Math.max(1, viewportH);
        int targetCenterX = (avoidBounds[0] + avoidBounds[2]) / 2;
        int targetCenterY = (avoidBounds[1] + avoidBounds[3]) / 2;
        int[][] rawCandidates = {
                {targetCenterX - menuW / 2, avoidBounds[1] - occupiedH - TextStyleButtons.TEXTBOX_FRAME_GAP},
                {targetCenterX - menuW / 2, avoidBounds[3] + TextStyleButtons.TEXTBOX_FRAME_GAP},
                {avoidBounds[0] - menuW - TextStyleButtons.TEXTBOX_FRAME_GAP, targetCenterY - occupiedH / 2},
                {avoidBounds[2] + TextStyleButtons.TEXTBOX_FRAME_GAP, targetCenterY - occupiedH / 2},
                {avoidBounds[0] - menuW - TextStyleButtons.TEXTBOX_FRAME_GAP, avoidBounds[1] - occupiedH - TextStyleButtons.TEXTBOX_FRAME_GAP},
                {avoidBounds[2] + TextStyleButtons.TEXTBOX_FRAME_GAP, avoidBounds[1] - occupiedH - TextStyleButtons.TEXTBOX_FRAME_GAP},
                {avoidBounds[0] - menuW - TextStyleButtons.TEXTBOX_FRAME_GAP, avoidBounds[3] + TextStyleButtons.TEXTBOX_FRAME_GAP},
                {avoidBounds[2] + TextStyleButtons.TEXTBOX_FRAME_GAP, avoidBounds[3] + TextStyleButtons.TEXTBOX_FRAME_GAP}
        };

        MenuCandidate best = null;
        for (int i = 0; i < rawCandidates.length; i++) {
            int rawX = rawCandidates[i][0];
            int rawY = rawCandidates[i][1];
            int x = clamp(rawX, TEXT_MENU_MARGIN, Math.max(TEXT_MENU_MARGIN, safeViewportW - menuW - TEXT_MENU_MARGIN));
            int y = clamp(rawY, TEXT_MENU_MARGIN, Math.max(TEXT_MENU_MARGIN, safeViewportH - occupiedH - TEXT_MENU_MARGIN));
            int overlap = overlapArea(x, y, menuW, occupiedH, avoidBounds);
            int overflow = overflowAmount(x, y, menuW, occupiedH, safeViewportW, safeViewportH);
            int shift = Math.abs(x - rawX) + Math.abs(y - rawY);
            long score = (long) overlap * 100_000L + (long) overflow * 1_000L + (long) shift * 10L + i;
            MenuCandidate candidate = new MenuCandidate(x, y, score);
            if (best == null || candidate.score() < best.score()) {
                best = candidate;
            }
        }
        return best == null ? new MenuCandidate(TEXT_MENU_MARGIN, TEXT_MENU_MARGIN, 0L) : best;
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(max, value));
    }

    private static int overlapArea(int x, int y, int w, int h, int[] bounds) {
        int left = Math.max(x, bounds[0]);
        int top = Math.max(y, bounds[1]);
        int right = Math.min(x + w, bounds[2]);
        int bottom = Math.min(y + h, bounds[3]);
        if (right <= left || bottom <= top) {
            return 0;
        }
        return (right - left) * (bottom - top);
    }

    private static int overflowAmount(int x, int y, int w, int h, int viewportW, int viewportH) {
        int overflow = 0;
        overflow += Math.max(0, TEXT_MENU_MARGIN - x);
        overflow += Math.max(0, TEXT_MENU_MARGIN - y);
        overflow += Math.max(0, x + w + TEXT_MENU_MARGIN - viewportW);
        overflow += Math.max(0, y + h + TEXT_MENU_MARGIN - viewportH);
        return overflow;
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

    private record MenuCandidate(int x, int y, long score) {
    }
}
