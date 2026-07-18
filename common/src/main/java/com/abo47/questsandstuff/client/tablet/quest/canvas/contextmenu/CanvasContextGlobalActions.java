package com.abo47.questsandstuff.client.tablet.quest.canvas.contextmenu;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSection;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuSections;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard.CanvasClipboardController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.viewport.CanvasCameraController;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;

final class CanvasContextGlobalActions {
    private CanvasContextGlobalActions() {
    }

    static void addGlobalActions(ContextMenuSections sections, CanvasViewport canvasViewport, TabletUiState state, Player player, String selectedChapter) {
        if (state.canvas.canvasZoom != 1.0f) {
            sections.add(ContextMenuSection.ARRANGE, ContextActionFactory.action(CanvasContextMenuController.tr("ui.questsandstuff.context.reset_zoom"), "reset_zoom", TabletColors.INTERACTIVE, () -> {
                CanvasCameraController.resetZoom(state, true);
                ContextMenuController.clearDeleteConfirm(state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=reset_zoom");
                canvasViewport.refresh();
            }));
        }
        if (!selectedChapter.isBlank() && CanvasClipboardController.hasClipboardContent(state)) {
            sections.add(ContextMenuSection.CLIPBOARD, ContextActionFactory.promoted(CanvasContextMenuController.tr("ui.questsandstuff.context.paste"), "paste", TabletColors.SUCCESS, () -> {
                CanvasContextMenuSupport.pasteClipboard(player, state);
                QuestsAndStuffMod.debugLog("[QnS:UI] canvas context action=paste target={}", state.contextMenu.contextMenuTarget);
                canvasViewport.refresh();
            }));
        }
    }
}
