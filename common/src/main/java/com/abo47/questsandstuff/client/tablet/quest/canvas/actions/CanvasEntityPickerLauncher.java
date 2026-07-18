package com.abo47.questsandstuff.client.tablet.quest.canvas.actions;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetState;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasMouseMode;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.util.naming.StableIdAllocator;

public final class CanvasEntityPickerLauncher {
    private CanvasEntityPickerLauncher() {
    }

    public static boolean run(Player player, TabletUiState state, String target, String pickedItem) {
        return run(player, state, ModalTargetParser.parse(target), pickedItem);
    }

    public static boolean run(Player player, TabletUiState state, ModalTargetParser.Target target, String pickedItem) {
        String entityId = EntityPreviewRenderer.entityIdFromSpawnEgg(pickedItem);
        EntityTarget parsed = entityTarget(target);
        if (parsed.chapter().isBlank() || entityId.isBlank()) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas entity pick ignored target={} item={} entity={}", target.raw(), pickedItem, entityId);
            return false;
        }
        if ("change".equals(parsed.action()) || "change_batch".equals(parsed.action())) {
            return changeEntity(state, pickedItem, entityId, parsed);
        }
        addEntity(state, entityId, parsed.chapter());
        return true;
    }

    private static boolean changeEntity(TabletUiState state, String pickedItem, String entityId, EntityTarget parsed) {
        Set<String> targets = new LinkedHashSet<>();
        if (parsed.imageIds() != null) {
            targets.addAll(parsed.imageIds());
        } else if (!parsed.imageId().isBlank()) {
            targets.add(parsed.imageId());
        }
        String entityAsset = EntityPreviewRenderer.entityAsset(entityId);
        String chapter = parsed.chapter();
        for (String imageId : targets) {
            if (imageId.isBlank()) continue;
            CanvasImageLayer img = CanvasLayerMutations.findCanvasImage(state, chapter, imageId);
            if (img == null) {
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas entity change skipped chapter={} image={} reason=missing", chapter, imageId);
                continue;
            }
            CanvasLayerMutations.putCanvasImage(state, chapter, img.withAsset(entityAsset));
        }
        if (!targets.isEmpty()) {
            String firstId = targets.iterator().next();
            state.canvas.canvasSelection.setPrimaryImageId(firstId);
            state.canvas.canvasSelection.imageIds().clear();
            state.canvas.canvasSelection.imageIds().addAll(targets);
            state.canvas.canvasSelection.setPrimaryTextId("");
            state.canvas.canvasSelection.textIds().clear();
            state.canvas.canvasSelection.questIds().clear();
        }
        ContextMenuController.close(state);
        ContextMenuController.clearDeleteConfirm(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas entity changed chapter={} images={} entity={}", chapter, targets.size(), entityId);
        return !targets.isEmpty();
    }

    private static void addEntity(TabletUiState state, String entityId, String chapter) {
        String id = StableIdAllocator.nextId("ent", canvasImageIds(state, chapter));
        int size = Math.max(48, CanvasGeometry.gridSize(state) * 4);
        int x = state.canvas.canvasImageLogicalX - size / 2;
        int y = state.canvas.canvasImageLogicalY - size / 2;
        if (!state.canvas.gridSnapLocked) {
            x = TabletUiFactory.snapToGrid(state, x);
            y = TabletUiFactory.snapToGrid(state, y);
        }
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, x, y, size, size);
        CanvasImageLayer image = new CanvasImageLayer(id, EntityPreviewRenderer.entityAsset(entityId), clamped.x, clamped.y, size, size, 0);
        if (state.canvas.gridSnapLocked) {
            image = CanvasGridFitController.fittedImage(state, image);
        }
        CanvasLayerMutations.putCanvasImage(state, chapter, image);
        state.canvas.canvasSelection.setPrimaryImageId(id);
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.questIds().clear();
        state.canvas.draggingCanvasImage = false;
        state.canvas.resizingCanvasImage = false;
        state.canvas.rotatingCanvasImage = false;
        state.canvas.mouseMode = CanvasMouseMode.SELECT_MOVE;
        ContextMenuController.close(state);
        ContextMenuController.clearDeleteConfirm(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas entity added chapter={} id={} entity={} pos={},{} size={}x{}", chapter, id, entityId, clamped.x, clamped.y, size, size);
    }

    private static List<String> canvasImageIds(TabletUiState state, String chapter) {
        List<String> ids = new ArrayList<>();
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            ids.add(image.id());
        }
        return ids;
    }

    private static EntityTarget entityTarget(ModalTargetParser.Target target) {
        if (target.kind().isBlank()) {
            return new EntityTarget("", "", "", List.of());
        }
        if (target.isCanvasEntityChange()) {
            if (!ModalTargetState.requireParts("canvas_entity_change", target, 3)) {
                return new EntityTarget("", "", "", List.of());
            }
            return new EntityTarget("change", target.questId(), target.entryId(), List.of());
        }
        if (target.isCanvasEntityChangeBatch()) {
            List<String> ids = new ArrayList<>();
            for (int i = 2; i < target.parts().length; i++) {
                String id = target.part(i);
                if (!id.isBlank()) {
                    ids.add(id);
                }
            }
            return new EntityTarget("change_batch", target.questId(), "", ids);
        }
        if (target.isCanvasEntityNew()) {
            if (!ModalTargetState.requireParts("canvas_entity_new", target, 2)) {
                return new EntityTarget("", "", "", List.of());
            }
            return new EntityTarget("new", target.questId(), "", List.of());
        }
        return new EntityTarget("new", target.raw(), "", List.of());
    }

    private record EntityTarget(String action, String chapter, String imageId, List<String> imageIds) {
    }
}
