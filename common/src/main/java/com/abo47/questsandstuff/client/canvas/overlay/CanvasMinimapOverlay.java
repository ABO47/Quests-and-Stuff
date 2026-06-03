package com.abo47.questsandstuff.client.canvas.overlay;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasMinimapController;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasMinimapGeometry;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterBackgroundTexture;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;

final class CanvasMinimapOverlay {
    private static final int MIN_QUEST_SIZE = 4;
    private static final int VISUAL_CONNECTION_GRID_STEP = 16;
    private static final float MINIMAP_CHEVRON_SCALE = 0.45f;
    private static final float BODY_REVEAL_START = 0.48f;
    private static final ResourceLocation DEFAULT_QUEST_BG = ResourceLocation.tryBuild("questsandstuff", "textures/gui/quest_backgrounds/default_quest_bg.png");

    private CanvasMinimapOverlay() {
    }

    static void render(
            CanvasViewport canvasViewport,
            TabletUiState state,
            List<QuestCardLayout> visibleCards,
            Map<String, QuestCardLayout> byQuestId
    ) {
        clearState(state);
        if (!QuestsAndStuffConfig.minimapEnabled()) {
            return;
        }
        CanvasMinimapGeometry.Layout hitLayout = CanvasMinimapGeometry.layout(canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), state.minimapCollapsed);
        applyLayout(state, hitLayout);
        if (hitLayout.panelW() <= 0 || hitLayout.panelH() <= 0) {
            return;
        }

        boolean animationsEnabled = QuestsAndStuffConfig.minimapAnimationsEnabled();
        boolean closing = CanvasMinimapController.isClosingAnimationRunning(state);
        if (hitLayout.collapsed() && !closing) {
            canvasViewport.addWidget(minimapWidget(canvasViewport, state, animationsEnabled, CanvasMinimapGeometry.layout(canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), false), hitLayout, null));
            return;
        }

        CanvasMinimapGeometry.Layout layout = hitLayout.collapsed()
                ? CanvasMinimapGeometry.layout(canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), false)
                : hitLayout;
        String group = selectedGroupName(state);
        CanvasMinimapGeometry.WorldBounds world = CanvasMinimapGeometry.worldBounds(state, visibleCards);
        CanvasMinimapGeometry.Projection projection = CanvasMinimapGeometry.projection(layout, world);
        if (!state.minimapCollapsed) {
            applyProjection(state, projection);
        }

        MiniSnapshot snapshot = snapshot(state, group, visibleCards, byQuestId, projection);
        CanvasMinimapGeometry.Layout collapsedLayout = CanvasMinimapGeometry.layout(canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), true);
        canvasViewport.addWidget(minimapWidget(canvasViewport, state, animationsEnabled, layout, collapsedLayout, snapshot));
    }

    private static WidgetGroup minimapWidget(
            CanvasViewport canvasViewport,
            TabletUiState state,
            boolean animationsEnabled,
            CanvasMinimapGeometry.Layout layout,
            CanvasMinimapGeometry.Layout collapsedLayout,
            MiniSnapshot snapshot
    ) {
        return new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int originX = getPositionX();
                int originY = getPositionY();
                float openProgress = minimapOpenProgress(state, animationsEnabled);
                float holderProgress = stagedProgress(openProgress, 0.0f, BODY_REVEAL_START);
                float bodyProgress = stagedProgress(openProgress, BODY_REVEAL_START, 1.0f);
                drawPanel(graphics, originX, originY, layout, collapsedLayout, holderProgress, bodyProgress, mouseX, mouseY);
                if (snapshot != null && bodyProgress > 0.02f) {
                    int clipW = Math.max(1, Math.round((layout.panelW() - layout.toggleW()) * bodyProgress));
                    int clipX = layout.toggleX() - clipW;
                    CanvasViewportScissor.draw(
                            graphics,
                            originX + clipX,
                            originY + layout.panelY(),
                            clipW,
                            layout.panelH(),
                            () -> drawSnapshot(graphics, state, snapshot, originX, originY, mouseX, mouseY, partialTicks)
                    );
                }
            }
        };
    }

    private static float minimapOpenProgress(TabletUiState state, boolean animationsEnabled) {
        if (!animationsEnabled) {
            return state.minimapCollapsed ? 0.0f : 1.0f;
        }
        return UiAnimationProgress.openProgress(
                !state.minimapCollapsed,
                state.minimapAnimationFromCollapsed,
                state.minimapAnimationStartMs,
                CanvasMinimapController.ANIMATION_MS
        );
    }

    private static MiniSnapshot snapshot(
            TabletUiState state,
            String group,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            CanvasMinimapGeometry.Projection projection
    ) {
        Map<String, MiniRect> questBoxes = new HashMap<>();
        List<MiniRect> questRects = new ArrayList<>();
        boolean visualMode = QuestsAndStuffConfig.visualMinimapEnabled();
        for (QuestCardLayout card : cards) {
            MiniRect box = projectRect(
                    projection,
                    card.visualLogicalX(),
                    card.visualLogicalY(),
                    card.logicalWidth(),
                    card.logicalHeight(),
                    MIN_QUEST_SIZE,
                    questColor(state, card),
                    visualMode ? visualCardAlpha(state, card) : cardAlpha(state, card),
                    card.questId(),
                    card.tag()
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
                        hidden ? 70 : 190,
                        ConnectionRenderer.isConnectionDirect(state, group, sourceId, target.questId())
                ));
            }
        }

        return new MiniSnapshot(List.copyOf(questRects), List.copyOf(connections), projection);
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

    private static int visualCardAlpha(TabletUiState state, QuestCardLayout card) {
        CompoundTag tag = card.tag();
        if (state.canEdit && tag.getBoolean("visual_hidden") && !tag.getBoolean("completed")) {
            return 130;
        }
        return 255;
    }

    private static MiniRect projectViewport(TabletUiState state, CanvasMinimapGeometry.Projection projection) {
        double left = CanvasCameraController.screenToLogicalX(state, state.canvasContentX, true);
        double top = CanvasCameraController.screenToLogicalY(state, state.canvasContentY, true);
        double right = CanvasCameraController.screenToLogicalX(state, state.canvasContentX + Math.max(1, state.canvasContentW), true);
        double bottom = CanvasCameraController.screenToLogicalY(state, state.canvasContentY + Math.max(1, state.canvasContentH), true);
        int x1 = clamp(CanvasMinimapGeometry.mapX(projection, left), projection.drawX(), projection.drawX() + projection.drawW());
        int y1 = clamp(CanvasMinimapGeometry.mapY(projection, top), projection.drawY(), projection.drawY() + projection.drawH());
        int x2 = clamp(CanvasMinimapGeometry.mapX(projection, right), projection.drawX(), projection.drawX() + projection.drawW());
        int y2 = clamp(CanvasMinimapGeometry.mapY(projection, bottom), projection.drawY(), projection.drawY() + projection.drawH());
        int x = Math.min(x1, x2);
        int y = Math.min(y1, y2);
        return new MiniRect(x, y, Math.max(1, Math.abs(x2 - x1)), Math.max(1, Math.abs(y2 - y1)), ModColors.TEXT_PRIMARY, 230, null, null);
    }

    private static MiniRect projectRect(
            CanvasMinimapGeometry.Projection projection,
            int logicalX,
            int logicalY,
            int logicalW,
            int logicalH,
            int minSize,
            int color,
            int alpha,
            String questId,
            CompoundTag tag
    ) {
        int x = clamp(CanvasMinimapGeometry.mapX(projection, logicalX), projection.drawX(), projection.drawX() + projection.drawW() - 1);
        int y = clamp(CanvasMinimapGeometry.mapY(projection, logicalY), projection.drawY(), projection.drawY() + projection.drawH() - 1);
        int w = Math.max(minSize, Math.round(Math.max(1, logicalW) * projection.scale()));
        int h = Math.max(minSize, Math.round(Math.max(1, logicalH) * projection.scale()));
        w = Math.max(1, Math.min(w, projection.drawX() + projection.drawW() - x));
        h = Math.max(1, Math.min(h, projection.drawY() + projection.drawH() - y));
        return new MiniRect(x, y, w, h, color, alpha, questId, tag);
    }

    private static void drawPanel(
            GuiGraphics graphics,
            int originX,
            int originY,
            CanvasMinimapGeometry.Layout layout,
            CanvasMinimapGeometry.Layout collapsedLayout,
            float holderProgress,
            float bodyProgress,
            int mouseX,
            int mouseY
    ) {
        int handleX = originX + layout.toggleX();
        int handleY = originY + UiAnimationProgress.interpolate(collapsedLayout.toggleY(), layout.toggleY(), holderProgress);
        int handleW = layout.toggleW();
        int handleH = UiAnimationProgress.interpolate(collapsedLayout.toggleH(), layout.toggleH(), holderProgress);
        int visibleBodyW = Math.round((layout.panelW() - layout.toggleW()) * bodyProgress);
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

    private static float stagedProgress(float progress, float start, float end) {
        if (end <= start) {
            return progress >= end ? 1.0f : 0.0f;
        }
        float staged = (progress - start) / (end - start);
        staged = Math.max(0.0f, Math.min(1.0f, staged));
        return staged * staged * (3.0f - 2.0f * staged);
    }

    private static void drawSnapshot(GuiGraphics graphics, TabletUiState state, MiniSnapshot snapshot, int originX, int originY, int mouseX, int mouseY, float partialTicks) {
        boolean visualMode = QuestsAndStuffConfig.visualMinimapEnabled();
        for (MiniConnection connection : snapshot.connections()) {
            if (visualMode) {
                drawMiniChevrons(graphics, connection, originX, originY);
            } else {
                drawMiniLine(
                        graphics,
                        originX + connection.x1(),
                        originY + connection.y1(),
                        originX + connection.x2(),
                        originY + connection.y2(),
                        withAlpha(connection.color(), connection.alpha())
                );
            }
        }
        for (MiniRect quest : snapshot.quests()) {
            if (visualMode) {
                drawQuestPreview(graphics, state, quest, originX, originY, mouseX, mouseY, partialTicks);
            } else {
                drawQuestBox(graphics, originX + quest.x(), originY + quest.y(), quest.w(), quest.h(), quest.color(), quest.alpha());
            }
        }
        MiniRect viewport = projectViewport(state, snapshot.projection());
        drawBorder(graphics, originX + viewport.x(), originY + viewport.y(), viewport.w(), viewport.h(), withAlpha(viewport.color(), viewport.alpha()));
    }

    private static void drawQuestPreview(GuiGraphics graphics, TabletUiState state, MiniRect quest, int originX, int originY, int mouseX, int mouseY, float partialTicks) {
        CompoundTag tag = quest.tag();
        int x = originX + quest.x();
        int y = originY + quest.y();
        if (tag == null) {
            drawQuestBox(graphics, x, y, quest.w(), quest.h(), quest.color(), quest.alpha());
            return;
        }

        drawQuestBackground(graphics, tag, x, y, quest.w(), quest.h(), mouseX, mouseY, quest.alpha());
        int min = Math.min(quest.w(), quest.h());
        int pad = Math.max(1, Math.round(min * 0.16f));
        int iconSize = Math.max(1, min - pad * 2);
        int iconX = x + (quest.w() - iconSize) / 2;
        int iconY = y + (quest.h() - iconSize) / 2;
        DisplayIconWidget.drawIcon(graphics, mouseX, mouseY, iconX, iconY, iconSize, iconSize, tag.getString("icon"), partialTicks, quest.alpha());

        if (state.canEdit && tag.getBoolean("visual_hidden") && !tag.getBoolean("completed")) {
            graphics.fill(x, y, x + quest.w(), y + quest.h(), withAlpha(ModColors.SURFACE_BASE, Math.min(120, quest.alpha() / 2)));
        }
        if (quest.questId() != null && state.selectedQuestIds.contains(quest.questId())) {
            drawHighlightBorder(graphics, x, y, quest.w(), quest.h(), quest.alpha());
        }
    }

    private static void drawQuestBackground(GuiGraphics graphics, CompoundTag tag, int x, int y, int w, int h, int mouseX, int mouseY, int alpha) {
        String background = tag.getString("quest_background");
        if (background == null || background.isBlank() || QuestDisplay.DEFAULT_QUEST_BACKGROUND.equals(background)) {
            drawTextureAlpha(graphics, new ResourceTexture(DEFAULT_QUEST_BG).setColor(defaultQuestBackgroundTint(tag)), mouseX, mouseY, x, y, w, h, alpha);
            return;
        }
        IGuiTexture texture = chapterBackgroundTexture(background, tag.getBoolean("quest_background_grayscale"));
        if (texture == null) {
            drawTextureAlpha(graphics, new ResourceTexture(DEFAULT_QUEST_BG).setColor(defaultQuestBackgroundTint(tag)), mouseX, mouseY, x, y, w, h, alpha);
            return;
        }
        drawTextureAlpha(graphics, texture, mouseX, mouseY, x, y, w, h, alpha);
        int filter = questBackgroundFilter(tag, alpha);
        if ((filter >>> 24) != 0) {
            graphics.fill(x, y, x + w, y + h, filter);
        }
    }

    private static int defaultQuestBackgroundTint(CompoundTag tag) {
        if (ClientQuestCache.questLockedPreview(tag)) {
            return withAlpha(ModColors.TEXT_SECONDARY, 255);
        }
        if (tag.getBoolean("claimed")) {
            return withAlpha(ModColors.WARNING, 255);
        }
        if (tag.getBoolean("completed")) {
            return withAlpha(ModColors.SUCCESS, 255);
        }
        return tag.getBoolean("unlocked") ? withAlpha(ModColors.INTERACTIVE, 255) : withAlpha(ModColors.TEXT_SECONDARY, 255);
    }

    private static int questBackgroundFilter(CompoundTag tag, int alpha) {
        if (ClientQuestCache.questLockedPreview(tag)) {
            return scaledAlpha(ModColors.SURFACE_BASE, 138, alpha);
        }
        if (tag.getBoolean("claimed")) {
            return scaledAlpha(ModColors.WARNING, 94, alpha);
        }
        if (tag.getBoolean("completed")) {
            return scaledAlpha(ModColors.SUCCESS, 82, alpha);
        }
        return 0x00000000;
    }

    private static int scaledAlpha(int color, int baseAlpha, int alpha) {
        return withAlpha(color, Math.max(0, Math.min(255, Math.round(baseAlpha * (Math.max(0, Math.min(255, alpha)) / 255.0f)))));
    }

    private static void drawTextureAlpha(GuiGraphics graphics, IGuiTexture texture, int mouseX, int mouseY, int x, int y, int w, int h, int alpha) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        if (texture == null || safeAlpha <= 0) {
            return;
        }
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, safeAlpha / 255.0f);
        try {
            texture.draw(graphics, mouseX, mouseY, x, y, w, h);
        } finally {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    private static void drawQuestBox(GuiGraphics graphics, int x, int y, int w, int h, int color, int alpha) {
        if (w < 5 || h < 5) {
            graphics.fill(x, y, x + w, y + h, withAlpha(color, 255));
            return;
        }
        graphics.fill(x, y, x + w, y + h, withAlpha(ModColors.SURFACE_BASE, 255));
        graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, withAlpha(color, 255));
    }

    private static void drawMiniChevrons(GuiGraphics graphics, MiniConnection connection, int originX, int originY) {
        List<CanvasPoint> path = visualConnectionPath(
                Math.round(originX + connection.x1()),
                Math.round(originY + connection.y1()),
                Math.round(originX + connection.x2()),
                Math.round(originY + connection.y2()),
                connection.direct()
        );
        ConnectionRenderer.drawStaticChevrons(graphics, path, connection.color(), connection.alpha(), MINIMAP_CHEVRON_SCALE, -4096, -4096, 8192, 8192);
    }

    private static List<CanvasPoint> visualConnectionPath(int sourceX, int sourceY, int targetX, int targetY, boolean direct) {
        if (direct) {
            return List.of(new CanvasPoint(sourceX, sourceY), new CanvasPoint(targetX, targetY));
        }
        int midX = snapToStep((sourceX + targetX) / 2, VISUAL_CONNECTION_GRID_STEP);
        if (Math.abs(midX - sourceX) < VISUAL_CONNECTION_GRID_STEP / 2) {
            midX += targetX >= sourceX ? VISUAL_CONNECTION_GRID_STEP : -VISUAL_CONNECTION_GRID_STEP;
        }
        return List.of(
                new CanvasPoint(sourceX, sourceY),
                new CanvasPoint(midX, sourceY),
                new CanvasPoint(midX, targetY),
                new CanvasPoint(targetX, targetY)
        );
    }

    private static int snapToStep(int value, int step) {
        return Math.round(value / (float) Math.max(1, step)) * Math.max(1, step);
    }

    private static void drawHighlightBorder(GuiGraphics graphics, int x, int y, int w, int h, int alpha) {
        int color = withAlpha(ModColors.BORDER_ACCENT, Math.min(255, alpha));
        drawBorder(graphics, x, y, w, h, color);
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

    private record MiniSnapshot(List<MiniRect> quests, List<MiniConnection> connections, CanvasMinimapGeometry.Projection projection) {
    }

    private record MiniRect(int x, int y, int w, int h, int color, int alpha, String questId, CompoundTag tag) {
    }

    private record MiniConnection(float x1, float y1, float x2, float y2, int color, int alpha, boolean direct) {
    }
}
