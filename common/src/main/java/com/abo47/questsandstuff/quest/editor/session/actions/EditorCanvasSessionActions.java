package com.abo47.questsandstuff.quest.editor.session.actions;

import com.abo47.questsandstuff.quest.editor.ClipboardEditService;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.canvas.CanvasEditService;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class EditorCanvasSessionActions {
    private final CanvasEditService canvasEdits;
    private final ClipboardEditService clipboardEdits;

    public EditorCanvasSessionActions(CanvasEditService canvasEdits, ClipboardEditService clipboardEdits) {
        this.canvasEdits = canvasEdits;
        this.clipboardEdits = clipboardEdits;
    }

    public void putCanvasExclusiveChoice(ServerPlayer player, String groupName, CanvasExclusiveChoice ec) {
        canvasEdits.putCanvasExclusiveChoice(player, groupName, ec);
    }

    public void removeCanvasExclusiveChoice(ServerPlayer player, String groupName, String ecId) {
        canvasEdits.removeCanvasExclusiveChoice(player, groupName, ecId);
    }

    public void putCanvasImage(ServerPlayer player, String groupName, CanvasImageLayer image) {
        canvasEdits.putCanvasImage(player, groupName, image);
    }

    public void removeCanvasImage(ServerPlayer player, String groupName, String imageId) {
        canvasEdits.removeCanvasImage(player, groupName, imageId);
    }

    public void putCanvasText(ServerPlayer player, String groupName, CanvasTextLayer text) {
        canvasEdits.putCanvasText(player, groupName, text);
    }

    public void removeCanvasText(ServerPlayer player, String groupName, String textId) {
        canvasEdits.removeCanvasText(player, groupName, textId);
    }

    public void setCanvasLayerOrder(ServerPlayer player, String groupName, List<String> layerOrder) {
        canvasEdits.setCanvasLayerOrder(player, groupName, layerOrder);
    }

    public void moveQuestsInGroup(ServerPlayer player, String groupName, Map<String, int[]> positions) {
        canvasEdits.moveQuestsInGroup(player, groupName, positions);
    }

    public void scaleQuestsInGroup(ServerPlayer player, String groupName, Map<String, Float> scales) {
        canvasEdits.scaleQuestsInGroup(player, groupName, scales);
    }

    public void copyQuestsToClipboard(ServerPlayer player, Set<String> questIds) {
        clipboardEdits.copyQuestsToClipboard(player, questIds);
    }

    public void copyQuestsToClipboard(ServerPlayer player, String groupName, Set<String> questIds) {
        clipboardEdits.copyQuestsToClipboard(player, groupName, questIds);
    }

    public void pasteClipboardInGroup(ServerPlayer player, String groupName, int anchorX, int anchorY) {
        clipboardEdits.pasteClipboardInGroup(player, groupName, anchorX, anchorY);
    }

    public void pasteBlueprintInGroup(ServerPlayer player, String groupName, int anchorX, int anchorY, CanvasBlueprint blueprint) {
        clipboardEdits.pasteBlueprintInGroup(player, groupName, anchorX, anchorY, blueprint);
    }
}
