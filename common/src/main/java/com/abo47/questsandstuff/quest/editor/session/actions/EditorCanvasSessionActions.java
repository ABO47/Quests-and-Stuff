package com.abo47.questsandstuff.quest.editor.session.actions;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.editor.ClipboardEditService;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.canvas.CanvasEditService;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

public final class EditorCanvasSessionActions {
    private final CanvasEditService canvasEdits;
    private final ClipboardEditService clipboardEdits;

    public EditorCanvasSessionActions(CanvasEditService canvasEdits, ClipboardEditService clipboardEdits) {
        this.canvasEdits = canvasEdits;
        this.clipboardEdits = clipboardEdits;
    }

    public void putCanvasExclusiveChoice(ServerPlayer player, String chapterName, CanvasExclusiveChoice ec) {
        canvasEdits.putCanvasExclusiveChoice(player, chapterName, ec);
    }

    public void putCanvasExclusiveChoices(ServerPlayer player, String chapterName, List<CanvasExclusiveChoice> ecs) {
        canvasEdits.putCanvasExclusiveChoices(player, chapterName, ecs);
    }

    public void removeCanvasExclusiveChoice(ServerPlayer player, String chapterName, String ecId) {
        canvasEdits.removeCanvasExclusiveChoice(player, chapterName, ecId);
    }

    public void ecConnectionHidden(ServerPlayer player, String chapterName, String sourceId, String targetId, boolean hidden) {
        canvasEdits.ecConnectionHidden(player, chapterName, sourceId, targetId, hidden);
    }

    public void putCanvasImage(ServerPlayer player, String chapterName, CanvasImageLayer image) {
        canvasEdits.putCanvasImage(player, chapterName, image);
    }

    public void removeCanvasImage(ServerPlayer player, String chapterName, String imageId) {
        canvasEdits.removeCanvasImage(player, chapterName, imageId);
    }

    public void putCanvasText(ServerPlayer player, String chapterName, CanvasTextLayer text) {
        canvasEdits.putCanvasText(player, chapterName, text);
    }

    public void removeCanvasText(ServerPlayer player, String chapterName, String textId) {
        canvasEdits.removeCanvasText(player, chapterName, textId);
    }

    public void setCanvasLayerOrder(ServerPlayer player, String chapterName, List<String> layerOrder) {
        canvasEdits.setCanvasLayerOrder(player, chapterName, layerOrder);
    }

    public void moveQuestsInChapter(ServerPlayer player, String chapterName, Map<String, int[]> positions) {
        canvasEdits.moveQuestsInChapter(player, chapterName, positions);
    }

    public void scaleQuestsInChapter(ServerPlayer player, String chapterName, Map<String, Float> scales) {
        canvasEdits.scaleQuestsInChapter(player, chapterName, scales);
    }

    public void copyQuestsToClipboard(ServerPlayer player, Set<String> questIds) {
        clipboardEdits.copyQuestsToClipboard(player, questIds);
    }

    public void copyQuestsToClipboard(ServerPlayer player, String chapterName, Set<String> questIds) {
        clipboardEdits.copyQuestsToClipboard(player, chapterName, questIds);
    }

    public void pasteClipboardInChapter(ServerPlayer player, String chapterName, int anchorX, int anchorY) {
        clipboardEdits.pasteClipboardInChapter(player, chapterName, anchorX, anchorY);
    }

    public void pasteBlueprintInChapter(ServerPlayer player, String chapterName, int anchorX, int anchorY, CanvasBlueprint blueprint) {
        clipboardEdits.pasteBlueprintInChapter(player, chapterName, anchorX, anchorY, blueprint);
    }
}
