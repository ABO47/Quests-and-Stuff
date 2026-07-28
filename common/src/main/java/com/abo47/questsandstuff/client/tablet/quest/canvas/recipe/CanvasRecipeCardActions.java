package com.abo47.questsandstuff.client.tablet.quest.canvas.recipe;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession;
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
        String chapter = parsed.part(1);
        if (chapter.isBlank()) {
            return false;
        }
        addRecipeCard(state, chapter, asset);
        state.questDetails.questDetailsPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas recipe card picked chapter={} recipe={}", chapter, recipe.trim());
        return true;
    }

    private static boolean changeRecipe(TabletUiState state, ModalTargetParser.Target parsed, String asset) {
        String chapter = parsed.part(1);
        String imageId = parsed.part(2);
        CanvasImageLayer current = CanvasLayerMutations.findCanvasImage(state, chapter, imageId);
        if (current == null) {
            return false;
        }
        CanvasLayerMutations.putCanvasImage(state, chapter, current.withAsset(asset));
        selectOnlyImage(state, imageId);
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas recipe card changed chapter={} image={} asset={}", chapter, imageId, asset);
        return true;
    }

    private static void addRecipeCard(TabletUiState state, String chapter, String asset) {
        String id = StableIdAllocator.nextId("rcp", canvasImageIds(state, chapter));
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
        CanvasLayerMutations.putCanvasImage(state, chapter, image);
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
        ContextMenuController.close(state);
        ContextMenuController.clearDeleteConfirm(state);
    }

    private static List<String> canvasImageIds(TabletUiState state, String chapter) {
        List<String> ids = new ArrayList<>();
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            ids.add(image.id());
        }
        return ids;
    }
}
