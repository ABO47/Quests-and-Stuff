package com.abo47.questsandstuff.client.canvas.blueprint;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.canvas.render.CanvasImageLayerRenderer;
import com.abo47.questsandstuff.client.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextStyleSpan;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterBackgroundTexture;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

public final class CanvasBlueprintMiniRenderer {
    private static final ResourceLocation DEFAULT_QUEST_BG = ResourceLocation.tryBuild("questsandstuff", "textures/gui/quest_backgrounds/default_quest_bg.png");
    private static final int GRID_CONNECTION_STEP = 16;

    private CanvasBlueprintMiniRenderer() {
    }

    public static WidgetGroup previewWidget(int x, int y, int w, int h, CanvasBlueprint blueprint) {
        return previewWidget(x, y, w, h, blueprint, Set.of(), Set.of());
    }

    public static WidgetGroup previewWidget(int x, int y, int w, int h, CanvasBlueprint blueprint, Set<String> highlightedQuestIds, Set<String> highlightedConnectionKeys) {
        Set<String> safeQuestIds = safeSet(highlightedQuestIds);
        Set<String> safeConnectionKeys = safeSet(highlightedConnectionKeys);
        return previewWidget(x, y, w, h, blueprint, () -> safeQuestIds, () -> safeConnectionKeys);
    }

    public static WidgetGroup previewWidget(int x, int y, int w, int h, CanvasBlueprint blueprint, Supplier<Set<String>> highlightedQuestIds, Supplier<Set<String>> highlightedConnectionKeys) {
        return new WidgetGroup(x, y, w, h) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                drawPreview(
                        graphics,
                        mouseX,
                        mouseY,
                        getPositionX(),
                        getPositionY(),
                        getSizeWidth(),
                        getSizeHeight(),
                        blueprint,
                        safeSet(highlightedQuestIds == null ? null : highlightedQuestIds.get()),
                        safeSet(highlightedConnectionKeys == null ? null : highlightedConnectionKeys.get()),
                        partialTicks
                );
            }
        };
    }

    private static Set<String> safeSet(Set<String> values) {
        return values == null || values.isEmpty() ? Set.of() : Set.copyOf(values);
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
                int anchorScreenX = originX + CanvasGeometry.screenX(state, anchor.x());
                int anchorScreenY = originY + CanvasGeometry.screenY(state, anchor.y());
                drawBlueprint(graphics, mouseX, mouseY, anchorScreenX, anchorScreenY, CanvasRenderer.clampZoom(state.canvasZoom), blueprint, partialTicks, 150);
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
            int[] rotated = CanvasElementGeometry.logicalBoundsAtPivot(
                    image.x() - blueprint.originX(),
                    image.y() - blueprint.originY(),
                    image.w(),
                    image.h(),
                    image.pivotX(),
                    image.pivotY(),
                    image.rotation()
            );
            minX = Math.min(minX, rotated[0]);
            minY = Math.min(minY, rotated[1]);
            maxX = Math.max(maxX, rotated[2]);
            maxY = Math.max(maxY, rotated[3]);
        }
        for (CanvasTextLayer text : blueprint.texts()) {
            int[] rotated = CanvasElementGeometry.logicalBounds(text.x() - blueprint.originX(), text.y() - blueprint.originY(), text.w(), text.h(), text.rotation());
            minX = Math.min(minX, rotated[0]);
            minY = Math.min(minY, rotated[1]);
            maxX = Math.max(maxX, rotated[2]);
            maxY = Math.max(maxY, rotated[3]);
        }
        if (minX == Integer.MAX_VALUE) {
            return new BlueprintBounds(0, 0, 1, 1);
        }
        return new BlueprintBounds(minX, minY, Math.max(1, maxX - minX), Math.max(1, maxY - minY));
    }

    private static void drawPreview(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, int w, int h, CanvasBlueprint blueprint, Set<String> highlightedQuestIds, Set<String> highlightedConnectionKeys, float partialTicks) {
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
        drawBlueprint(
                graphics,
                mouseX,
                mouseY,
                Math.round(offsetX - bounds.minX() * scale),
                Math.round(offsetY - bounds.minY() * scale),
                scale,
                blueprint,
                highlightedQuestIds,
                highlightedConnectionKeys,
                partialTicks,
                255
        );
    }

    private static void drawBlueprint(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, float scale, CanvasBlueprint blueprint, float partialTicks, int alpha) {
        drawBlueprint(graphics, mouseX, mouseY, x, y, scale, blueprint, Set.of(), Set.of(), partialTicks, alpha);
    }

    private static void drawBlueprint(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, float scale, CanvasBlueprint blueprint, Set<String> highlightedQuestIds, Set<String> highlightedConnectionKeys, float partialTicks, int alpha) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        if (safeAlpha <= 0 || scale <= 0.0f) {
            return;
        }
        Map<String, BlueprintRect> questBoxes = questBoxes(blueprint);
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0f);
        graphics.pose().scale(scale, scale, 1.0f);
        drawConnections(graphics, blueprint, questBoxes, highlightedConnectionKeys, safeAlpha);
        for (String key : layerOrder(blueprint)) {
            if (key.startsWith(CanvasLayerOrdering.IMAGE_PREFIX)) {
                CanvasImageLayer image = imageById(blueprint, key.substring(CanvasLayerOrdering.IMAGE_PREFIX.length()));
                if (image != null) {
                    drawImage(graphics, mouseX, mouseY, blueprint, image, safeAlpha);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.TEXT_PREFIX)) {
                CanvasTextLayer text = textById(blueprint, key.substring(CanvasLayerOrdering.TEXT_PREFIX.length()));
                if (text != null) {
                    drawText(graphics, blueprint, text, safeAlpha);
                }
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.QUEST_PREFIX)) {
                CanvasBlueprint.QuestEntry quest = questById(blueprint, key.substring(CanvasLayerOrdering.QUEST_PREFIX.length()));
                if (quest != null) {
                    BlueprintRect rect = questBoxes.get(quest.sourceId());
                    if (rect != null) {
                        drawQuest(graphics, mouseX, mouseY, quest, rect, highlightedQuestIds.contains(quest.sourceId()), partialTicks, safeAlpha);
                    }
                }
            }
        }
        graphics.pose().popPose();
    }

    private static Map<String, BlueprintRect> questBoxes(CanvasBlueprint blueprint) {
        Map<String, BlueprintRect> questBoxes = new HashMap<>();
        for (CanvasBlueprint.QuestEntry quest : blueprint.quests()) {
            int visualW = CanvasGeometry.visualLogicalWidth(quest.scale());
            int visualH = CanvasGeometry.visualLogicalHeight(quest.scale());
            int slotW = CanvasGeometry.slotSpanForVisualSize(visualW);
            int slotH = CanvasGeometry.slotSpanForVisualSize(visualH);
            int x = quest.sourceX() - blueprint.originX() + CanvasGeometry.visualInsetForSlot(slotW, visualW);
            int y = quest.sourceY() - blueprint.originY() + CanvasGeometry.visualInsetForSlot(slotH, visualH);
            questBoxes.put(quest.sourceId(), new BlueprintRect(x, y, visualW, visualH));
        }
        return questBoxes;
    }

    private static void drawConnections(GuiGraphics graphics, CanvasBlueprint blueprint, Map<String, BlueprintRect> questBoxes, Set<String> highlightedConnectionKeys, int alpha) {
        int connectionAlpha = Math.min(alpha, 210);
        for (CanvasBlueprint.QuestEntry target : blueprint.quests()) {
            QuestDefinition definition = target.definition();
            if (definition == null || !definition.settings().showPrerequisiteArrow()) {
                continue;
            }
            BlueprintRect targetBox = questBoxes.get(target.sourceId());
            if (targetBox == null) {
                continue;
            }
            for (String prerequisiteId : definition.prerequisites()) {
                BlueprintRect sourceBox = questBoxes.get(prerequisiteId);
                if (sourceBox == null) {
                    continue;
                }
                boolean direct = !"grid".equals(definition.connectionModes().get(prerequisiteId));
                boolean highlighted = highlightedConnectionKeys.contains(connectionKey(prerequisiteId, target.sourceId()));
                int color = highlighted ? ModColors.BORDER_ACCENT : definition.connectionColors().getOrDefault(prerequisiteId, ModColors.TEXT_SECONDARY);
                int drawAlpha = highlighted ? Math.min(255, alpha) : connectionAlpha;
                List<CanvasPoint> path = connectionPath(sourceBox, targetBox, direct);
                ConnectionRenderer.drawStaticChevrons(graphics, path, color, drawAlpha, -4096, -4096, 8192, 8192);
            }
        }
    }

    private static String connectionKey(String sourceQuestId, String targetQuestId) {
        return sourceQuestId + "->" + targetQuestId;
    }

    private static List<CanvasPoint> connectionPath(BlueprintRect source, BlueprintRect target, boolean direct) {
        int sourceX = source.centerX();
        int sourceY = source.centerY();
        int targetX = target.centerX();
        int targetY = target.centerY();
        if (direct) {
            return List.of(new CanvasPoint(sourceX, sourceY), new CanvasPoint(targetX, targetY));
        }
        int midX = CanvasGeometry.snapValueToGrid((sourceX + targetX) / 2, GRID_CONNECTION_STEP);
        if (Math.abs(midX - sourceX) < GRID_CONNECTION_STEP / 2) {
            midX += targetX >= sourceX ? GRID_CONNECTION_STEP : -GRID_CONNECTION_STEP;
        }
        return List.of(
                new CanvasPoint(sourceX, sourceY),
                new CanvasPoint(midX, sourceY),
                new CanvasPoint(midX, targetY),
                new CanvasPoint(targetX, targetY)
        );
    }

    private static void drawQuest(GuiGraphics graphics, int mouseX, int mouseY, CanvasBlueprint.QuestEntry quest, BlueprintRect rect, boolean highlighted, float partialTicks, int alpha) {
        QuestDefinition definition = quest.definition();
        QuestDisplay display = definition == null ? QuestDisplay.DEFAULT : definition.display();
        boolean gated = definition != null && !definition.prerequisites().isEmpty();
        drawQuestBackground(graphics, mouseX, mouseY, display, rect, gated, alpha);
        int min = Math.min(rect.w(), rect.h());
        int pad = Math.max(1, Math.round(min * 0.16f));
        int iconSize = Math.max(1, min - pad * 2);
        int iconX = rect.x() + (rect.w() - iconSize) / 2;
        int iconY = rect.y() + (rect.h() - iconSize) / 2;
        DisplayIconWidget.drawIcon(graphics, mouseX, mouseY, iconX, iconY, iconSize, iconSize, display.icon(), partialTicks, alpha);
        if (display.visualHidden() || gated) {
            graphics.fill(rect.x(), rect.y(), rect.x() + rect.w(), rect.y() + rect.h(), withAlpha(ModColors.SURFACE_BASE, Math.min(130, alpha / 2)));
        }
        if (highlighted) {
            drawHighlightBorder(graphics, rect, alpha);
        }
    }

    private static void drawHighlightBorder(GuiGraphics graphics, BlueprintRect rect, int alpha) {
        int color = withAlpha(ModColors.BORDER_ACCENT, Math.min(255, alpha));
        graphics.fill(rect.x() - 2, rect.y() - 2, rect.x() + rect.w() + 2, rect.y(), color);
        graphics.fill(rect.x() - 2, rect.y() + rect.h(), rect.x() + rect.w() + 2, rect.y() + rect.h() + 2, color);
        graphics.fill(rect.x() - 2, rect.y(), rect.x(), rect.y() + rect.h(), color);
        graphics.fill(rect.x() + rect.w(), rect.y(), rect.x() + rect.w() + 2, rect.y() + rect.h(), color);
    }

    private static void drawQuestBackground(GuiGraphics graphics, int mouseX, int mouseY, QuestDisplay display, BlueprintRect rect, boolean gated, int alpha) {
        String background = display.questBackground();
        if (background == null || background.isBlank() || QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(background)) {
            new ResourceTexture(DEFAULT_QUEST_BG)
                    .setColor(withAlpha(display.visualHidden() || gated ? ModColors.TEXT_SECONDARY : ModColors.INTERACTIVE, alpha))
                    .draw(graphics, mouseX, mouseY, rect.x(), rect.y(), rect.w(), rect.h());
            return;
        }
        IGuiTexture texture = chapterBackgroundTexture(background, display.questBackgroundGrayscale());
        if (texture == null) {
            new ResourceTexture(DEFAULT_QUEST_BG)
                    .setColor(withAlpha(ModColors.INTERACTIVE, alpha))
                    .draw(graphics, mouseX, mouseY, rect.x(), rect.y(), rect.w(), rect.h());
            return;
        }
        drawTextureAlpha(graphics, texture, mouseX, mouseY, rect.x(), rect.y(), rect.w(), rect.h(), alpha);
    }

    private static void drawImage(GuiGraphics graphics, int mouseX, int mouseY, CanvasBlueprint blueprint, CanvasImageLayer image, int alpha) {
        int x = image.x() - blueprint.originX();
        int y = image.y() - blueprint.originY();
        withShaderAlpha(alpha, () -> CanvasImageLayerRenderer.draw(graphics, mouseX, mouseY, image, x, y, image.w(), image.h(), image.pivotX(), image.pivotY()));
    }

    private static void drawText(GuiGraphics graphics, CanvasBlueprint blueprint, CanvasTextLayer text, int alpha) {
        CanvasTextLayer drawText = textWithAlpha(text, alpha);
        int x = drawText.x() - blueprint.originX();
        int y = drawText.y() - blueprint.originY();
        graphics.pose().pushPose();
        graphics.pose().translate(
                x + CanvasElementGeometry.defaultPivot(drawText.w()),
                y + CanvasElementGeometry.defaultPivot(drawText.h()),
                0.0f
        );
        graphics.pose().mulPose(new Quaternionf().rotationXYZ(0.0f, 0.0f, (float) Math.toRadians(drawText.rotation())));
        CanvasTextRenderer.drawTextLayer(graphics, null, drawText, drawText.w(), drawText.h(), false);
        graphics.pose().popPose();
    }

    private static CanvasTextLayer textWithAlpha(CanvasTextLayer text, int alpha) {
        if (alpha >= 255) {
            return text;
        }
        List<CanvasTextStyleSpan> spans = new ArrayList<>();
        for (CanvasTextStyleSpan span : text.spans()) {
            spans.add(new CanvasTextStyleSpan(span.start(), span.end(), span.style(), withAlpha(span.color(), alpha)));
        }
        return new CanvasTextLayer(
                text.id(),
                text.text(),
                text.x(),
                text.y(),
                text.w(),
                text.h(),
                text.rotation(),
                text.align(),
                text.style(),
                withAlpha(text.color(), alpha),
                text.fontSize(),
                spans
        );
    }

    private static void drawTextureAlpha(GuiGraphics graphics, IGuiTexture texture, int mouseX, int mouseY, int x, int y, int width, int height, int alpha) {
        withShaderAlpha(alpha, () -> texture.draw(graphics, mouseX, mouseY, x, y, width, height));
    }

    private static void withShaderAlpha(int alpha, Runnable draw) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        if (safeAlpha <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, safeAlpha / 255.0f);
        try {
            draw.run();
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private static List<String> layerOrder(CanvasBlueprint blueprint) {
        if (blueprint.layerOrder().isEmpty()) {
            return defaultOrder(blueprint);
        }
        return blueprint.layerOrder();
    }

    private static List<String> defaultOrder(CanvasBlueprint blueprint) {
        List<String> order = new ArrayList<>();
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

    public record BlueprintBounds(int minX, int minY, int width, int height) {
    }

    private record BlueprintRect(int x, int y, int w, int h) {
        int centerX() {
            return x + w / 2;
        }

        int centerY() {
            return y + h / 2;
        }
    }
}
