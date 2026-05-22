package com.abo47.questsandstuff.client.canvas.render;

import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
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
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.selectedGroupName;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.withAlpha;
import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_TEX_COLOR;

public final class ConnectionRenderer {
    private static final ResourceLocation CONNECTION_CHEVRON = ResourceLocation.tryBuild("questsandstuff", "textures/gui/chevron.png");
    private static final float CHEVRON_U0 = 50.0f / 256.0f;
    private static final float CHEVRON_U1 = 206.0f / 256.0f;

    private ConnectionRenderer() {
    }

    public static String edgeKey(String sourceQuestId, String targetQuestId) {
        return CanvasConnectionAnimation.edgeKey(sourceQuestId, targetQuestId);
    }

    public static int connectionColor(TabletUiState state, String group, String sourceQuestId, String targetQuestId) {
        CompoundTag target = ClientQuestCache.quests().get(targetQuestId);
        if (target != null && target.contains("connection_colors", Tag.TAG_COMPOUND)) {
            CompoundTag colorsTag = target.getCompound("connection_colors");
            if (colorsTag.contains(sourceQuestId, Tag.TAG_INT)) {
                return colorsTag.getInt(sourceQuestId);
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
        CompoundTag target = ClientQuestCache.quests().get(targetQuestId);
        if (target != null && target.contains("hidden_connections", Tag.TAG_LIST)) {
            ListTag hiddenTag = target.getList("hidden_connections", Tag.TAG_STRING);
            for (int i = 0; i < hiddenTag.size(); i++) {
                if (sourceQuestId.equals(hiddenTag.getString(i))) {
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
        CompoundTag target = ClientQuestCache.quests().get(targetQuestId);
        if (target != null && target.contains("connection_modes", Tag.TAG_COMPOUND)) {
            String mode = target.getCompound("connection_modes").getString(sourceQuestId);
            if ("grid".equals(mode)) {
                return false;
            }
            if (!mode.isBlank()) {
                return true;
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

    public static void renderPrerequisiteConnections(WidgetGroup canvasViewport, TabletUiState state, List<QuestCardLayout> cards, Map<String, QuestCardLayout> byQuestId) {
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

                String edgeId = edgeKey(prerequisiteId, quest.questId());
                if (!rendered.add(edgeId)) {
                    continue;
                }
                boolean hidden = isConnectionHidden(state, group, prerequisiteId, quest.questId());

                lines.add(new ConnectionLine(
                        edgeId,
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
                        isConnectionDirect(state, group, prerequisiteId, quest.questId()),
                        false,
                        connectionColor(state, group, prerequisiteId, quest.questId()),
                        hidden,
                        hidden ? 64 : 245
                ));
            }
        }
        if (state.canEdit) {
            Set<String> pendingSources = new HashSet<>(state.connectSourceQuestIds);
            if (!state.connectSourceQuestId.isBlank()) {
                pendingSources.add(state.connectSourceQuestId);
            }
            for (String sourceQuestId : pendingSources) {
                QuestCardLayout source = byQuestId.get(sourceQuestId);
                if (source != null) {
                    lines.add(new ConnectionLine(
                            "",
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
        if (lines.isEmpty()) {
            return;
        }
        canvasViewport.addWidget(new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int originX = getPositionX();
                int originY = getPositionY();
                long now = System.currentTimeMillis();
                for (ConnectionLine line : lines) {
                    drawConnection(graphics, originX, originY, state, line, mouseX, mouseY, now);
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

    private static void drawConnection(GuiGraphics graphics, int originX, int originY, TabletUiState state, ConnectionLine line, int mouseX, int mouseY, long now) {
        int startX = originX + line.startX();
        int startY = originY + line.startY();
        int endX = originX + line.endX();
        int endY = originY + line.endY();

        if (line.pending()) {
            graphics.fill(startX - 5, startY - 5, startX + 6, startY + 6, withAlpha(ModColors.SUCCESS, 72));
            graphics.renderOutline(startX - 5, startY - 5, 11, 11, withAlpha(ModColors.SUCCESS, 220));
            return;
        }

        if (line.hidden() && !state.canEdit && !isHoveringEndpoint(originX, originY, line, mouseX, mouseY)) {
            return;
        }
        int alpha = line.hidden() && isHoveringEndpoint(originX, originY, line, mouseX, mouseY) ? 245 : line.alpha();
        List<CanvasPoint> path = connectionPath(state, originX, originY, startX, startY, endX, endY, line.direct());
        CanvasConnectionAnimation.AnimationState animation = CanvasConnectionAnimation.current(state, line.edgeId(), now);
        if (animation.running()) {
            int animatedAlpha = Math.min(255, Math.round(alpha * (0.58f + 0.42f * animation.progress())));
            drawTexturedChevrons(graphics, path, line.color(), animatedAlpha, animation.progress());
            return;
        }
        drawTexturedChevrons(graphics, path, line.color(), alpha);
    }

    private static boolean isHoveringEndpoint(int originX, int originY, ConnectionLine line, int mouseX, int mouseY) {
        return inside(mouseX, mouseY, originX + line.sourceX(), originY + line.sourceY(), line.sourceW(), line.sourceH())
                || inside(mouseX, mouseY, originX + line.targetX(), originY + line.targetY(), line.targetW(), line.targetH());
    }

    private static boolean inside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static int snapScreenLocalToGrid(TabletUiState state, int localX, int cell) {
        int relative = localX - state.canvasContentX - state.canvasOffsetX;
        int snapped = Math.round((float) relative / (float) cell) * cell;
        return state.canvasContentX + state.canvasOffsetX + snapped;
    }

    private static void drawTexturedChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha) {
        drawTexturedChevrons(graphics, path, color, alpha, 1.0f);
    }

    private static void drawTexturedChevrons(GuiGraphics graphics, List<CanvasPoint> path, int color, int alpha, float progress) {
        int glyphW = 5;
        int glyphH = 9;
        double spacing = Math.max(1.0, glyphW - 1.0);
        int safeAlpha = Math.max(0, Math.min(255, alpha));
        int lightColor = withAlpha(color, safeAlpha);
        int darkColor = withAlpha(darkenColor(color, 0.52f), safeAlpha);
        double totalLength = pathLength(path);
        if (totalLength < glyphW) {
            return;
        }
        double visibleLength = Math.max(glyphW / 2.0, totalLength * Math.max(0.0f, Math.min(1.0f, progress)));
        int phase = 0;
        setChevronTextureFilter(GL11.GL_LINEAR);
        for (double distance = glyphW / 2.0; distance < Math.min(totalLength, visibleLength); distance += spacing) {
            ChevronPlacement placement = chevronAtDistance(path, distance);
            if (placement == null) {
                continue;
            }
            int chevronColor = (phase++ % 2 == 0) ? lightColor : darkColor;
            drawTexturedChevron(graphics, chevronColor, placement.x(), placement.y(), placement.dirX(), placement.dirY(), glyphW, glyphH);
        }
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

    private static ChevronPlacement chevronAtDistance(List<CanvasPoint> path, double targetDistance) {
        double walked = 0.0;
        for (int i = 0; i + 1 < path.size(); i++) {
            CanvasPoint a = path.get(i);
            CanvasPoint b = path.get(i + 1);
            double dx = b.x - a.x;
            double dy = b.y - a.y;
            double length = Math.sqrt(dx * dx + dy * dy);
            if (length <= 0.0) {
                continue;
            }
            if (walked + length >= targetDistance) {
                double segmentDistance = targetDistance - walked;
                double dirX = dx / length;
                double dirY = dy / length;
                return new ChevronPlacement(a.x + dirX * segmentDistance, a.y + dirY * segmentDistance, dirX, dirY);
            }
            walked += length;
        }
        return null;
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

    private static void drawTexturedChevron(GuiGraphics graphics, int color, double x, double y, double dirX, double dirY, int glyphW, int glyphH) {
        float direction = (float) Math.atan2(dirY, dirX);
        graphics.pose().pushPose();
        graphics.pose().translate((float) x, (float) y, 0.0f);
        graphics.pose().mulPose(new Quaternionf().rotationZ(direction));
        drawChevronQuad(graphics, -glyphW / 2.0f, -glyphH / 2.0f, glyphW, glyphH, color);
        graphics.pose().popPose();
    }

    private static void drawChevronQuad(GuiGraphics graphics, float x, float y, float width, float height, int color) {
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, CONNECTION_CHEVRON);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        var matrix = graphics.pose().last().pose();
        buffer.begin(VertexFormat.Mode.QUADS, POSITION_TEX_COLOR);
        buffer.vertex(matrix, x, y + height, 0).uv(CHEVRON_U0, 1.0f).color(color).endVertex();
        buffer.vertex(matrix, x + width, y + height, 0).uv(CHEVRON_U1, 1.0f).color(color).endVertex();
        buffer.vertex(matrix, x + width, y, 0).uv(CHEVRON_U1, 0.0f).color(color).endVertex();
        buffer.vertex(matrix, x, y, 0).uv(CHEVRON_U0, 0.0f).color(color).endVertex();
        tessellator.end();
        RenderSystem.disableBlend();
    }

    private record ConnectionLine(
            String edgeId,
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

    private record ChevronPlacement(double x, double y, double dirX, double dirY) {
    }
}
