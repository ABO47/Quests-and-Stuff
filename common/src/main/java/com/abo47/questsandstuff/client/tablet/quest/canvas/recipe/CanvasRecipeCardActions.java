package com.abo47.questsandstuff.client.tablet.quest.canvas.recipe;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetState;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasMouseMode;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.util.StableIdAllocator;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class CanvasRecipeCardActions {
    private static final int CARD_W = 136;
    private static final int CARD_H = 92;

    private CanvasRecipeCardActions() {
    }

    public static boolean applyRecipePick(Player player, TabletUiState state, String recipe) {
        ModalTargetParser.Target parsed = ModalTargetState.parsedTarget(state, ModalSession.TargetSlot.QUEST_DETAILS_PICK, state.questDetails.questDetailsPickTarget);
        if (parsed.kind().isBlank() || recipe == null || recipe.isBlank()) {
            return false;
        }
        int requiredParts = parsed.isCanvasRecipeChange() ? 3 : 2;
        if (!ModalTargetState.requireParts("canvas_recipe_card", parsed, requiredParts)) {
            return false;
        }
        String asset = CanvasRecipeCardAsset.assetForPick(recipe);
        if (asset.isBlank()) {
            return false;
        }
        if (parsed.isCanvasRecipeChange()) {
            boolean changed = changeRecipe(state, parsed, asset);
            if (changed) {
                state.questDetails.questDetailsPickTarget = "";
            }
            return changed;
        }
        if (!parsed.isCanvasRecipeNew()) {
            return false;
        }
        String group = parsed.part(1);
        if (group.isBlank()) {
            return false;
        }
        addRecipeCard(state, group, asset);
        state.questDetails.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas recipe card picked group={} recipe={}", group, recipe.trim());
        return true;
    }

    private static boolean changeRecipe(TabletUiState state, ModalTargetParser.Target parsed, String asset) {
        String group = parsed.part(1);
        String imageId = parsed.part(2);
        CanvasImageLayer current = CanvasLayerMutations.findCanvasImage(state, group, imageId);
        if (current == null) {
            return false;
        }
        CanvasLayerMutations.putCanvasImage(state, group, current.withAsset(asset));
        selectOnlyImage(state, imageId);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas recipe card changed group={} image={} asset={}", group, imageId, asset);
        return true;
    }

    private static void addRecipeCard(TabletUiState state, String group, String asset) {
        String id = StableIdAllocator.nextId("rcp", canvasImageIds(state, group));
        int x = state.canvas.canvasImageLogicalX - CARD_W / 2;
        int y = state.canvas.canvasImageLogicalY - CARD_H / 2;
        if (!state.canvas.gridSnapLocked) {
            x = TabletUiFactory.snapToGrid(state, x);
            y = TabletUiFactory.snapToGrid(state, y);
        }
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, x, y, CARD_W, CARD_H);
        CanvasImageLayer image = new CanvasImageLayer(id, asset, clamped.x, clamped.y, CARD_W, CARD_H, 0);
        if (state.canvas.gridSnapLocked) {
            image = CanvasGridFitController.fittedImage(state, image);
        }
        CanvasLayerMutations.putCanvasImage(state, group, image);
        selectOnlyImage(state, id);
        state.canvas.draggingCanvasImage = false;
        state.canvas.resizingCanvasImage = false;
        state.canvas.rotatingCanvasImage = false;
        state.canvas.mouseMode = CanvasMouseMode.SELECT_MOVE;
    }

    private static void selectOnlyImage(TabletUiState state, String id) {
        state.canvas.canvasSelection.setPrimaryImageId(id);
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.imageIds().add(id);
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.questIds().clear();
        ContextMenuState.close(state);
        ContextMenuState.clearDeleteConfirm(state);
    }

    private static List<String> canvasImageIds(TabletUiState state, String group) {
        List<String> ids = new ArrayList<>();
        for (CanvasImageLayer image : state.canvas.canvasImagesByGroup.getOrDefault(group, List.of())) {
            ids.add(image.id());
        }
        return ids;
    }
}
