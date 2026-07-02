package com.abo47.questsandstuff.client.tablet.quest.chapter;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.CardReorderController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import net.minecraft.world.entity.player.Player;

public final class ChapterDragController {
    private ChapterDragController() {
    }

    public static boolean handleDrag(TabletUiState state, Player player, Runnable refresh, int rootY, double mouseX, double mouseY, int button) {
        if (state.chapterPanel.chapterDragActive) {
            int localY = (int) Math.round(mouseY - rootY - TabletUiFactory.CHAPTER_Y);
            int nextTarget = TabletUiFactory.chapterInsertIndexAtY(localY, state);
            if (nextTarget != state.chapterPanel.chapterDragTargetIndex) {
                state.chapterPanel.chapterDragTargetIndex = nextTarget;
                QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag preview moving={} targetIndex={}", state.chapterPanel.chapterDragName, nextTarget);
                refresh.run();
            }
            return true;
        }
        if (state.chapterPanel.chapterDragPending && button == 0) {
            if (!CardReorderController.pastDragThreshold(mouseX, mouseY, state.chapterPanel.chapterDragStartX, state.chapterPanel.chapterDragStartY)) {
                return true;
            }
            state.chapterPanel.chapterDragPending = false;
            state.chapterPanel.chapterDragActive = true;
            int localY = (int) Math.round(mouseY - rootY - TabletUiFactory.CHAPTER_Y);
            state.chapterPanel.chapterDragTargetIndex = TabletUiFactory.chapterInsertIndexAtY(localY, state);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag start moving={} targetIndex={}", state.chapterPanel.chapterDragName, state.chapterPanel.chapterDragTargetIndex);
            refresh.run();
            return true;
        }
        return false;
    }

    public static boolean finish(TabletUiState state, Player player, Runnable refresh) {
        if (state.chapterPanel.chapterDragActive) {
            finishActive(state, player);
            refresh.run();
            return true;
        }
        if (state.chapterPanel.chapterDragPending) {
            state.chapterPanel.chapterDragPending = false;
            state.chapterPanel.chapterDragName = "";
            state.chapterPanel.chapterDragTargetIndex = -1;
            refresh.run();
            return true;
        }
        return false;
    }

    private static void finishActive(TabletUiState state, Player player) {
        String moving = state.chapterPanel.chapterDragName;
        int target = Math.max(0, state.chapterPanel.chapterDragTargetIndex);
        state.chapterPanel.chapterDragActive = false;
        state.chapterPanel.chapterDragPending = false;
        state.chapterPanel.chapterDragName = "";
        state.chapterPanel.chapterDragTargetIndex = -1;
        if (moving.isBlank()) {
            return;
        }
        int fromIndex = ClientQuestCache.groupOrder().indexOf(moving);
        int size = ClientQuestCache.groupOrder().size();
        target = CardReorderController.targetIndexAfterDrop(fromIndex, target, size);
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag drop moving={} fromIndex={} targetIndex={}", moving, fromIndex, target);
        if (fromIndex >= 0 && target >= 0 && target != fromIndex) {
            TabletUiFactory.runGroupAction(player, state, "move_to", moving, "", target);
        }
        state.root.selectedGroup = moving;
        TabletUiFactory.persistUiState(state);
    }
}
