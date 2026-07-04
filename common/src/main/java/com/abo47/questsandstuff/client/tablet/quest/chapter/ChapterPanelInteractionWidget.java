package com.abo47.questsandstuff.client.tablet.quest.chapter;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.CardDragSortUtil;
import com.abo47.questsandstuff.client.tablet.controls.ScrollMath;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuAnimationBridge;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorChapterCommandClient;
import com.abo47.questsandstuff.client.tablet.layout.TabletPanelChrome;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalStateQueries;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.widget.TabletWidgetCoordinates;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_X;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CHAPTER_Y;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.CONTENT_ICON_SIZE;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterAtY;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterInsertIndexAtY;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.chapterRowStep;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.isChapterCardAreaHit;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.isChapterScrollBarHit;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.persistUiState;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.runChapterAction;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.updateChapterScrollByMouse;

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
        TabletPanelChrome.drawPanelChrome(graphics, this, state);
        drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
        TabletPanelChrome.drawPanelOutline(graphics, this, state);
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
            state.chapterPanel.chapterScrollDragging = true;
            int previous = state.chapterPanel.chapterScroll;
            updateChapterScrollByMouse(localY, state);
            if (state.chapterPanel.chapterScroll != previous) {
                refreshChapterViews.run();
            }
            return true;
        }
        if (button == 0 && isMouseOverElement(mouseX, mouseY)) {
            if (!state.canvas.pendingChapterRename.isBlank() || ModalStateQueries.anyOpen(state) || state.chapterPanel.chapterTextMenuOpen) {
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
        if (state.chapterPanel.chapterScrollDragging) {
            int previous = state.chapterPanel.chapterScroll;
            updateChapterScrollByMouse(TabletWidgetCoordinates.localY(this, CHAPTER_Y, mouseY), state);
            if (state.chapterPanel.chapterScroll != previous) {
                refreshChapterViews.run();
            }
            return true;
        }
        if (state.chapterPanel.chapterDragActive) {
            int localY = TabletWidgetCoordinates.localY(this, CHAPTER_Y, mouseY);
            int nextTarget = chapterInsertIndexAtY(localY, state);
            if (nextTarget != state.chapterPanel.chapterDragTargetIndex) {
                state.chapterPanel.chapterDragTargetIndex = nextTarget;
                QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag preview moving={} targetIndex={}", state.chapterPanel.chapterDragName, nextTarget);
                refreshChapterViews.run();
            }
            return true;
        }
        if (state.chapterPanel.chapterDragPending && button == 0) {
            if (!CardDragSortUtil.pastDragThreshold(mouseX, mouseY, state.chapterPanel.chapterDragStartX, state.chapterPanel.chapterDragStartY)) {
                return true;
            }
            state.chapterPanel.chapterDragPending = false;
            state.chapterPanel.chapterDragActive = true;
            int localY = TabletWidgetCoordinates.localY(this, CHAPTER_Y, mouseY);
            state.chapterPanel.chapterDragTargetIndex = chapterInsertIndexAtY(localY, state);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag start moving={} targetIndex={}", state.chapterPanel.chapterDragName, state.chapterPanel.chapterDragTargetIndex);
            refreshChapterViews.run();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (state.chapterPanel.chapterScrollDragging) {
            state.chapterPanel.chapterScrollDragging = false;
            refreshChapterViews.run();
            return true;
        }
        if (state.chapterPanel.chapterDragActive) {
            finishChapterDrag();
            return true;
        }
        if (state.chapterPanel.chapterDragPending) {
            state.chapterPanel.chapterDragPending = false;
            state.chapterPanel.chapterDragName = "";
            state.chapterPanel.chapterDragTargetIndex = -1;
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
        int step = Math.max(8, chapterRowStep(state) / 3);
        int next = ScrollMath.wheel(state.chapterPanel.chapterScroll, state.chapterPanel.chapterScrollMax, step, wheelDelta);
        if (next != state.chapterPanel.chapterScroll) {
            state.chapterPanel.chapterScroll = next;
            refreshChapterViews.run();
        }
        return true;
    }

    private void openContextAt(int localX, int localY) {
        String hit = chapterAtY(localY, state);
        if (hit != null && !hit.isBlank()) {
            if (canOpenChapter(hit)) {
                selectChapterDirect(hit);
            }
        } else {
            clearChapterSelection();
        }
        state.chapterPanel.chapterDragPending = false;
        state.chapterPanel.chapterDragActive = false;
        state.chapterPanel.chapterDragName = "";
        state.chapterPanel.chapterDragTargetIndex = -1;
        if (state.root.canEdit) {
            String menuTarget = hit == null || hit.isBlank() ? "" : hit;
            state.chapterPanel.chapterMenuOpen = true;
            ContextMenuAnimationBridge.start(state, ContextMenuAnimationBridge.CHAPTER_KEY);
            state.chapterPanel.chapterMenuOpenedByClick = true;
            state.chapterPanel.chapterMenuTarget = menuTarget;
            state.chapterPanel.chapterMenuX = CHAPTER_X + localX;
            state.chapterPanel.chapterMenuY = CHAPTER_Y + localY;
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter context menu open target={} emptyArea={}", menuTarget.isBlank() ? "<none>" : menuTarget, hit == null || hit.isBlank());
        }
        refresh.run();
    }

    private boolean selectOrClearAt(double mouseX, double mouseY, int localX, int localY) {
        String hit = chapterAtY(localY, state);
        if (hit != null && !hit.isBlank() && isChapterCardAreaHit(localX, localY, state)) {
            if (!canOpenChapter(hit)) {
                state.chapterPanel.chapterMenuOpen = false;
                state.chapterPanel.chapterDragPending = false;
                state.chapterPanel.chapterDragActive = false;
                state.chapterPanel.chapterDragName = "";
                state.chapterPanel.chapterDragTargetIndex = -1;
                refresh.run();
                return true;
            }
            selectChapterDirect(hit);
            state.chapterPanel.chapterMenuOpen = false;
            if (EditorChapterCommandClient.canManageChapters(state) && (state.chapterPanel.chapterSearch == null || state.chapterPanel.chapterSearch.isBlank())) {
                state.chapterPanel.chapterDragPending = true;
                state.chapterPanel.chapterDragStartX = (int) Math.round(mouseX);
                state.chapterPanel.chapterDragStartY = (int) Math.round(mouseY);
                state.chapterPanel.chapterDragActive = false;
                state.chapterPanel.chapterDragName = hit;
                state.chapterPanel.chapterDragTargetIndex = chapterInsertIndexAtY(localY, state);
            }
            refresh.run();
            return true;
        }
        if (isChapterCardAreaHit(localX, localY, state)) {
            clearChapterSelection();
            state.chapterPanel.chapterMenuOpen = false;
            refresh.run();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, 0);
    }

    private boolean openIconPickerAt(int localX, int localY) {
        if (!EditorChapterCommandClient.canManageChapters(state) || !isChapterCardAreaHit(localX, localY, state) || !isChapterIconHit(localX, localY)) {
            return false;
        }
        String hit = chapterAtY(localY, state);
        if (hit == null || hit.isBlank()) {
            return false;
        }
        selectChapterDirect(hit);
        state.chapterPanel.chapterMenuOpen = false;
        state.chapterPanel.chapterDragPending = false;
        state.chapterPanel.chapterDragActive = false;
        state.chapterPanel.chapterDragName = "";
        state.chapterPanel.chapterDragTargetIndex = -1;
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
        int cardLeft = state.chapterPanel.chapterCardHitLeft;
        int cardRight = state.chapterPanel.chapterCardHitRight;
        int cardWidth = Math.max(1, cardRight - cardLeft);
        boolean collapsed = state.chapterPanel.chapterPanelCollapsed || state.chapterPanel.chapterListWidth <= 54;
        int iconLeft = collapsed ? cardLeft + Math.max(0, (cardWidth - CONTENT_ICON_SIZE) / 2) : cardLeft + 2;
        int rowStep = chapterRowStep(state);
        int iconTop = state.chapterPanel.chapterRowStartY + index * rowStep - state.chapterPanel.chapterScroll + (collapsed ? Math.max(0, (rowStep - CONTENT_ICON_SIZE) / 2) : 8);
        return localX >= iconLeft
                && localX < iconLeft + CONTENT_ICON_SIZE
                && localY >= iconTop
                && localY < iconTop + CONTENT_ICON_SIZE;
    }

    private java.util.List<String> visibleChapterGroups() {
        String query = SearchFilter.normalize(state.chapterPanel.chapterSearch);
        java.util.List<String> visible = new java.util.ArrayList<>();
        for (String chapter : ClientQuestStateFacade.chapterOrder()) {
            if (com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.DRAFT_CHAPTER.equals(chapter)) {
                continue;
            }
            if (!state.root.canEdit && ClientQuestStateFacade.chapterHiddenPreview(chapter)) {
                continue;
            }
            if (!SearchFilter.matches(query, chapter)) {
                continue;
            }
            visible.add(chapter);
        }
        return visible;
    }

    private void finishChapterDrag() {
        String moving = state.chapterPanel.chapterDragName;
        int target = Math.max(0, state.chapterPanel.chapterDragTargetIndex);
        state.chapterPanel.chapterDragActive = false;
        state.chapterPanel.chapterDragName = "";
        state.chapterPanel.chapterDragTargetIndex = -1;
        if (!moving.isBlank()) {
            int fromIndex = ClientQuestStateFacade.chapterOrder().indexOf(moving);
            int size = ClientQuestStateFacade.chapterOrder().size();
            target = CardDragSortUtil.targetIndexAfterDrop(fromIndex, target, size);
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter drag drop moving={} fromIndex={} targetIndex={}", moving, fromIndex, target);
            if (fromIndex >= 0 && target >= 0 && target != fromIndex) {
                runChapterAction(player, state, "move_to", moving, "", target);
            }
            state.root.selectedChapter = moving;
            persistUiState(state);
        }
        refresh.run();
    }

    private void clearChapterSelection() {
        state.root.selectedChapter = "";
        state.chapterPanel.chapterDraft = "";
        state.chapterPanel.chapterDraftName = "";
        state.canvas.pendingChapterRename = "";
        state.chapterPanel.chapterTextMenuOpen = false;
        state.chapterPanel.chapterTextMenuTarget = "";
        state.chapterPanel.chapterTextFontSizeFieldTarget = "";
        state.chapterPanel.chapterSelectionJustChanged = false;
        persistUiState(state);
    }

    private void selectChapterDirect(String chapter) {
        if (chapter == null || chapter.isBlank()) {
            return;
        }
        if (!canOpenChapter(chapter)) {
            return;
        }
        state.root.selectedChapter = chapter;
        state.chapterPanel.chapterDraft = chapter;
        state.chapterPanel.chapterDraftName = chapter;
        state.canvas.pendingChapterRename = "";
        state.chapterPanel.chapterTextMenuOpen = false;
        state.chapterPanel.chapterTextMenuTarget = "";
        state.chapterPanel.chapterTextFontSizeFieldTarget = "";
        state.chapterPanel.chapterSelectionJustChanged = true;
        ClientQuestStateFacade.clearChapterCompletionNotice(chapter);
        persistUiState(state);
    }

    private boolean canOpenChapter(String chapter) {
        return state.root.canEdit || ClientQuestStateFacade.chapterOpenablePreview(chapter);
    }
}
