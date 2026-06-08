package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.text.TextEditSession;
import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerIntegrations;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.layout.TabletGridControls;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.util.StableIdAllocator;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.addQuestAt;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.runGroupAction;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.snapToGrid;

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
    }

    private static List<String> canvasTextIds(TabletUiState state, String group) {
        List<String> ids = new ArrayList<>();
        for (CanvasTextLayer text : state.canvas.canvasTextsByGroup.getOrDefault(group, List.of())) {
            ids.add(text.id());
        }
        return ids;
    }
}
