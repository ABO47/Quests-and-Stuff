package com.abo47.questsandstuff.client.tablet.modal.actions;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasMouseMode;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.model.CanvasModelPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.util.StableIdAllocator;

import java.util.ArrayList;
import java.util.List;

public final class CanvasModelPickerActions {
    private static final int MIN_MODEL_SIZE = 48;

    private CanvasModelPickerActions() {
    }

    public static boolean run(TabletUiState state, String target, String pickedValue) {
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        String asset = assetForPick(parsed, pickedValue);
        String group = parsed.part(1);
        if (group.isBlank() || asset.isBlank()) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas model pick ignored target={} value={} asset={}", target, pickedValue, asset);
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
            return CanvasModelPreviewRenderer.itemAssetForPick(pickedValue);
        }
        if (parsed.isCanvasBlockNew() || parsed.isCanvasBlockChange()) {
            return CanvasModelPreviewRenderer.blockAssetForPick(pickedValue);
        }
        return "";
    }

    private static boolean changeModel(TabletUiState state, ModalTargetParser.Target parsed, String asset) {
        String group = parsed.part(1);
        String imageId = parsed.part(2);
        CanvasImageLayer current = CanvasRenderer.findCanvasImage(state, group, imageId);
        if (current == null) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas model change ignored group={} image={} reason=missing_image", group, imageId);
            return false;
        }
        CanvasRenderer.putCanvasImage(state, group, current.withAsset(asset));
        selectOnlyImage(state, current.id());
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas model changed group={} image={} asset={}", group, current.id(), asset);
        return true;
    }

    private static void addModel(TabletUiState state, ModalTargetParser.Target parsed, String group, String asset) {
        String id = StableIdAllocator.nextId(parsed.isCanvasBlockNew() ? "blk" : "itm", canvasImageIds(state, group));
        int size = Math.max(MIN_MODEL_SIZE, CanvasGeometry.gridSize(state) * 3);
        int x = TabletUiFactory.snapToGrid(state, state.canvasImageLogicalX - size / 2);
        int y = TabletUiFactory.snapToGrid(state, state.canvasImageLogicalY - size / 2);
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, x, y, size, size);
        CanvasImageLayer image = CanvasModelPreviewRenderer.isBlockModelAsset(asset)
                ? new CanvasImageLayer(id, asset, clamped.x, clamped.y, size, size, 0, CanvasModelPreviewRenderer.DEFAULT_BLOCK_YAW, CanvasImageLayer.DEFAULT_ENTITY_SPIN_SPEED, CanvasModelPreviewRenderer.DEFAULT_BLOCK_PITCH)
                : new CanvasImageLayer(id, asset, clamped.x, clamped.y, size, size, 0);
        CanvasRenderer.putCanvasImage(state, group, image);
        selectOnlyImage(state, id);
        state.draggingCanvasImage = false;
        state.resizingCanvasImage = false;
        state.rotatingCanvasImage = false;
        state.mouseMode = CanvasMouseMode.SELECT_MOVE;
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas model added group={} id={} asset={} pos={},{} size={}x{}", group, id, asset, clamped.x, clamped.y, size, size);
    }

    private static void selectOnlyImage(TabletUiState state, String id) {
        state.selectedCanvasImageId = id;
        state.selectedCanvasImageIds.clear();
        state.selectedCanvasImageIds.add(id);
        state.selectedCanvasTextId = "";
        state.selectedCanvasTextIds.clear();
        state.selectedQuestIds.clear();
        state.contextMenuOpen = false;
        state.contextDeleteConfirmKey = "";
    }

    private static List<String> canvasImageIds(TabletUiState state, String group) {
        List<String> ids = new ArrayList<>();
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            ids.add(image.id());
        }
        return ids;
    }
}
