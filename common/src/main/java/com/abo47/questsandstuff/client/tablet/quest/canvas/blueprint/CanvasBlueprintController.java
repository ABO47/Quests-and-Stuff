package com.abo47.questsandstuff.client.tablet.quest.canvas.blueprint;

import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuController;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasMiniNotificationController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionSet;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.state.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CanvasBlueprintController {
    private CanvasBlueprintController() {
    }

    public static void openBlueprintLibrary(TabletUiState state) {
        ModalOpenActions.openBlueprintPicker(state, state.canvas.blueprintPlacement.asset());
    }

    public static boolean saveSelectionWithNotice(CanvasViewport canvasViewport, TabletUiState state, int noticeX, int noticeY) {
        String saved = saveSelection(canvasViewport, state);
        if (saved.isBlank()) {
            return false;
        }
        CanvasMiniNotificationController.show(state, "ui.questsandstuff.canvas_notifications.saved", noticeX, noticeY);
        return true;
    }

    public static String saveSelection(CanvasViewport canvasViewport, TabletUiState state) {
        CanvasBlueprint blueprint = buildSelection(canvasViewport, state);
        if (blueprint.isEmpty()) {
            return "";
        }
        String saved = CanvasBlueprintStore.save(blueprint, blueprint.name());
        if (!saved.isBlank()) {
            state.canvas.blueprintPlacement.rememberAsset(saved);
        }
        return saved;
    }

    public static void beginPlacement(TabletUiState state, String relativePath) {
        String path = relativePath == null ? "" : relativePath.trim();
        if (path.isBlank() || !CanvasBlueprintStore.isBlueprint(path)) {
            return;
        }
        CanvasBlueprint blueprint = CanvasBlueprintStore.read(path);
        if (blueprint.isEmpty()) {
            return;
        }
        state.canvas.blueprintPlacement.begin(path);
        ContextMenuController.close(state);
        state.pickers.assetContextOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] placement begin path={} entries={}", path, blueprint.contentCount());
    }

    public static boolean cancelPlacement(TabletUiState state) {
        if (state == null || !state.canvas.blueprintPlacement.active()) {
            return false;
        }
        QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] placement cancel path={}", state.canvas.blueprintPlacement.asset());
        state.canvas.blueprintPlacement.cancel();
        return true;
    }

    public static boolean placeAt(Player player, TabletUiState state, int localX, int localY) {
        if (state == null || !state.canvas.blueprintPlacement.active()) {
            return false;
        }
        String asset = state.canvas.blueprintPlacement.asset();
        CanvasBlueprint blueprint = CanvasBlueprintStore.read(asset);
        if (blueprint.isEmpty()) {
            state.canvas.blueprintPlacement.cancel();
            return false;
        }
        PlacementAnchor anchor = placementAnchor(state, blueprint, localX, localY);
        state.canvas.canvasSelection.questIds().clear();
        state.canvas.canvasSelection.setPrimaryImageId("");
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.textIds().clear();
        state.clipboard.canvasClipboard.clearPendingPastedLayers();
        EditorCanvasCommandClient.runCanvasPasteBlueprintAction(player, state, blueprint, anchor.x(), anchor.y());
        state.canvas.blueprintPlacement.finish();
        QuestsAndStuffMod.debugLog("[QnS:UI:Blueprint] placement commit path={} anchor={},{} entries={}",
                asset, anchor.x(), anchor.y(), blueprint.contentCount());
        return true;
    }

    public static WidgetGroup placementGhost(CanvasViewport canvasViewport, TabletUiState state) {
        if (state == null || !state.canvas.blueprintPlacement.active() || !state.canvas.blueprintPlacement.hasAsset()) {
            return null;
        }
        return CanvasBlueprintMiniRenderer.placementGhost(canvasViewport, state);
    }

    static PlacementAnchor placementAnchor(TabletUiState state, CanvasBlueprint blueprint, int localX, int localY) {
        CanvasBlueprintMiniRenderer.BlueprintBounds bounds = CanvasBlueprintMiniRenderer.bounds(blueprint);
        int logicalX = (int) Math.round(CanvasGeometry.screenToLogicalX(state, localX) - bounds.width() / 2.0);
        int logicalY = (int) Math.round(CanvasGeometry.screenToLogicalY(state, localY) - bounds.height() / 2.0);
        int snappedX = TabletUiFactory.snapToGrid(state, logicalX);
        int snappedY = TabletUiFactory.snapToGrid(state, logicalY);
        CanvasPoint clamped = CanvasGeometry.clampAnchorToCanvas(state, snappedX, snappedY, bounds.width(), bounds.height());
        return new PlacementAnchor(clamped.x, clamped.y);
    }

    private static CanvasBlueprint buildSelection(CanvasViewport canvasViewport, TabletUiState state) {
        if (canvasViewport == null || state == null) {
            return CanvasBlueprint.empty();
        }
        String chapter = TabletStateQueries.selectedChapterName(state);
        if (chapter.isBlank()) {
            return CanvasBlueprint.empty();
        }
        CanvasSelectionSet selection = CanvasSelectionSet.current(state);
        if (selection.size() == 0) {
            return CanvasBlueprint.empty();
        }
        List<CanvasBlueprint.QuestEntry> quests = selectedQuests(selection.questIds(), chapter);
        List<CanvasImageLayer> images = selectedImages(state, chapter, selection.imageIds());
        List<CanvasTextLayer> texts = selectedTexts(state, chapter, selection.textIds());
        List<CanvasBlueprint.ExclusiveChoiceEntry> ecs = selectedExclusiveChoices(state, chapter, selection.ecIds());
        if (quests.isEmpty() && images.isEmpty() && texts.isEmpty() && ecs.isEmpty()) {
            return CanvasBlueprint.empty();
        }
        CanvasPoint origin = origin(state, quests, images, texts, ecs);
        String name = preferredName(chapter, quests, images, texts);
        return new CanvasBlueprint(name, origin.x, origin.y, quests, images, texts, selectedLayerOrder(state, chapter, selection), ecs);
    }

    private static List<CanvasBlueprint.QuestEntry> selectedQuests(Set<String> questIds, String chapter) {
        if (questIds.isEmpty()) {
            return List.of();
        }
        List<CanvasBlueprint.QuestEntry> quests = new ArrayList<>();
        for (String questId : questIds) {
            CompoundTag tag = ClientQuestStateFacade.quest(questId);
            QuestDefinition definition = ClientQuestDefinitionSnapshots.fromClientTag(questId, tag);
            if (definition == null) {
                continue;
            }
            ChapterDef view = definition.display().chapters().get(chapter);
            if (view == null) {
                continue;
            }
            quests.add(new CanvasBlueprint.QuestEntry(questId, chapter, view.x(), view.y(), view.scale(), definition));
        }
        return quests;
    }

    private static List<CanvasImageLayer> selectedImages(TabletUiState state, String chapter, Set<String> imageIds) {
        if (imageIds.isEmpty()) {
            return List.of();
        }
        List<CanvasImageLayer> images = new ArrayList<>();
        for (CanvasImageLayer image : state.canvas.canvasImagesByChapter.getOrDefault(chapter, List.of())) {
            if (imageIds.contains(image.id())) {
                images.add(image);
            }
        }
        return images;
    }

    private static List<CanvasTextLayer> selectedTexts(TabletUiState state, String chapter, Set<String> textIds) {
        if (textIds.isEmpty()) {
            return List.of();
        }
        List<CanvasTextLayer> texts = new ArrayList<>();
        for (CanvasTextLayer text : state.canvas.canvasTextsByChapter.getOrDefault(chapter, List.of())) {
            if (textIds.contains(text.id())) {
                texts.add(text);
            }
        }
        return texts;
    }

    private static List<CanvasBlueprint.ExclusiveChoiceEntry> selectedExclusiveChoices(TabletUiState state, String chapter, Set<String> ecIds) {
        if (ecIds.isEmpty()) {
            return List.of();
        }
        List<CanvasBlueprint.ExclusiveChoiceEntry> entries = new ArrayList<>();
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(chapter, List.of())) {
            if (ecIds.contains(ec.id())) {
                entries.add(new CanvasBlueprint.ExclusiveChoiceEntry(
                        ec.id(), chapter, ec.x(), ec.y(), ec.w(), ec.h(), ec.rotation(),
                        ec.background(), ec.connectionQuestIds(), Set.copyOf(ec.prerequisiteQuestIds()),
                        ec.connectionColors(), ec.connectionModes(), ec.connectionTextures(),
                        ec.connectionTextureSpacings(), ec.hiddenConnections()));
            }
        }
        return entries;
    }

    private static CanvasPoint origin(TabletUiState state, List<CanvasBlueprint.QuestEntry> quests, List<CanvasImageLayer> images, List<CanvasTextLayer> texts, List<CanvasBlueprint.ExclusiveChoiceEntry> ecs) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (CanvasBlueprint.QuestEntry quest : quests) {
            minX = Math.min(minX, quest.sourceX());
            minY = Math.min(minY, quest.sourceY());
        }
        for (CanvasImageLayer image : images) {
            minX = Math.min(minX, image.x());
            minY = Math.min(minY, image.y());
        }
        for (CanvasTextLayer text : texts) {
            minX = Math.min(minX, text.x());
            minY = Math.min(minY, text.y());
        }
        for (CanvasBlueprint.ExclusiveChoiceEntry ec : ecs) {
            minX = Math.min(minX, ec.sourceX());
            minY = Math.min(minY, ec.sourceY());
        }
        if (minX == Integer.MAX_VALUE) {
            return new CanvasPoint(TabletUiFactory.snapToGrid(state, state.contextMenu.contextLogicalX), TabletUiFactory.snapToGrid(state, state.contextMenu.contextLogicalY));
        }
        return new CanvasPoint(minX, minY);
    }

    private static List<String> selectedLayerOrder(TabletUiState state, String chapter, CanvasSelectionSet selection) {
        Set<String> selected = new LinkedHashSet<>(selection.layerKeys());
        List<String> order = new ArrayList<>();
        for (String key : state.canvas.canvasLayerOrderByChapter.getOrDefault(chapter, List.of())) {
            if (selected.remove(key)) {
                order.add(key);
            }
        }
        order.addAll(selected);
        return order;
    }

    private static String preferredName(String chapter, List<CanvasBlueprint.QuestEntry> quests, List<CanvasImageLayer> images, List<CanvasTextLayer> texts) {
        for (CanvasBlueprint.QuestEntry quest : quests) {
            String title = quest.definition().display().title();
            if (title != null && !title.isBlank()) {
                return title;
            }
        }
        int count = quests.size() + images.size() + texts.size();
        return (chapter == null || chapter.isBlank() ? "blueprint" : chapter) + "_" + count;
    }

    public record PlacementAnchor(int x, int y) {
    }
}
