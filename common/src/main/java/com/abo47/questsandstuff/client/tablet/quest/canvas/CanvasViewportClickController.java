package com.abo47.questsandstuff.client.tablet.quest.canvas;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasElementTransformController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasSelectionTransformController;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.ui.TabletWidgetCoordinates;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;

final class CanvasViewportClickController {
    private CanvasViewportClickController() {
    }

    static boolean mouseClicked(
            CanvasViewport canvasViewport,
            TabletUiState state,
            Player player,
            Runnable refresher,
            List<QuestCardLayout> cards,
            Map<String, QuestCardLayout> byQuestId,
            CanvasInlineTextEditor textEditor,
            CanvasElementTransformController elementTransforms,
            CanvasSelectionTransformController selectionTransforms,
            double mouseX,
            double mouseY,
            int button
    ) {
        if (!canvasViewport.isMouseOverElement(mouseX, mouseY)) {
            return canvasViewport.callSuperMouseClicked(mouseX, mouseY, button);
        }
        int localX = TabletWidgetCoordinates.localX(canvasViewport, state.canvasPanelX + state.canvasViewportX, mouseX);
        int localY = TabletWidgetCoordinates.localY(canvasViewport, state.canvasPanelY + state.canvasViewportY, mouseY);

        if (state.blueprintPlacement.active()) {
            if (!state.canEdit) {
                CanvasBlueprintController.cancelPlacement(state);
                refresher.run();
                return true;
            }
            if (button == 0) {
                CanvasBlueprintController.placeAt(player, state, localX, localY);
                refresher.run();
                return true;
            }
            if (button == 1) {
                CanvasBlueprintController.cancelPlacement(state);
                refresher.run();
                return true;
            }
        }

        if (EntityMotionEditor.isMainCanvasOpen(state)) {
            if (EntityMotionEditor.isMainCanvasHit(state, localX, localY)) {
                canvasViewport.callSuperMouseClicked(mouseX, mouseY, button);
                return true;
            }
            if (button == 0 || button == 1) {
                EntityMotionEditor.close(state);
                refresher.run();
                return true;
            }
        }

        if (button == 2) {
            state.draggingCanvas = true;
            state.dragCurrentX = localX;
            state.dragCurrentY = localY;
            canvasViewport.beginCanvasPan();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas pan start button=middle x={} y={} locked={} zoom={}", localX, localY, state.gridCanvasLocked, state.canvasZoom);
            return true;
        }

        if (!state.pendingQuestTitleChangeId.isBlank()) {
            boolean handledByEditor = canvasViewport.callSuperMouseClicked(mouseX, mouseY, button);
            if (handledByEditor) {
                return true;
            }
            if (button == 0 || button == 1) {
                EditorCommandClient.cancelQuestTitleChange(state);
                refresher.run();
                return true;
            }
        }

        if (state.contextMenuOpen) {
            if (button == 0) {
                if (canvasViewport.callSuperMouseClicked(mouseX, mouseY, button)) {
                    refresher.run();
                    return true;
                }
                return true;
            }
            if (button == 0 || button == 1) {
                CanvasViewportContextRouter.closeContextMenu(state);
                refresher.run();
                return true;
            }
        }

        boolean textMenuHit = state.canvasTextMenuOpen && textEditor.isMenuHit(localX, localY);
        boolean textEditorHit = state.canvasTextMenuOpen && textEditor.isEditorHit(localX, localY);
        boolean textOwnerHit = state.canvasTextMenuOpen && textEditor.isOwnerHit(localX, localY);
        if (state.canvasTextMenuOpen && !textMenuHit && !textEditorHit && !textOwnerHit) {
            state.canvasTextMenuOpen = false;
            state.canvasTextMenuTarget = "";
            state.canvasTextFontSizeFieldTarget = "";
            refresher.run();
        }

        if (state.canvasTextEditOpen && state.questDetailsTextEditTarget.isBlank() && button == 0) {
            CanvasTextLayer editingText = textEditor.activeText();
            boolean transformHandleHit = editingText != null
                    && (CanvasRenderer.isCanvasTextResizeHandleHit(state, editingText, localX, localY)
                    || CanvasRenderer.isCanvasTextRotateHandleHit(state, editingText, localX, localY));
            if (transformHandleHit) {
                textEditor.close("transform_start");
            } else {
                if (textEditorHit) {
                    int cursor = editingText == null ? state.canvasTextEditDraft.length() : CanvasRenderer.canvasTextCursorAt(state, editingText, localX, localY);
                    state.canvasTextEditCursor = cursor;
                    if (!canvasViewport.shiftDown()) {
                        state.canvasTextSelectionAnchor = cursor;
                    }
                    state.selectingCanvasTextRange = true;
                    canvasViewport.setFocus(true);
                    refresher.run();
                    return true;
                }
                if (textMenuHit) {
                    canvasViewport.callSuperMouseClicked(mouseX, mouseY, button);
                    return true;
                }
                textEditor.close("outside_click");
                refresher.run();
                if (state.mouseMode != CanvasMouseMode.DRAG_CANVAS) {
                    return true;
                }
            }
        }
        if (textMenuHit) {
            canvasViewport.callSuperMouseClicked(mouseX, mouseY, button);
            return true;
        }

        if (CanvasMinimapController.handleClick(state, localX, localY)) {
            canvasViewport.refreshCanvas();
            return true;
        }

        QuestCardLayout hit = TabletUiFactory.hitTestCard(cards, localX, localY);
        CanvasImageLayer imageHit = state.canEdit ? CanvasRenderer.hitTestCanvasImage(state, localX, localY) : null;
        if (imageHit == null && state.canEdit) {
            imageHit = CanvasRenderer.hitTestSelectedCanvasImageControls(state, localX, localY);
        }
        CanvasTextLayer textHit = state.canEdit ? CanvasRenderer.hitTestCanvasText(state, localX, localY) : null;
        if (textHit == null && state.canEdit) {
            textHit = CanvasRenderer.hitTestSelectedCanvasTextControls(state, localX, localY);
        }
        String selectedGroup = TabletStateQueries.selectedGroupName(state);
        List<CanvasImageLayer> canvasImages = state.canvasImagesByGroup.getOrDefault(selectedGroup, List.of());
        List<CanvasTextLayer> canvasTexts = state.canvasTextsByGroup.getOrDefault(selectedGroup, List.of());
        List<String> connectionKeys = ConnectionRenderer.prerequisiteConnectionLayerKeys(
                state,
                cards,
                byQuestId,
                canvasViewport.getSize().width,
                canvasViewport.getSize().height
        );
        CanvasLayerHit layerHit = CanvasLayerOrdering.normalizedOrder(state, selectedGroup, cards, canvasImages, canvasTexts, connectionKeys)
                .resolveElementHit(hit, imageHit, textHit);
        hit = layerHit.quest();
        imageHit = layerHit.image();
        textHit = layerHit.text();
        if (state.canEdit && button == 1) {
            CanvasViewportContextRouter.openContextMenu(state, refresher, cards, byQuestId, localX, localY, hit, imageHit, textHit);
            return true;
        }

        if (!state.canEdit) {
            if (hit != null) {
                state.canvasSelection.questIds().clear();
                state.canvasSelection.questIds().add(hit.questId());
                state.lastJumpQuest = hit.questId();
                if (button == 0 && !ClientQuestCache.questLockedPreview(hit.tag()) && !ClientQuestCache.questHiddenPreview(hit.tag())) {
                    int viewportScreenX = TabletWidgetCoordinates.screenX(canvasViewport, state.canvasPanelX + state.canvasViewportX);
                    int viewportScreenY = TabletWidgetCoordinates.screenY(canvasViewport, state.canvasPanelY + state.canvasViewportY);
                    QuestDetailsWindow.openAtSource(
                            state,
                            hit.questId(),
                            viewportScreenX + hit.x(),
                            viewportScreenY + hit.y(),
                            hit.width(),
                            hit.height()
                    );
                }
                refresher.run();
                return true;
            }
            return canvasViewport.callSuperMouseClicked(mouseX, mouseY, button);
        }

        if (CanvasConnectionClickActions.handleQuickConnect(state, player, refresher, hit, button)) {
            return true;
        }

        if (CanvasConnectionClickActions.handlePendingConnect(state, player, refresher, hit, button)) {
            return true;
        }

        if (state.mouseMode == CanvasMouseMode.ADD_QUEST && button == 0) {
            if (hit == null) {
                CanvasPoint anchor = CanvasGeometry.anchorForScreenVisualCenter(state, localX, localY, 1.0f);
                int logicalX = TabletUiFactory.snapToGrid(state, anchor.x);
                int logicalY = TabletUiFactory.snapToGrid(state, anchor.y);
                CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(
                        state,
                        logicalX,
                        logicalY,
                        CanvasGeometry.slotLogicalWidth(state, 1.0f),
                        CanvasGeometry.slotLogicalHeight(state, 1.0f)
                );
                TabletUiFactory.addQuestAt(player, state, clamped.x, clamped.y, "");
                refresher.run();
            }
            return true;
        }

        if (state.mouseMode == CanvasMouseMode.CONNECT_QUESTS) {
            return CanvasConnectionClickActions.handleConnectMode(canvasViewport, state, player, refresher, hit, button);
        }

        if (state.mouseMode == CanvasMouseMode.DRAG_CANVAS) {
            state.draggingCanvas = true;
            state.dragCurrentX = localX;
            state.dragCurrentY = localY;
            canvasViewport.beginCanvasPan();
            return true;
        }

        if (state.mouseMode == CanvasMouseMode.SELECT_MOVE) {
            CanvasSelectMoveClickActions.handleSelectMove(canvasViewport, state, refresher, byQuestId, textEditor, elementTransforms, selectionTransforms, localX, localY, button, hit, imageHit, textHit);
            return true;
        }

        return canvasViewport.callSuperMouseClicked(mouseX, mouseY, button);
    }
}
