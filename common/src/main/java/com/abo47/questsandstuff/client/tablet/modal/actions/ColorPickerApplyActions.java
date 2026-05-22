package com.abo47.questsandstuff.client.tablet.modal.actions;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.overlay.CanvasOverlayController;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import net.minecraft.world.entity.player.Player;

public final class ColorPickerApplyActions {
    private ColorPickerApplyActions() {
    }

    public static int currentValue(TabletUiState state, String target) {
        if (state.colorDraft != 0) {
            return state.colorDraft;
        }
        String[] connection = connectionColorTarget(target);
        if (connection != null) {
            return CanvasRenderer.connectionColor(state, connection[0], connection[1], connection[2]);
        }
        String connectionSelection = connectionSelectionColorTarget(target);
        if (connectionSelection != null) {
            var edges = CanvasOverlayController.selectedConnectedEdges(state, connectionSelection);
            if (!edges.isEmpty()) {
                var first = edges.get(0);
                return CanvasRenderer.connectionColor(state, connectionSelection, first.prerequisiteId(), first.questId());
            }
            return ModColors.TEXT_SECONDARY;
        }
        String[] canvasText = canvasTextColorTarget(target);
        if (canvasText != null) {
            var text = CanvasRenderer.findCanvasText(state, canvasText[0], canvasText[1]);
            return text == null ? ModColors.TEXT_PRIMARY : text.color();
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (parsed.isQuestDescText()) {
            return state.colorDraft == 0 ? ModColors.TEXT_PRIMARY : state.colorDraft;
        }
        return ClientQuestCache.groupTextColor(target);
    }

    public static void apply(Player player, TabletUiState state, String target, int color) {
        String[] connection = connectionColorTarget(target);
        if (connection != null) {
            CanvasRenderer.setConnectionColor(state, connection[0], connection[1], connection[2], color);
            EditorCommandClient.runConnectionColorAction(player, connection[2], connection[1], color);
            state.colorPickerTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] connection color picked group={} source={} target={} color={}", connection[0], connection[1], connection[2], color);
            return;
        }
        String connectionSelection = connectionSelectionColorTarget(target);
        if (connectionSelection != null) {
            int applied = 0;
            for (var edge : CanvasOverlayController.selectedConnectedEdges(state, connectionSelection)) {
                CanvasRenderer.setConnectionColor(state, connectionSelection, edge.prerequisiteId(), edge.questId(), color);
                EditorCommandClient.runConnectionColorAction(player, edge.questId(), edge.prerequisiteId(), color);
                applied++;
            }
            state.colorPickerTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] connection selection color picked group={} edges={} color={}", connectionSelection, applied, color);
            return;
        }
        String[] canvasText = canvasTextColorTarget(target);
        if (canvasText != null) {
            CanvasRenderer.updateCanvasText(state, canvasText[0], canvasText[1], text -> CanvasRenderer.applyTextColorSelection(state, text, color));
            state.colorPickerTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas text color picked group={} id={} color={}", canvasText[0], canvasText[1], color);
            return;
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (parsed.isQuestDescText()) {
            QuestDetailsWindow.applyTextColor(player, state, target, color);
            state.colorPickerTarget = "";
            return;
        }
        TabletUiFactory.runGroupAction(player, state, "set_text_color", target, String.valueOf(color), 0);
    }

    private static String[] connectionColorTarget(String target) {
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.isConnection()) {
            return null;
        }
        if (!parsed.hasAtLeast(4) || parsed.part(1).isBlank() || parsed.part(2).isBlank() || parsed.part(3).isBlank()) {
            return null;
        }
        return new String[]{parsed.part(1), parsed.part(2), parsed.part(3)};
    }

    private static String connectionSelectionColorTarget(String target) {
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.isConnectionSelection()) {
            return null;
        }
        if (!parsed.hasAtLeast(2) || parsed.part(1).isBlank()) {
            return null;
        }
        return parsed.part(1);
    }

    private static String[] canvasTextColorTarget(String target) {
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.isCanvasText()) {
            return null;
        }
        if (!parsed.hasAtLeast(3) || parsed.part(1).isBlank() || parsed.part(2).isBlank()) {
            return null;
        }
        return new String[]{parsed.part(1), parsed.part(2)};
    }
}
