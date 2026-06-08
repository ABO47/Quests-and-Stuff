package com.abo47.questsandstuff.client.tablet.quest.canvas.actions;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasMouseMode;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.util.StableIdAllocator;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class CanvasEntityPickerActions {
    private CanvasEntityPickerActions() {
    }

    public static boolean run(Player player, TabletUiState state, String target, String pickedItem) {
        String entityId = EntityPreviewRenderer.entityIdFromSpawnEgg(pickedItem);
        EntityTarget parsed = parseEntityTarget(target);
        if (parsed.group().isBlank() || entityId.isBlank()) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas entity pick ignored target={} item={} entity={}", target, pickedItem, entityId);
            return false;
        }
        if ("change".equals(parsed.action())) {
            return changeEntity(state, pickedItem, entityId, parsed);
        }
        addEntity(state, entityId, parsed.group());
        return true;
    }

    private static boolean changeEntity(TabletUiState state, String pickedItem, String entityId, EntityTarget parsed) {
        CanvasImageLayer current = CanvasLayerMutations.findCanvasImage(state, parsed.group(), parsed.imageId());
        if (current == null) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas entity change ignored group={} image={} item={} reason=missing_image", parsed.group(), parsed.imageId(), pickedItem);
            return false;
        }
        CanvasLayerMutations.putCanvasImage(state, parsed.group(), current.withAsset(EntityPreviewRenderer.entityAsset(entityId)));
        state.selectedCanvasImageId = current.id();
        state.selectedCanvasImageIds.clear();
        state.selectedCanvasImageIds.add(current.id());
        state.selectedCanvasTextId = "";
        state.selectedCanvasTextIds.clear();
        state.selectedQuestIds.clear();
        ContextMenuState.close(state);
        ContextMenuState.clearDeleteConfirm(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas entity changed group={} image={} entity={}", parsed.group(), current.id(), entityId);
        return true;
    }

    private static void addEntity(TabletUiState state, String entityId, String group) {
        String id = StableIdAllocator.nextId("ent", canvasImageIds(state, group));
        int size = Math.max(48, CanvasGeometry.gridSize(state) * 4);
        int x = state.canvasImageLogicalX - size / 2;
        int y = state.canvasImageLogicalY - size / 2;
        if (!state.gridSnapLocked) {
            x = TabletUiFactory.snapToGrid(state, x);
            y = TabletUiFactory.snapToGrid(state, y);
        }
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, x, y, size, size);
        CanvasImageLayer image = new CanvasImageLayer(id, EntityPreviewRenderer.entityAsset(entityId), clamped.x, clamped.y, size, size, 0);
        if (state.gridSnapLocked) {
            image = CanvasGridFitController.fittedImage(state, image);
        }
        CanvasLayerMutations.putCanvasImage(state, group, image);
        state.selectedCanvasImageId = id;
        state.selectedCanvasImageIds.clear();
        state.selectedCanvasTextId = "";
        state.selectedCanvasTextIds.clear();
        state.selectedQuestIds.clear();
        state.draggingCanvasImage = false;
        state.resizingCanvasImage = false;
        state.rotatingCanvasImage = false;
        state.mouseMode = CanvasMouseMode.SELECT_MOVE;
        ContextMenuState.close(state);
        ContextMenuState.clearDeleteConfirm(state);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas entity added group={} id={} entity={} pos={},{} size={}x{}", group, id, entityId, clamped.x, clamped.y, size, size);
    }

    private static List<String> canvasImageIds(TabletUiState state, String group) {
        List<String> ids = new ArrayList<>();
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            ids.add(image.id());
        }
        return ids;
    }

    private static EntityTarget parseEntityTarget(String target) {
        String clean = target == null ? "" : target.trim();
        if (clean.isBlank()) {
            return new EntityTarget("", "", "");
        }
        String[] parts = clean.split("\\|", 3);
        if (parts.length >= 3 && "change".equals(parts[0])) {
            return new EntityTarget("change", parts[1], parts[2]);
        }
        if (parts.length >= 2 && "new".equals(parts[0])) {
            return new EntityTarget("new", parts[1], "");
        }
        return new EntityTarget("new", clean, "");
    }

    private record EntityTarget(String action, String group, String imageId) {
    }
}
