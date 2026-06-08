package com.abo47.questsandstuff.client.tablet.quest.canvas.render;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayoutService;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries.selectedGroupName;

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

                ConnectionRenderStyle style = ConnectionStyleResolver.style(state, group, prerequisiteId, quest.questId(), questTag);
                if (!rendered.add(style.edgeId())) {
                    continue;
                }
                if (!CanvasLayoutService.intersectsPanRenderWindow(prerequisite, viewportW, viewportH)
                        && !CanvasLayoutService.intersectsPanRenderWindow(quest, viewportW, viewportH)) {
                    continue;
                }
                lines.add(lineFromStyle(style, prerequisite, quest, false));
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
            keys.add(CanvasLayerOrdering.connectionKey(line.edgeId()));
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
                        ModColors.TEXT_SECONDARY,
                        false,
                        245
                ));
            }
        }
        return lines;
    }

    private static ConnectionLine lineFromStyle(ConnectionRenderStyle style, QuestCardLayout source, QuestCardLayout target, boolean pending) {
        return new ConnectionLine(
                style.edgeId(),
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
                style.alpha()
        );
    }
}
