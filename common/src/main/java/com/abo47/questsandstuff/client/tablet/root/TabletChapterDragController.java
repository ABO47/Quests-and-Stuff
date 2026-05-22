package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.CardReorderController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import net.minecraft.world.entity.player.Player;

final class TabletChapterDragController {
    private TabletChapterDragController() {
    }

    static boolean handleDrag(TabletUiState state, Player player, Runnable refresh, int rootY, double mouseX, double mouseY, int button) {
        if (state.chapterDragActive) {
            int localY = (int) Math.round(mouseY - rootY - TabletUiFactory.CHAPTER_Y);
            int nextTarget = TabletUiFactory.chapterInsertIndexAtY(localY, state);
            if (nextTarget != state.chapterDragTargetIndex) {
                state.chapterDragTargetIndex = nextTarget;
                QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag preview moving={} targetIndex={}", state.chapterDragName, nextTarget);
                refresh.run();
            }
            return true;
        }
        if (state.chapterDragPending && button == 0) {
            if (!CardReorderController.pastDragThreshold(mouseX, mouseY, state.chapterDragStartX, state.chapterDragStartY)) {
                return true;
            }
            state.chapterDragPending = false;
            state.chapterDragActive = true;
            int localY = (int) Math.round(mouseY - rootY - TabletUiFactory.CHAPTER_Y);
            state.chapterDragTargetIndex = TabletUiFactory.chapterInsertIndexAtY(localY, state);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag start moving={} targetIndex={}", state.chapterDragName, state.chapterDragTargetIndex);
            refresh.run();
            return true;
        }
        return false;
    }

    static boolean finish(TabletUiState state, Player player, Runnable refresh) {
        if (state.chapterDragActive) {
            finishActive(state, player);
            refresh.run();
            return true;
        }
        if (state.chapterDragPending) {
            state.chapterDragPending = false;
            state.chapterDragName = "";
            state.chapterDragTargetIndex = -1;
            refresh.run();
            return true;
        }
        return false;
    }

    private static void finishActive(TabletUiState state, Player player) {
        String moving = state.chapterDragName;
        int target = Math.max(0, state.chapterDragTargetIndex);
        state.chapterDragActive = false;
        state.chapterDragPending = false;
        state.chapterDragName = "";
        state.chapterDragTargetIndex = -1;
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
        state.selectedGroup = moving;
        TabletUiFactory.persistUiState(state);
    }
}
