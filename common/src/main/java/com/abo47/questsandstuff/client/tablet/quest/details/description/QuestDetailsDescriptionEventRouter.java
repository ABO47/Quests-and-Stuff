package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.ui.TabletModifierKeys;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformMode;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsCoordinates;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsTransientManager;
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
            QuestDetailsTransientManager.closeContext(state);
            ToolMenuAnimation.closeQuestDetails(state);
            return true;
        }
        if (!QuestDetailsEditController.canEdit(state)) {
            return true;
        }
        int lx = localX(mouseX);
        int visibleY = localY(mouseY);
        int ly = visibleY + state.questDetails.questDetailsDescScroll;
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
        QuestDetailsTransientManager.closeContext(state);
        QuestDetailsTransientManager.closeTypePicker(state);
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
        if (!resizeHit && !rotateHit && !shiftMoveHit) {
            if (TabletModifierKeys.ctrlDown()) {
                toggleSelection(hit);
                TextStyleSession.closeQuestDetails(state);
                refresh.run();
                return true;
            }
            if (TabletModifierKeys.shiftDown()) {
                rangeSelect(model, hit);
                TextStyleSession.closeQuestDetails(state);
                refresh.run();
                return true;
            }
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
        if (state.questDetails.questDetailsPanning) {
            int dy = (int) Math.round(mouseY) - state.questDetails.questDetailsPanStartY;
            setScroll(state.questDetails.questDetailsPanStartScroll - dy);
            return true;
        }
        if (!QuestDetailsEditController.canEdit(state)) {
            return surface.mouseDraggedFallback(mouseX, mouseY, button, dragX, dragY);
        }
        if (textEdit.dragSelectionTo(localX(mouseX), localY(mouseY))) {
            return true;
        }
        if (state.questDetails.questDetailsBoxSelecting) {
            QuestDetailsDescriptionInteractionState.updateBoxSelection(state, localX(mouseX), localY(mouseY));
            return true;
        }
        if (state.questDetails.questDetailsTransformId.isBlank()) {
            return surface.mouseDraggedFallback(mouseX, mouseY, button, dragX, dragY);
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        transforms.applyTransform(model, pointerScreenX(mouseX), pointerScreenY(mouseY));
        QuestDetailsDescriptionTransformApply.preview(state, model);
        if ("desc_text".equals(state.questDetails.questDetailsTransformKind)
                && state.questDetails.questDetailsTextStyleOpen
                && state.questDetails.questDetailsTransformId.equals(state.questDetails.questDetailsTextStyleTarget)) {
            refresh.run();
        }
        return true;
    }

    boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (TabletRootWindowController.isFontSizeFieldOpen(state)) {
            return surface.keyPressedFallback(keyCode, scanCode, modifiers);
        }
        if (QuestDetailsEditController.canEdit(state) && textEdit.handleKey(keyCode)) {
            return true;
        }
        return surface.keyPressedFallback(keyCode, scanCode, modifiers);
    }

    boolean charTyped(char codePoint, int modifiers) {
        if (TabletRootWindowController.isFontSizeFieldOpen(state)) {
            return surface.charTypedFallback(codePoint, modifiers);
        }
        if (QuestDetailsEditController.canEdit(state) && textEdit.handleChar(codePoint)) {
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
        if (state.questDetails.questDetailsPanning) {
            state.questDetails.questDetailsPanning = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details description pan commit quest={} scroll={}", questId, state.questDetails.questDetailsDescScroll);
            refresh.run();
            return true;
        }
        if (!QuestDetailsEditController.canEdit(state)) {
            QuestDetailsDescriptionTransformApply.clearEditDragState(state);
            return surface.mouseReleasedFallback(mouseX, mouseY, button);
        }
        if (state.canvas.selectingCanvasTextRange && textEdit.isEditing()) {
            TextEditSession.finishRangeSelection(state);
            refresh.run();
            return true;
        }
        if (state.questDetails.questDetailsBoxSelecting) {
            state.questDetails.questDetailsBoxSelecting = false;
            QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
            selection.finishBoxSelection(model);
            refresh.run();
            return true;
        }
        if (state.questDetails.questDetailsTransformId.isBlank()) {
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
        CanvasTextLayer editingText = model.text(state.questDetails.questDetailsTextEditTarget);
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
        QuestDetailsTransientManager.openContext(
                state,
                selectionHit ? "desc_selection" : (hit == null ? "description" : hit.kind()),
                hit == null ? "" : hit.id(),
                state.questDetails.questDetailsContextX,
                state.questDetails.questDetailsContextY
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
        state.questDetails.questDetailsDescRangeAnchorKind = "";
        state.questDetails.questDetailsDescRangeAnchorId = "";
        selection.clear();
        QuestDetailsDescriptionInteractionState.beginBoxSelection(state, lx, visibleY, false, selection::clear);
        refresh.run();
        return true;
    }

    private void beginTransform(QuestDetailsDescriptionModel model, QuestDetailsDescriptionHitTest.Hit hit, int lx, int visibleY, int ly) {
        QuestDetailsDescriptionHitTest.Rect rect = hitTest.rect(model, hit);
        CanvasImageLayer image = "desc_image".equals(hit.kind()) ? model.image(hit.id()) : null;
        boolean gizmoSupported = image != null && CanvasTransformGizmo.supports(image.asset());
        CanvasTransformMode gizmoMode = hitTest.imageGizmoMode(model, hit, lx, visibleY);
        if (gizmoSupported && gizmoMode == null) {
            state.questDetails.questDetailsTransformAxis = "";
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
        state.questDetails.questDetailsTransformAxis = selectionMove ? "" : transformAxis;
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
        state.questDetails.questDetailsSelectedObjectiveKind = "";
        state.questDetails.questDetailsSelectedObjectiveId = "";
        state.questDetails.questDetailsDescRangeAnchorKind = hit.kind();
        state.questDetails.questDetailsDescRangeAnchorId = hit.id();
        if ("desc_text".equals(hit.kind())) {
            if (selection.count() > 1 && selection.isSelectedText(hit.id())) {
                state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId(hit.id());
            } else {
                state.questDetails.questDetailsDescriptionSelection.textIds().clear();
                state.questDetails.questDetailsDescriptionSelection.imageIds().clear();
                state.questDetails.questDetailsDescriptionSelection.textIds().add(hit.id());
                state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId("");
            }
            state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId(hit.id());
        } else {
            if (selection.count() > 1 && selection.isSelectedImage(hit.id())) {
                state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId(hit.id());
            } else {
                state.questDetails.questDetailsDescriptionSelection.textIds().clear();
                state.questDetails.questDetailsDescriptionSelection.imageIds().clear();
                state.questDetails.questDetailsDescriptionSelection.imageIds().add(hit.id());
                state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId("");
            }
            state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId(hit.id());
            TextStyleSession.closeQuestDetails(state);
        }
    }

    private void toggleSelection(QuestDetailsDescriptionHitTest.Hit hit) {
        state.questDetails.questDetailsSelectedObjectiveKind = "";
        state.questDetails.questDetailsSelectedObjectiveId = "";
        if ("desc_text".equals(hit.kind())) {
            toggle(state.questDetails.questDetailsDescriptionSelection.textIds(), hit.id());
            state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId(state.questDetails.questDetailsDescriptionSelection.textIds().contains(hit.id()) ? hit.id() : state.questDetails.questDetailsDescriptionSelection.textIds().stream().findFirst().orElse(""));
        } else {
            toggle(state.questDetails.questDetailsDescriptionSelection.imageIds(), hit.id());
            state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId(state.questDetails.questDetailsDescriptionSelection.imageIds().contains(hit.id()) ? hit.id() : state.questDetails.questDetailsDescriptionSelection.imageIds().stream().findFirst().orElse(""));
        }
    }

    private void rangeSelect(QuestDetailsDescriptionModel model, QuestDetailsDescriptionHitTest.Hit hit) {
        String anchorKind = state.questDetails.questDetailsDescRangeAnchorKind;
        String anchorId = state.questDetails.questDetailsDescRangeAnchorId;
        if (anchorId.isBlank()) {
            select(hit);
            return;
        }
        int[] anchorBounds = elementBounds(model, anchorKind, anchorId);
        int[] clickBounds = elementBounds(model, hit.kind(), hit.id());
        if (anchorBounds == null || clickBounds == null) {
            select(hit);
            return;
        }
        int rangeMinX = Math.min(anchorBounds[0], clickBounds[0]);
        int rangeMinY = Math.min(anchorBounds[1], clickBounds[1]);
        int rangeMaxX = Math.max(anchorBounds[2], clickBounds[2]);
        int rangeMaxY = Math.max(anchorBounds[3], clickBounds[3]);
        state.questDetails.questDetailsDescriptionSelection.textIds().clear();
        state.questDetails.questDetailsDescriptionSelection.imageIds().clear();
        for (CanvasTextLayer text : model.texts.values()) {
            int[] b = elementBounds(model, "desc_text", text.id());
            if (b != null && intersectsRange(b, rangeMinX, rangeMinY, rangeMaxX, rangeMaxY)) {
                state.questDetails.questDetailsDescriptionSelection.textIds().add(text.id());
                state.questDetails.questDetailsDescriptionSelection.setPrimaryTextId(text.id());
            }
        }
        for (CanvasImageLayer image : model.images.values()) {
            int[] b = elementBounds(model, "desc_image", image.id());
            if (b != null && intersectsRange(b, rangeMinX, rangeMinY, rangeMaxX, rangeMaxY)) {
                state.questDetails.questDetailsDescriptionSelection.imageIds().add(image.id());
                state.questDetails.questDetailsDescriptionSelection.setPrimaryImageId(image.id());
            }
        }
    }

    private int[] elementBounds(QuestDetailsDescriptionModel model, String kind, String id) {
        if ("desc_text".equals(kind)) {
            CanvasTextLayer text = model.text(id);
            if (text == null) return null;
            return new int[]{text.x(), text.y(), text.x() + text.w(), text.y() + text.h()};
        }
        if ("desc_image".equals(kind)) {
            CanvasImageLayer image = model.image(id);
            if (image == null) return null;
            return new int[]{image.x(), image.y(), image.x() + image.w(), image.y() + image.h()};
        }
        return null;
    }

    private static boolean intersectsRange(int[] bounds, int minX, int minY, int maxX, int maxY) {
        return bounds[0] < maxX && bounds[2] > minX && bounds[1] < maxY && bounds[3] > minY;
    }

    private boolean handleTextDoubleClick(QuestDetailsDescriptionHitTest.Hit hit) {
        return QuestDetailsDescriptionInteractionState.recordTextClick(state, hit.id(), System.currentTimeMillis(), 350L);
    }

    private int localX(double mouseX) {
        return QuestDetailsCoordinates.localX(state, mouseX, surface.contentX(), surface.contentW());
    }

    private int localY(double mouseY) {
        return QuestDetailsCoordinates.localY(state, mouseY, surface.contentY(), surface.contentH());
    }

    private int pointerScreenX(double mouseX) {
        return QuestDetailsCoordinates.screenX(state, surface.contentX()) + localX(mouseX);
    }

    private int pointerScreenY(double mouseY) {
        return QuestDetailsCoordinates.screenY(state, surface.contentY()) + localY(mouseY);
    }

    private void setScroll(int scroll) {
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        state.questDetails.questDetailsDescScroll = QuestDetailsDescriptionLayout.clampDescriptionScroll(state, model, surface.contentH(), scroll);
    }

    private void storeContextPosition(double mouseX, double mouseY, int lx, int ly) {
        QuestDetailsCoordinates.openContextAtPointer(state, state.questDetails.questDetailsContextKind, state.questDetails.questDetailsContextId, mouseX, mouseY, surface.contentX(), surface.contentY(), lx, ly);
    }

    private boolean isQuestDetailsTextStyleMenuHit(double mouseX, double mouseY) {
        if (!state.questDetails.questDetailsTextStyleOpen || state.questDetails.questDetailsTextStyleMenuW <= 0 || state.questDetails.questDetailsTextStyleMenuH <= 0) {
            return false;
        }
        if (QuestDetailsWindow.isTextStyleMenuHit(state, mouseX, mouseY)) {
            return true;
        }
        int localMenuX = state.questDetails.questDetailsTextStyleMenuX - (QuestDetailsCoordinates.screenX(state, surface.contentX()) - state.questDetails.questDetailsScreenX);
        int localMenuY = state.questDetails.questDetailsTextStyleMenuY - (QuestDetailsCoordinates.screenY(state, surface.contentY()) - state.questDetails.questDetailsScreenY);
        return inside(mouseX, mouseY, localMenuX, localMenuY, state.questDetails.questDetailsTextStyleMenuW, state.questDetails.questDetailsTextStyleMenuH)
                || inside(mouseX, mouseY, state.questDetails.questDetailsScreenX + localMenuX, state.questDetails.questDetailsScreenY + localMenuY,
                state.questDetails.questDetailsTextStyleMenuW, state.questDetails.questDetailsTextStyleMenuH);
    }

    private boolean shouldYieldToContextMenu(double mouseX, double mouseY) {
        return state.questDetails.questDetailsContextOpen
                && (state.contextMenu.contextMenuScrollDragging || QuestDetailsWindow.isContextMenuHit(state, mouseX, mouseY));
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
