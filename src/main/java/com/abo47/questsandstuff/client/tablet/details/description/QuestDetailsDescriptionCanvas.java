package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.client.tablet.details.QuestDetailsMouse;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsTransientState;

import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.canvas.viewport.CanvasViewportScissor;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.tools.ToolMenuAnimation;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;

import static com.lowdragmc.lowdraglib.gui.widget.Widget.isShiftDown;

public final class QuestDetailsDescriptionCanvas extends WidgetGroup {
    private final TabletUiState state;
    private final Player player;
    private final Runnable refresh;
    private final String questId;
    private final QuestDetailsDescriptionTransform transforms;
    private final QuestDetailsDescriptionTextEdit textEdit;
    private final QuestDetailsDescriptionSelection selection;
    private final QuestDetailsDescriptionHitTest hitTest;

    QuestDetailsDescriptionCanvas(int x, int y, int w, int h, TabletUiState state, Player player, Runnable refresh, String questId) {
        super(x, y, w, h);
        this.state = state;
        this.player = player;
        this.refresh = refresh;
        this.questId = questId;
        this.transforms = new QuestDetailsDescriptionTransform(state, this::contentX, this::contentY, this::contentW, this::contentH);
        this.textEdit = new QuestDetailsDescriptionTextEdit(state, refresh, questId, this::contentW, this::contentH);
        this.selection = new QuestDetailsDescriptionSelection(state, this::contentX, this::contentY, this::contentW, this::contentH);
        this.hitTest = new QuestDetailsDescriptionHitTest(state, selection, this::contentW, this::contentH);
    }

    @Override
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        drawBackgroundTexture(graphics, mouseX, mouseY);
        withScissor(graphics, () -> {
            QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
            QuestDetailsDescriptionCanvasRenderer.drawContent(graphics, state, model, contentX(), contentY(), contentW(), contentH());
            selection.drawMultiSelectionBounds(graphics, model);
            selection.drawBoxSelection(graphics);
            drawWidgetsBackground(graphics, mouseX, mouseY, partialTicks);
        });
    }

    @Override
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        withScissor(graphics, () -> drawWidgetsForeground(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public void drawOverlay(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        withScissor(graphics, () -> super.drawOverlay(graphics, mouseX, mouseY, partialTicks));
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (isQuestDetailsTextStyleMenuHit(mouseX, mouseY)) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        if (!isMouseOverElement(mouseX, mouseY)) {
            return super.mouseWheelMove(mouseX, mouseY, wheelDelta);
        }
        state.questDetailsDescScroll = Math.max(0, state.questDetailsDescScroll + (wheelDelta < 0 ? 18 : -18));
        refresh.run();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isQuestDetailsTextStyleMenuHit(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (!isMouseOverElement(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 2) {
            state.questDetailsPanning = true;
            state.questDetailsPanStartX = (int) Math.round(mouseX);
            state.questDetailsPanStartY = (int) Math.round(mouseY);
            state.questDetailsPanStartScroll = state.questDetailsDescScroll;
            QuestDetailsTransientState.closeContext(state);
            ToolMenuAnimation.closeQuestDetails(state);
            return true;
        }
        if (!state.canEdit || !state.questDetailsEditMode) {
            return true;
        }
        int lx = localX(mouseX);
        int visibleY = localY(mouseY);
        int ly = visibleY + state.questDetailsDescScroll;
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        QuestDetailsDescriptionHitTest.Hit hit = hitTest.hit(model, lx, visibleY, ly);
        if (button == 0 && textEdit.isEditing()) {
            CanvasTextLayer editingText = model.text(state.questDetailsTextEditTarget);
            QuestDetailsDescriptionHitTest.Rect editingRect = editingText == null
                    ? null
                    : new QuestDetailsDescriptionHitTest.Rect(editingText.x(), editingText.y(), editingText.w(), editingText.h(), editingText.rotation());
            boolean transformHandleHit = editingRect != null && (hitTest.inResizeHandle(editingRect, lx, visibleY) || hitTest.inRotateHandle(editingRect, lx, visibleY));
            if (transformHandleHit) {
                textEdit.finish("transform-start");
            } else if (editingText != null && textEdit.hitTextEditor(editingText, lx, visibleY)) {
                textEdit.updateCursor(model, editingText.id(), lx, visibleY, !isShiftDown());
                state.selectingCanvasTextRange = true;
                setFocus(true);
                refresh.run();
                return true;
            } else {
                textEdit.finish("outside-click");
                refresh.run();
                return true;
            }
        }
        if (button == 1) {
            boolean selectionHit = (hit == null && selection.selectionBoundsHit(model, lx, visibleY))
                    || (hit != null && selection.count() > 1 && hitTest.isHitSelected(hit));
            storeContextPosition(mouseX, mouseY, lx, visibleY);
            QuestDetailsTransientState.openContext(
                    state,
                    selectionHit ? "desc_selection" : (hit == null ? "description" : hit.kind()),
                    hit == null ? "" : hit.id(),
                    state.questDetailsContextX,
                    state.questDetailsContextY
            );
            ToolMenuAnimation.closeQuestDetails(state);
            EntityMotionEditor.close(state);
            refresh.run();
            return true;
        }
        if (button != 0) {
            return true;
        }
        QuestDetailsTransientState.closeContext(state);
        QuestDetailsTransientState.closeTypePicker(state);
        if (hit == null) {
            if (selection.selectionBoundsHit(model, lx, visibleY)) {
                transforms.beginSelectionTransform(model, lx, visibleY);
                state.questDetailsTextStyleOpen = false;
                refresh.run();
                return true;
            }
            textEdit.finish("canvas-click");
            state.questDetailsBoxSelecting = true;
            state.questDetailsBoxAdditive = isShiftDown();
            state.questDetailsBoxStartX = lx;
            state.questDetailsBoxStartY = visibleY;
            state.questDetailsBoxCurrentX = lx;
            state.questDetailsBoxCurrentY = visibleY;
            if (!state.questDetailsBoxAdditive) {
                selection.clear();
            }
            refresh.run();
            return true;
        }
        QuestDetailsDescriptionHitTest.Rect hitRect = hitTest.rect(model, hit);
        boolean resizeHit = hitTest.inResizeHandle(hitRect, lx, visibleY);
        boolean rotateHit = hitTest.inRotateHandle(hitRect, lx, visibleY);
        if (isShiftDown() && !resizeHit && !rotateHit) {
            toggleSelection(hit);
            state.questDetailsTextStyleOpen = false;
            refresh.run();
            return true;
        }
        if ("desc_text".equals(hit.kind()) && !resizeHit && !rotateHit && handleTextDoubleClick(hit)) {
            select(hit);
            textEdit.begin(model, hit.id(), () -> setFocus(true));
            refresh.run();
            return true;
        }
        select(hit);
        if ("desc_text".equals(hit.kind()) && state.canvasTextEditOpen && hit.id().equals(state.canvasTextEditTarget)) {
            textEdit.updateCursor(model, hit.id(), lx, visibleY, true);
            setFocus(true);
            refresh.run();
            return true;
        }
        beginTransform(model, hit, lx, visibleY, ly);
        refresh.run();
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isQuestDetailsTextStyleMenuHit(mouseX, mouseY)) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        if (state.questDetailsPanning) {
            int dy = (int) Math.round(mouseY) - state.questDetailsPanStartY;
            state.questDetailsDescScroll = Math.max(0, state.questDetailsPanStartScroll - dy);
            refresh.run();
            return true;
        }
        if (textEdit.dragSelectionTo(localX(mouseX), localY(mouseY))) {
            return true;
        }
        if (state.questDetailsBoxSelecting) {
            state.questDetailsBoxCurrentX = localX(mouseX);
            state.questDetailsBoxCurrentY = localY(mouseY);
            refresh.run();
            return true;
        }
        if (state.questDetailsTransformId.isBlank()) {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        transforms.applyTransform(model, pointerScreenX(mouseX), pointerScreenY(mouseY));
        QuestDetailsDescriptionModel.preview(questId, model);
        refresh.run();
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (textEdit.handleKey(keyCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (textEdit.handleChar(codePoint)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (isQuestDetailsTextStyleMenuHit(mouseX, mouseY)) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        if (state.questDetailsPanning) {
            state.questDetailsPanning = false;
            refresh.run();
            return true;
        }
        if (state.selectingCanvasTextRange && textEdit.isEditing()) {
            state.selectingCanvasTextRange = false;
            refresh.run();
            return true;
        }
        if (state.questDetailsBoxSelecting) {
            state.questDetailsBoxSelecting = false;
            QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
            selection.finishBoxSelection(model);
            refresh.run();
            return true;
        }
        if (state.questDetailsTransformId.isBlank()) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        transforms.applyTransform(model, pointerScreenX(mouseX), pointerScreenY(mouseY));
        QuestDetailsDescriptionModel.save(player, questId, model);
        state.questDetailsTransformId = "";
        state.questDetailsTransformKind = "";
        state.questDetailsTransformMode = "";
        state.dragStartTextPositions.clear();
        state.dragStartImagePositions.clear();
        state.snapGuideXVisible = false;
        state.snapGuideYVisible = false;
        refresh.run();
        return true;
    }

    private void beginTransform(QuestDetailsDescriptionModel model, QuestDetailsDescriptionHitTest.Hit hit, int lx, int visibleY, int ly) {
        QuestDetailsDescriptionHitTest.Rect rect = hitTest.rect(model, hit);
        boolean resizeHit = hitTest.inResizeHandle(rect, lx, visibleY);
        boolean rotateHit = hitTest.inRotateHandle(rect, lx, visibleY);
        boolean selectionMove = selection.count() > 1 && hitTest.isHitSelected(hit) && !resizeHit && !rotateHit;
        transforms.beginTransform(model, hit.kind(), hit.id(), new QuestDetailsDescriptionTransform.ElementRect(rect.x(), rect.y(), rect.w(), rect.h(), rect.rotation()), selectionMove, resizeHit, rotateHit, lx, visibleY, ly);
    }

    private void select(QuestDetailsDescriptionHitTest.Hit hit) {
        state.questDetailsSelectedObjectiveKind = "";
        state.questDetailsSelectedObjectiveId = "";
        if ("desc_text".equals(hit.kind())) {
            if (selection.count() > 1 && selection.isSelectedText(hit.id())) {
                state.questDetailsSelectedTextId = hit.id();
            } else {
                state.questDetailsSelectedTextIds.clear();
                state.questDetailsSelectedImageIds.clear();
                state.questDetailsSelectedTextIds.add(hit.id());
                state.questDetailsSelectedImageId = "";
            }
            state.questDetailsSelectedTextId = hit.id();
        } else {
            if (selection.count() > 1 && selection.isSelectedImage(hit.id())) {
                state.questDetailsSelectedImageId = hit.id();
            } else {
                state.questDetailsSelectedTextIds.clear();
                state.questDetailsSelectedImageIds.clear();
                state.questDetailsSelectedImageIds.add(hit.id());
                state.questDetailsSelectedTextId = "";
            }
            state.questDetailsSelectedImageId = hit.id();
            state.questDetailsTextStyleOpen = false;
        }
    }

    private void toggleSelection(QuestDetailsDescriptionHitTest.Hit hit) {
        state.questDetailsSelectedObjectiveKind = "";
        state.questDetailsSelectedObjectiveId = "";
        if ("desc_text".equals(hit.kind())) {
            toggle(state.questDetailsSelectedTextIds, hit.id());
            state.questDetailsSelectedTextId = state.questDetailsSelectedTextIds.contains(hit.id()) ? hit.id() : state.questDetailsSelectedTextIds.stream().findFirst().orElse("");
        } else {
            toggle(state.questDetailsSelectedImageIds, hit.id());
            state.questDetailsSelectedImageId = state.questDetailsSelectedImageIds.contains(hit.id()) ? hit.id() : state.questDetailsSelectedImageIds.stream().findFirst().orElse("");
        }
    }

    private boolean handleTextDoubleClick(QuestDetailsDescriptionHitTest.Hit hit) {
        long now = System.currentTimeMillis();
        boolean doubleClick = hit.id().equals(state.questDetailsTextLastClickId) && now - state.questDetailsTextLastClickAtMs <= 350L;
        state.questDetailsTextLastClickId = hit.id();
        state.questDetailsTextLastClickAtMs = now;
        return doubleClick;
    }

    private int localX(double mouseX) {
        return QuestDetailsMouse.localCoord(mouseX, contentX(), contentW());
    }

    private int localY(double mouseY) {
        return QuestDetailsMouse.localCoord(mouseY, contentY(), contentH());
    }

    private int pointerScreenX(double mouseX) {
        return contentX() + localX(mouseX);
    }

    private int pointerScreenY(double mouseY) {
        return contentY() + localY(mouseY);
    }

    private void withScissor(GuiGraphics graphics, Runnable draw) {
        CanvasViewportScissor.draw(graphics, contentX(), contentY(), contentW() + 1, contentH() + 1, draw);
    }

    private int contentX() {
        return getPositionX();
    }

    private int contentY() {
        return getPositionY();
    }

    private int contentW() {
        return Math.max(1, getSizeWidth() - 1);
    }

    private int contentH() {
        return Math.max(1, getSizeHeight() - 1);
    }

    private void storeContextPosition(double mouseX, double mouseY, int lx, int ly) {
        QuestDetailsMouse.openContextAtPointer(state, state.questDetailsContextKind, state.questDetailsContextId, mouseX, mouseY, getPositionX(), getPositionY(), lx, ly);
    }

    private boolean isQuestDetailsTextStyleMenuHit(double mouseX, double mouseY) {
        if (!state.questDetailsTextStyleOpen || state.questDetailsTextStyleMenuW <= 0 || state.questDetailsTextStyleMenuH <= 0) {
            return false;
        }
        if (QuestDetailsWindow.isTextStyleMenuHit(state, mouseX, mouseY)) {
            return true;
        }
        int localMenuX = state.questDetailsTextStyleMenuX - contentX();
        int localMenuY = state.questDetailsTextStyleMenuY - contentY();
        return inside(mouseX, mouseY, localMenuX, localMenuY, state.questDetailsTextStyleMenuW, state.questDetailsTextStyleMenuH)
                || inside(mouseX, mouseY, state.questDetailsScreenX + localMenuX, state.questDetailsScreenY + localMenuY,
                state.questDetailsTextStyleMenuW, state.questDetailsTextStyleMenuH);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static void toggle(java.util.Set<String> values, String id) {
        if (!values.add(id)) {
            values.remove(id);
        }
    }
}
