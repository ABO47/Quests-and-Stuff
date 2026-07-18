package com.abo47.questsandstuff.client.tablet.quest.canvas;

import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerHit;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.CanvasLayerOrdering;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextStyleSession;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasElementTransformController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasInlineTextEditor;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasMinimapController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasSelectionTransformController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.ui.widget.TabletWidgetCoordinates;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

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
        int localX = TabletWidgetCoordinates.localX(canvasViewport, state.canvas.canvasPanelX + state.canvas.canvasViewportX, mouseX);
        int localY = TabletWidgetCoordinates.localY(canvasViewport, state.canvas.canvasPanelY + state.canvas.canvasViewportY, mouseY);

        if (state.canvas.blueprintPlacement.active()) {
            if (!state.root.canEdit) {
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
            state.canvas.draggingCanvas = true;
            state.canvas.dragCurrentX = localX;
            state.canvas.dragCurrentY = localY;
            canvasViewport.beginCanvasPan();
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas pan start button=middle x={} y={} locked={} zoom={}", localX, localY, state.canvas.gridCanvasLocked, state.canvas.canvasZoom);
            return true;
        }

        if (!state.questDetails.pendingQuestTitleChangeId.isBlank()) {
            boolean handledByEditor = canvasViewport.callSuperMouseClicked(mouseX, mouseY, button);
            if (handledByEditor) {
                return true;
            }
            if (button == 0 || button == 1) {
                EditorQuestCommandClient.cancelQuestTitleChange(state);
                refresher.run();
                return true;
            }
        }

        if (state.contextMenu.contextMenuOpen) {
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

        boolean textMenuHit = state.canvas.canvasTextMenuOpen && textEditor.isMenuHit(localX, localY);
        boolean textEditorHit = state.canvas.canvasTextMenuOpen && textEditor.isEditorHit(localX, localY);
        boolean textOwnerHit = state.canvas.canvasTextMenuOpen && textEditor.isOwnerHit(localX, localY);
        if (state.canvas.canvasTextMenuOpen && !textMenuHit && !textEditorHit && !textOwnerHit) {
            TextStyleSession.closeMainCanvas(state);
            refresher.run();
        }

        if (TextEditSession.isMainCanvasEditing(state) && button == 0) {
            CanvasTextLayer editingText = textEditor.activeText();
            boolean transformHandleHit = editingText != null
                    && (CanvasRenderer.isCanvasTextResizeHandleHit(state, editingText, localX, localY)
                    || CanvasRenderer.isCanvasTextRotateHandleHit(state, editingText, localX, localY));
            if (transformHandleHit) {
                textEditor.close("transform_start");
            } else {
                if (textEditorHit) {
                    int cursor = editingText == null ? state.canvas.canvasTextEditDraft.length() : CanvasRenderer.canvasTextCursorAt(state, editingText, localX, localY);
                    TextEditSession.moveCursor(state, cursor, canvasViewport.shiftDown());
                    TextEditSession.startRangeSelection(state);
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
                if (state.canvas.mouseMode != CanvasMouseMode.DRAG_CANVAS) {
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
        CanvasImageLayer imageHit = state.root.canEdit ? CanvasRenderer.hitTestCanvasImage(state, localX, localY) : null;
        if (imageHit == null && state.root.canEdit) {
            imageHit = CanvasRenderer.hitTestSelectedCanvasImageControls(state, localX, localY);
        }
        CanvasTextLayer textHit = state.root.canEdit ? CanvasRenderer.hitTestCanvasText(state, localX, localY) : null;
        if (textHit == null && state.root.canEdit) {
            textHit = CanvasRenderer.hitTestSelectedCanvasTextControls(state, localX, localY);
        }
        CanvasExclusiveChoice ecHit = state.root.canEdit ? CanvasRenderer.hitTestCanvasExclusiveChoice(state, localX, localY) : null;
        if (ecHit == null && state.root.canEdit) {
            ecHit = CanvasRenderer.hitTestSelectedCanvasExclusiveChoiceControls(state, localX, localY);
        }
        String selectedChapter = TabletStateQueries.selectedChapterName(state);
        List<CanvasImageLayer> canvasImages = state.canvas.canvasImagesByChapter.getOrDefault(selectedChapter, List.of());
        List<CanvasTextLayer> canvasTexts = state.canvas.canvasTextsByChapter.getOrDefault(selectedChapter, List.of());
        List<CanvasExclusiveChoice> canvasExclusiveChoices = state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(selectedChapter, List.of());
        List<String> connectionKeys = ConnectionRenderer.prerequisiteConnectionLayerKeys(
                state,
                cards,
                byQuestId,
                canvasViewport.getSize().width,
                canvasViewport.getSize().height
        );
        CanvasLayerHit layerHit = CanvasLayerOrdering.normalizedOrder(state, selectedChapter, cards, canvasImages, canvasTexts, connectionKeys, canvasExclusiveChoices)
                .resolveElementHit(hit, imageHit, textHit, ecHit);
        hit = layerHit.quest();
        imageHit = layerHit.image();
        textHit = layerHit.text();
        ecHit = layerHit.exclusiveChoice();
        if (state.root.canEdit && button == 1) {
            CanvasViewportContextRouter.openContextMenu(state, refresher, cards, byQuestId, localX, localY, hit, imageHit, textHit, ecHit);
            return true;
        }

        if (!state.root.canEdit) {
            if (hit != null) {
                state.canvas.canvasSelection.questIds().clear();
                state.canvas.canvasSelection.questIds().add(hit.questId());
                state.chapterPanel.lastJumpQuest = hit.questId();
                if (button == 0 && !ClientQuestStateFacade.questLockedPreview(hit.tag()) && !ClientQuestStateFacade.questHiddenPreview(hit.tag())) {
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
                }
                refresher.run();
                return true;
            }
            return canvasViewport.callSuperMouseClicked(mouseX, mouseY, button);
        }

        if (CanvasConnectionClickActions.handleQuickConnect(state, player, refresher, hit, ecHit, button)) {
            return true;
        }

        if (CanvasConnectionClickActions.handlePendingConnect(state, player, refresher, hit, ecHit, button)) {
            return true;
        }

        if (state.canvas.mouseMode == CanvasMouseMode.ADD_QUEST && button == 0) {
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

        if (state.canvas.mouseMode == CanvasMouseMode.CONNECT_QUESTS) {
            return CanvasConnectionClickActions.handleConnectMode(canvasViewport, state, player, refresher, hit, button);
        }

        if (state.canvas.mouseMode == CanvasMouseMode.DRAG_CANVAS) {
            state.canvas.draggingCanvas = true;
            state.canvas.dragCurrentX = localX;
            state.canvas.dragCurrentY = localY;
            canvasViewport.beginCanvasPan();
            return true;
        }

        if (state.canvas.mouseMode == CanvasMouseMode.SELECT_MOVE) {
            CanvasSelectMoveClickActions.handleSelectMove(canvasViewport, state, refresher, byQuestId, textEditor, elementTransforms, selectionTransforms, localX, localY, button, hit, imageHit, textHit, ecHit);
            return true;
        }

        return canvasViewport.callSuperMouseClicked(mouseX, mouseY, button);
    }
}
