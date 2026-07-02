package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerIntegrations;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.layout.TabletGridControls;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.tokens.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.util.naming.StableIdAllocator;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.addQuestAt;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.runGroupAction;
import static com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory.snapToGrid;

final class CanvasContextCanvasActions {
    private CanvasContextCanvasActions() {
    }

    static void addCanvasEmptyActions(List<ContextAction> actions, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedGroup) {
        if (state.contextMenu.contextMenuTarget != ContextMenuTarget.CANVAS || selectedGroup.isBlank()) {
            return;
        }
        actions.add(ContextActions.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.quick_add_quest"), "add", ModColors.SUCCESS, () -> {
            int logicalX = snapToGrid(state, state.contextMenu.contextLogicalX);
            int logicalY = snapToGrid(state, state.contextMenu.contextLogicalY);
            CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(
                    state,
                    logicalX,
                    logicalY,
                    CanvasGeometry.slotLogicalWidth(state, 1.0f),
                    CanvasGeometry.slotLogicalHeight(state, 1.0f)
            );
            logicalX = clamped.x;
            logicalY = clamped.y;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add logicalX={} logicalY={} target={}", logicalX, logicalY, state.contextMenu.contextMenuTarget);
            addQuestAt(player, state, logicalX, logicalY, "");
            canvasViewport.refresh();
        }));
        List<ContextAction> addActions = new ArrayList<>();
        addActions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_image"), "image", ModColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasImagePicker(state, selectedGroup, state.contextMenu.contextPointerLogicalX, state.contextMenu.contextPointerLogicalY);
            ContextMenuState.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_image group={} logical={},{}", selectedGroup, state.contextMenu.contextLogicalX, state.contextMenu.contextLogicalY);
            canvasViewport.refresh();
        }));
        addActions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_text_box"), "text", ModColors.SUCCESS, () -> {
            String id = StableIdAllocator.nextId("txt", canvasTextIds(state, selectedGroup));
            int textW = 96;
            int textH = 32;
            int x = snapToGrid(state, state.contextMenu.contextPointerLogicalX - textW / 2);
            int y = snapToGrid(state, state.contextMenu.contextPointerLogicalY - textH / 2);
            CanvasTextLayer text = new CanvasTextLayer(id, "Text", x, y, textW, textH, 0, "left", "normal", ModColors.TEXT_PRIMARY);
            if (state.canvas.gridSnapLocked) {
                text = CanvasGridFitController.fittedText(state, text);
            }
            CanvasLayerMutations.putCanvasText(state, selectedGroup, text);
            state.canvas.canvasSelection.setPrimaryTextId(id);
            state.canvas.canvasSelection.setPrimaryImageId("");
            state.canvas.canvasSelection.questIds().clear();
            TextEditSession.beginMainCanvas(state, id, text.text());
            ContextMenuState.close(state);
            canvasViewport.setFocus(true);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_text_box group={} id={} logical={},{}", selectedGroup, id, x, y);
            canvasViewport.refresh();
        }));
        addActions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_entity"), "entity", ModColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasEntityPicker(state, ModalTargets.canvasEntityNew(selectedGroup), state.contextMenu.contextPointerLogicalX, state.contextMenu.contextPointerLogicalY);
            ContextMenuState.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_entity group={} logical={},{}", selectedGroup, state.contextMenu.contextLogicalX, state.contextMenu.contextLogicalY);
            canvasViewport.refresh();
        }));
        addActions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_exclusive_choice"), "split", ModColors.SUCCESS, () -> {
            String id = StableIdAllocator.nextId("ec", canvasExclusiveChoiceIds(state, selectedGroup));
            int defaultW = TabletUiFactory.CARD_W;
            int defaultH = TabletUiFactory.CARD_H;
            int x = snapToGrid(state, state.contextMenu.contextPointerLogicalX - defaultW / 2);
            int y = snapToGrid(state, state.contextMenu.contextPointerLogicalY - defaultH / 2);
            CanvasExclusiveChoice ec = new CanvasExclusiveChoice(id, x, y, defaultW, defaultH, 0, List.of());
            if (state.canvas.gridSnapLocked) {
                ec = CanvasGridFitController.fittedExclusiveChoice(state, ec);
            }
            CanvasLayerMutations.putCanvasExclusiveChoice(state, selectedGroup, ec);
            state.canvas.canvasSelection.setPrimaryEcId(id);
            state.canvas.canvasSelection.setPrimaryImageId("");
            state.canvas.canvasSelection.setPrimaryTextId("");
            state.canvas.canvasSelection.questIds().clear();
            ContextMenuState.close(state);
            canvasViewport.setFocus(true);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_exclusive_choice group={} id={} logical={},{}", selectedGroup, id, x, y);
            canvasViewport.refresh();
        }));
        addActions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_item"), "icon", ModColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasItemPicker(state, ModalTargets.canvasItemNew(selectedGroup), state.contextMenu.contextPointerLogicalX, state.contextMenu.contextPointerLogicalY);
            ContextMenuState.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_item_model group={} logical={},{}", selectedGroup, state.contextMenu.contextLogicalX, state.contextMenu.contextLogicalY);
            canvasViewport.refresh();
        }));
        addActions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_block"), "add_block", ModColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasBlockPicker(state, ModalTargets.canvasBlockNew(selectedGroup), state.contextMenu.contextPointerLogicalX, state.contextMenu.contextPointerLogicalY);
            ContextMenuState.close(state);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_block_model group={} logical={},{}", selectedGroup, state.contextMenu.contextLogicalX, state.contextMenu.contextLogicalY);
            canvasViewport.refresh();
        }));
        if (RecipeViewerIntegrations.hasAvailableViewer()) {
            addActions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.add_recipe_card"), "recipe", ModColors.SUCCESS, () -> {
                ModalOpenActions.openCanvasRecipePicker(state, ModalTargets.canvasRecipeNew(selectedGroup), state.contextMenu.contextPointerLogicalX, state.contextMenu.contextPointerLogicalY);
                ContextMenuState.close(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_recipe_card group={} logical={},{}", selectedGroup, state.contextMenu.contextLogicalX, state.contextMenu.contextLogicalY);
                canvasViewport.refresh();
            }));
        }
        actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_ADD), "add", ModColors.SUCCESS, addActions));

        actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.change_canvas_bg"), "background", ModColors.INTERACTIVE, () -> {
            ModalOpenActions.openCanvasBackgroundPicker(state, selectedGroup, ClientQuestCache.groupCanvasBackground(selectedGroup));
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_canvas_bg group={}", selectedGroup);
            canvasViewport.refresh();
        }));
        actions.add(ContextActions.action(TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_GRID_COLOR), "style_color", ModColors.INTERACTIVE, () -> {
            int color = TabletGridControls.defaultGridColor(state);
            ModalOpenActions.openColorPicker(state, ModalTargets.gridColor(), color);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_grid_color color={}", color);
            canvasViewport.refresh();
        }));
        if (!ClientQuestCache.groupCanvasBackground(selectedGroup).isBlank()
                && !"default".equals(ClientQuestCache.groupCanvasBackground(selectedGroup))) {
            actions.add(ContextActions.action(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_canvas_bg"), "delete", ModColors.WARNING, () -> {
                runGroupAction(player, state, "set_canvas_background", selectedGroup, "default", 0);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_canvas_bg group={}", selectedGroup);
                canvasViewport.refresh();
            }));
        }

        List<ContextAction> debugActions = new ArrayList<>();
        debugActions.add(ContextActions.action(
                CanvasContextMenuController.tr("ui.questsandstuff.context.debug_spawn_all_entities"),
                "entity", ModColors.INTERACTIVE, () -> spawnAllEntities(state, selectedGroup, canvasViewport)));
        actions.add(ContextActions.submenu(
                CanvasContextMenuController.tr("ui.questsandstuff.context.debug"),
                "debug", ModColors.INTERACTIVE, debugActions));
    }

    private static void spawnAllEntities(TabletUiState state, String group, CanvasViewport canvasViewport) {
        List<String> eggs = EntityPreviewRenderer.searchableSpawnEggEntries("");
        if (eggs.isEmpty()) return;
        int size = Math.max(48, CanvasGeometry.gridSize(state) * 4);
        int gap = 8;
        int perRow = 8;
        int startCenterX = state.canvas.canvasImageLogicalX;
        int startCenterY = state.canvas.canvasImageLogicalY;
        List<String> existingIds = new ArrayList<>();
        for (CanvasImageLayer image : state.canvas.canvasImagesByGroup.getOrDefault(group, List.of())) {
            existingIds.add(image.id());
        }
        for (int i = 0; i < eggs.size(); i++) {
            String entityId = EntityPreviewRenderer.entityIdFromSpawnEgg(eggs.get(i));
            if (entityId.isBlank()) continue;
            String id = StableIdAllocator.nextId("ent", existingIds);
            existingIds.add(id);
            int col = i % perRow;
            int row = i / perRow;
            int x = startCenterX + col * (size + gap) - size / 2;
            int y = startCenterY + row * (size + gap) - size / 2;
            String asset = EntityPreviewRenderer.entityAsset(entityId);
            CanvasImageLayer image = new CanvasImageLayer(id, asset, x, y, size, size, 0, 205, 1);
            if (state.canvas.gridSnapLocked) {
                image = CanvasGridFitController.fittedImage(state, image);
            }
            CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, image.x(), image.y(), image.w(), image.h());
            image = new CanvasImageLayer(id, asset, clamped.x, clamped.y, image.w(), image.h(), 0, 205, 1);
            CanvasLayerMutations.putCanvasImage(state, group, image);
        }
        canvasViewport.refresh();
        QuestsAndStuffMod.debugLog("[QnS:UI] debug spawned {} entities group={}", eggs.size(), group);
    }

    private static List<String> canvasExclusiveChoiceIds(TabletUiState state, String group) {
        List<String> ids = new ArrayList<>();
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of())) {
            ids.add(ec.id());
        }
        return ids;
    }

    private static List<String> canvasTextIds(TabletUiState state, String group) {
        List<String> ids = new ArrayList<>();
        for (CanvasTextLayer text : state.canvas.canvasTextsByGroup.getOrDefault(group, List.of())) {
            ids.add(text.id());
        }
        return ids;
    }
}
