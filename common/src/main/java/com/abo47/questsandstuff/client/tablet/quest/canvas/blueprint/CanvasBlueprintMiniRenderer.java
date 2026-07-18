package com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

import org.joml.Quaternionf;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasImageLayerRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTextRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.QuestCardBackgroundRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.QuestMiniCardRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextStyleSpan;

import static com.abo47.questsandstuff.client.tablet.theme.render.SurfaceFactory.withAlpha;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterBackgroundTexture;

public final class CanvasBlueprintMiniRenderer {
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
                CanvasBlueprint blueprint = CanvasBlueprintStore.read(state.canvas.blueprintPlacement.asset());
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
                drawBlueprint(graphics, mouseX, mouseY, anchorScreenX, anchorScreenY, CanvasRenderer.clampZoom(state.canvas.canvasZoom), blueprint, partialTicks, 150);
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
        for (CanvasBlueprint.ExclusiveChoiceEntry ec : blueprint.exclusiveChoices()) {
            BlueprintRect rect = ecRect(ec.sourceId(), ec.sourceX(), ec.sourceY(), ec.sourceW(), ec.sourceH(), blueprint.originX(), blueprint.originY());
            minX = Math.min(minX, rect.x());
            minY = Math.min(minY, rect.y());
            maxX = Math.max(maxX, rect.x() + rect.w());
            maxY = Math.max(maxY, rect.y() + rect.h());
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
                continue;
            }
            if (key.startsWith(CanvasLayerOrdering.EXCLUSIVE_CHOICE_PREFIX)) {
                CanvasBlueprint.ExclusiveChoiceEntry ec = exclusiveChoiceById(blueprint, key.substring(CanvasLayerOrdering.EXCLUSIVE_CHOICE_PREFIX.length()));
                if (ec != null) {
                    BlueprintRect rect = questBoxes.get(ec.sourceId());
                    if (rect != null) {
                        drawEc(graphics, mouseX, mouseY, ec, rect, highlightedQuestIds.contains(ec.sourceId()), safeAlpha);
                    }
                }
            }
        }
        graphics.pose().popPose();
    }

    private static CanvasBlueprint.ExclusiveChoiceEntry exclusiveChoiceById(CanvasBlueprint blueprint, String id) {
        for (CanvasBlueprint.ExclusiveChoiceEntry entry : blueprint.exclusiveChoices()) {
            if (entry.sourceId().equals(id)) {
                return entry;
            }
        }
        return null;
    }

    private static Map<String, BlueprintRect> questBoxes(CanvasBlueprint blueprint) {
        Map<String, BlueprintRect> questBoxes = new HashMap<>();
        for (CanvasBlueprint.QuestEntry quest : blueprint.quests()) {
            BlueprintRect rect = questRect(quest.sourceId(), quest.sourceX(), quest.sourceY(), quest.scale(), blueprint.originX(), blueprint.originY());
            questBoxes.put(quest.sourceId(), rect);
        }
        for (CanvasBlueprint.ExclusiveChoiceEntry ec : blueprint.exclusiveChoices()) {
            BlueprintRect rect = ecRect(ec.sourceId(), ec.sourceX(), ec.sourceY(), ec.sourceW(), ec.sourceH(), blueprint.originX(), blueprint.originY());
            questBoxes.put(ec.sourceId(), rect);
        }
        return questBoxes;
    }

    private static BlueprintRect questRect(String id, int sourceX, int sourceY, float scale, int originX, int originY) {
        int visualW = CanvasGeometry.visualLogicalWidth(scale);
        int visualH = CanvasGeometry.visualLogicalHeight(scale);
        int slotW = CanvasGeometry.slotSpanForVisualSize(visualW);
        int slotH = CanvasGeometry.slotSpanForVisualSize(visualH);
        int x = sourceX - originX + CanvasGeometry.visualInsetForSlot(slotW, visualW);
        int y = sourceY - originY + CanvasGeometry.visualInsetForSlot(slotH, visualH);
        return new BlueprintRect(x, y, visualW, visualH);
    }

    private static BlueprintRect ecRect(String id, int sourceX, int sourceY, int w, int h, int originX, int originY) {
        return new BlueprintRect(sourceX - originX, sourceY - originY, w, h);
    }

    private static void drawConnections(GuiGraphics graphics, CanvasBlueprint blueprint, Map<String, BlueprintRect> questBoxes, Set<String> highlightedConnectionKeys, int alpha) {
        int connectionAlpha = Math.min(alpha, 210);
        for (CanvasBlueprint.QuestEntry target : blueprint.quests()) {
            drawEntryConnections(graphics, questBoxes, highlightedConnectionKeys, connectionAlpha, target.sourceId(), target.definition(), alpha);
        }
        for (CanvasBlueprint.ExclusiveChoiceEntry target : blueprint.exclusiveChoices()) {
            drawEcConnections(graphics, questBoxes, highlightedConnectionKeys, connectionAlpha, target, alpha);
        }
    }

    private static void drawEntryConnections(GuiGraphics graphics, Map<String, BlueprintRect> questBoxes, Set<String> highlightedConnectionKeys, int connectionAlpha, String sourceId, QuestDefinition definition, int alpha) {
        if (definition == null || !definition.settings().showPrerequisiteArrow()) {
            return;
        }
        BlueprintRect targetBox = questBoxes.get(sourceId);
        if (targetBox == null) {
            return;
        }
        for (String prerequisiteId : definition.prerequisites()) {
            BlueprintRect sourceBox = questBoxes.get(prerequisiteId);
            if (sourceBox == null) {
                continue;
            }
            boolean direct = !"grid".equals(definition.connectionModes().get(prerequisiteId));
            boolean highlighted = highlightedConnectionKeys.contains(connectionKey(prerequisiteId, sourceId));
            int color = highlighted ? TabletColors.BORDER_ACCENT : definition.connectionColors().getOrDefault(prerequisiteId, TabletColors.TEXT_SECONDARY);
            int drawAlpha = highlighted ? Math.min(255, alpha) : connectionAlpha;
            String texture = definition.connectionTextures().getOrDefault(prerequisiteId, "");
            List<CanvasPoint> path = connectionPath(sourceBox, targetBox, direct);
            if (texture.isBlank()) {
                ConnectionRenderer.drawStaticChevrons(graphics, path, color, drawAlpha, -4096, -4096, 8192, 8192);
            } else {
                ConnectionRenderer.drawTexturedChevrons(graphics, path, color, drawAlpha, 1.0f, texture, -4096, -4096, 8192, 8192);
            }
        }
    }

    private static void drawEcConnections(GuiGraphics graphics, Map<String, BlueprintRect> questBoxes, Set<String> highlightedConnectionKeys, int connectionAlpha, CanvasBlueprint.ExclusiveChoiceEntry target, int alpha) {
        BlueprintRect targetBox = questBoxes.get(target.sourceId());
        if (targetBox == null) {
            return;
        }
        for (String prerequisiteId : target.prerequisites()) {
            BlueprintRect sourceBox = questBoxes.get(prerequisiteId);
            if (sourceBox == null) {
                continue;
            }
            drawOneEcConnection(graphics, highlightedConnectionKeys, connectionAlpha, target,
                    prerequisiteId, sourceBox, targetBox, connectionKey(prerequisiteId, target.sourceId()), alpha);
        }
        for (String connectedId : target.connections()) {
            BlueprintRect connectedBox = questBoxes.get(connectedId);
            if (connectedBox == null) {
                continue;
            }
            drawOneEcConnection(graphics, highlightedConnectionKeys, connectionAlpha, target,
                    connectedId, targetBox, connectedBox, connectionKey(target.sourceId(), connectedId), alpha);
        }
    }

    private static void drawOneEcConnection(GuiGraphics graphics, Set<String> highlightedConnectionKeys, int connectionAlpha, CanvasBlueprint.ExclusiveChoiceEntry target, String otherId, BlueprintRect sourceBox, BlueprintRect targetBox, String key, int alpha) {
        boolean highlighted = highlightedConnectionKeys.contains(key);
        int color = highlighted ? TabletColors.BORDER_ACCENT : target.connectionColors().getOrDefault(otherId, TabletColors.TEXT_SECONDARY);
        int drawAlpha = highlighted ? Math.min(255, alpha) : connectionAlpha;
        boolean direct = !"grid".equals(target.connectionModes().get(otherId));
        String texture = target.connectionTextures().getOrDefault(otherId, "");
        List<CanvasPoint> path = connectionPath(sourceBox, targetBox, direct);
        if (texture.isBlank()) {
            ConnectionRenderer.drawStaticChevrons(graphics, path, color, drawAlpha, -4096, -4096, 8192, 8192);
        } else {
            ConnectionRenderer.drawTexturedChevrons(graphics, path, color, drawAlpha, 1.0f, texture, -4096, -4096, 8192, 8192);
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
        QuestMiniCardRenderer.drawDisplayCard(graphics, display, gated, rect.x(), rect.y(), rect.w(), rect.h(), mouseX, mouseY, partialTicks, alpha, highlighted);
    }

    private static void drawEc(GuiGraphics graphics, int mouseX, int mouseY, CanvasBlueprint.ExclusiveChoiceEntry ec, BlueprintRect rect, boolean highlighted, int alpha) {
        String bg = QuestDisplay.normalizeQuestBackground(ec.background());
        if (!QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(bg)) {
            IGuiTexture bgTexture = chapterBackgroundTexture(bg, false);
            if (bgTexture != null) {
                withShaderAlpha(alpha, () -> bgTexture.draw(graphics, mouseX, mouseY, rect.x(), rect.y(), rect.w(), rect.h()));
            }
        } else {
            withShaderAlpha(alpha, () -> QuestCardBackgroundRenderer.EXCLUSIVE_CHOICE_TEXTURE.draw(graphics, mouseX, mouseY, rect.x(), rect.y(), rect.w(), rect.h()));
        }
        if (highlighted) {
            QuestMiniCardRenderer.drawHighlightBorder(graphics, rect.x(), rect.y(), rect.w(), rect.h(), alpha);
        }
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
        for (CanvasBlueprint.ExclusiveChoiceEntry ec : blueprint.exclusiveChoices()) {
            order.add(CanvasLayerOrdering.exclusiveChoiceKey(ec.sourceId()));
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
        SurfaceFactory.fill(withAlpha(TabletColors.TEXT_MUTED, 100)).draw(graphics, 0, 0, cx - 12, cy - 1, 24, 2);
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
