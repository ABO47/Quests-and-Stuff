package com.abo47.questsandstuff.client.tablet.modal.actions;

import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.layout.TabletGridControls;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetState;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasOverlayController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.description.QuestDetailsDescriptionModel;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

public final class ColorPickerApplyActions {
    private ColorPickerApplyActions() {
    }

    public static int currentValue(TabletUiState state, String target) {
        return currentValue(state, ModalTargetParser.parse(target));
    }

    public static int currentValue(TabletUiState state, ModalTargetParser.Target target) {
        if (state.pickers.colorDraft != 0) {
            return state.pickers.colorDraft;
        }
        String[] connection = connectionColorTarget(target);
        if (connection != null) {
            return CanvasRenderer.connectionColor(state, connection[0], connection[1], connection[2]);
        }
        String connectionSelection = connectionSelectionColorTarget(target);
        if (connectionSelection != null) {
            var edges = CanvasOverlayController.selectedConnections(state, connectionSelection);
            if (!edges.isEmpty()) {
                var first = edges.get(0);
                return CanvasRenderer.connectionColor(state, connectionSelection, first.prerequisiteId(), first.questId());
            }
            return TabletColors.TEXT_SECONDARY;
        }
        String[] canvasText = canvasTextColorTarget(target);
        if (canvasText != null) {
            var text = CanvasLayerMutations.findCanvasText(state, canvasText[0], canvasText[1]);
            return text == null ? TabletColors.TEXT_PRIMARY : CanvasRenderer.activeTextColor(state, text);
        }
        if (target.isGridColor()) {
            return TabletGridControls.defaultGridColor(state);
        }
        if (target.isQuestDescText()) {
            if (target.hasAtLeast(3)) {
                QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestStateFacade.quest(target.questId()));
                var text = model.text(target.entryId());
                if (text != null) {
                    return CanvasRenderer.activeTextColor(state, text);
                }
            }
            return state.pickers.colorDraft == 0 ? TabletColors.TEXT_PRIMARY : state.pickers.colorDraft;
        }
        return ClientQuestStateFacade.chapterTextColor(target.raw());
    }

    public static void apply(Player player, TabletUiState state, String target, int color) {
        apply(player, state, ModalTargetParser.parse(target), color);
    }

    public static void apply(Player player, TabletUiState state, ModalTargetParser.Target target, int color) {
        String[] connection = connectionColorTarget(target);
        if (connection != null) {
            String chapter = connection[0];
            String sourceId = connection[1];
            String targetId = connection[2];
            boolean isEc = ConnectionRenderer.isEcId(state, chapter, sourceId)
                    || ConnectionRenderer.isEcId(state, chapter, targetId);
            if (isEc) {
                EditorCanvasCommandClient.runEcConnectionColorAction(player, state, sourceId, targetId, color);
            } else {
                ConnectionRenderer.setConnectionColor(state, chapter, sourceId, targetId, color);
                EditorCanvasCommandClient.runConnectionColorAction(player, targetId, sourceId, color);
            }
            state.pickers.colorPickerTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] connection color picked chapter={} source={} target={} color={}", chapter, sourceId, targetId, color);
            return;
        }
        String connectionSelection = connectionSelectionColorTarget(target);
        if (connectionSelection != null) {
            String chapter = connectionSelection;
            int applied = 0;
            for (var connectionRef : CanvasOverlayController.selectedConnections(state, chapter)) {
                String prereq = connectionRef.prerequisiteId();
                String quest = connectionRef.questId();
                boolean isEc = ConnectionRenderer.isEcId(state, chapter, prereq)
                        || ConnectionRenderer.isEcId(state, chapter, quest);
                if (isEc) {
                    String ecId = ConnectionRenderer.isEcId(state, chapter, prereq) ? prereq : quest;
                    String questId = ConnectionRenderer.isEcId(state, chapter, prereq) ? quest : prereq;
                    ConnectionRenderer.setEcConnectionColor(state, chapter, ecId, questId, color);
                    EditorCanvasCommandClient.runEcConnectionColorAction(player, state, ecId, questId, color);
                } else {
                    ConnectionRenderer.setConnectionColor(state, chapter, prereq, quest, color);
                    EditorCanvasCommandClient.runConnectionColorAction(player, quest, prereq, color);
                }
                applied++;
            }
            state.pickers.colorPickerTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] connection selection color picked chapter={} connections={} color={}", chapter, applied, color);
            return;
        }
        String[] canvasText = canvasTextColorTarget(target);
        if (canvasText != null) {
            CanvasLayerMutations.updateCanvasText(state, canvasText[0], canvasText[1], text -> CanvasRenderer.applyTextColorSelection(state, text, color));
            state.pickers.colorPickerTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas text color picked chapter={} id={} color={}", canvasText[0], canvasText[1], color);
            return;
        }
        if (target.isGridColor()) {
            TabletGridControls.applyGridColor(state, color);
            TabletUiFactory.persistUiState(state);
            state.pickers.colorPickerTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] grid color picked color={}", color);
            return;
        }
        if (target.isQuestDescText()) {
            QuestDetailsWindow.applyTextColor(player, state, target, color);
            state.pickers.colorPickerTarget = "";
            return;
        }
        TabletUiFactory.runChapterAction(player, state, "set_text_color", target.raw(), String.valueOf(color), 0);
    }

    private static String[] connectionColorTarget(ModalTargetParser.Target target) {
        if (!target.isConnection()) {
            return null;
        }
        if (!ModalTargetState.requireParts("color_connection", target, 4)
                || !ModalTargetState.requireNonBlankParts("color_connection", target, 1, 2, 3)) {
            return null;
        }
        return new String[]{target.part(1), target.part(2), target.part(3)};
    }

    private static String connectionSelectionColorTarget(ModalTargetParser.Target target) {
        if (!target.isConnectionSelection()) {
            return null;
        }
        if (!ModalTargetState.requireParts("color_connection_selection", target, 2)
                || !ModalTargetState.requireNonBlankParts("color_connection_selection", target, 1)) {
            return null;
        }
        return target.part(1);
    }

    private static String[] canvasTextColorTarget(ModalTargetParser.Target target) {
        if (!target.isCanvasText()) {
            return null;
        }
        if (!ModalTargetState.requireParts("color_canvas_text", target, 3)
                || !ModalTargetState.requireNonBlankParts("color_canvas_text", target, 1, 2)) {
            return null;
        }
        return new String[]{target.part(1), target.part(2)};
    }
}
