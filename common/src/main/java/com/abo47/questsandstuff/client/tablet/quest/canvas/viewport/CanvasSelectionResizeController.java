package com.abo47.questsandstuff.client.tablet.quest.canvas.viewport;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasElementGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapBounds;
import com.abo47.questsandstuff.client.tablet.quest.canvas.snap.CanvasSnapEngine;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionResize;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasLayerSelectionSnapshot;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionSnapshot;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import java.util.List;
import java.util.Map;

import static com.lowdragmc.lowdraglib.gui.widget.Widget.isShiftDown;

final class CanvasSelectionResizeController {
    private final TabletUiState state;

    CanvasSelectionResizeController(TabletUiState state) {
        this.state = state;
    }

    void beginResize(int localX, int localY, Map<String, QuestCardLayout> byQuestId) {
        CanvasTransformSessions.clearMainCanvasSession(state);
        state.canvas.draggingSelection = false;
        state.canvas.resizingSelection = true;
        state.canvas.rotatingSelection = false;
        state.canvas.resizeStartMouseX = CanvasSelectionBounds.toLogicalX(state, localX);
        state.canvas.resizeStartMouseY = CanvasSelectionBounds.toLogicalY(state, localY);
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for (String questId : state.canvas.canvasSelection.questIds()) {
            QuestCardLayout card = byQuestId.get(questId);
            if (card == null) {
                continue;
            }
            minX = Math.min(minX, card.visualLogicalX());
            minY = Math.min(minY, card.visualLogicalY());
            maxX = Math.max(maxX, card.logicalRight());
            maxY = Math.max(maxY, card.logicalBottom());
            state.canvas.resizeStartPositions.put(questId, new CanvasPoint(card.logicalX(), card.logicalY()));
            state.canvas.resizeStartScales.put(questId, CanvasSelectionBounds.scaleForQuest(questId, byQuestId));
        }
        CanvasSelectionSnapshot snapshot = CanvasSelectionSnapshot.capture(state, TabletStateQueries.selectedChapterName(state), byQuestId);
        state.canvas.resizeStartImageLayers.putAll(snapshot.images());
        state.canvas.resizeStartTextLayers.putAll(snapshot.texts());
        String group = TabletStateQueries.selectedChapterName(state);
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(group, List.of())) {
            if (CanvasSelectionActions.isExclusiveChoiceSelected(state, ec.id())) {
                state.canvas.resizeStartEcLayers.put(ec.id(), ec);
                CanvasSnapEngine.Bounds ecBounds = CanvasSnapBounds.forExclusiveChoice(ec);
                minX = Math.min(minX, ecBounds.left());
                minY = Math.min(minY, ecBounds.top());
                maxX = Math.max(maxX, ecBounds.right());
                maxY = Math.max(maxY, ecBounds.bottom());
            }
        }
        if (snapshot.hasBounds()) {
            minX = Math.min(minX, snapshot.left());
            minY = Math.min(minY, snapshot.top());
            maxX = Math.max(maxX, snapshot.right());
            maxY = Math.max(maxY, snapshot.bottom());
        }
        if (minX == Integer.MAX_VALUE) {
            state.canvas.resizeStartLeft = 0;
            state.canvas.resizeStartTop = 0;
            state.canvas.resizeStartRight = TabletUiFactory.CARD_W;
            state.canvas.resizeStartBottom = TabletUiFactory.CARD_H;
            return;
        }
        state.canvas.resizeStartLeft = minX;
        state.canvas.resizeStartTop = minY;
        state.canvas.resizeStartRight = maxX;
        state.canvas.resizeStartBottom = maxY;
    }

    void updateResize(int localX, int localY) {
        int logicalMouseX = CanvasSelectionBounds.toLogicalX(state, localX);
        int logicalMouseY = CanvasSelectionBounds.toLogicalY(state, localY);
        CanvasLayerSelectionSnapshot layerSnapshot = new CanvasLayerSelectionSnapshot(
                state.canvas.resizeStartLeft,
                state.canvas.resizeStartTop,
                state.canvas.resizeStartRight,
                state.canvas.resizeStartBottom,
                state.canvas.resizeStartImageLayers,
                state.canvas.resizeStartTextLayers
        );
        CanvasSelectionResize.Result resize = CanvasSelectionResize.resizeBottomRight(
                layerSnapshot,
                logicalMouseX,
                logicalMouseY,
                resizeConstraints()
        );

        state.canvas.transientQuestPositions.clear();
        state.canvas.transientQuestScales.clear();
        for (Map.Entry<String, Float> entry : state.canvas.resizeStartScales.entrySet()) {
            String questId = entry.getKey();
            Float baseScale = entry.getValue();
            CanvasPoint basePos = state.canvas.resizeStartPositions.get(questId);
            if (baseScale == null || basePos == null) {
                continue;
            }
            float targetScale = Math.max(0.5f, (float) (baseScale * resize.uniformScale()));
            int baseVisualX = basePos.x + CanvasGeometry.visualInsetX(state, baseScale);
            int baseVisualY = basePos.y + CanvasGeometry.visualInsetY(state, baseScale);
            double baseCenterX = baseVisualX + CanvasGeometry.visualLogicalWidth(baseScale) / 2.0;
            double baseCenterY = baseVisualY + CanvasGeometry.visualLogicalHeight(baseScale) / 2.0;
            double targetCenterX = resize.bounds().left() + (baseCenterX - state.canvas.resizeStartLeft) * resize.scaleX();
            double targetCenterY = resize.bounds().top() + (baseCenterY - state.canvas.resizeStartTop) * resize.scaleY();
            CanvasPoint anchor = CanvasGeometry.anchorForVisualCenter(state, targetCenterX, targetCenterY, targetScale);
            state.canvas.transientQuestPositions.put(questId, new CanvasPoint(anchor.x, anchor.y));
            state.canvas.transientQuestScales.put(questId, targetScale);
        }
        for (CanvasImageLayer image : resize.images().values()) {
            CanvasLayerMutations.putTransientCanvasImage(state, fitAndClampImage(image));
        }
        for (CanvasTextLayer text : resize.texts().values()) {
            CanvasLayerMutations.putTransientCanvasText(state, fitAndClampText(text));
        }
        for (Map.Entry<String, CanvasExclusiveChoice> entry : state.canvas.resizeStartEcLayers.entrySet()) {
            CanvasExclusiveChoice ec = entry.getValue();
            double centerX = resize.bounds().left() + (ec.x() + ec.w() / 2.0D - state.canvas.resizeStartLeft) * resize.scaleX();
            double centerY = resize.bounds().top() + (ec.y() + ec.h() / 2.0D - state.canvas.resizeStartTop) * resize.scaleY();
            int targetW = Math.max(8, (int) Math.round(ec.w() * resize.uniformScale()));
            int targetH = Math.max(8, (int) Math.round(ec.h() * resize.uniformScale()));
            int targetX = (int) Math.round(centerX - targetW / 2.0D);
            int targetY = (int) Math.round(centerY - targetH / 2.0D);
            CanvasLayerMutations.putTransientCanvasExclusiveChoice(state, fitAndClampExclusiveChoice(ec.moveTo(targetX, targetY).resizeTo(targetW, targetH)));
        }
    }

    private CanvasSelectionResize.Constraints resizeConstraints() {
        int minimum = Math.max(4, CanvasGeometry.gridSize(state) / 2);
        int minLeft = state.canvas.gridCanvasLocked ? 0 : CanvasSelectionResize.UNBOUNDED;
        int minTop = state.canvas.gridCanvasLocked ? 0 : CanvasSelectionResize.UNBOUNDED;
        int maxRight = state.canvas.gridCanvasLocked ? state.canvas.canvasContentW : CanvasSelectionResize.UNBOUNDED;
        int maxBottom = state.canvas.gridCanvasLocked ? state.canvas.canvasContentH : CanvasSelectionResize.UNBOUNDED;
        return new CanvasSelectionResize.Constraints(
                minimum,
                minimum,
                CanvasGeometry.gridSize(state),
                state.canvas.gridSnapLocked || isShiftDown(),
                isShiftDown() || isSingleQuestOnlyResize(),
                minLeft,
                minTop,
                maxRight,
                maxBottom
        );
    }

    private boolean isSingleQuestOnlyResize() {
        return state.canvas.resizeStartScales.size() == 1
                && state.canvas.resizeStartImageLayers.isEmpty()
                && state.canvas.resizeStartTextLayers.isEmpty();
    }

    private CanvasImageLayer fittedImageIfGridLocked(CanvasImageLayer image) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedImage(state, image) : image;
    }

    private CanvasTextLayer fittedTextIfGridLocked(CanvasTextLayer text) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedText(state, text) : text;
    }

    private CanvasExclusiveChoice fittedExclusiveChoiceIfGridLocked(CanvasExclusiveChoice ec) {
        return state.canvas.gridSnapLocked ? CanvasGridFitController.fittedExclusiveChoice(state, ec) : ec;
    }

    private CanvasImageLayer fitAndClampImage(CanvasImageLayer image) {
        CanvasImageLayer fitted = fittedImageIfGridLocked(image);
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, fitted.x(), fitted.y(), fitted.w(), fitted.h(), fitted.pivotX(), fitted.pivotY(), fitted.rotation());
        return fitted.moveTo(clamped.x, clamped.y);
    }

    private CanvasTextLayer fitAndClampText(CanvasTextLayer text) {
        CanvasTextLayer fitted = fittedTextIfGridLocked(text);
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, fitted.x(), fitted.y(), fitted.w(), fitted.h(), fitted.w() / 2, fitted.h() / 2, fitted.rotation());
        return fitted.moveTo(clamped.x, clamped.y);
    }

    private CanvasExclusiveChoice fitAndClampExclusiveChoice(CanvasExclusiveChoice ec) {
        CanvasExclusiveChoice fitted = fittedExclusiveChoiceIfGridLocked(ec);
        CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, fitted.x(), fitted.y(), fitted.w(), fitted.h(), CanvasElementGeometry.defaultPivot(fitted.w()), CanvasElementGeometry.defaultPivot(fitted.h()), fitted.rotation());
        return fitted.moveTo(clamped.x, clamped.y);
    }
}
