package com.abo47.questsandstuff.client.canvas.contextmenu;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuTarget;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
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
        if (state.contextMenuTarget != ContextMenuTarget.CANVAS || selectedGroup.isBlank()) {
            return;
        }
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.quick_add_quest"), "add", ModColors.SUCCESS, () -> {
            int logicalX = snapToGrid(state, state.contextLogicalX);
            int logicalY = snapToGrid(state, state.contextLogicalY);
            CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(
                    state,
                    logicalX,
                    logicalY,
                    CanvasGeometry.slotLogicalWidth(state, 1.0f),
                    CanvasGeometry.slotLogicalHeight(state, 1.0f)
            );
            logicalX = clamped.x;
            logicalY = clamped.y;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add logicalX={} logicalY={} target={}", logicalX, logicalY, state.contextMenuTarget);
            addQuestAt(player, state, logicalX, logicalY, "");
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.change_canvas_bg"), "background", ModColors.INTERACTIVE, () -> {
            ModalOpenActions.openCanvasBackgroundPicker(state, selectedGroup, ClientQuestCache.groupCanvasBackground(selectedGroup));
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=change_canvas_bg group={}", selectedGroup);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.add_image"), "image", ModColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasImagePicker(state, selectedGroup, state.contextPointerLogicalX, state.contextPointerLogicalY);
            state.contextMenuOpen = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_image group={} logical={},{}", selectedGroup, state.contextLogicalX, state.contextLogicalY);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.add_entity"), "entity", ModColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasEntityPicker(state, ModalTargets.canvasEntityNew(selectedGroup), state.contextPointerLogicalX, state.contextPointerLogicalY);
            state.contextMenuOpen = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_entity group={} logical={},{}", selectedGroup, state.contextLogicalX, state.contextLogicalY);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.add_item"), "icon", ModColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasItemPicker(state, ModalTargets.canvasItemNew(selectedGroup), state.contextPointerLogicalX, state.contextPointerLogicalY);
            state.contextMenuOpen = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_item_model group={} logical={},{}", selectedGroup, state.contextLogicalX, state.contextLogicalY);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.add_block"), "box", ModColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasBlockPicker(state, ModalTargets.canvasBlockNew(selectedGroup), state.contextPointerLogicalX, state.contextPointerLogicalY);
            state.contextMenuOpen = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_block_model group={} logical={},{}", selectedGroup, state.contextLogicalX, state.contextLogicalY);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.add_text_box"), "text", ModColors.SUCCESS, () -> {
            String id = StableIdAllocator.nextId("txt", canvasTextIds(state, selectedGroup));
            int textW = 96;
            int textH = 32;
            int x = snapToGrid(state, state.contextPointerLogicalX - textW / 2);
            int y = snapToGrid(state, state.contextPointerLogicalY - textH / 2);
            CanvasTextLayer text = new CanvasTextLayer(id, "Text", x, y, textW, textH, 0, "left", "normal", ModColors.TEXT_PRIMARY);
            if (state.gridSnapLocked) {
                text = CanvasGridFitController.fittedText(state, text);
            }
            CanvasRenderer.putCanvasText(state, selectedGroup, text);
            state.selectedCanvasTextId = id;
            state.selectedCanvasImageId = "";
            state.selectedQuestIds.clear();
            state.canvasTextMenuOpen = true;
            state.canvasTextMenuTarget = id;
            state.canvasTextEditOpen = true;
            state.canvasTextEditTarget = id;
            state.canvasTextEditDraft = text.text();
            state.canvasTextEditCursor = state.canvasTextEditDraft.length();
            state.canvasTextSelectionAnchor = state.canvasTextEditCursor;
            state.selectingCanvasTextRange = false;
            state.contextMenuOpen = false;
            canvasViewport.setFocus(true);
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_text_box group={} id={} logical={},{}", selectedGroup, id, x, y);
            canvasViewport.refresh();
        }));
        actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.add_recipe_card"), "recipe", ModColors.SUCCESS, () -> {
            ModalOpenActions.openCanvasRecipePicker(state, ModalTargets.canvasRecipeNew(selectedGroup), state.contextPointerLogicalX, state.contextPointerLogicalY);
            state.contextMenuOpen = false;
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=add_recipe_card group={} logical={},{}", selectedGroup, state.contextLogicalX, state.contextLogicalY);
            canvasViewport.refresh();
        }));
        if (!ClientQuestCache.groupCanvasBackground(selectedGroup).isBlank()
                && !"default".equals(ClientQuestCache.groupCanvasBackground(selectedGroup))) {
            actions.add(new ContextAction(CanvasContextMenuController.tr("ui.questsandstuff.context.remove_canvas_bg"), "delete", ModColors.WARNING, () -> {
                runGroupAction(player, state, "set_canvas_background", selectedGroup, "default", 0);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=remove_canvas_bg group={}", selectedGroup);
                canvasViewport.refresh();
            }));
        }
    }

    private static List<String> canvasTextIds(TabletUiState state, String group) {
        List<String> ids = new ArrayList<>();
        for (CanvasTextLayer text : state.canvasTextsByGroup.getOrDefault(group, List.of())) {
            ids.add(text.id());
        }
        return ids;
    }
}
