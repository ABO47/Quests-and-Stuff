package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;

import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerKind;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformGizmo;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasTransformMode;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasBoxSelectionController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasElementTransformController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasSelectionTransformController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.widget.TabletWidgetCoordinates;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Map;

final class CanvasSelectMoveClickActions {
    private CanvasSelectMoveClickActions() {
    }

    static void handleSelectMove(
            CanvasViewport canvasViewport,
            TabletUiState state,
            Runnable refresher,
            Map<String, QuestCardLayout> byQuestId,
            CanvasInlineTextEditor textEditor,
            CanvasElementTransformController elementTransforms,
            CanvasSelectionTransformController selectionTransforms,
            int localX,
            int localY,
            int button,
            QuestCardLayout hit,
            CanvasImageLayer imageHit,
            CanvasTextLayer textHit,
            CanvasExclusiveChoice ecHit
    ) {
        if (button == 0 && state.root.canEdit) {
            if (handleElementTransformStart(canvasViewport, state, refresher, byQuestId, textEditor, elementTransforms, selectionTransforms, localX, localY, imageHit, textHit, ecHit)) {
                return;
            }
        }
        if (hit != null) {
            long now = System.currentTimeMillis();
            boolean doubleClick = hit.questId().equals(state.canvas.canvasLastClickedQuestId) && now - state.canvas.canvasLastQuestClickAtMs <= 350L;
            state.canvas.canvasLastClickedQuestId = hit.questId();
            state.canvas.canvasLastQuestClickAtMs = now;
            if (doubleClick && button == 0) {
                int viewportScreenX = TabletWidgetCoordinates.screenX(canvasViewport, state.canvas.canvasPanelX + state.canvas.canvasViewportX);
                int viewportScreenY = TabletWidgetCoordinates.screenY(canvasViewport, state.canvas.canvasPanelY + state.canvas.canvasViewportY);
                QuestDetailsWindow.openAtSource(
                        state,
                        hit.questId(),
                        viewportScreenX + hit.x(),
                        viewportScreenY + hit.y(),
                        hit.width(),
                        hit.height()
                );
                refresher.run();
                return;
            }
            if (canvasViewport.ctrlDown()) {
                if (!state.canvas.canvasSelection.questIds().add(hit.questId())) {
                    state.canvas.canvasSelection.questIds().remove(hit.questId());
                }
            } else if (canvasViewport.shiftDown()) {
                rangeSelect(state, byQuestId, state.canvas.canvasSelectionRangeAnchorKind, state.canvas.canvasSelectionRangeAnchorId, CanvasLayerKind.QUEST, hit.questId());
            } else if (!state.canvas.canvasSelection.questIds().contains(hit.questId())) {
                CanvasSelectionActions.clearCanvasSelection(state);
                state.canvas.canvasSelection.questIds().add(hit.questId());
                state.canvas.canvasSelectionRangeAnchorKind = CanvasLayerKind.QUEST.name();
                state.canvas.canvasSelectionRangeAnchorId = hit.questId();
            }
            selectionTransforms.beginDrag(localX, localY, byQuestId);
            canvasViewport.beginSelectionDragPreview();
        } else {
            state.canvas.draggingSelection = false;
            state.canvas.resizingSelection = false;
            state.canvas.rotatingSelection = false;
            state.canvas.transientQuestPositions.clear();
            state.canvas.transientQuestScales.clear();
            CanvasSelectionActions.clearCanvasSelection(state);
            state.canvas.canvasSelectionRangeAnchorKind = "";
            state.canvas.canvasSelectionRangeAnchorId = "";
            CanvasBoxSelectionController.beginBoxSelection(state, false, localX, localY);
        }
        refresher.run();
    }

    private static boolean handleElementTransformStart(
            CanvasViewport canvasViewport,
            TabletUiState state,
            Runnable refresher,
            Map<String, QuestCardLayout> byQuestId,
            CanvasInlineTextEditor textEditor,
            CanvasElementTransformController elementTransforms,
            CanvasSelectionTransformController selectionTransforms,
            int localX,
            int localY,
            CanvasImageLayer imageHit,
            CanvasTextLayer textHit,
            CanvasExclusiveChoice ecHit
    ) {
        int selectionCount = CanvasSelectionActions.totalCanvasSelectionCount(state);
        if (selectionCount > 1) {
            if (CanvasRenderer.isSelectionRotateHandleHit(state, localX, localY)) {
                selectionTransforms.beginRotate(localX, localY, byQuestId);
                refresher.run();
                return true;
            }
            if (CanvasRenderer.isSelectionResizeHandleHit(state, localX, localY)) {
                selectionTransforms.beginResize(localX, localY, byQuestId);
                refresher.run();
                return true;
            }
            if (!canvasViewport.shiftDown() && !canvasViewport.ctrlDown() && CanvasRenderer.isSelectionBoundsHit(state, localX, localY)) {
                selectionTransforms.beginDrag(localX, localY, byQuestId);
                canvasViewport.beginSelectionDragPreview();
                refresher.run();
                return true;
            }
        }
        boolean textTransformHandleHit = textHit != null
                && (CanvasRenderer.isCanvasTextResizeHandleHit(state, textHit, localX, localY)
                || CanvasRenderer.isCanvasTextRotateHandleHit(state, textHit, localX, localY));
        boolean imageTransformHandleHit = imageHit != null && imageTransformHit(canvasViewport, state, imageHit, localX, localY);
        boolean ecTransformHandleHit = ecHit != null
                && (CanvasRenderer.isCanvasExclusiveChoiceResizeHandleHit(state, ecHit, localX, localY)
                || CanvasRenderer.isCanvasExclusiveChoiceRotateHandleHit(state, ecHit, localX, localY));
        if (textTransformHandleHit) {
            state.canvas.canvasTextLastClickId = "";
            elementTransforms.beginTextTransform(textHit, localX, localY);
            refresher.run();
            return true;
        }
        if (imageTransformHandleHit) {
            elementTransforms.beginImageTransform(imageHit, localX, localY);
            refresher.run();
            return true;
        }
        if (ecTransformHandleHit) {
            elementTransforms.beginExclusiveChoiceTransform(ecHit, localX, localY);
            refresher.run();
            return true;
        }
        boolean questResizeTransform = !state.canvas.canvasSelection.questIds().isEmpty();
        if (questResizeTransform && CanvasRenderer.isSelectionResizeHandleHit(state, localX, localY)) {
            selectionTransforms.beginResize(localX, localY, byQuestId);
            refresher.run();
            return true;
        }
        if (textHit != null) {
            if (canvasViewport.ctrlDown()) {
                CanvasBoxSelectionController.toggleCanvasTextSelection(state, textHit.id());
                refresher.run();
                return true;
            }
            if (canvasViewport.shiftDown()) {
                rangeSelect(state, byQuestId, state.canvas.canvasSelectionRangeAnchorKind, state.canvas.canvasSelectionRangeAnchorId, CanvasLayerKind.TEXT, textHit.id());
                refresher.run();
                return true;
            }
            long now = System.currentTimeMillis();
            boolean doubleClick = textHit.id().equals(state.canvas.canvasTextLastClickId) && now - state.canvas.canvasTextLastClickAtMs <= 350L;
            state.canvas.canvasTextLastClickId = textHit.id();
            state.canvas.canvasTextLastClickAtMs = now;
            if (doubleClick) {
                textEditor.begin(textHit);
                refresher.run();
                return true;
            }
            if (selectionCount > 1 && CanvasSelectionActions.isTextSelected(state, textHit.id())) {
                selectionTransforms.beginDrag(localX, localY, byQuestId);
                canvasViewport.beginSelectionDragPreview();
                refresher.run();
                return true;
            }
            elementTransforms.beginTextTransform(textHit, localX, localY);
            refresher.run();
            return true;
        }
        if (imageHit != null) {
            if (canvasViewport.ctrlDown()) {
                CanvasBoxSelectionController.toggleCanvasImageSelection(state, imageHit.id());
                refresher.run();
                return true;
            }
            if (canvasViewport.shiftDown()) {
                rangeSelect(state, byQuestId, state.canvas.canvasSelectionRangeAnchorKind, state.canvas.canvasSelectionRangeAnchorId, CanvasLayerKind.IMAGE, imageHit.id());
                refresher.run();
                return true;
            }
            if (selectionCount > 1 && CanvasSelectionActions.isImageSelected(state, imageHit.id())) {
                selectionTransforms.beginDrag(localX, localY, byQuestId);
                canvasViewport.beginSelectionDragPreview();
                refresher.run();
                return true;
            }
            elementTransforms.beginImageTransform(imageHit, localX, localY);
            refresher.run();
            return true;
        }
        if (ecHit != null) {
            if (canvasViewport.ctrlDown()) {
                CanvasBoxSelectionController.toggleCanvasExclusiveChoiceSelection(state, ecHit.id());
                refresher.run();
                return true;
            }
            if (canvasViewport.shiftDown()) {
                rangeSelect(state, byQuestId, state.canvas.canvasSelectionRangeAnchorKind, state.canvas.canvasSelectionRangeAnchorId, CanvasLayerKind.EXCLUSIVE_CHOICE, ecHit.id());
                refresher.run();
                return true;
            }
            if (selectionCount > 1 && CanvasSelectionActions.isExclusiveChoiceSelected(state, ecHit.id())) {
                selectionTransforms.beginDrag(localX, localY, byQuestId);
                canvasViewport.beginSelectionDragPreview();
                refresher.run();
                return true;
            }
            elementTransforms.beginExclusiveChoiceTransform(ecHit, localX, localY);
            refresher.run();
            return true;
        }
        return false;
    }

    private static boolean imageTransformHit(CanvasViewport canvasViewport, TabletUiState state, CanvasImageLayer image, int localX, int localY) {
        if (!CanvasTransformGizmo.supports(image.asset())) {
            return CanvasRenderer.isCanvasImageResizeHandleHit(state, image, localX, localY)
                    || CanvasRenderer.isCanvasImageRotateHandleHit(state, image, localX, localY);
        }
        if (canvasViewport.shiftDown()
                && CanvasTransformGizmo.activeMode(state) == CanvasTransformMode.RESIZE
                && CanvasTransformGizmo.boundsHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), localX, localY)) {
            return true;
        }
        CanvasTransformMode hitMode = CanvasTransformGizmo.modeAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), image.entityYaw(), image.modelPitch(), localX, localY);
        return hitMode != null
                || (canvasViewport.shiftDown()
                && CanvasTransformGizmo.activeMode(state) == CanvasTransformMode.MOVE
                && CanvasTransformGizmo.boundsHitAtPivot(state, image.x(), image.y(), image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation(), localX, localY));
    }

    private static void rangeSelect(
            TabletUiState state,
            Map<String, QuestCardLayout> byQuestId,
            String anchorKind,
            String anchorId,
            CanvasLayerKind clickedKind,
            String clickedId
    ) {
        if (anchorId.isBlank()) {
            CanvasSelectionActions.clearCanvasSelection(state);
            selectSingle(state, clickedKind, clickedId);
            state.canvas.canvasSelectionRangeAnchorKind = clickedKind.name();
            state.canvas.canvasSelectionRangeAnchorId = clickedId;
            return;
        }
        CanvasLayerKind anchorEnumKind = null;
        if (!anchorKind.isBlank()) {
            try {
                anchorEnumKind = CanvasLayerKind.valueOf(anchorKind.toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (anchorEnumKind == null) {
            return;
        }
        String chapter = TabletStateQueries.selectedChapterName(state);
        List<CanvasImageLayer> images = state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of());
        List<CanvasTextLayer> texts = state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of());
        List<CanvasExclusiveChoice> ecs = state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of());
        int[] anchorBounds = elementBounds(byQuestId, images, texts, ecs, anchorEnumKind, anchorId);
        int[] clickBounds = elementBounds(byQuestId, images, texts, ecs, clickedKind, clickedId);
        if (anchorBounds == null || clickBounds == null) {
            return;
        }
        int minX = Math.min(anchorBounds[0], clickBounds[0]);
        int minY = Math.min(anchorBounds[1], clickBounds[1]);
        int maxX = Math.max(anchorBounds[2], clickBounds[2]);
        int maxY = Math.max(anchorBounds[3], clickBounds[3]);
        CanvasSelectionActions.clearCanvasSelection(state);
        for (QuestCardLayout card : byQuestId.values()) {
            if (card.x() < maxX && card.x() + card.width() > minX
                    && card.y() < maxY && card.y() + card.height() > minY) {
                state.canvas.canvasSelection.questIds().add(card.questId());
            }
        }
        for (CanvasImageLayer img : images) {
            if (img.x() < maxX && img.x() + img.w() > minX && img.y() < maxY && img.y() + img.h() > minY) {
                state.canvas.canvasSelection.imageIds().add(img.id());
                state.canvas.canvasSelection.setPrimaryImageId(img.id());
            }
        }
        for (CanvasTextLayer txt : texts) {
            if (txt.x() < maxX && txt.x() + txt.w() > minX && txt.y() < maxY && txt.y() + txt.h() > minY) {
                state.canvas.canvasSelection.textIds().add(txt.id());
                state.canvas.canvasSelection.setPrimaryTextId(txt.id());
            }
        }
        for (CanvasExclusiveChoice ec : ecs) {
            int[] ecBounds = CanvasElementGeometry.logicalBoundsAtPivot(ec.x(), ec.y(), ec.w(), ec.h(), ec.pivotX(), ec.pivotY(), ec.rotation());
            if (ecBounds[0] < maxX && ecBounds[2] > minX && ecBounds[1] < maxY && ecBounds[3] > minY) {
                state.canvas.canvasSelection.ecIds().add(ec.id());
                state.canvas.canvasSelection.setPrimaryEcId(ec.id());
            }
        }
    }

    private static void selectSingle(TabletUiState state, CanvasLayerKind kind, String id) {
        switch (kind) {
            case QUEST -> state.canvas.canvasSelection.questIds().add(id);
            case IMAGE -> {
                state.canvas.canvasSelection.imageIds().add(id);
                state.canvas.canvasSelection.setPrimaryImageId(id);
            }
            case TEXT -> {
                state.canvas.canvasSelection.textIds().add(id);
                state.canvas.canvasSelection.setPrimaryTextId(id);
            }
            case EXCLUSIVE_CHOICE -> {
                state.canvas.canvasSelection.ecIds().add(id);
                state.canvas.canvasSelection.setPrimaryEcId(id);
            }
            case CONNECTION -> {
            }
        }
    }

    private static int[] elementBounds(
            Map<String, QuestCardLayout> byQuestId,
            List<CanvasImageLayer> images,
            List<CanvasTextLayer> texts,
            List<CanvasExclusiveChoice> ecs,
            CanvasLayerKind kind,
            String id
    ) {
        switch (kind) {
            case QUEST -> {
                QuestCardLayout card = byQuestId.get(id);
                if (card != null) {
                    return new int[]{card.x(), card.y(), card.x() + card.width(), card.y() + card.height()};
                }
            }
            case IMAGE -> {
                for (CanvasImageLayer img : images) {
                    if (img.id().equals(id)) {
                        return new int[]{img.x(), img.y(), img.x() + img.w(), img.y() + img.h()};
                    }
                }
            }
            case TEXT -> {
                for (CanvasTextLayer txt : texts) {
                    if (txt.id().equals(id)) {
                        return new int[]{txt.x(), txt.y(), txt.x() + txt.w(), txt.y() + txt.h()};
                    }
                }
            }
            case EXCLUSIVE_CHOICE -> {
                for (CanvasExclusiveChoice ec : ecs) {
                    if (ec.id().equals(id)) {
                        return CanvasElementGeometry.logicalBoundsAtPivot(ec.x(), ec.y(), ec.w(), ec.h(), ec.pivotX(), ec.pivotY(), ec.rotation());
                    }
                }
            }
            case CONNECTION -> {
            }
        }
        return null;
    }
}
