package com.abo47.questsandstuff.client.tablet.quest.canvas.overlay;

import com.abo47.questsandstuff.QuestsAndStuffConfig;
import com.abo47.questsandstuff.client.tablet.animation.UiAnimationProgress;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;

final class CanvasMinimapOverlay {
    private static final int MIN_QUEST_SIZE = 4;

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

        CanvasMinimapSnapshot snapshot = snapshot(state, group, visibleCards, byQuestId, projection);
        CanvasMinimapGeometry.Layout collapsedLayout = CanvasMinimapGeometry.layout(canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight(), true);
        canvasViewport.addWidget(minimapWidget(canvasViewport, state, animationsEnabled, layout, collapsedLayout, snapshot));
    }

    private static WidgetGroup minimapWidget(
            CanvasViewport canvasViewport,
            TabletUiState state,
            boolean animationsEnabled,
            CanvasMinimapGeometry.Layout layout,
            CanvasMinimapGeometry.Layout collapsedLayout,
            CanvasMinimapSnapshot snapshot
    ) {
        return new WidgetGroup(0, 0, canvasViewport.getSizeWidth(), canvasViewport.getSizeHeight()) {
            @Override
            public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
                int originX = getPositionX();
                int originY = getPositionY();
                float openProgress = minimapOpenProgress(state, animationsEnabled);
                float holderProgress = CanvasMinimapPainter.stagedProgress(openProgress, 0.0f, CanvasMinimapPainter.BODY_REVEAL_START);
                float bodyProgress = CanvasMinimapPainter.stagedProgress(openProgress, CanvasMinimapPainter.BODY_REVEAL_START, 1.0f);
                CanvasMinimapPainter.drawPanel(graphics, originX, originY, layout, collapsedLayout, holderProgress, bodyProgress, mouseX, mouseY);
                if (snapshot != null && bodyProgress > 0.02f) {
                    int clipW = Math.max(1, Math.round((layout.panelW() - layout.toggleW()) * bodyProgress));
                    int clipX = layout.toggleX() - clipW;
                    CanvasViewportScissor.draw(
                            graphics,
                            originX + clipX,
                            originY + layout.panelY(),
                            clipW,
                            layout.panelH(),
                            () -> CanvasMinimapPainter.drawSnapshot(graphics, state, snapshot, originX, originY, mouseX, mouseY, partialTicks)
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

    private static CanvasMinimapSnapshot snapshot(
            TabletUiState state,
            String group,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            CanvasMinimapGeometry.Projection projection
    ) {
        Map<String, CanvasMinimapRect> questBoxes = new HashMap<>();
        List<CanvasMinimapRect> questRects = new ArrayList<>();
        boolean visualMode = QuestsAndStuffConfig.visualMinimapEnabled();
        for (QuestCardLayout card : cards) {
            CanvasMinimapRect box = projectRect(
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

        List<CanvasMinimapConnection> connections = new ArrayList<>();
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
                CanvasMinimapRect sourceBox = questBoxes.get(sourceId);
                CanvasMinimapRect targetBox = questBoxes.get(target.questId());
                if (sourceBox == null || targetBox == null) {
                    continue;
                }
                float sourceCenterX = sourceBox.x() + sourceBox.w() / 2.0f;
                float sourceCenterY = sourceBox.y() + sourceBox.h() / 2.0f;
                float targetCenterX = targetBox.x() + targetBox.w() / 2.0f;
                float targetCenterY = targetBox.y() + targetBox.h() / 2.0f;
                connections.add(new CanvasMinimapConnection(
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

        return new CanvasMinimapSnapshot(List.copyOf(questRects), List.copyOf(connections), projection);
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

    private static CanvasMinimapRect projectRect(
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
        return new CanvasMinimapRect(x, y, w, h, color, alpha, questId, tag);
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

}
