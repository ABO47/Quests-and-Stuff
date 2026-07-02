package com.abo47.questsandstuff.client.tablet.quest.canvas.actions;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasMouseMode;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetState;
import com.abo47.questsandstuff.client.tablet.preview.ModelAssetPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.util.naming.StableIdAllocator;

import java.util.ArrayList;
import java.util.List;

public final class CanvasModelPickerLauncher {
    private static final int MIN_MODEL_SIZE = 48;

    private CanvasModelPickerLauncher() {
    }

    public static boolean run(TabletUiState state, String target, String pickedValue) {
        return run(state, ModalTargetParser.parse(target), pickedValue);
    }

    public static boolean run(TabletUiState state, ModalTargetParser.Target parsed, String pickedValue) {
        int requiredParts = parsed.isCanvasItemChange() || parsed.isCanvasBlockChange() ? 3 : 2;
        if (!ModalTargetState.requireParts("canvas_model", parsed, requiredParts)) {
            return false;
        }
        String asset = assetForPick(parsed, pickedValue);
        String group = parsed.part(1);
        if (group.isBlank() || asset.isBlank()) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas model pick ignored target={} value={} asset={}", parsed.raw(), pickedValue, asset);
            return false;
        }
        if (parsed.isCanvasItemChange() || parsed.isCanvasBlockChange()) {
            return changeModel(state, parsed, asset);
        }
        addModel(state, parsed, group, asset);
        return true;
    }

    private static String assetForPick(ModalTargetParser.Target parsed, String pickedValue) {
        if (parsed.isCanvasItemNew() || parsed.isCanvasItemChange()) {
            return ModelAssetPreviewRenderer.itemAssetForPick(pickedValue);
        }
        if (parsed.isCanvasBlockNew() || parsed.isCanvasBlockChange()) {
            return ModelAssetPreviewRenderer.blockAssetForPick(pickedValue);
        }
        return "";
    }

    private static boolean changeModel(TabletUiState state, ModalTargetParser.Target parsed, String asset) {
        String group = parsed.part(1);
        String imageId = parsed.part(2);
        CanvasImageLayer current = CanvasLayerMutations.findCanvasImage(state, group, imageId);
        if (current == null) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas model change ignored group={} image={} reason=missing_image", group, imageId);
            return false;
        }
        CanvasLayerMutations.putCanvasImage(state, group, current.withAsset(asset));
        selectOnlyImage(state, current.id());
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas model changed group={} image={} asset={}", group, current.id(), asset);
        return true;
    }

    private static void addModel(TabletUiState state, ModalTargetParser.Target parsed, String group, String asset) {
        String id = StableIdAllocator.nextId(parsed.isCanvasBlockNew() ? "blk" : "itm", canvasImageIds(state, group));
        int size = Math.max(MIN_MODEL_SIZE, CanvasGeometry.gridSize(state) * 3);
        int x = state.canvas.canvasImageLogicalX - size / 2;
        int y = state.canvas.canvasImageLogicalY - size / 2;
        if (!state.canvas.gridSnapLocked) {
            x = TabletUiFactory.snapToGrid(state, x);
            y = TabletUiFactory.snapToGrid(state, y);
        }
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, x, y, size, size);
        CanvasImageLayer image = ModelAssetPreviewRenderer.isBlockModelAsset(asset)
                ? new CanvasImageLayer(id, asset, clamped.x, clamped.y, size, size, 0, ModelAssetPreviewRenderer.DEFAULT_BLOCK_YAW, CanvasImageLayer.DEFAULT_ENTITY_SPIN_SPEED, ModelAssetPreviewRenderer.DEFAULT_BLOCK_PITCH)
                : new CanvasImageLayer(id, asset, clamped.x, clamped.y, size, size, 0);
        if (state.canvas.gridSnapLocked) {
            image = CanvasGridFitController.fittedImage(state, image);
        }
        CanvasLayerMutations.putCanvasImage(state, group, image);
        selectOnlyImage(state, id);
        state.canvas.draggingCanvasImage = false;
        state.canvas.resizingCanvasImage = false;
        state.canvas.rotatingCanvasImage = false;
        state.canvas.mouseMode = CanvasMouseMode.SELECT_MOVE;
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas model added group={} id={} asset={} pos={},{} size={}x{}", group, id, asset, clamped.x, clamped.y, size, size);
    }

    private static void selectOnlyImage(TabletUiState state, String id) {
        state.canvas.canvasSelection.setPrimaryImageId(id);
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.imageIds().add(id);
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.questIds().clear();
        ContextMenuController.close(state);
        ContextMenuController.clearDeleteConfirm(state);
    }

    private static List<String> canvasImageIds(TabletUiState state, String group) {
        List<String> ids = new ArrayList<>();
        for (CanvasImageLayer image : state.canvas.canvasImagesByGroup.getOrDefault(group, List.of())) {
            ids.add(image.id());
        }
        return ids;
    }
}
