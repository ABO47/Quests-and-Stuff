package com.abo47.questsandstuff.client.tablet.chapter;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.CardReorderController;
import com.abo47.questsandstuff.client.tablet.controls.ScrollController;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletWidgetCoordinates;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_CARD_GAP;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_CARD_H;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_X;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CHAPTER_Y;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.CONTENT_ICON_SIZE;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterAtY;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.chapterInsertIndexAtY;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.isChapterCardAreaHit;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.isChapterScrollBarHit;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.runGroupAction;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.updateChapterScrollByMouse;

public final class ChapterPanelInteractionWidget extends WidgetGroup {
    private final TabletUiState state;
    private final Player player;
    private final Runnable refresh;
    private final Runnable refreshChapterViews;

    public ChapterPanelInteractionWidget(int x, int y, int width, int height, TabletUiState state, Player player, Runnable refresh, Runnable refreshChapterViews) {
        super(x, y, width, height);
        this.state = state;
        this.player = player;
        this.refresh = refresh;
        this.refreshChapterViews = refreshChapterViews;
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        TabletPanelChrome.drawPanelChrome(graphics, this);
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
        TabletPanelChrome.drawPanelOutline(graphics, this);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (ModalStateQueries.anyOpen(state)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int localX = TabletWidgetCoordinates.localX(this, CHAPTER_X, mouseX);
        int localY = TabletWidgetCoordinates.localY(this, CHAPTER_Y, mouseY);
        if (button == 1 && isMouseOverElement(mouseX, mouseY) && isChapterCardAreaHit(localX, localY, state)) {
            openContextAt(localX, localY);
            return true;
        }
        if (button == 0 && isMouseOverElement(mouseX, mouseY) && isChapterScrollBarHit(localX, localY, state)) {
            state.chapterScrollDragging = true;
            int previous = state.chapterScroll;
            updateChapterScrollByMouse(localY, state);
            if (state.chapterScroll != previous) {
                refreshChapterViews.run();
            }
            return true;
        }
        if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
            if (!state.pendingChapterRename.isBlank() || state.iconPickerOpen || state.assetPickerOpen || state.biomePickerOpen || state.lootTablePickerOpen || state.chapterTextMenuOpen) {
                return super.mouseClicked(mouseX, mouseY, button);
            }
            if (openIconPickerAt(localX, localY)) {
                return true;
            }
            return selectOrClearAt(mouseX, mouseY, localX, localY);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (state.chapterScrollDragging) {
            int previous = state.chapterScroll;
            updateChapterScrollByMouse(TabletWidgetCoordinates.localY(this, CHAPTER_Y, mouseY), state);
            if (state.chapterScroll != previous) {
                refreshChapterViews.run();
            }
            return true;
        }
        if (state.chapterDragActive) {
            int localY = TabletWidgetCoordinates.localY(this, CHAPTER_Y, mouseY);
            int nextTarget = chapterInsertIndexAtY(localY, state);
            if (nextTarget != state.chapterDragTargetIndex) {
                state.chapterDragTargetIndex = nextTarget;
                QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag preview moving={} targetIndex={}", state.chapterDragName, nextTarget);
                refreshChapterViews.run();
            }
            return true;
        }
        if (state.chapterDragPending && button == 0) {
            if (!CardReorderController.pastDragThreshold(mouseX, mouseY, state.chapterDragStartX, state.chapterDragStartY)) {
                return true;
            }
            state.chapterDragPending = false;
            state.chapterDragActive = true;
            int localY = TabletWidgetCoordinates.localY(this, CHAPTER_Y, mouseY);
            state.chapterDragTargetIndex = chapterInsertIndexAtY(localY, state);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag start moving={} targetIndex={}", state.chapterDragName, state.chapterDragTargetIndex);
            refreshChapterViews.run();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (state.chapterScrollDragging) {
            state.chapterScrollDragging = false;
            refreshChapterViews.run();
            return true;
        }
        if (state.chapterDragActive) {
            finishChapterDrag();
            return true;
        }
        if (state.chapterDragPending) {
            state.chapterDragPending = false;
            state.chapterDragName = "";
            state.chapterDragTargetIndex = -1;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (!isMouseOverElement(mouseX, mouseY)) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        if (ModalStateQueries.anyOpen(state)) {
            return true;
        }
        int step = Math.max(8, (CHAPTER_CARD_H + CHAPTER_CARD_GAP) / 3);
        int next = ScrollController.wheel(state.chapterScroll, state.chapterScrollMax, step, wheelDelta);
        if (next != state.chapterScroll) {
            state.chapterScroll = next;
            refreshChapterViews.run();
        }
        return true;
    }

    private void openContextAt(int localX, int localY) {
        String hit = chapterAtY(localY, state);
        if (hit != null && !hit.isBlank()) {
            selectChapterDirect(hit);
        } else {
            clearChapterSelection();
        }
        state.chapterDragPending = false;
        state.chapterDragActive = false;
        state.chapterDragName = "";
        state.chapterDragTargetIndex = -1;
        if (state.canEdit) {
            String menuTarget = hit == null || hit.isBlank() ? "" : hit;
            state.chapterMenuOpen = true;
            ContextMenuAnimation.start(state, ContextMenuAnimation.CHAPTER_KEY);
            state.chapterMenuOpenedByClick = true;
            state.chapterMenuTarget = menuTarget;
            state.chapterMenuX = CHAPTER_X + localX;
            state.chapterMenuY = CHAPTER_Y + localY;
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter context menu open target={} emptyArea={}", menuTarget.isBlank() ? "<none>" : menuTarget, hit == null || hit.isBlank());
        }
        refresh.run();
    }

    private boolean selectOrClearAt(double mouseX, double mouseY, int localX, int localY) {
        String hit = chapterAtY(localY, state);
        if (hit != null && !hit.isBlank() && isChapterCardAreaHit(localX, localY, state)) {
            selectChapterDirect(hit);
            state.chapterMenuOpen = false;
            if (EditorCommandClient.canManageGroups(state) && (state.chapterSearch == null || state.chapterSearch.isBlank())) {
                state.chapterDragPending = true;
                state.chapterDragStartX = (int) Math.round(mouseX);
                state.chapterDragStartY = (int) Math.round(mouseY);
                state.chapterDragActive = false;
                state.chapterDragName = hit;
                state.chapterDragTargetIndex = chapterInsertIndexAtY(localY, state);
            }
            refresh.run();
            return true;
        }
        if (isChapterCardAreaHit(localX, localY, state)) {
            clearChapterSelection();
            state.chapterMenuOpen = false;
            refresh.run();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, 0);
    }

    private boolean openIconPickerAt(int localX, int localY) {
        if (!EditorCommandClient.canManageGroups(state) || !isChapterCardAreaHit(localX, localY, state) || !isChapterIconHit(localX, localY)) {
            return false;
        }
        String hit = chapterAtY(localY, state);
        if (hit == null || hit.isBlank()) {
            return false;
        }
        selectChapterDirect(hit);
        state.chapterMenuOpen = false;
        state.chapterDragPending = false;
        state.chapterDragActive = false;
        state.chapterDragName = "";
        state.chapterDragTargetIndex = -1;
        ModalOpenActions.openChapterIconPicker(state, hit);
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter icon picker open target={}", hit);
        refresh.run();
        return true;
    }

    private boolean isChapterIconHit(int localX, int localY) {
        int index = visibleChapterGroups().indexOf(chapterAtY(localY, state));
        if (index < 0) {
            return false;
        }
        int cardLeft = state.chapterCardHitLeft;
        int cardRight = state.chapterCardHitRight;
        int cardWidth = Math.max(1, cardRight - cardLeft);
        boolean collapsed = state.chapterPanelCollapsed || state.chapterListWidth <= 54;
        int iconLeft = collapsed ? cardLeft + Math.max(0, (cardWidth - CONTENT_ICON_SIZE) / 2) : cardLeft + 2;
        int iconTop = state.chapterRowStartY + index * (CHAPTER_CARD_H + CHAPTER_CARD_GAP) - state.chapterScroll + 8;
        return localX >= iconLeft
                && localX < iconLeft + CONTENT_ICON_SIZE
                && localY >= iconTop
                && localY < iconTop + CONTENT_ICON_SIZE;
    }

    private java.util.List<String> visibleChapterGroups() {
        String query = SearchFilter.normalize(state.chapterSearch);
        java.util.List<String> visible = new java.util.ArrayList<>();
        for (String group : ClientQuestCache.groupOrder()) {
            if (com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.DRAFT_CHAPTER.equals(group)) {
                continue;
            }
            if (!SearchFilter.matches(query, group)) {
                continue;
            }
            visible.add(group);
        }
        return visible;
    }

    private void finishChapterDrag() {
        String moving = state.chapterDragName;
        int target = Math.max(0, state.chapterDragTargetIndex);
        state.chapterDragActive = false;
        state.chapterDragName = "";
        state.chapterDragTargetIndex = -1;
        if (!moving.isBlank()) {
            int fromIndex = ClientQuestCache.groupOrder().indexOf(moving);
            int size = ClientQuestCache.groupOrder().size();
            target = CardReorderController.targetIndexAfterDrop(fromIndex, target, size);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag drop moving={} fromIndex={} targetIndex={}", moving, fromIndex, target);
            if (fromIndex >= 0 && target >= 0 && target != fromIndex) {
                runGroupAction(player, state, "move_to", moving, "", target);
            }
            state.selectedGroup = moving;
            persistUiState(state);
        }
        refresh.run();
    }

    private void clearChapterSelection() {
        state.selectedGroup = "";
        state.groupDraft = "";
        state.chapterDraftName = "";
        state.pendingChapterRename = "";
        state.chapterTextMenuOpen = false;
        state.chapterTextMenuTarget = "";
        state.chapterTextFontSizeSliderTarget = "";
        state.chapterSelectionJustChanged = false;
        persistUiState(state);
    }

    private void selectChapterDirect(String group) {
        if (group == null || group.isBlank()) {
            return;
        }
        state.selectedGroup = group;
        state.groupDraft = group;
        state.chapterDraftName = group;
        state.pendingChapterRename = "";
        state.chapterTextMenuOpen = false;
        state.chapterTextMenuTarget = "";
        state.chapterTextFontSizeSliderTarget = "";
        state.chapterSelectionJustChanged = true;
        persistUiState(state);
    }
}
