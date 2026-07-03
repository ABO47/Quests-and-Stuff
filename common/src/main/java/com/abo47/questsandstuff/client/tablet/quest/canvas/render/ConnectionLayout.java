package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayoutService;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries.selectedChapterName;

final class ConnectionLayout {
    private ConnectionLayout() {
    }

    static List<ConnectionLine> prerequisiteAndPendingConnectionLines(
            TabletUiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = new ArrayList<>(prerequisiteConnectionLines(state, cards, byQuestId, viewportW, viewportH));
        lines.addAll(pendingConnectionLines(state, byQuestId, viewportW, viewportH));
        return lines;
    }

    static List<ConnectionLine> prerequisiteConnectionLines(
            TabletUiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = new ArrayList<>();
        Set<String> rendered = new HashSet<>();
        String chapter = selectedChapterName(state);

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

                ConnectionRenderStyle style = ConnectionStyleResolver.style(state, chapter, prerequisiteId, quest.questId(), questTag);
                if (!rendered.add(style.connectionId())) {
                    continue;
                }
                if (!CanvasLayoutService.intersectsPanRenderWindow(prerequisite, viewportW, viewportH)
                        && !CanvasLayoutService.intersectsPanRenderWindow(quest, viewportW, viewportH)) {
                    continue;
                }
                lines.add(lineFromStyle(style, prerequisite, quest, false));
            }
        }
        List<CanvasExclusiveChoice> ecs = state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of());
        for (CanvasExclusiveChoice ec : ecs) {
            CanvasExclusiveChoice drawEc = CanvasLayerMutations.effectiveCanvasExclusiveChoice(state, ec);
            CanvasElementGeometry.Box ecBox = CanvasElementGeometry.screenBoxAtPivot(state, drawEc.x(), drawEc.y(), drawEc.w(), drawEc.h(), 0, 0, drawEc.rotation());
            int ecBoxLeft = (int) Math.floor(ecBox.centerX() + ecBox.left());
            int ecBoxTop = (int) Math.floor(ecBox.centerY() + ecBox.top());
            int ecBoxRight = (int) Math.ceil(ecBox.centerX() + ecBox.right());
            int ecBoxBottom = (int) Math.ceil(ecBox.centerY() + ecBox.bottom());
            int ecScreenW = Math.max(1, ecBoxRight - ecBoxLeft);
            int ecScreenH = Math.max(1, ecBoxBottom - ecBoxTop);
            int ecCenterX = (int) Math.round(ecBox.centerX() + ecBox.width() / 2.0);
            int ecCenterY = (int) Math.round(ecBox.centerY() + ecBox.height() / 2.0);
            for (String connectedQuestId : drawEc.connectionQuestIds()) {
                QuestCardLayout connectedQuest = byQuestId.get(connectedQuestId);
                if (connectedQuest == null) {
                    continue;
                }
                String connectionId = CanvasConnectionAnimation.connectionKey(ec.id(), connectedQuestId);
                if (!rendered.add(connectionId)) {
                    continue;
                }
                if (!intersectsViewport(ecBoxLeft, ecBoxTop, ecScreenW, ecScreenH, viewportW, viewportH)
                        && !CanvasLayoutService.intersectsPanRenderWindow(connectedQuest, viewportW, viewportH)) {
                    continue;
                }
                boolean direct = ConnectionStyleResolver.ecIsConnectionDirect(state, chapter, ec.id(), connectedQuestId);
                int color = ConnectionStyleResolver.ecConnectionColor(state, chapter, ec.id(), connectedQuestId);
                String texture = ConnectionStyleResolver.ecConnectionTexture(state, chapter, ec.id(), connectedQuestId);
                int textureSpacing = ConnectionStyleResolver.ecConnectionTextureSpacing(state, chapter, ec.id(), connectedQuestId);
                boolean hidden = ConnectionStyleResolver.ecIsConnectionHidden(state, chapter, ec.id(), connectedQuestId);
                int alpha = hidden ? ConnectionRenderStyle.HIDDEN_ALPHA : ConnectionRenderStyle.VISIBLE_ALPHA;
                lines.add(new ConnectionLine(
                        connectionId, ec.id(), connectedQuestId,
                        ecBoxLeft, ecBoxTop, ecScreenW, ecScreenH,
                        connectedQuest.x(), connectedQuest.y(), connectedQuest.width(), connectedQuest.height(),
                        ecCenterX, ecCenterY,
                        connectedQuest.centerX(), connectedQuest.centerY(),
                        direct, false, color, hidden, alpha, texture, textureSpacing
                ));
            }
            for (String prerequisiteQuestId : drawEc.prerequisiteQuestIds()) {
                QuestCardLayout prerequisiteQuest = byQuestId.get(prerequisiteQuestId);
                if (prerequisiteQuest == null) {
                    continue;
                }
                String connectionId = CanvasConnectionAnimation.connectionKey(prerequisiteQuestId, ec.id());
                if (!rendered.add(connectionId)) {
                    continue;
                }
                if (!intersectsViewport(ecBoxLeft, ecBoxTop, ecScreenW, ecScreenH, viewportW, viewportH)
                        && !CanvasLayoutService.intersectsPanRenderWindow(prerequisiteQuest, viewportW, viewportH)) {
                    continue;
                }
                boolean direct = ConnectionStyleResolver.ecIsConnectionDirect(state, chapter, prerequisiteQuestId, ec.id());
                int color = ConnectionStyleResolver.ecConnectionColor(state, chapter, prerequisiteQuestId, ec.id());
                String texture = ConnectionStyleResolver.ecConnectionTexture(state, chapter, prerequisiteQuestId, ec.id());
                int textureSpacing = ConnectionStyleResolver.ecConnectionTextureSpacing(state, chapter, prerequisiteQuestId, ec.id());
                boolean hidden = ConnectionStyleResolver.ecIsConnectionHidden(state, chapter, prerequisiteQuestId, ec.id());
                int alpha = hidden ? ConnectionRenderStyle.HIDDEN_ALPHA : ConnectionRenderStyle.VISIBLE_ALPHA;
                lines.add(new ConnectionLine(
                        connectionId, prerequisiteQuestId, ec.id(),
                        prerequisiteQuest.x(), prerequisiteQuest.y(), prerequisiteQuest.width(), prerequisiteQuest.height(),
                        ecBoxLeft, ecBoxTop, ecScreenW, ecScreenH,
                        prerequisiteQuest.centerX(), prerequisiteQuest.centerY(),
                        ecCenterX, ecCenterY,
                        direct, false, color, hidden, alpha, texture, textureSpacing
                ));
            }
        }
        return lines;
    }

    static List<String> prerequisiteConnectionLayerKeys(
            TabletUiState state,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = prerequisiteConnectionLines(state, cards, byQuestId, viewportW, viewportH);
        List<String> keys = new ArrayList<>();
        for (ConnectionLine line : lines) {
            keys.add(CanvasLayerOrdering.connectionKey(line.connectionId()));
        }
        return keys;
    }

    static List<ConnectionLine> pendingConnectionLines(
            TabletUiState state,
            Map<String, QuestCardLayout> byQuestId,
            int viewportW,
            int viewportH
    ) {
        List<ConnectionLine> lines = new ArrayList<>();
        if (!state.root.canEdit) {
            return lines;
        }
        Set<String> pendingSources = new HashSet<>(state.canvas.connectSourceQuestIds);
        if (!state.canvas.connectSourceQuestId.isBlank()) {
            pendingSources.add(state.canvas.connectSourceQuestId);
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
                        TabletColors.TEXT_SECONDARY,
                        false,
                        245, "", 0
                ));
            }
        }
        return lines;
    }

    private static boolean intersectsViewport(int x, int y, int w, int h, int viewportW, int viewportH) {
        int marginX = CanvasLayoutService.panRenderOverscanX(viewportW);
        int marginY = CanvasLayoutService.panRenderOverscanY(viewportH);
        return x + w >= -marginX && y + h >= -marginY && x <= viewportW + marginX && y <= viewportH + marginY;
    }

    private static ConnectionLine lineFromStyle(ConnectionRenderStyle style, QuestCardLayout source, QuestCardLayout target, boolean pending) {
        return new ConnectionLine(
                style.connectionId(),
                style.sourceQuestId(),
                style.targetQuestId(),
                source.x(),
                source.y(),
                source.width(),
                source.height(),
                target.x(),
                target.y(),
                target.width(),
                target.height(),
                source.centerX(),
                source.centerY(),
                target.centerX(),
                target.centerY(),
                style.direct(),
                pending,
                style.color(),
                style.hidden(),
                style.alpha(),
                style.texture(),
                style.textureSpacing()
        );
    }
}
