package com.abo47.questsandstuff.client.tablet.modal.actions;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.canvas.CanvasMouseMode;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
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
        String soundTarget = state.modalQuestCompletionSoundTarget == null ? "" : state.modalQuestCompletionSoundTarget.trim();
        if (!soundTarget.isBlank()) {
            EditorCommandClient.setQuestCompletionSound(player, soundTarget, background);
            state.modalQuestCompletionSoundTarget = "";
            state.assetBrowseDir = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest change completion sound picked quest={} asset={}", soundTarget, background);
            return;
        }
        String questBackgroundTarget = state.modalQuestBackgroundTarget == null ? "" : state.modalQuestBackgroundTarget.trim();
        if (!questBackgroundTarget.isBlank()) {
            EditorCommandClient.setQuestBackground(player, questBackgroundTarget, background, state.modalQuestBackgroundGrayscale);
            state.modalQuestBackgroundTarget = "";
            QuestsAndStuffMod.debugLog("[QnS:UI] quest background picked quest={} asset={} grayscale={}", questBackgroundTarget, background, state.modalQuestBackgroundGrayscale);
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
        int x = TabletUiFactory.snapToGrid(state, state.canvasImageLogicalX - imageW / 2);
        int y = TabletUiFactory.snapToGrid(state, state.canvasImageLogicalY - imageH / 2);
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, x, y, imageW, imageH);
        CanvasImageLayer image = new CanvasImageLayer(id, asset, clamped.x, clamped.y, imageW, imageH, 0);
        CanvasRenderer.putCanvasImage(state, group, image);
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
}
