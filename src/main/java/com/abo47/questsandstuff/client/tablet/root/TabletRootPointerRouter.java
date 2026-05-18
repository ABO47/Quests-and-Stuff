package com.abo47.questsandstuff.client.tablet.root;

import com.abo47.questsandstuff.client.chapter.ChapterPanel;
import com.abo47.questsandstuff.client.tablet.details.objective.QuestObjectiveDragDispatcher;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class TabletRootPointerRouter {
    private TabletRootPointerRouter() {
    }

    static boolean mouseClicked(TabletRootWidget root, TabletUiState state, WidgetGroup modalLayer, WidgetGroup frontWindowLayer, Runnable refresher, MouseClickDelegate selfClick, double mouseX, double mouseY, int button) {
        if (root.isAnyModalOpen()) {
            if (modalLayer != null) {
                modalLayer.mouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        if (root.isFrontWindowOpen()) {
            if (frontWindowLayer != null) {
                frontWindowLayer.mouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        if (EntityMotionEditor.isChapterPanelOpen(state)) {
            int localX = localRootX(root, mouseX);
            int localY = localRootY(root, mouseY);
            if (EntityMotionEditor.isChapterPanelHit(state, localX, localY)) {
                selfClick.invoke(mouseX, mouseY, button);
                return true;
            }
            EntityMotionEditor.close(state);
            refresher.run();
            return true;
        }
        if (button == 0 && beginChapterScrollDrag(root, state, refresher, mouseX, mouseY)) {
            return true;
        }
        TabletRootDismissals.ClickDismissState dismissState = TabletRootDismissals.capture(root, state, mouseX, mouseY);
        if (button == 0 && dismissState.chapterMenuHit()) {
            if (ChapterPanel.clickChapterMenu(state, root.resolvePlayer(), refresher, localRootX(root, mouseX), localRootY(root, mouseY))) {
                return true;
            }
        }
        boolean handled = selfClick.invoke(mouseX, mouseY, button);
        return TabletRootDismissals.handleAfterClick(root, state, refresher, dismissState, mouseX, mouseY, button, handled);
    }

    static boolean mouseDragged(TabletRootWidget root, TabletUiState state, WidgetGroup modalLayer, WidgetGroup frontWindowLayer, Runnable refresher, MouseDragDelegate selfDrag, double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (root.isAnyModalOpen()) {
            if (modalLayer != null) {
                modalLayer.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
            return true;
        }
        if (root.isFrontWindowOpen()) {
            if (QuestObjectiveDragDispatcher.handleDrag(root.resolvePlayer(), state, refresher, mouseX, mouseY, button)) {
                return true;
            }
            if (frontWindowLayer != null) {
                frontWindowLayer.mouseDragged(mouseX, mouseY, button, dragX, dragY);
            }
            return true;
        }
        if (state.chapterScrollDragging) {
            updateChapterScrollDrag(root, state, refresher, mouseY);
            return true;
        }
        if (EntityMotionEditor.isDragging(state)) {
            selfDrag.invoke(mouseX, mouseY, button, dragX, dragY);
            return true;
        }
        if (TabletChapterDragController.handleDrag(state, root.resolvePlayer(), refresher, root.getPositionY(), mouseX, mouseY, button)) {
            return true;
        }
        return selfDrag.invoke(mouseX, mouseY, button, dragX, dragY);
    }

    static boolean mouseReleased(TabletRootWidget root, TabletUiState state, WidgetGroup modalLayer, WidgetGroup frontWindowLayer, Runnable refresher, MouseClickDelegate selfRelease, double mouseX, double mouseY, int button) {
        if (root.isAnyModalOpen()) {
            if (modalLayer != null) {
                modalLayer.mouseReleased(mouseX, mouseY, button);
            }
            return true;
        }
        if (root.isFrontWindowOpen()) {
            if (QuestObjectiveDragDispatcher.handleRelease(root.resolvePlayer(), state, refresher)) {
                return true;
            }
            if (frontWindowLayer != null) {
                frontWindowLayer.mouseReleased(mouseX, mouseY, button);
            }
            return true;
        }
        if (state.chapterScrollDragging) {
            state.chapterScrollDragging = false;
            refresher.run();
            return true;
        }
        if (EntityMotionEditor.isDragging(state)) {
            selfRelease.invoke(mouseX, mouseY, button);
            return true;
        }
        if (TabletChapterDragController.finish(state, root.resolvePlayer(), refresher)) {
            return true;
        }
        return selfRelease.invoke(mouseX, mouseY, button);
    }

    static boolean mouseWheelMove(TabletRootWidget root, TabletUiState state, WidgetGroup modalLayer, WidgetGroup frontWindowLayer, MouseWheelDelegate selfWheel, double mouseX, double mouseY, double wheelDelta) {
        if (root.isAnyModalOpen()) {
            if (modalLayer != null) {
                modalLayer.mouseWheelMove(mouseX, mouseY, wheelDelta);
            }
            return true;
        }
        if (root.isFrontWindowOpen()) {
            if (frontWindowLayer != null) {
                frontWindowLayer.mouseWheelMove(mouseX, mouseY, wheelDelta);
            }
            return true;
        }
        if (EntityMotionEditor.isChapterPanelOpen(state)) {
            int localX = localRootX(root, mouseX);
            int localY = localRootY(root, mouseY);
            if (EntityMotionEditor.isChapterPanelHit(state, localX, localY)) {
                selfWheel.invoke(mouseX, mouseY, wheelDelta);
            }
            return true;
        }
        return selfWheel.invoke(mouseX, mouseY, wheelDelta);
    }

    private static boolean beginChapterScrollDrag(TabletRootWidget root, TabletUiState state, Runnable refresher, double mouseX, double mouseY) {
        if (!TabletRootHitTest.isInsideChapterPanel(state, root.getPositionX(), root.getPositionY(), mouseX, mouseY)) {
            return false;
        }
        int localX = localChapterX(root, mouseX);
        int localY = localChapterY(root, mouseY);
        if (!TabletUiFactory.isChapterScrollBarHit(localX, localY, state)) {
            return false;
        }
        state.chapterDragPending = false;
        state.chapterDragActive = false;
        state.chapterDragName = "";
        state.chapterDragTargetIndex = -1;
        state.chapterScrollDragging = true;
        int previous = state.chapterScroll;
        TabletUiFactory.updateChapterScrollByMouse(localY, state);
        if (state.chapterScroll != previous) {
            refresher.run();
        }
        return true;
    }

    private static void updateChapterScrollDrag(TabletRootWidget root, TabletUiState state, Runnable refresher, double mouseY) {
        int previous = state.chapterScroll;
        TabletUiFactory.updateChapterScrollByMouse(localChapterY(root, mouseY), state);
        if (state.chapterScroll != previous) {
            refresher.run();
        }
    }

    private static int localRootX(TabletRootWidget root, double mouseX) {
        return (int) Math.round(mouseX - root.getPositionX());
    }

    private static int localRootY(TabletRootWidget root, double mouseY) {
        return (int) Math.round(mouseY - root.getPositionY());
    }

    private static int localChapterX(TabletRootWidget root, double mouseX) {
        return (int) Math.round(mouseX - root.getPositionX() - TabletUiFactory.CHAPTER_X);
    }

    private static int localChapterY(TabletRootWidget root, double mouseY) {
        return (int) Math.round(mouseY - root.getPositionY() - TabletUiFactory.CHAPTER_Y);
    }

    @FunctionalInterface
    interface MouseClickDelegate {
        boolean invoke(double mouseX, double mouseY, int button);
    }

    @FunctionalInterface
    interface MouseDragDelegate {
        boolean invoke(double mouseX, double mouseY, int button, double dragX, double dragY);
    }

    @FunctionalInterface
    interface MouseWheelDelegate {
        boolean invoke(double mouseX, double mouseY, double wheelDelta);
    }
}
