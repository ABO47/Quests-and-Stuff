package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class QuestObjectiveDragDispatcher {
    private static final int SECTION_GAP = 6;
    private static final int TITLE_H = 18;
    private static final int HEADER_H = 14;

    private QuestObjectiveDragDispatcher() {
    }

    public static boolean handleDrag(Player player, TabletUiState state, Runnable refresh, double mouseX, double mouseY, int button) {
        if (state == null || (!state.questDetailsObjectiveDragPending && !state.questDetailsObjectiveDragActive)) {
            return false;
        }
        ObjectiveDragScope scope = dragScope(state);
        if (!scope.valid()) {
            QuestObjectiveListInteractions.clearDrag(state);
            refresh.run();
            return true;
        }
        int localY = (int) Math.round(mouseY - state.questDetailsScreenY - scope.sectionY());
        return QuestObjectiveListInteractions.handleDrag(
                player,
                state,
                refresh,
                state.questDetailsQuestId,
                scope.entries(),
                scope.kind(),
                TITLE_H,
                scope.sectionH() - 4,
                localY,
                mouseX,
                mouseY,
                button
        );
    }

    public static boolean handleRelease(Player player, TabletUiState state, Runnable refresh) {
        if (state == null || (!state.questDetailsObjectiveDragPending && !state.questDetailsObjectiveDragActive)) {
            return false;
        }
        ObjectiveDragScope scope = dragScope(state);
        if (scope.valid()) {
            return QuestObjectiveListInteractions.handleRelease(player, state, refresh, state.questDetailsQuestId, scope.entries(), scope.kind());
        }
        QuestObjectiveListInteractions.clearDrag(state);
        refresh.run();
        return true;
    }

    private static ObjectiveDragScope dragScope(TabletUiState state) {
        String questId = state.questDetailsQuestId == null ? "" : state.questDetailsQuestId.trim();
        CompoundTag quest = questId.isBlank() ? null : ClientQuestCache.quest(questId);
        if (quest == null || quest.isEmpty()) {
            return ObjectiveDragScope.invalid();
        }
        int panelY = TabletUiFactory.CHAPTER_Y + QuestDetailsWindow.CONTENT_INSET;
        int panelH = TabletUiFactory.CHAPTER_H - QuestDetailsWindow.CONTENT_INSET * 2;
        int sectionsY = HEADER_H + SECTION_GAP;
        int sectionsH = panelH - HEADER_H - SECTION_GAP;
        int sectionH = Math.max(20, (sectionsH - SECTION_GAP) / 2);
        if ("requirements".equals(state.questDetailsObjectiveDragKind)) {
            return new ObjectiveDragScope("requirements", QuestObjectiveEntries.entries(quest.getCompound("tasks"), quest.getList("tasks_order", Tag.TAG_STRING)), panelY + sectionsY, sectionH);
        }
        if ("rewards".equals(state.questDetailsObjectiveDragKind)) {
            return new ObjectiveDragScope("rewards", QuestObjectiveEntries.entries(quest.getCompound("rewards"), quest.getList("rewards_order", Tag.TAG_STRING)), panelY + sectionsY + sectionH + SECTION_GAP, sectionH);
        }
        return ObjectiveDragScope.invalid();
    }

    private record ObjectiveDragScope(String kind, List<QuestDetailsObjectiveEntry> entries, int sectionY, int sectionH) {
        private static ObjectiveDragScope invalid() {
            return new ObjectiveDragScope("", List.of(), 0, 0);
        }

        private boolean valid() {
            return !kind.isBlank();
        }
    }
}
