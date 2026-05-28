package com.abo47.questsandstuff.client.canvas.blueprint;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.client.utils.RenderBufferUtils;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.phys.Vec2;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class CanvasBlueprintMiniRenderer {
    private CanvasBlueprintMiniRenderer() {
    }

    public static WidgetGroup previewWidget(int x, int y, int w, int h, CanvasBlueprint blueprint) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawPreview(graphics, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight(), blueprint, 255);
            }
        };
    }

    public static WidgetGroup placementGhost(CanvasViewport canvasViewport, TabletUiState state) {
        return new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                CanvasBlueprint blueprint = CanvasBlueprintStore.read(state.blueprintPlacementAsset);
                if (blueprint.isEmpty()) {
                    return;
                }
                int originX = getPositionX();
                int originY = getPositionY();
                int localX = mouseX - originX;
                int localY = mouseY - originY;
                CanvasBlueprintController.PlacementAnchor anchor = CanvasBlueprintController.placementAnchor(state, blueprint, localX, localY);
                drawCanvasGhost(graphics, state, originX, originY, blueprint, anchor.x(), anchor.y());
            }
        };
    }

    public static BlueprintBounds bounds(CanvasBlueprint blueprint) {
        if (blueprint == null || blueprint.isEmpty()) {
            return new BlueprintBounds(0, 0, 1, 1);
        }
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (CanvasBlueprint.QuestEntry quest : blueprint.quests()) {
            int x = quest.sourceX() - blueprint.originX();
            int y = quest.sourceY() - blueprint.originY();
            int w = CanvasGeometry.slotSpanForVisualSize(CanvasGeometry.visualLogicalWidth(quest.scale()));
            int h = CanvasGeometry.slotSpanForVisualSize(CanvasGeometry.visualLogicalHeight(quest.scale()));
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + w);
            maxY = Math.max(maxY, y + h);
        }
        for (CanvasImageLayer image : blueprint.images()) {
            int x = image.x() - blueprint.originX();
            int y = image.y() - blueprint.originY();
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + image.w());
            maxY = Math.max(maxY, y + image.h());
        }
        for (CanvasTextLayer text : blueprint.texts()) {
            int x = text.x() - blueprint.originX();
            int y = text.y() - blueprint.originY();
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x + text.w());
            maxY = Math.max(maxY, y + text.h());
        }
        if (minX == Integer.MAX_VALUE) {
            return new BlueprintBounds(0, 0, 1, 1);
        }
        return new BlueprintBounds(minX, minY, Math.max(1, maxX - minX), Math.max(1, maxY - minY));
    }

    private static void drawPreview(GuiGraphics graphics, int x, int y, int w, int h, CanvasBlueprint blueprint, int alpha) {
        if (blueprint == null || blueprint.isEmpty()) {
            drawCenteredTextPlaceholder(graphics, x, y, w, h);
            return;
        }
        BlueprintBounds bounds = bounds(blueprint);
        int pad = 8;
        int drawW = Math.max(1, w - pad * 2);
        int drawH = Math.max(1, h - pad * 2);
        float scale = Math.min(drawW / (float) bounds.width(), drawH / (float) bounds.height());
        scale = Math.max(0.01f, scale);
        int offsetX = x + pad + Math.max(0, Math.round((drawW - bounds.width() * scale) / 2.0f));
        int offsetY = y + pad + Math.max(0, Math.round((drawH - bounds.height() * scale) / 2.0f));
        Map<String, MiniRect> questBoxes = drawLayeredPreview(graphics, blueprint, bounds, offsetX, offsetY, scale, alpha);
        drawPreviewConnections(graphics, blueprint, bounds, offsetX, offsetY, scale, questBoxes, alpha);
    }

    private static void drawCanvasGhost(GuiGraphics graphics, TabletUiState state, int originX, int originY, CanvasBlueprint blueprint, int anchorX, int anchorY) {
        Map<String, MiniRect> questBoxes = new HashMap<>();
        for (String key : layerOrder(blueprint)) {
            if (key.startsWith(CanvasLayerOrdering.IMAGE_PREFIX)) {
                CanvasImageLayer image = imageById(blueprint, key.substring(CanvasLayerOrdering.IMAGE_PREFIX.length()));
                if (image != null) {
                    int x = originX + CanvasGeometry.screenX(state, anchorX + image.x() - blueprint.originX());
                    int y = originY + CanvasGeometry.screenY(state, anchorY + image.y() - blueprint.originY());
                    int right = originX + CanvasGeometry.screenX(state, anchorX + image.x() - blueprint.originX() + image.w());
                    int bottom = originY + CanvasGeometry.screenY(state, anchorY + image.y() - blueprint.originY() + image.h());
                    drawFilledBox(graphics, x, y, Math.max(1, right - x), Math.max(1, bottom - y), ModColors.TEXT_SECONDARY, 86);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.TEXT_PREFIX)) {
                CanvasTextLayer text = textById(blueprint, key.substring(CanvasLayerOrdering.TEXT_PREFIX.length()));
                if (text != null) {
                    int x = originX + CanvasGeometry.screenX(state, anchorX + text.x() - blueprint.originX());
                    int y = originY + CanvasGeometry.screenY(state, anchorY + text.y() - blueprint.originY());
                    int right = originX + CanvasGeometry.screenX(state, anchorX + text.x() - blueprint.originX() + text.w());
                    int bottom = originY + CanvasGeometry.screenY(state, anchorY + text.y() - blueprint.originY() + text.h());
                    drawOutlinedBox(graphics, x, y, Math.max(1, right - x), Math.max(1, bottom - y), ModColors.WARNING, 96);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.QUEST_PREFIX)) {
                CanvasBlueprint.QuestEntry quest = questById(blueprint, key.substring(CanvasLayerOrdering.QUEST_PREFIX.length()));
                if (quest != null) {
                    int logicalX = anchorX + quest.sourceX() - blueprint.originX();
                    int logicalY = anchorY + quest.sourceY() - blueprint.originY();
                    int slotW = CanvasGeometry.slotSpanForVisualSize(CanvasGeometry.visualLogicalWidth(quest.scale()));
                    int slotH = CanvasGeometry.slotSpanForVisualSize(CanvasGeometry.visualLogicalHeight(quest.scale()));
                    int x = originX + CanvasGeometry.screenX(state, logicalX);
                    int y = originY + CanvasGeometry.screenY(state, logicalY);
                    int right = originX + CanvasGeometry.screenX(state, logicalX + slotW);
                    int bottom = originY + CanvasGeometry.screenY(state, logicalY + slotH);
                    MiniRect box = new MiniRect(x, y, Math.max(1, right - x), Math.max(1, bottom - y));
                    questBoxes.put(quest.sourceId(), box);
                    drawQuestBox(graphics, box.x(), box.y(), box.w(), box.h(), ModColors.INTERACTIVE, 118);
                }
            }
        }
        drawGhostConnections(graphics, blueprint, questBoxes);
    }

    private static Map<String, MiniRect> drawLayeredPreview(GuiGraphics graphics, CanvasBlueprint blueprint, BlueprintBounds bounds, int offsetX, int offsetY, float scale, int alpha) {
        Map<String, MiniRect> questBoxes = new HashMap<>();
        for (String key : layerOrder(blueprint)) {
            if (key.startsWith(CanvasLayerOrdering.IMAGE_PREFIX)) {
                CanvasImageLayer image = imageById(blueprint, key.substring(CanvasLayerOrdering.IMAGE_PREFIX.length()));
                if (image != null) {
                    MiniRect rect = miniRect(image.x() - blueprint.originX(), image.y() - blueprint.originY(), image.w(), image.h(), bounds, offsetX, offsetY, scale);
                    drawFilledBox(graphics, rect.x(), rect.y(), rect.w(), rect.h(), ModColors.TEXT_SECONDARY, Math.min(alpha, 120));
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.TEXT_PREFIX)) {
                CanvasTextLayer text = textById(blueprint, key.substring(CanvasLayerOrdering.TEXT_PREFIX.length()));
                if (text != null) {
                    MiniRect rect = miniRect(text.x() - blueprint.originX(), text.y() - blueprint.originY(), text.w(), text.h(), bounds, offsetX, offsetY, scale);
                    drawOutlinedBox(graphics, rect.x(), rect.y(), rect.w(), rect.h(), ModColors.WARNING, Math.min(alpha, 150));
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.QUEST_PREFIX)) {
                CanvasBlueprint.QuestEntry quest = questById(blueprint, key.substring(CanvasLayerOrdering.QUEST_PREFIX.length()));
                if (quest != null) {
                    int slotW = CanvasGeometry.slotSpanForVisualSize(CanvasGeometry.visualLogicalWidth(quest.scale()));
                    int slotH = CanvasGeometry.slotSpanForVisualSize(CanvasGeometry.visualLogicalHeight(quest.scale()));
                    MiniRect rect = miniRect(quest.sourceX() - blueprint.originX(), quest.sourceY() - blueprint.originY(), slotW, slotH, bounds, offsetX, offsetY, scale);
                    questBoxes.put(quest.sourceId(), rect);
                    drawQuestBox(graphics, rect.x(), rect.y(), rect.w(), rect.h(), ModColors.INTERACTIVE, Math.min(alpha, 210));
                }
            }
        }
        return questBoxes;
    }

    private static void drawPreviewConnections(GuiGraphics graphics, CanvasBlueprint blueprint, BlueprintBounds bounds, int offsetX, int offsetY, float scale, Map<String, MiniRect> questBoxes, int alpha) {
        drawConnections(graphics, blueprint, questBoxes, Math.min(alpha, 170));
    }

    private static void drawGhostConnections(GuiGraphics graphics, CanvasBlueprint blueprint, Map<String, MiniRect> questBoxes) {
        drawConnections(graphics, blueprint, questBoxes, 118);
    }

    private static void drawConnections(GuiGraphics graphics, CanvasBlueprint blueprint, Map<String, MiniRect> questBoxes, int alpha) {
        for (CanvasBlueprint.QuestEntry target : blueprint.quests()) {
            MiniRect targetBox = questBoxes.get(target.sourceId());
            if (targetBox == null) {
                continue;
            }
            for (String prerequisiteId : target.definition().prerequisites()) {
                MiniRect sourceBox = questBoxes.get(prerequisiteId);
                if (sourceBox == null) {
                    continue;
                }
                int color = target.definition().connectionColors().getOrDefault(prerequisiteId, ModColors.TEXT_SECONDARY);
                drawMiniLine(
                        graphics,
                        sourceBox.x() + sourceBox.w() / 2.0f,
                        sourceBox.y() + sourceBox.h() / 2.0f,
                        targetBox.x() + targetBox.w() / 2.0f,
                        targetBox.y() + targetBox.h() / 2.0f,
                        withAlpha(color, alpha)
                );
            }
        }
    }

    private static MiniRect miniRect(int x, int y, int w, int h, BlueprintBounds bounds, int offsetX, int offsetY, float scale) {
        return new MiniRect(
                offsetX + Math.round((x - bounds.minX()) * scale),
                offsetY + Math.round((y - bounds.minY()) * scale),
                Math.max(2, Math.round(Math.max(1, w) * scale)),
                Math.max(2, Math.round(Math.max(1, h) * scale))
        );
    }

    private static List<String> layerOrder(CanvasBlueprint blueprint) {
        if (blueprint.layerOrder().isEmpty()) {
            return defaultOrder(blueprint);
        }
        return blueprint.layerOrder();
    }

    private static List<String> defaultOrder(CanvasBlueprint blueprint) {
        java.util.ArrayList<String> order = new java.util.ArrayList<>();
        for (CanvasImageLayer image : blueprint.images()) {
            order.add(CanvasLayerOrdering.imageKey(image.id()));
        }
        for (CanvasTextLayer text : blueprint.texts()) {
            order.add(CanvasLayerOrdering.textKey(text.id()));
        }
        for (CanvasBlueprint.QuestEntry quest : blueprint.quests()) {
            order.add(CanvasLayerOrdering.questKey(quest.sourceId()));
        }
        return order;
    }

    private static CanvasBlueprint.QuestEntry questById(CanvasBlueprint blueprint, String id) {
        for (CanvasBlueprint.QuestEntry quest : blueprint.quests()) {
            if (quest.sourceId().equals(id)) {
                return quest;
            }
        }
        return null;
    }

    private static CanvasImageLayer imageById(CanvasBlueprint blueprint, String id) {
        for (CanvasImageLayer image : blueprint.images()) {
            if (image.id().equals(id)) {
                return image;
            }
        }
        return null;
    }

    private static CanvasTextLayer textById(CanvasBlueprint blueprint, String id) {
        for (CanvasTextLayer text : blueprint.texts()) {
            if (text.id().equals(id)) {
                return text;
            }
        }
        return null;
    }

    private static void drawCenteredTextPlaceholder(GuiGraphics graphics, int x, int y, int w, int h) {
        int cx = x + w / 2;
        int cy = y + h / 2;
        graphics.fill(cx - 12, cy - 1, cx + 12, cy + 1, withAlpha(ModColors.TEXT_MUTED, 100));
    }

    private static void drawQuestBox(GuiGraphics graphics, int x, int y, int w, int h, int color, int alpha) {
        drawFilledBox(graphics, x, y, w, h, ModColors.SURFACE_BASE, Math.min(220, alpha + 40));
        if (w > 3 && h > 3) {
            graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, withAlpha(color, alpha));
        }
    }

    private static void drawFilledBox(GuiGraphics graphics, int x, int y, int w, int h, int color, int alpha) {
        graphics.fill(x, y, x + Math.max(1, w), y + Math.max(1, h), withAlpha(color, alpha));
    }

    private static void drawOutlinedBox(GuiGraphics graphics, int x, int y, int w, int h, int color, int alpha) {
        int right = x + Math.max(1, w);
        int bottom = y + Math.max(1, h);
        int line = withAlpha(color, alpha);
        graphics.fill(x, y, right, y + 1, line);
        graphics.fill(x, bottom - 1, right, bottom, line);
        graphics.fill(x, y, x + 1, bottom, line);
        graphics.fill(right - 1, y, right, bottom, line);
    }

    private static void drawMiniLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, int color) {
        Tesselator tessellator = Tesselator.getInstance();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        RenderBufferUtils.drawColorLines(
                graphics.pose(),
                buffer,
                List.of(new Vec2(x1, y1), new Vec2(x2, y2)),
                color,
                color,
                0.55f
        );
        tessellator.end();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    public record BlueprintBounds(int minX, int minY, int width, int height) {
    }

    private record MiniRect(int x, int y, int w, int h) {
    }
}
