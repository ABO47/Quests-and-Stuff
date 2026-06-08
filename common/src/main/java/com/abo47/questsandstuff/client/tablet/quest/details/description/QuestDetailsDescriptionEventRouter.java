package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.input.TabletModifierKeys;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformMode;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsMouse;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.tools.ToolMenuAnimation;
import com.abo47.questsandstuff.client.tablet.root.TabletRootWindowController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.world.entity.player.Player;

final class QuestDetailsDescriptionEventRouter {
    private final TabletUiState state;
    private final Player player;
    private final Runnable refresh;
    private final String questId;
    private final QuestDetailsDescriptionTransform transforms;
    private final QuestDetailsDescriptionTextEdit textEdit;
    private final QuestDetailsDescriptionSelection selection;
    private final QuestDetailsDescriptionHitTest hitTest;
    private final Surface surface;

    QuestDetailsDescriptionEventRouter(
            TabletUiState state,
            Player player,
            Runnable refresh,
            String questId,
            QuestDetailsDescriptionTransform transforms,
            QuestDetailsDescriptionTextEdit textEdit,
            QuestDetailsDescriptionSelection selection,
            QuestDetailsDescriptionHitTest hitTest,
            Surface surface
    ) {
        this.state = state;
        this.player = player;
        this.refresh = refresh;
        this.questId = questId;
        this.transforms = transforms;
        this.textEdit = textEdit;
        this.selection = selection;
        this.hitTest = hitTest;
        this.surface = surface;
    }

    boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        if (shouldYieldToContextMenu(mouseX, mouseY)) {
            return false;
        }
        if (isQuestDetailsTextStyleMenuHit(mouseX, mouseY)) {
            return surface.mouseWheelMoveFallback(mouseX, mouseY, wheelDelta);
        }
        if (!surface.isMouseOverElement(mouseX, mouseY)) {
            return surface.mouseWheelMoveFallback(mouseX, mouseY, wheelDelta);
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        if (QuestDetailsDescriptionInteractionState.scrollByWheel(state, model, surface.contentH(), wheelDelta)) {
            refresh.run();
        }
        return true;
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (shouldYieldToContextMenu(mouseX, mouseY)) {
            return false;
        }
        if (isQuestDetailsTextStyleMenuHit(mouseX, mouseY)) {
            return surface.mouseClickedFallback(mouseX, mouseY, button);
        }
        if (!surface.isMouseOverElement(mouseX, mouseY)) {
            return surface.mouseClickedFallback(mouseX, mouseY, button);
        }
        if (surface.mouseClickedFallback(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 2) {
            QuestDetailsDescriptionInteractionState.beginPanning(state, mouseX, mouseY);
            QuestDetailsTransientState.closeContext(state);
            ToolMenuAnimation.closeQuestDetails(state);
            return true;
        }
        if (!QuestDetailsEditState.canEdit(state)) {
            return true;
        }
        int lx = localX(mouseX);
        int visibleY = localY(mouseY);
        int ly = visibleY + state.questDetailsDescScroll;
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        QuestDetailsDescriptionHitTest.Hit hit = hitTest.hit(model, lx, visibleY);
        if (button == 0 && selection.count() > 1) {
            if (selection.selectionRotateHandleHit(model, lx, visibleY)) {
                transforms.beginSelectionTransform(model, lx, visibleY, "rotate");
                TextStyleSession.closeQuestDetails(state);
                refresh.run();
                return true;
            }
            if (selection.selectionResizeHandleHit(model, lx, visibleY)) {
                transforms.beginSelectionTransform(model, lx, visibleY, "resize");
                TextStyleSession.closeQuestDetails(state);
                refresh.run();
                return true;
            }
        }
        if (button == 0 && textEdit.isEditing() && handleEditingTextClick(model, hit, lx, visibleY)) {
            return true;
        }
        if (button == 1) {
            openContext(mouseX, mouseY, model, hit, lx, visibleY);
            return true;
        }
        if (button != 0) {
            return true;
        }
        QuestDetailsTransientState.closeContext(state);
        QuestDetailsTransientState.closeTypePicker(state);
        if (hit == null) {
            return handleCanvasClick(model, lx, visibleY);
        }
        if ("desc_image".equals(hit.kind())) {
            CanvasImageLayer hitImage = model.image(hit.id());
            if (hitImage != null && CanvasTransformGizmo.supports(hitImage.asset()) && !selection.isSelectedImage(hit.id())) {
                CanvasTransformGizmo.setMode(state, CanvasTransformMode.MOVE);
            }
        }
        QuestDetailsDescriptionHitTest.Rect hitRect = hitTest.rect(model, hit);
        boolean groupHit = selection.count() > 1 && hitTest.isHitSelected(hit);
        CanvasTransformMode clickedGizmoMode = groupHit ? null : hitTest.imageGizmoMode(model, hit, lx, visibleY);
        boolean resizeHit = !groupHit && (clickedGizmoMode == CanvasTransformMode.RESIZE || hitTest.inResizeHandle(hitRect, lx, visibleY));
        boolean rotateHit = !groupHit && (clickedGizmoMode == CanvasTransformMode.ROTATE || hitTest.inRotateHandle(hitRect, lx, visibleY));
        boolean shiftMoveHit = clickedGizmoMode == CanvasTransformMode.MOVE && shiftMoveHit(model, hit);
        if (TabletModifierKeys.shiftDown() && !resizeHit && !rotateHit && !shiftMoveHit) {
            toggleSelection(hit);
            TextStyleSession.closeQuestDetails(state);
            refresh.run();
            return true;
        }
        if ("desc_text".equals(hit.kind()) && !resizeHit && !rotateHit && handleTextDoubleClick(hit)) {
            select(hit);
            textEdit.begin(model, hit.id(), () -> surface.focus(true));
            refresh.run();
            return true;
        }
        select(hit);
        if ("desc_text".equals(hit.kind()) && TextEditSession.isEditingTarget(state, hit.id())) {
            textEdit.updateCursor(model, hit.id(), lx, visibleY, true);
            surface.focus(true);
            refresh.run();
            return true;
        }
        beginTransform(model, hit, lx, visibleY, ly);
        refresh.run();
        return true;
    }

    boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (shouldYieldToContextMenu(mouseX, mouseY)) {
            return false;
        }
        if (isQuestDetailsTextStyleMenuHit(mouseX, mouseY)) {
            return surface.mouseDraggedFallback(mouseX, mouseY, button, dragX, dragY);
        }
        if (state.questDetailsPanning) {
            int dy = (int) Math.round(mouseY) - state.questDetailsPanStartY;
            setScroll(state.questDetailsPanStartScroll - dy);
            return true;
        }
        if (!QuestDetailsEditState.canEdit(state)) {
            return surface.mouseDraggedFallback(mouseX, mouseY, button, dragX, dragY);
        }
        if (textEdit.dragSelectionTo(localX(mouseX), localY(mouseY))) {
            return true;
        }
        if (state.questDetailsBoxSelecting) {
            QuestDetailsDescriptionInteractionState.updateBoxSelection(state, localX(mouseX), localY(mouseY));
            return true;
        }
        if (state.questDetailsTransformId.isBlank()) {
            return surface.mouseDraggedFallback(mouseX, mouseY, button, dragX, dragY);
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        transforms.applyTransform(model, pointerScreenX(mouseX), pointerScreenY(mouseY));
        QuestDetailsDescriptionTransformApply.preview(state, model);
        if ("desc_text".equals(state.questDetailsTransformKind)
                && state.questDetailsTextStyleOpen
                && state.questDetailsTransformId.equals(state.questDetailsTextStyleTarget)) {
            refresh.run();
        }
        return true;
    }

    boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (TabletRootWindowController.isFontSizeFieldOpen(state)) {
            return surface.keyPressedFallback(keyCode, scanCode, modifiers);
        }
        if (QuestDetailsEditState.canEdit(state) && textEdit.handleKey(keyCode)) {
            return true;
        }
        return surface.keyPressedFallback(keyCode, scanCode, modifiers);
    }

    boolean charTyped(char codePoint, int modifiers) {
        if (TabletRootWindowController.isFontSizeFieldOpen(state)) {
            return surface.charTypedFallback(codePoint, modifiers);
        }
        if (QuestDetailsEditState.canEdit(state) && textEdit.handleChar(codePoint)) {
            return true;
        }
        return surface.charTypedFallback(codePoint, modifiers);
    }

    boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (shouldYieldToContextMenu(mouseX, mouseY)) {
            return false;
        }
        if (isQuestDetailsTextStyleMenuHit(mouseX, mouseY)) {
            return surface.mouseReleasedFallback(mouseX, mouseY, button);
        }
        if (state.questDetailsPanning) {
            state.questDetailsPanning = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details description pan commit quest={} scroll={}", questId, state.questDetailsDescScroll);
            refresh.run();
            return true;
        }
        if (!QuestDetailsEditState.canEdit(state)) {
            QuestDetailsDescriptionTransformApply.clearEditDragState(state);
            return surface.mouseReleasedFallback(mouseX, mouseY, button);
        }
        if (state.selectingCanvasTextRange && textEdit.isEditing()) {
            TextEditSession.finishRangeSelection(state);
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
            return surface.mouseReleasedFallback(mouseX, mouseY, button);
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        QuestDetailsDescriptionTransformApply.CommitInfo info = QuestDetailsDescriptionTransformApply.commit(
                player,
                state,
                questId,
                transforms,
                model,
                pointerScreenX(mouseX),
                pointerScreenY(mouseY)
        );
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details description transform commit quest={} kind={} id={} mode={}", questId, info.kind(), info.id(), info.mode());
        refresh.run();
        return true;
    }

    private boolean handleEditingTextClick(QuestDetailsDescriptionModel model, QuestDetailsDescriptionHitTest.Hit hit, int lx, int visibleY) {
        CanvasTextLayer editingText = model.text(state.questDetailsTextEditTarget);
        QuestDetailsDescriptionHitTest.Rect editingRect = editingText == null
                ? null
                : new QuestDetailsDescriptionHitTest.Rect(editingText.x(), editingText.y(), editingText.w(), editingText.h(), editingText.rotation());
        boolean transformHandleHit = editingRect != null && (hitTest.inResizeHandle(editingRect, lx, visibleY) || hitTest.inRotateHandle(editingRect, lx, visibleY));
        if (transformHandleHit) {
            textEdit.finish("transform-start");
            return false;
        }
        if (editingText != null && textEdit.hitTextEditor(editingText, lx, visibleY)) {
            textEdit.updateCursor(model, editingText.id(), lx, visibleY, !TabletModifierKeys.shiftDown());
            TextEditSession.startRangeSelection(state);
            surface.focus(true);
            refresh.run();
            return true;
        }
        textEdit.finish("outside-click");
        refresh.run();
        return true;
    }

    private void openContext(double mouseX, double mouseY, QuestDetailsDescriptionModel model, QuestDetailsDescriptionHitTest.Hit hit, int lx, int visibleY) {
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
    }

    private boolean handleCanvasClick(QuestDetailsDescriptionModel model, int lx, int visibleY) {
        if (selection.selectionBoundsHit(model, lx, visibleY)) {
            transforms.beginSelectionTransform(model, lx, visibleY);
            TextStyleSession.closeQuestDetails(state);
            refresh.run();
            return true;
        }
        textEdit.finish("canvas-click");
        QuestDetailsDescriptionInteractionState.beginBoxSelection(state, lx, visibleY, TabletModifierKeys.shiftDown(), selection::clear);
        refresh.run();
        return true;
    }

    private void beginTransform(QuestDetailsDescriptionModel model, QuestDetailsDescriptionHitTest.Hit hit, int lx, int visibleY, int ly) {
        QuestDetailsDescriptionHitTest.Rect rect = hitTest.rect(model, hit);
        CanvasImageLayer image = "desc_image".equals(hit.kind()) ? model.image(hit.id()) : null;
        boolean gizmoSupported = image != null && CanvasTransformGizmo.supports(image.asset());
        CanvasTransformMode gizmoMode = hitTest.imageGizmoMode(model, hit, lx, visibleY);
        if (gizmoSupported && gizmoMode == null) {
            state.questDetailsTransformAxis = "";
            return;
        }
        String transformAxis = gizmoSupported ? hitTest.imageGizmoAxis(model, hit, lx, visibleY) : "";
        if (gizmoMode == CanvasTransformMode.MOVE) {
            transformAxis = CanvasTransformGizmo.moveAxisOrFree(transformAxis);
        }
        boolean resizeHit = gizmoMode == CanvasTransformMode.RESIZE
                || (!gizmoSupported && gizmoMode == null && hitTest.inResizeHandle(rect, lx, visibleY));
        boolean rotateHit = gizmoMode == CanvasTransformMode.ROTATE
                || (!gizmoSupported && gizmoMode == null && hitTest.inRotateHandle(rect, lx, visibleY));
        boolean selectionMove = selection.count() > 1 && hitTest.isHitSelected(hit) && !resizeHit && !rotateHit;
        transforms.beginTransform(model, hit.kind(), hit.id(), new QuestDetailsDescriptionTransform.ElementRect(rect.x(), rect.y(), rect.w(), rect.h(), rect.rotation()), selectionMove, resizeHit, rotateHit, lx, visibleY, ly);
        state.questDetailsTransformAxis = selectionMove ? "" : transformAxis;
    }

    private boolean shiftMoveHit(QuestDetailsDescriptionModel model, QuestDetailsDescriptionHitTest.Hit hit) {
        if (!TabletModifierKeys.shiftDown() || !"desc_image".equals(hit.kind())) {
            return false;
        }
        CanvasImageLayer image = model.image(hit.id());
        CanvasTransformMode active = CanvasTransformGizmo.activeMode(state);
        if (image == null || !CanvasTransformGizmo.supports(image.asset()) || (active != CanvasTransformMode.MOVE && active != CanvasTransformMode.RESIZE)) {
            return false;
        }
        return true;
    }

    private void select(QuestDetailsDescriptionHitTest.Hit hit) {
        state.questDetailsSelectedObjectiveKind = "";
        state.questDetailsSelectedObjectiveId = "";
        if ("desc_text".equals(hit.kind())) {
            if (selection.count() > 1 && selection.isSelectedText(hit.id())) {
                state.questDetailsDescriptionSelection.setPrimaryTextId(hit.id());
            } else {
                state.questDetailsDescriptionSelection.textIds().clear();
                state.questDetailsDescriptionSelection.imageIds().clear();
                state.questDetailsDescriptionSelection.textIds().add(hit.id());
                state.questDetailsDescriptionSelection.setPrimaryImageId("");
            }
            state.questDetailsDescriptionSelection.setPrimaryTextId(hit.id());
        } else {
            if (selection.count() > 1 && selection.isSelectedImage(hit.id())) {
                state.questDetailsDescriptionSelection.setPrimaryImageId(hit.id());
            } else {
                state.questDetailsDescriptionSelection.textIds().clear();
                state.questDetailsDescriptionSelection.imageIds().clear();
                state.questDetailsDescriptionSelection.imageIds().add(hit.id());
                state.questDetailsDescriptionSelection.setPrimaryTextId("");
            }
            state.questDetailsDescriptionSelection.setPrimaryImageId(hit.id());
            TextStyleSession.closeQuestDetails(state);
        }
    }

    private void toggleSelection(QuestDetailsDescriptionHitTest.Hit hit) {
        state.questDetailsSelectedObjectiveKind = "";
        state.questDetailsSelectedObjectiveId = "";
        if ("desc_text".equals(hit.kind())) {
            toggle(state.questDetailsDescriptionSelection.textIds(), hit.id());
            state.questDetailsDescriptionSelection.setPrimaryTextId(state.questDetailsDescriptionSelection.textIds().contains(hit.id()) ? hit.id() : state.questDetailsDescriptionSelection.textIds().stream().findFirst().orElse(""));
        } else {
            toggle(state.questDetailsDescriptionSelection.imageIds(), hit.id());
            state.questDetailsDescriptionSelection.setPrimaryImageId(state.questDetailsDescriptionSelection.imageIds().contains(hit.id()) ? hit.id() : state.questDetailsDescriptionSelection.imageIds().stream().findFirst().orElse(""));
        }
    }

    private boolean handleTextDoubleClick(QuestDetailsDescriptionHitTest.Hit hit) {
        return QuestDetailsDescriptionInteractionState.recordTextClick(state, hit.id(), System.currentTimeMillis(), 350L);
    }

    private int localX(double mouseX) {
        return QuestDetailsMouse.localX(state, mouseX, surface.contentX(), surface.contentW());
    }

    private int localY(double mouseY) {
        return QuestDetailsMouse.localY(state, mouseY, surface.contentY(), surface.contentH());
    }

    private int pointerScreenX(double mouseX) {
        return QuestDetailsMouse.screenX(state, surface.contentX()) + localX(mouseX);
    }

    private int pointerScreenY(double mouseY) {
        return QuestDetailsMouse.screenY(state, surface.contentY()) + localY(mouseY);
    }

    private void setScroll(int scroll) {
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        state.questDetailsDescScroll = QuestDetailsDescriptionLayout.clampDescriptionScroll(state, model, surface.contentH(), scroll);
    }

    private void storeContextPosition(double mouseX, double mouseY, int lx, int ly) {
        QuestDetailsMouse.openContextAtPointer(state, state.questDetailsContextKind, state.questDetailsContextId, mouseX, mouseY, surface.contentX(), surface.contentY(), lx, ly);
    }

    private boolean isQuestDetailsTextStyleMenuHit(double mouseX, double mouseY) {
        if (!state.questDetailsTextStyleOpen || state.questDetailsTextStyleMenuW <= 0 || state.questDetailsTextStyleMenuH <= 0) {
            return false;
        }
        if (QuestDetailsWindow.isTextStyleMenuHit(state, mouseX, mouseY)) {
            return true;
        }
        int localMenuX = state.questDetailsTextStyleMenuX - (QuestDetailsMouse.screenX(state, surface.contentX()) - state.questDetailsScreenX);
        int localMenuY = state.questDetailsTextStyleMenuY - (QuestDetailsMouse.screenY(state, surface.contentY()) - state.questDetailsScreenY);
        return inside(mouseX, mouseY, localMenuX, localMenuY, state.questDetailsTextStyleMenuW, state.questDetailsTextStyleMenuH)
                || inside(mouseX, mouseY, state.questDetailsScreenX + localMenuX, state.questDetailsScreenY + localMenuY,
                state.questDetailsTextStyleMenuW, state.questDetailsTextStyleMenuH);
    }

    private boolean shouldYieldToContextMenu(double mouseX, double mouseY) {
        return state.questDetailsContextOpen
                && (state.contextMenuScrollDragging || QuestDetailsWindow.isContextMenuHit(state, mouseX, mouseY));
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private static void toggle(java.util.Set<String> values, String id) {
        if (!values.add(id)) {
            values.remove(id);
        }
    }

    interface Surface {
        boolean isMouseOverElement(double mouseX, double mouseY);

        boolean mouseWheelMoveFallback(double mouseX, double mouseY, double wheelDelta);

        boolean mouseClickedFallback(double mouseX, double mouseY, int button);

        boolean mouseDraggedFallback(double mouseX, double mouseY, int button, double dragX, double dragY);

        boolean mouseReleasedFallback(double mouseX, double mouseY, int button);

        boolean keyPressedFallback(int keyCode, int scanCode, int modifiers);

        boolean charTypedFallback(char codePoint, int modifiers);

        void focus(boolean focus);

        int contentX();

        int contentY();

        int contentW();

        int contentH();
    }
}
