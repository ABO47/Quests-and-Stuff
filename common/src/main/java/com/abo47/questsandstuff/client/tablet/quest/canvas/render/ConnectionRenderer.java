package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayoutService;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMetadata;
import com.abo47.questsandstuff.quest.model.connection.QuestConnectionMode;
import com.abo47.questsandstuff.quest.sync.QuestSyncKeys;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

public final class ConnectionRenderer {
    private static final ResourceLocation CONNECTION_CHEVRON = ResourceLocation.tryBuild("questsandstuff", "textures/gui/chevron.png");
    private static final float CHEVRON_U0 = 50.0f / 256.0f;
    private static final float CHEVRON_U1 = 206.0f / 256.0f;

    private ConnectionRenderer() {
    }

    public static String edgeKey(String sourceQuestId, String targetQuestId) {
        return QuestConnectionMetadata.edgeKey(sourceQuestId, targetQuestId);
    }

    public static int connectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return connectionColor(state, group, sourceQuestId, targetQuestId, ClientQuestCache.quest(targetQuestId));
    }

    public static int connectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(QuestSyncKeys.Quest.CONNECTION_COLORS, Tag.TAG_COMPOUND)) {
            CompoundTag colorsTag = target.getCompound(QuestSyncKeys.Quest.CONNECTION_COLORS);
            if (colorsTag.contains(metadataKey, Tag.TAG_INT)) {
                return colorsTag.getInt(metadataKey);
            }
        }
        Map<String, Integer> colors = state.connectionColorsByGroup.get(group);
        if (colors == null) {
            return ModColors.TEXT_SECONDARY;
        }
        return colors.getOrDefault(edgeKey(sourceQuestId, targetQuestId), ModColors.TEXT_SECONDARY);
    }

    public static void setConnectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId, int color) {
        String key = edgeKey(sourceQuestId, targetQuestId);
        Map<String, Integer> colors = state.connectionColorsByGroup.computeIfAbsent(group, ignored -> new HashMap<>());
        colors.put(key, color);
    }

    public static boolean isConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return isConnectionHidden(state, group, sourceQuestId, targetQuestId, ClientQuestCache.quest(targetQuestId));
    }

    public static boolean isConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_LIST)) {
            ListTag hiddenTag = target.getList(QuestSyncKeys.Quest.HIDDEN_CONNECTIONS, Tag.TAG_STRING);
            for (int i = 0; i < hiddenTag.size(); i++) {
                if (metadataKey.equals(hiddenTag.getString(i))) {
                    return true;
                }
            }
        }
        Set<String> hidden = state.hiddenConnectionsByGroup.get(group);
        if (hidden == null || hidden.isEmpty()) {
            return false;
        }
        return hidden.contains(edgeKey(sourceQuestId, targetQuestId));
    }

    public static void setConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId, boolean hidden) {
        String key = edgeKey(sourceQuestId, targetQuestId);
        Set<String> groupHidden = state.hiddenConnectionsByGroup.computeIfAbsent(group, ignored -> new HashSet<>());
        if (hidden) {
            groupHidden.add(key);
        } else {
            groupHidden.remove(key);
            if (groupHidden.isEmpty()) {
                state.hiddenConnectionsByGroup.remove(group);
            }
        }
    }

    public static void toggleConnectionHidden(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        boolean hidden = isConnectionHidden(state, group, sourceQuestId, targetQuestId);
        setConnectionHidden(state, group, sourceQuestId, targetQuestId, !hidden);
    }

    public static boolean isConnectionDirect(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return isConnectionDirect(state, group, sourceQuestId, targetQuestId, ClientQuestCache.quest(targetQuestId));
    }

    public static boolean isConnectionDirect(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        String metadataKey = QuestConnectionMetadata.metadataKey(sourceQuestId);
        if (target != null && target.contains(QuestSyncKeys.Quest.CONNECTION_MODES, Tag.TAG_COMPOUND)) {
            CompoundTag modes = target.getCompound(QuestSyncKeys.Quest.CONNECTION_MODES);
            if (modes.contains(metadataKey, Tag.TAG_STRING)) {
                return QuestConnectionMode.fromSerializedName(modes.getString(metadataKey)) != QuestConnectionMode.GRID;
            }
        }
        Set<String> grid = state.gridConnectionsByGroup.get(group);
        return grid == null || !grid.contains(edgeKey(sourceQuestId, targetQuestId));
    }

    public static void toggleConnectionMode(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        String key = edgeKey(sourceQuestId, targetQuestId);
        Set<String> groupGrid = state.gridConnectionsByGroup.computeIfAbsent(group, ignored -> new HashSet<>());
        if (isConnectionDirect(state, group, sourceQuestId, targetQuestId)) {
            groupGrid.add(key);
        } else {
            groupGrid.remove(key);
        }
        if (groupGrid.isEmpty()) {
            state.gridConnectionsByGroup.remove(group);
        }
    }

    public static QuestConnectionMetadata connectionMetadata(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        return connectionMetadata(state, group, sourceQuestId, targetQuestId, ClientQuestCache.quest(targetQuestId));
    }

    public static QuestConnectionMetadata connectionMetadata(TabletUiState state, String group, String sourceQuestId, String targetQuestId, CompoundTag target) {
        boolean direct = isConnectionDirect(state, group, sourceQuestId, targetQuestId, target);
        return new QuestConnectionMetadata(
                sourceQuestId,
                targetQuestId,
                connectionColor(state, group, sourceQuestId, targetQuestId, target),
                direct ? QuestConnectionMode.DIRECT : QuestConnectionMode.GRID,
                isConnectionHidden(state, group, sourceQuestId, targetQuestId, target)
        );
    }

    public static void renderPrerequisiteConnections(
            WidgetGroup canvasViewport,
            TabletUiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = new ArrayList<>(prerequisiteConnectionLines(state, cards, byQuestId, viewportW, viewportH));
        lines.addAll(pendingConnectionLines(state, byQuestId, viewportW, viewportH));
        renderConnectionLines(canvasViewport, state, lines);
    }

    public static List<ConnectionLine> prerequisiteConnectionLines(
            TabletUiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = new ArrayList<>();
        Set<String> rendered = new HashSet<>();
        String group = selectedGroupName(state);

        for (QuestCardLayout quest : cards) {
            CompoundTag questTag = quest.tag();
            if (!questTag.getBoolean(QuestSettings.SHOW_PREREQUISITE_ARROW_FIELD)) {
                continue;
            }
            ListTag prerequisites = questTag.getList(QuestDefinition.PREREQUISITES_FIELD, Tag.TAG_STRING);
            for (int i = 0; i < prerequisites.size(); i++) {
                String prerequisiteId = prerequisites.getString(i);
                QuestCardLayout prerequisite = byQuestId.get(prerequisiteId);
                if (prerequisite == null) {
                    continue;
                }

                QuestConnectionMetadata metadata = connectionMetadata(state, group, prerequisiteId, quest.questId(), questTag);
                String edgeId = metadata.edgeKey();
                if (!rendered.add(edgeId)) {
                    continue;
                }
                if (!CanvasLayoutService.intersectsPanRenderWindow(prerequisite, viewportW, viewportH)
                        && !CanvasLayoutService.intersectsPanRenderWindow(quest, viewportW, viewportH)) {
                    continue;
                }
                boolean hidden = metadata.hidden();

                lines.add(new ConnectionLine(
                        edgeId,
                        metadata.sourceQuestId(),
                        metadata.targetQuestId(),
                        prerequisite.x(),
                        prerequisite.y(),
                        prerequisite.width(),
                        prerequisite.height(),
                        quest.x(),
                        quest.y(),
                        quest.width(),
                        quest.height(),
                        prerequisite.centerX(),
                        prerequisite.centerY(),
                        quest.centerX(),
                        quest.centerY(),
                        metadata.direct(),
                        false,
                        metadata.color(),
                        hidden,
                        hidden ? 64 : 245
                ));
            }
        }
        return lines;
    }

    public static List<String> prerequisiteConnectionLayerKeys(
            TabletUiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = prerequisiteConnectionLines(state, cards, byQuestId, viewportW, viewportH);
        List<String> keys = new ArrayList<>();
        for (ConnectionLine line : lines) {
            keys.add(CanvasLayerOrdering.connectionKey(line.edgeId()));
        }
        return keys;
    }

    public static void renderConnectionLayer(WidgetGroup canvasViewport, TabletUiState state, ConnectionLine line) {
        renderConnectionLines(canvasViewport, state, line == null ? List.of() : List.of(line));
    }

    public static void renderPendingConnections(
            WidgetGroup canvasViewport,
            TabletUiState state,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        renderConnectionLines(canvasViewport, state, pendingConnectionLines(state, byQuestId, viewportW, viewportH));
    }

    private static List<ConnectionLine> pendingConnectionLines(
            TabletUiState state,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = new ArrayList<>();
        if (state.canEdit) {
            Set<String> pendingSources = new HashSet<>(state.connectSourceQuestIds);
            if (!state.connectSourceQuestId.isBlank()) {
                pendingSources.add(state.connectSourceQuestId);
            }
            for (String sourceQuestId : pendingSources) {
                QuestCardLayout source = byQuestId.get(sourceQuestId);
                if (source != null && CanvasLayoutService.intersectsPanRenderWindow(source, viewportW, viewportH)) {
                    lines.add(new ConnectionLine(
                            "",
                            sourceQuestId,
                            sourceQuestId,
                            source.x(),
                            source.y(),
                            source.width(),
                            source.height(),
                            source.x(),
                            source.y(),
                            source.width(),
                            source.height(),
                            source.centerX(),
                            source.centerY(),
                            source.centerX(),
                            source.centerY(),
                            false,
                            true,
                            ModColors.TEXT_SECONDARY,
                            false,
                            245
                    ));
                }
            }
        }
        return lines;
    }

    private static void renderConnectionLines(WidgetGroup canvasViewport, TabletUiState state, List<ConnectionLine> lines) {
        if (lines.isEmpty()) {
            return;
        }
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int originX = getPositionX();
                int originY = getPositionY();
                int clipMinX = originX - state.canvasLivePanX;
                int clipMinY = originY - state.canvasLivePanY;
                int clipMaxX = clipMinX + getSizeWidth();
                int clipMaxY = clipMinY + getSizeHeight();
                long now = System.currentTimeMillis();
                for (ConnectionLine line : lines) {
                    drawConnection(graphics, originX, originY, state, line, mouseX, mouseY, now, clipMinX, clipMinY, clipMaxX, clipMaxY);
                }
            }
        });
    }

    public static List<CanvasPoint> connectionPath(TabletUiState state, int originX, int originY, int sourceX, int sourceY, int targetX, int targetY, boolean direct) {
        if (direct) {
            return List.of(new CanvasPoint(sourceX, sourceY), new CanvasPoint(targetX, targetY));
        }
        int cell = Math.max(1, state.gridCellPx);
        int localSourceX = sourceX - originX;
        int localTargetX = targetX - originX;
        int midLocalX = snapScreenLocalToGrid(state, (localSourceX + localTargetX) / 2, cell);
        int midX = originX + midLocalX;
        if (Math.abs(midX - sourceX) < cell / 2) {
            midX += targetX >= sourceX ? cell : -cell;
        }
        return List.of(
                new CanvasPoint(sourceX, sourceY),
                new CanvasPoint(midX, sourceY),
                new CanvasPoint(midX, targetY),
                new CanvasPoint(targetX, targetY)
        );
    }

    public static void drawStaticChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        drawTexturedChevrons(graphics, path, color, alpha, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    public static void drawStaticChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, float scale, int clipMinX, int clipMinY, int clipMaxX, int clipMaxY) {
        drawTexturedChevrons(graphics, path, color, alpha, 1.0f, scale, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    private static void drawConnection(
            GuiGraphics graphics,
            int originX,
            int originY,
            TabletUiState state,
            ConnectionLine line,
            int mouseX,
            int mouseY,
            long now,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        int sourceOffsetX = selectionDragOffsetX(state, line.sourceQuestId());
        int sourceOffsetY = selectionDragOffsetY(state, line.sourceQuestId());
        int targetOffsetX = selectionDragOffsetX(state, line.targetQuestId());
        int targetOffsetY = selectionDragOffsetY(state, line.targetQuestId());
        int startX = originX + line.startX() + sourceOffsetX;
        int startY = originY + line.startY() + sourceOffsetY;
        int endX = originX + line.endX() + targetOffsetX;
        int endY = originY + line.endY() + targetOffsetY;

        if (line.pending()) {
            graphics.fill(startX - 5, startY - 5, startX + 6, startY + 6, withAlpha(ModColors.SUCCESS, 72));
            graphics.renderOutline(startX - 5, startY - 5, 11, 11, withAlpha(ModColors.SUCCESS, 220));
            return;
        }

        boolean hoveringEndpoint = isHoveringEndpoint(
                originX,
                originY,
                line,
                mouseX,
                mouseY,
                sourceOffsetX,
                sourceOffsetY,
                targetOffsetX,
                targetOffsetY
        );
        if (line.hidden() && !state.canEdit && !hoveringEndpoint) {
            return;
        }
        int alpha = line.hidden() && hoveringEndpoint ? 245 : line.alpha();
        List<CanvasPoint> path = connectionPath(state, originX, originY, startX, startY, endX, endY, line.direct());
        CanvasConnectionAnimation.AnimationState animation = CanvasConnectionAnimation.current(state, line.edgeId(), now);
        if (animation.running()) {
            int animatedAlpha = Math.min(255, Math.round(alpha * (0.58f + 0.42f * animation.progress())));
            drawTexturedChevrons(graphics, path, line.color(), animatedAlpha, animation.progress(), clipMinX, clipMinY, clipMaxX, clipMaxY);
            return;
        }
        drawTexturedChevrons(graphics, path, line.color(), alpha, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    private static boolean isHoveringEndpoint(
            int originX,
            int originY,
            ConnectionLine line,
            int mouseX,
            int mouseY,
            int sourceOffsetX,
            int sourceOffsetY,
            int targetOffsetX,
            int targetOffsetY
    ) {
        return inside(mouseX, mouseY, originX + line.sourceX() + sourceOffsetX, originY + line.sourceY() + sourceOffsetY, line.sourceW(), line.sourceH())
                || inside(mouseX, mouseY, originX + line.targetX() + targetOffsetX, originY + line.targetY() + targetOffsetY, line.targetW(), line.targetH());
    }

    private static int selectionDragOffsetX(TabletUiState state, String questId) {
        if (!state.draggingSelection || questId == null || questId.isBlank() || !state.selectedQuestIds.contains(questId)) {
            return 0;
        }
        return CanvasGeometry.screenX(state, state.dragStartBoundsLeft + state.dragSelectionDeltaX)
                - CanvasGeometry.screenX(state, state.dragStartBoundsLeft);
    }

    private static int selectionDragOffsetY(TabletUiState state, String questId) {
        if (!state.draggingSelection || questId == null || questId.isBlank() || !state.selectedQuestIds.contains(questId)) {
            return 0;
        }
        return CanvasGeometry.screenY(state, state.dragStartBoundsTop + state.dragSelectionDeltaY)
                - CanvasGeometry.screenY(state, state.dragStartBoundsTop);
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static int snapScreenLocalToGrid(TabletUiState state, int localX, int cell) {
        int relative = localX - state.canvasContentX - state.canvasOffsetX;
        int snapped = Math.round((float) relative / (float) cell) * cell;
        return state.canvasContentX + state.canvasOffsetX + snapped;
    }

    private static void drawTexturedChevrons(
            GuiGraphics graphics,
            List<CanvasPoint> path,
            int color,
            int alpha,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        drawTexturedChevrons(graphics, path, color, alpha, 1.0f, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    private static void drawTexturedChevrons(
            GuiGraphics graphics,
            List<CanvasPoint> path,
            int color,
            int alpha,
            float progress,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        drawTexturedChevrons(graphics, path, color, alpha, progress, 1.0f, clipMinX, clipMinY, clipMaxX, clipMaxY);
    }

    private static void drawTexturedChevrons(
            GuiGraphics graphics,
            List<CanvasPoint> path,
            int color,
            int alpha,
            float progress,
            float scale,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        float safeScale = Math.max(0.25f, Math.min(2.0f, scale));
        int glyphW = Math.max(1, Math.round(5 * safeScale));
        int glyphH = Math.max(1, Math.round(9 * safeScale));
        double spacing = Math.max(1.0, glyphW - 1.0);
        double totalLength = pathLength(path);
        if (totalLength < glyphW) {
            return;
        }
        double visibleLength = Math.max(glyphW / 2.0, totalLength * Math.max(0.0f, Math.min(1.0f, progress)));
        List<ChevronGlyph> glyphs = chevronGlyphs(path, color, alpha, visibleLength, glyphW, glyphH, spacing, clipMinX, clipMinY, clipMaxX, clipMaxY);
        if (glyphs.isEmpty()) {
            return;
        }
        setChevronTextureFilter(GL11.GL_LINEAR);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, CONNECTION_CHEVRON);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        Matrix4f matrix = graphics.pose().last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);
        for (ChevronGlyph glyph : glyphs) {
            emitChevronQuad(buffer, matrix, glyph, glyphW, glyphH);
        }
        tessellator.end();
        RenderSystem.disableBlend();
        setChevronTextureFilter(GL11.GL_NEAREST);
    }

    private static double pathLength(List<CanvasPoint> path) {
        double total = 0.0;
        for (int i = 0; i + 1 < path.size(); i++) {
            CanvasPoint a = path.get(i);
            CanvasPoint b = path.get(i + 1);
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            total += Math.sqrt(dx * dx + dy * dy);
        }
        return total;
    }

    private static List<ChevronGlyph> chevronGlyphs(
            List<CanvasPoint> path,
            int color,
            int alpha,
            double visibleLength,
            int glyphW,
            int glyphH,
            double spacing,
            int clipMinX,
            int clipMinY,
            int clipMaxX,
            int clipMaxY
    ) {
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        int lightColor = withAlpha(color, safeAlpha);
        int darkColor = withAlpha(darkenColor(color, 0.52f), safeAlpha);
        int pad = Math.max(glyphW, glyphH) + 2;
        double startDistance = glyphW / 2.0;
        double walked = 0.0D;
        List<ChevronGlyph> glyphs = new ArrayList<>();
        for (int i = 0; i + 1 < path.size(); i++) {
            CanvasPoint a = path.get(i);
            CanvasPoint b = path.get(i + 1);
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length <= 0.0) {
                continue;
            }
            double segmentEnd = walked + length;
            if (segmentEnd <= startDistance || walked >= visibleLength) {
                walked = segmentEnd;
                continue;
            }
            double[] range = clippedSegmentRange(
                    a.x,
                    a.y,
                    b.x,
                    b.y,
                    clipMinX - pad,
                    clipMinY - pad,
                    clipMaxX + pad,
                    clipMaxY + pad
            );
            if (range != null) {
                double rangeStart = Math.max(startDistance, walked + range[0] * length);
                double rangeEnd = Math.min(visibleLength, walked + range[1] * length);
                int firstIndex = (int) Math.ceil((rangeStart - startDistance) / spacing);
                for (double distance = startDistance + firstIndex * spacing; distance < rangeEnd; distance += spacing, firstIndex++) {
                    double segmentDistance = distance - walked;
                    double dirX = dx / length;
                    double dirY = dy / length;
                    int chevronColor = (firstIndex % 2 == 0) ? lightColor : darkColor;
                    glyphs.add(new ChevronGlyph(a.x + dirX * segmentDistance, a.y + dirY * segmentDistance, dirX, dirY, chevronColor));
                }
            }
            walked = segmentEnd;
        }
        return glyphs;
    }

    private static double[] clippedSegmentRange(double x0, double y0, double x1, double y1, double minX, double minY, double maxX, double maxY) {
        double[] range = {0.0D, 1.0D};
        double dx = x1 - x0;
        double dy = y1 - y0;
        if (!clipTest(-dx, x0 - minX, range)) {
            return null;
        }
        if (!clipTest(dx, maxX - x0, range)) {
            return null;
        }
        if (!clipTest(-dy, y0 - minY, range)) {
            return null;
        }
        if (!clipTest(dy, maxY - y0, range)) {
            return null;
        }
        return range;
    }

    private static boolean clipTest(double p, double q, double[] range) {
        if (p == 0.0D) {
            return q >= 0.0D;
        }
        double r = q / p;
        if (p < 0.0D) {
            if (r > range[1]) {
                return false;
            }
            if (r > range[0]) {
                range[0] = r;
            }
            return true;
        }
        if (r < range[0]) {
            return false;
        }
        if (r < range[1]) {
            range[1] = r;
        }
        return true;
    }

    private static void setChevronTextureFilter(int filter) {
        RenderSystem.setShaderTexture(0, CONNECTION_CHEVRON);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
    }

    private static int darkenColor(int color, float factor) {
        int alpha = color & 0xFF000000;
        int r = Math.max(0, Math.min(255, Math.round(((color >> 16) & 0xFF) * factor)));
        int g = Math.max(0, Math.min(255, Math.round(((color >> 8) & 0xFF) * factor)));
        int b = Math.max(0, Math.min(255, Math.round((color & 0xFF) * factor)));
        return alpha | (r << 16) | (g << 8) | b;
    }

    private static void emitChevronQuad(BufferBuilder buffer, Matrix4f matrix, ChevronGlyph glyph, int glyphW, int glyphH) {
        float halfW = glyphW / 2.0f;
        float halfH = glyphH / 2.0f;
        emitChevronVertex(buffer, matrix, glyph, -halfW, halfH, CHEVRON_U0, 1.0f);
        emitChevronVertex(buffer, matrix, glyph, halfW, halfH, CHEVRON_U1, 1.0f);
        emitChevronVertex(buffer, matrix, glyph, halfW, -halfH, CHEVRON_U1, 0.0f);
        emitChevronVertex(buffer, matrix, glyph, -halfW, -halfH, CHEVRON_U0, 0.0f);
    }

    private static void emitChevronVertex(BufferBuilder buffer, Matrix4f matrix, ChevronGlyph glyph, float localX, float localY, float u, float v) {
        float x = (float) (glyph.x() + glyph.dirX() * localX - glyph.dirY() * localY);
        float y = (float) (glyph.y() + glyph.dirY() * localX + glyph.dirX() * localY);
        buffer.vertex(matrix, x, y, 0.0f).uv(u, v).color(glyph.color()).endVertex();
    }

    public record ConnectionLine(
            String edgeId,
            String sourceQuestId,
            String targetQuestId,
            int sourceX,
            int sourceY,
            int sourceW,
            int sourceH,
            int targetX,
            int targetY,
            int targetW,
            int targetH,
            int startX,
            int startY,
            int endX,
            int endY,
            boolean direct,
            boolean pending,
            int color,
            boolean hidden,
            int alpha
    ) {
    }

    private record ChevronGlyph(double x, double y, double dirX, double dirY, int color) {
    }
}
