package com.abo47.questsandstuff.client.canvas.overlay;

import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasMinimapGeometry;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec2;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class CanvasMinimapOverlay {
    private static final long ANIMATION_MS = 180L;
    private static final int MIN_QUEST_SIZE = 4;
    private static final int MIN_ELEMENT_SIZE = 2;

    private CanvasMinimapOverlay() {
    }

    static void render(
            CanvasViewport canvasViewport,
            TabletUiState state,
            List<QuestCardLayout> visibleCards,
            Map<String, QuestCardLayout> byQuestId
    ) {
        clearState(state);
        CanvasMinimapGeometry.Layout hitLayout = CanvasMinimapGeometry.layout(canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), state.minimapCollapsed);
        applyLayout(state, hitLayout);
        if (hitLayout.panelW() <= 0 || hitLayout.panelH() <= 0) {
            return;
        }

        boolean closing = UiAnimationProgress.running(state.minimapAnimationStartMs, ANIMATION_MS)
                && state.minimapCollapsed
                && !state.minimapAnimationFromCollapsed;
        if (hitLayout.collapsed() && !closing) {
            canvasViewport.addWidget(minimapWidget(canvasViewport, state, CanvasMinimapGeometry.layout(canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), false), hitLayout, null));
            return;
        }

        CanvasMinimapGeometry.Layout layout = hitLayout.collapsed()
                ? CanvasMinimapGeometry.layout(canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), false)
                : hitLayout;
        String group = selectedGroupName(state);
        List<CanvasImageLayer> images = state.canvasImagesByGroup.getOrDefault(group, List.of());
        List<CanvasTextLayer> texts = state.canvasTextsByGroup.getOrDefault(group, List.of());
        CanvasMinimapGeometry.WorldBounds world = CanvasMinimapGeometry.worldBounds(state, visibleCards, images, texts);
        CanvasMinimapGeometry.Projection projection = CanvasMinimapGeometry.projection(layout, world);
        if (!state.minimapCollapsed) {
            applyProjection(state, projection);
        }

        MiniSnapshot snapshot = snapshot(state, group, visibleCards, byQuestId, images, texts, projection);
        CanvasMinimapGeometry.Layout collapsedLayout = CanvasMinimapGeometry.layout(canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), true);
        canvasViewport.addWidget(minimapWidget(canvasViewport, state, layout, collapsedLayout, snapshot));
    }

    private static WidgetGroup minimapWidget(
            CanvasViewport canvasViewport,
            TabletUiState state,
            CanvasMinimapGeometry.Layout layout,
            CanvasMinimapGeometry.Layout collapsedLayout,
            MiniSnapshot snapshot
    ) {
        return new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int originX = getPositionX();
                int originY = getPositionY();
                float openProgress = UiAnimationProgress.openProgress(
                        !state.minimapCollapsed,
                        state.minimapAnimationFromCollapsed,
                        state.minimapAnimationStartMs,
                        ANIMATION_MS
                );
                drawPanel(graphics, originX, originY, layout, collapsedLayout, openProgress, mouseX, mouseY);
                if (snapshot != null && openProgress > 0.02f) {
                    int clipW = Math.max(1, Math.round((layout.panelW() - layout.toggleW()) * openProgress));
                    int clipX = layout.toggleX() - clipW;
                    CanvasViewportScissor.draw(
                            graphics,
                            originX + clipX,
                            originY + layout.panelY(),
                            clipW,
                            layout.panelH(),
                            () -> drawSnapshot(graphics, snapshot, originX, originY)
                    );
                }
            }
        };
    }

    private static MiniSnapshot snapshot(
            TabletUiState state,
            String group,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            List<CanvasImageLayer> images,
            List<CanvasTextLayer> texts,
            CanvasMinimapGeometry.Projection projection
    ) {
        List<MiniRect> elements = new ArrayList<>();
        for (CanvasImageLayer image : images) {
            elements.add(projectRect(projection, image.x(), image.y(), image.w(), image.h(), MIN_ELEMENT_SIZE, ModColors.TEXT_MUTED, 110));
        }
        for (CanvasTextLayer text : texts) {
            elements.add(projectRect(projection, text.x(), text.y(), text.w(), text.h(), MIN_ELEMENT_SIZE, ModColors.TEXT_SECONDARY, 125));
        }

        Map<String, MiniRect> questBoxes = new HashMap<>();
        List<MiniRect> questRects = new ArrayList<>();
        for (QuestCardLayout card : cards) {
            MiniRect box = projectRect(
                    projection,
                    card.visualLogicalX(),
                    card.visualLogicalY(),
                    card.logicalWidth(),
                    card.logicalHeight(),
                    MIN_QUEST_SIZE,
                    questColor(state, card),
                    cardAlpha(state, card)
            );
            questBoxes.put(card.questId(), box);
            questRects.add(box);
        }

        List<MiniConnection> connections = new ArrayList<>();
        Set<String> rendered = new HashSet<>();
        for (QuestCardLayout target : cards) {
            CompoundTag targetTag = target.tag();
            if (!targetTag.getBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD)) {
                continue;
            }
            ListTag prerequisites = targetTag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
            for (int i = 0; i < prerequisites.size(); i++) {
                String sourceId = prerequisites.getString(i);
                QuestCardLayout source = byQuestId.get(sourceId);
                if (source == null) {
                    continue;
                }
                String edgeKey = ConnectionRenderer.edgeKey(sourceId, target.questId());
                if (!rendered.add(edgeKey)) {
                    continue;
                }
                boolean hidden = ConnectionRenderer.isConnectionHidden(state, group, sourceId, target.questId());
                if (hidden && !state.canEdit) {
                    continue;
                }
                MiniRect sourceBox = questBoxes.get(sourceId);
                MiniRect targetBox = questBoxes.get(target.questId());
                if (sourceBox == null || targetBox == null) {
                    continue;
                }
                float sourceCenterX = sourceBox.x() + sourceBox.w() / 2.0f;
                float sourceCenterY = sourceBox.y() + sourceBox.h() / 2.0f;
                float targetCenterX = targetBox.x() + targetBox.w() / 2.0f;
                float targetCenterY = targetBox.y() + targetBox.h() / 2.0f;
                connections.add(new MiniConnection(
                        sourceCenterX,
                        sourceCenterY,
                        targetCenterX,
                        targetCenterY,
                        ConnectionRenderer.connectionColor(state, group, sourceId, target.questId()),
                        hidden ? 70 : 190
                ));
            }
        }

        MiniRect viewport = projectViewport(state, projection);
        return new MiniSnapshot(List.copyOf(elements), List.copyOf(questRects), List.copyOf(connections), viewport);
    }

    private static int questColor(TabletUiState state, QuestCardLayout card) {
        if (state.selectedQuestIds.contains(card.questId())) {
            return ModColors.WARNING;
        }
        CompoundTag tag = card.tag();
        if (tag.getBoolean("claimed") || tag.getBoolean("completed")) {
            return ModColors.SUCCESS;
        }
        if (tag.getBoolean("unlocked")) {
            return ModColors.INTERACTIVE;
        }
        return ModColors.TEXT_MUTED;
    }

    private static int cardAlpha(TabletUiState state, QuestCardLayout card) {
        CompoundTag tag = card.tag();
        if (state.canEdit && tag.getBoolean("visual_hidden") && !tag.getBoolean("completed")) {
            return 115;
        }
        return 220;
    }

    private static MiniRect projectViewport(TabletUiState state, CanvasMinimapGeometry.Projection projection) {
        double left = CanvasGeometry.screenToLogicalX(state, state.canvasContentX);
        double top = CanvasGeometry.screenToLogicalY(state, state.canvasContentY);
        double right = CanvasGeometry.screenToLogicalX(state, state.canvasContentX + Math.max(1, state.canvasContentW));
        double bottom = CanvasGeometry.screenToLogicalY(state, state.canvasContentY + Math.max(1, state.canvasContentH));
        int x1 = clamp(CanvasMinimapGeometry.mapX(projection, left), projection.drawX(), projection.drawX() + projection.drawW());
        int y1 = clamp(CanvasMinimapGeometry.mapY(projection, top), projection.drawY(), projection.drawY() + projection.drawH());
        int x2 = clamp(CanvasMinimapGeometry.mapX(projection, right), projection.drawX(), projection.drawX() + projection.drawW());
        int y2 = clamp(CanvasMinimapGeometry.mapY(projection, bottom), projection.drawY(), projection.drawY() + projection.drawH());
        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        return new MiniRect(x, y, Math.max(1, Math.abs(x2 - x1)), Math.max(1, Math.abs(y2 - y1)), ModColors.TEXT_PRIMARY, 230);
    }

    private static MiniRect projectRect(
            CanvasMinimapGeometry.Projection projection,
            int logicalX,
            int logicalY,
            int logicalW,
            int logicalH,
            int minSize,
            int color,
            int alpha
    ) {
        int x = clamp(CanvasMinimapGeometry.mapX(projection, logicalX), projection.drawX(), projection.drawX() + projection.drawW() - 1);
        int y = clamp(CanvasMinimapGeometry.mapY(projection, logicalY), projection.drawY(), projection.drawY() + projection.drawH() - 1);
        int w = Math.max(minSize, Math.round(Math.max(1, logicalW) * projection.scale()));
        int h = Math.max(minSize, Math.round(Math.max(1, logicalH) * projection.scale()));
        w = Math.max(1, Math.min(w, projection.drawX() + projection.drawW() - x));
        h = Math.max(1, Math.min(h, projection.drawY() + projection.drawH() - y));
        return new MiniRect(x, y, w, h, color, alpha);
    }

    private static void drawPanel(
            GuiGraphics graphics,
            int originX,
            int originY,
            CanvasMinimapGeometry.Layout layout,
            CanvasMinimapGeometry.Layout collapsedLayout,
            float openProgress,
            int mouseX,
            int mouseY
    ) {
        int handleX = originX + layout.toggleX();
        int handleY = originY + UiAnimationProgress.interpolate(collapsedLayout.toggleY(), layout.toggleY(), openProgress);
        int handleW = layout.toggleW();
        int handleH = UiAnimationProgress.interpolate(collapsedLayout.toggleH(), layout.toggleH(), openProgress);
        int visibleBodyW = Math.round((layout.panelW() - layout.toggleW()) * openProgress);
        if (visibleBodyW > 0) {
            int bodyX = handleX - visibleBodyW;
            int bodyY = originY + layout.panelY();
            int bodyH = layout.panelH();
            graphics.fill(bodyX, bodyY, handleX, bodyY + bodyH, withAlpha(ModColors.SURFACE_BASE, 248));
            graphics.fill(bodyX, bodyY, handleX, bodyY + 1, withAlpha(ModColors.BORDER_BASE, 150));
            graphics.fill(bodyX, bodyY + bodyH - 1, handleX, bodyY + bodyH, withAlpha(ModColors.BORDER_BASE, 150));
            graphics.fill(bodyX, bodyY, bodyX + 1, bodyY + bodyH, withAlpha(ModColors.BORDER_BASE, 150));
        }
        drawHandle(graphics, handleX, handleY, handleW, handleH, mouseX, mouseY);
    }

    private static void drawSnapshot(GuiGraphics graphics, MiniSnapshot snapshot, int originX, int originY) {
        for (MiniRect element : snapshot.elements()) {
            drawFlatBox(graphics, originX + element.x(), originY + element.y(), element.w(), element.h(), element.color(), element.alpha());
        }
        for (MiniConnection connection : snapshot.connections()) {
            drawMiniLine(
                    graphics,
                    originX + connection.x1(),
                    originY + connection.y1(),
                    originX + connection.x2(),
                    originY + connection.y2(),
                    withAlpha(connection.color(), connection.alpha())
            );
        }
        for (MiniRect quest : snapshot.quests()) {
            drawQuestBox(graphics, originX + quest.x(), originY + quest.y(), quest.w(), quest.h(), quest.color(), quest.alpha());
        }
        MiniRect viewport = snapshot.viewport();
        drawBorder(graphics, originX + viewport.x(), originY + viewport.y(), viewport.w(), viewport.h(), withAlpha(viewport.color(), viewport.alpha()));
    }

    private static void drawQuestBox(GuiGraphics graphics, int x, int y, int w, int h, int color, int alpha) {
        if (w < 5 || h < 5) {
            graphics.fill(x, y, x + w, y + h, withAlpha(color, 255));
            return;
        }
        graphics.fill(x, y, x + w, y + h, withAlpha(ModColors.SURFACE_BASE, 255));
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, withAlpha(color, 255));
    }

    private static void drawFlatBox(GuiGraphics graphics, int x, int y, int w, int h, int color, int alpha) {
        graphics.fill(x, y, x + w, y + h, withAlpha(color, alpha));
    }

    private static void drawHandle(GuiGraphics graphics, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
        int fill = hovered ? withAlpha(ModColors.INTERACTIVE, 115) : withAlpha(ModColors.SURFACE_PANEL_ALT, 236);
        int border = hovered ? withAlpha(ModColors.BORDER_ACCENT, 235) : withAlpha(ModColors.BORDER_BASE, 180);
        graphics.fill(x, y, x + w, y + h, fill);
        drawBorder(graphics, x, y, w, h, border);
    }

    private static void drawMiniLine(GuiGraphics graphics, float x1, float y1, float x2, float y2, int color) {
        if (x1 == x2 && y1 == y2) {
            int x = Math.round(x1);
            int y = Math.round(y1);
            graphics.fill(x, y, x + 1, y + 1, color);
            return;
        }
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

    private static void drawBorder(GuiGraphics graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static void clearState(TabletUiState state) {
        state.minimapPanelX = 0;
        state.minimapPanelY = 0;
        state.minimapPanelW = 0;
        state.minimapPanelH = 0;
        state.minimapToggleX = 0;
        state.minimapToggleY = 0;
        state.minimapToggleW = 0;
        state.minimapToggleH = 0;
        state.minimapX = 0;
        state.minimapY = 0;
        state.minimapW = 0;
        state.minimapH = 0;
    }

    private static void applyLayout(TabletUiState state, CanvasMinimapGeometry.Layout layout) {
        state.minimapPanelX = layout.panelX();
        state.minimapPanelY = layout.panelY();
        state.minimapPanelW = layout.panelW();
        state.minimapPanelH = layout.panelH();
        state.minimapToggleX = layout.toggleX();
        state.minimapToggleY = layout.toggleY();
        state.minimapToggleW = layout.toggleW();
        state.minimapToggleH = layout.toggleH();
    }

    private static void applyProjection(TabletUiState state, CanvasMinimapGeometry.Projection projection) {
        state.minimapX = projection.drawX();
        state.minimapY = projection.drawY();
        state.minimapW = projection.drawW();
        state.minimapH = projection.drawH();
        state.minimapWorldMinX = projection.world().minX();
        state.minimapWorldMinY = projection.world().minY();
        state.minimapWorldWidth = projection.world().width();
        state.minimapWorldHeight = projection.world().height();
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record MiniSnapshot(List<MiniRect> elements, List<MiniRect> quests, List<MiniConnection> connections, MiniRect viewport) {
    }

    private record MiniRect(int x, int y, int w, int h, int color, int alpha) {
    }

    private record MiniConnection(float x1, float y1, float x2, float y2, int color, int alpha) {
    }
}
