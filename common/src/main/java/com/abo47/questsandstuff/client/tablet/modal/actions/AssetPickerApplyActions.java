package com.abo47.questsandstuff.client.tablet.modal.actions;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint.CanvasBlueprintController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasMouseMode;
import com.abo47.questsandstuff.client.quest.hud.QuestHudLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.util.StableIdAllocator;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class AssetPickerApplyActions {
    private AssetPickerApplyActions() {
    }

    public static void run(Player player, TabletUiState state, String background) {
        String blueprintTarget = state.modalBlueprintTarget == null ? "" : state.modalBlueprintTarget.trim();
        if (!blueprintTarget.isBlank()) {
            CanvasBlueprintController.beginPlacement(state, background);
            state.modalBlueprintTarget = "";
            state.assetBrowseDir = "";
            QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] picked blueprint target={} asset={}", blueprintTarget, background);
            return;
        }
        String soundTarget = state.modalQuestCompletionSoundTarget == null ? "" : state.modalQuestCompletionSoundTarget.trim();
        if (!state.modalQuestCompletionSoundTargets.isEmpty()) {
            int count = state.modalQuestCompletionSoundTargets.size();
            EditorCommandClient.setQuestCompletionSound(player, state.modalQuestCompletionSoundTargets, background);
            state.modalQuestCompletionSoundTargets.clear();
            state.assetBrowseDir = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest batch completion sound picked quests={} asset={}", count, background);
            return;
        }
        if (!soundTarget.isBlank()) {
            EditorCommandClient.setQuestCompletionSound(player, soundTarget, background);
            state.modalQuestCompletionSoundTarget = "";
            state.assetBrowseDir = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest change completion sound picked quest={} asset={}", soundTarget, background);
            return;
        }
        String questBackgroundTarget = state.modalQuestBackgroundTarget == null ? "" : state.modalQuestBackgroundTarget.trim();
        if (!state.modalQuestBackgroundTargets.isEmpty()) {
            EditorCommandClient.setQuestBackground(player, state.modalQuestBackgroundTargets, background, state.modalQuestBackgroundGrayscale);
            int count = state.modalQuestBackgroundTargets.size();
            state.modalQuestBackgroundTargets.clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] quest batch background picked quests={} asset={} grayscale={}", count, background, state.modalQuestBackgroundGrayscale);
            return;
        }
        if (!questBackgroundTarget.isBlank()) {
            EditorCommandClient.setQuestBackground(player, questBackgroundTarget, background, state.modalQuestBackgroundGrayscale);
            state.modalQuestBackgroundTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest background picked quest={} asset={} grayscale={}", questBackgroundTarget, background, state.modalQuestBackgroundGrayscale);
            return;
        }
        String completionHudTarget = state.modalQuestCompletionHudBackgroundTarget == null ? "" : state.modalQuestCompletionHudBackgroundTarget.trim();
        if (!state.modalQuestCompletionHudBackgroundTargets.isEmpty()) {
            EditorCommandClient.setQuestCompletionHudBackground(player, state.modalQuestCompletionHudBackgroundTargets, background);
            int count = state.modalQuestCompletionHudBackgroundTargets.size();
            state.modalQuestCompletionHudBackgroundTargets.clear();
            QuestsAndStuffMod.debugLog("[QnS:UI] quest batch completion hud background picked quests={} asset={}", count, background);
            return;
        }
        if (!completionHudTarget.isBlank()) {
            EditorCommandClient.setQuestCompletionHudBackground(player, completionHudTarget, background);
            state.modalQuestCompletionHudBackgroundTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest completion hud background picked quest={} asset={}", completionHudTarget, background);
            return;
        }
        String hudTarget = state.modalHudBackgroundTarget == null ? "" : state.modalHudBackgroundTarget.trim();
        QuestHudLayout.Element hudElement = hudElement(hudTarget);
        if (hudElement != null) {
            QuestHudLayout.setBackground(hudElement, background);
            state.modalHudBackgroundTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] hud background picked target={} asset={}", hudTarget, background);
            return;
        }
        String detailsTarget = state.questDetailsAssetPickTarget == null ? "" : state.questDetailsAssetPickTarget.trim();
        if (!detailsTarget.isBlank()) {
            QuestDetailsWindow.applyAssetPick(player, state, background);
            return;
        }
        String imageTarget = state.modalCanvasImageTarget == null ? "" : state.modalCanvasImageTarget.trim();
        if (!imageTarget.isBlank()) {
            addCanvasImage(state, imageTarget, background);
            return;
        }
        String canvasTarget = state.modalCanvasBackgroundTarget == null ? "" : state.modalCanvasBackgroundTarget.trim();
        if (!canvasTarget.isBlank()) {
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas background picked group={} background={}", canvasTarget, background);
            TabletUiFactory.runGroupAction(player, state, "set_canvas_background", canvasTarget, background, 0);
            return;
        }
        TabletUiFactory.runGroupAction(player, state, "set_background", state.modalChapterTarget, background, 0);
    }

    private static void addCanvasImage(TabletUiState state, String group, String asset) {
        String id = StableIdAllocator.nextId("img", canvasImageIds(state, group));
        int[] imageSize = canvasImageSpawnSize(state, asset);
        int imageW = imageSize[0];
        int imageH = imageSize[1];
        int x = state.canvasImageLogicalX - imageW / 2;
        int y = state.canvasImageLogicalY - imageH / 2;
        if (!state.gridSnapLocked) {
            x = TabletUiFactory.snapToGrid(state, x);
            y = TabletUiFactory.snapToGrid(state, y);
        }
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, x, y, imageW, imageH);
        CanvasImageLayer image = new CanvasImageLayer(id, asset, clamped.x, clamped.y, imageW, imageH, 0);
        if (state.gridSnapLocked) {
            image = CanvasGridFitController.fittedImage(state, image);
        }
        CanvasLayerMutations.putCanvasImage(state, group, image);
        state.selectedCanvasImageId = id;
        state.selectedQuestIds.clear();
        state.draggingCanvasImage = false;
        state.resizingCanvasImage = false;
        state.rotatingCanvasImage = false;
        state.mouseMode = CanvasMouseMode.SELECT_MOVE;
        state.contextMenuOpen = false;
        state.contextDeleteConfirmKey = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] canvas image added group={} id={} asset={} pos={},{} size={}x{}", group, id, asset, clamped.x, clamped.y, imageW, imageH);
    }

    private static List<String> canvasImageIds(TabletUiState state, String group) {
        List<String> ids = new ArrayList<>();
        for (CanvasImageLayer image : state.canvasImagesByGroup.getOrDefault(group, List.of())) {
            ids.add(image.id());
        }
        return ids;
    }

    private static int[] canvasImageSpawnSize(TabletUiState state, String asset) {
        var dimensions = TabletUiFactory.assetDimensions(asset);
        if (dimensions == null || dimensions.width() <= 0 || dimensions.height() <= 0) {
            return new int[]{96, 64};
        }
        int maxSize = Math.max(CanvasGeometry.gridSize(state), CanvasGeometry.gridSize(state) * 6);
        double scale = Math.min(1.0, maxSize / (double) Math.max(dimensions.width(), dimensions.height()));
        int width = Math.max(8, (int) Math.round(dimensions.width() * scale));
        int height = Math.max(8, (int) Math.round(dimensions.height() * scale));
        return new int[]{width, height};
    }

    private static QuestHudLayout.Element hudElement(String target) {
        if ("completion".equalsIgnoreCase(target)) {
            return QuestHudLayout.Element.COMPLETION;
        }
        if ("pinned".equalsIgnoreCase(target)) {
            return QuestHudLayout.Element.PINNED;
        }
        return null;
    }
}
