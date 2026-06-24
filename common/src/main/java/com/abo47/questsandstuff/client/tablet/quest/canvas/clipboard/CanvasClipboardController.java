package com.abo47.questsandstuff.client.tablet.quest.canvas.clipboard;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuState;

import com.abo47.questsandstuff.client.tablet.quest.canvas.selection.CanvasSelectionActions;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGeometry;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasGridFitController;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasViewport;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.CanvasPoint;
import com.abo47.questsandstuff.client.tablet.quest.canvas.model.QuestCardLayout;
import com.abo47.questsandstuff.client.tablet.quest.canvas.overlay.CanvasMiniNotificationController;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletStateQueries;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.util.StableIdAllocator;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CanvasClipboardController {
    private CanvasClipboardController() {
    }

    public static boolean canCopyContext(CanvasViewport canvasViewport, TabletUiState state) {
        return canCopy(canvasViewport, state, contextSelection(state));
    }

    public static boolean copyContextToClipboard(CanvasViewport canvasViewport, TabletUiState state) {
        boolean copied = copyToClipboard(canvasViewport, state, contextSelection(state), "context");
        if (copied) {
            CanvasMiniNotificationController.show(state, "ui.questsandstuff.canvas_notifications.copied", state.contextMenu.contextLastClickX, state.contextMenu.contextLastClickY);
        }
        return copied;
    }

    public static boolean copySelectionToClipboard(CanvasViewport canvasViewport, TabletUiState state) {
        boolean copied = copyToClipboard(canvasViewport, state, currentSelection(state), "shortcut");
        if (copied) {
            CanvasMiniNotificationController.showAtPointer(state, canvasViewport, "ui.questsandstuff.canvas_notifications.copied");
        }
        return copied;
    }

    public static boolean hasClipboardContent(TabletUiState state) {
        return state != null && state.clipboard.canvasClipboard.hasContent();
    }

    public static boolean pasteAtContext(Player player, TabletUiState state) {
        int anchorX = TabletUiFactory.snapToGrid(state, state.contextMenu.contextLogicalX);
        int anchorY = TabletUiFactory.snapToGrid(state, state.contextMenu.contextLogicalY);
        return pasteAt(player, state, anchorX, anchorY, "context");
    }

    public static boolean pasteNearSelectionOrViewportCenter(Player player, TabletUiState state, CanvasViewport canvasViewport) {
        CanvasPoint anchor = keyboardPasteAnchor(state, canvasViewport);
        return pasteAt(player, state, anchor.x, anchor.y, "shortcut");
    }

    private static boolean canCopy(CanvasViewport canvasViewport, TabletUiState state, ClipboardSelection selection) {
        if (canvasViewport == null || state == null || selection.isEmpty()) {
            return false;
        }
        Map<String, QuestCardLayout> byQuestId = canvasViewport.cardLookup();
        for (String questId : selection.questIds()) {
            if (byQuestId.containsKey(questId) || ClientQuestCache.containsQuest(questId)) {
                return true;
            }
        }
        String group = TabletStateQueries.selectedGroupName(state);
        for (CanvasImageLayer image : state.canvas.canvasImagesByGroup.getOrDefault(group, List.of())) {
            if (selection.imageIds().contains(image.id())) {
                return true;
            }
        }
        for (CanvasTextLayer text : state.canvas.canvasTextsByGroup.getOrDefault(group, List.of())) {
            if (selection.textIds().contains(text.id())) {
                return true;
            }
        }
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of())) {
            if (selection.ecIds().contains(ec.id())) {
                return true;
            }
        }
        return false;
    }

    private static ClipboardSelection contextSelection(TabletUiState state) {
        ClipboardSelection current = currentSelection(state);
        return switch (state.contextMenu.contextMenuTarget) {
            case SELECTION -> current;
            case QUEST -> {
                if (current.questIds().contains(state.contextMenu.contextQuestId) && !current.isEmpty()) {
                    yield current;
                }
                yield ClipboardSelection.ofQuest(state.contextMenu.contextQuestId);
            }
            case IMAGE -> {
                if (current.imageIds().contains(state.contextMenu.contextCanvasImageId) && !current.isEmpty()) {
                    yield current;
                }
                yield ClipboardSelection.ofImage(state.contextMenu.contextCanvasImageId);
            }
            case TEXT -> {
                if (current.textIds().contains(state.contextMenu.contextCanvasTextId) && !current.isEmpty()) {
                    yield current;
                }
                yield ClipboardSelection.ofText(state.contextMenu.contextCanvasTextId);
            }
            case EXCLUSIVE_CHOICE -> {
                if (current.ecIds().contains(state.contextMenu.contextCanvasExclusiveChoiceId) && !current.isEmpty()) {
                    yield current;
                }
                yield ClipboardSelection.ofExclusiveChoice(state.contextMenu.contextCanvasExclusiveChoiceId);
            }
            default -> ClipboardSelection.empty();
        };
    }

    private static ClipboardSelection currentSelection(TabletUiState state) {
        return new ClipboardSelection(
                new LinkedHashSet<>(state.canvas.canvasSelection.questIds()),
                CanvasSelectionActions.selectedImageIds(state),
                CanvasSelectionActions.selectedTextIds(state),
                CanvasSelectionActions.selectedEcIds(state)
        );
    }

    private static boolean copyToClipboard(CanvasViewport canvasViewport, TabletUiState state, ClipboardSelection selection, String source) {
        if (state == null || canvasViewport == null || selection.isEmpty()) {
            return false;
        }
        String group = TabletStateQueries.selectedGroupName(state);
        Set<String> copiedQuestIds = copiedQuestIds(canvasViewport, selection.questIds());
        List<CanvasImageLayer> copiedImages = copiedImages(state, group, selection.imageIds());
        List<CanvasTextLayer> copiedTexts = copiedTexts(state, group, selection.textIds());
        List<CanvasExclusiveChoice> copiedEcs = copiedExclusiveChoices(state, group, selection.ecIds());
        if (copiedQuestIds.isEmpty() && copiedImages.isEmpty() && copiedTexts.isEmpty() && copiedEcs.isEmpty()) {
            QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] copy skipped source={} group={} reason=no_canvas_selection", source, group);
            return false;
        }

        CanvasPoint origin = clipboardOrigin(state, canvasViewport, copiedQuestIds, copiedImages, copiedTexts, copiedEcs);
        state.clipboard.canvasClipboard.store(!copiedQuestIds.isEmpty(), copiedImages, copiedTexts, copiedEcs, origin.x, origin.y);
        ContextMenuState.clearDeleteConfirm(state);

        if (!copiedQuestIds.isEmpty()) {
            EditorCommandClient.runCanvasCopyAction(canvasViewport.player(), group, copiedQuestIds);
        }
        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] copy stored source={} group={} quests={} images={} texts={} ecs={} origin={},{}",
                source, group, copiedQuestIds.size(), copiedImages.size(), copiedTexts.size(), copiedEcs.size(), origin.x, origin.y);
        return true;
    }

    private static Set<String> copiedQuestIds(CanvasViewport canvasViewport, Set<String> ids) {
        Set<String> copiedIds = new LinkedHashSet<>();
        Map<String, QuestCardLayout> byQuestId = canvasViewport.cardLookup();
        for (String questId : ids) {
            String normalized = questId == null ? "" : questId.trim();
            if (normalized.isBlank()) {
                continue;
            }
            if (byQuestId.containsKey(normalized) || ClientQuestCache.containsQuest(normalized)) {
                copiedIds.add(normalized);
            }
        }
        return copiedIds;
    }

    private static List<CanvasImageLayer> copiedImages(TabletUiState state, String group, Set<String> imageIds) {
        if (imageIds.isEmpty()) {
            return List.of();
        }
        List<CanvasImageLayer> copied = new ArrayList<>();
        for (CanvasImageLayer image : state.canvas.canvasImagesByGroup.getOrDefault(group, List.of())) {
            if (imageIds.contains(image.id())) {
                copied.add(image);
            }
        }
        return copied;
    }

    private static List<CanvasTextLayer> copiedTexts(TabletUiState state, String group, Set<String> textIds) {
        if (textIds.isEmpty()) {
            return List.of();
        }
        List<CanvasTextLayer> copied = new ArrayList<>();
        for (CanvasTextLayer text : state.canvas.canvasTextsByGroup.getOrDefault(group, List.of())) {
            if (textIds.contains(text.id())) {
                copied.add(text);
            }
        }
        return copied;
    }

    private static List<CanvasExclusiveChoice> copiedExclusiveChoices(TabletUiState state, String group, Set<String> ecIds) {
        if (ecIds.isEmpty()) {
            return List.of();
        }
        List<CanvasExclusiveChoice> copied = new ArrayList<>();
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of())) {
            if (ecIds.contains(ec.id())) {
                copied.add(ec);
            }
        }
        return copied;
    }

    private static CanvasPoint clipboardOrigin(
            TabletUiState state,
            CanvasViewport canvasViewport,
            Set<String> questIds,
            List<CanvasImageLayer> images,
            List<CanvasTextLayer> texts,
            List<CanvasExclusiveChoice> ecs
    ) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (String questId : questIds) {
            QuestCardLayout card = canvasViewport.cardLookup().get(questId);
            if (card == null) {
                continue;
            }
            minX = Math.min(minX, card.logicalX());
            minY = Math.min(minY, card.logicalY());
        }
        if (minX == Integer.MAX_VALUE) {
            for (CanvasImageLayer image : images) {
                minX = Math.min(minX, image.x());
                minY = Math.min(minY, image.y());
            }
            for (CanvasTextLayer text : texts) {
                minX = Math.min(minX, text.x());
                minY = Math.min(minY, text.y());
            }
            for (CanvasExclusiveChoice ec : ecs) {
                minX = Math.min(minX, ec.x());
                minY = Math.min(minY, ec.y());
            }
        }
        if (minX == Integer.MAX_VALUE) {
            return new CanvasPoint(TabletUiFactory.snapToGrid(state, state.contextMenu.contextLogicalX), TabletUiFactory.snapToGrid(state, state.contextMenu.contextLogicalY));
        }
        return new CanvasPoint(minX, minY);
    }

    private static boolean pasteAt(Player player, TabletUiState state, int anchorX, int anchorY, String source) {
        String group = TabletStateQueries.selectedGroupName(state);
        if (group.isBlank() || !hasClipboardContent(state)) {
            return false;
        }

        state.canvas.canvasSelection.questIds().clear();
        state.canvas.canvasSelection.setPrimaryImageId("");
        state.canvas.canvasSelection.imageIds().clear();
        state.canvas.canvasSelection.setPrimaryTextId("");
        state.canvas.canvasSelection.textIds().clear();
        state.canvas.canvasSelection.setPrimaryEcId("");
        state.canvas.canvasSelection.ecIds().clear();
        state.clipboard.canvasClipboard.clearPendingPastedLayers();

        boolean pastedElements = pasteCanvasElements(state, group, anchorX, anchorY);
        if (state.clipboard.canvasClipboard.hasQuestClipboard()) {
            EditorCommandClient.runCanvasPasteClipboardAction(player, group, anchorX, anchorY);
        }

        QuestsAndStuffMod.debugLog("[QnS:UI:Clipboard] paste requested source={} group={} quests={} images={} texts={} anchor={},{}",
                source, group, state.clipboard.canvasClipboard.hasQuestClipboard(), state.clipboard.canvasClipboard.imageCount(), state.clipboard.canvasClipboard.textCount(), anchorX, anchorY);
        return state.clipboard.canvasClipboard.hasQuestClipboard() || pastedElements;
    }

    private static boolean pasteCanvasElements(TabletUiState state, String group, int anchorX, int anchorY) {
        boolean pasted = false;
        Set<String> existingImageIds = existingImageIds(state, group);
        Set<String> existingTextIds = existingTextIds(state, group);
        Set<String> existingEcIds = existingExclusiveChoiceIds(state, group);
        int index = 0;
        for (CanvasImageLayer image : state.clipboard.canvasClipboard.imageLayers()) {
            String id = uniqueLayerId("img", existingImageIds);
            int x = TabletUiFactory.snapToGrid(state, anchorX + image.x() - state.clipboard.canvasClipboard.originX());
            int y = TabletUiFactory.snapToGrid(state, anchorY + image.y() - state.clipboard.canvasClipboard.originY());
            CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, image.w(), image.h(), image.pivotX(), image.pivotY(), image.rotation());
            CanvasImageLayer duplicate = new CanvasImageLayer(id, image.asset(), clamped.x, clamped.y, image.w(), image.h(), image.rotation(), image.entityYaw(), image.entitySpinSpeed(), image.modelPitch(), image.pivotX(), image.pivotY());
            if (state.canvas.gridSnapLocked) {
                duplicate = CanvasGridFitController.fittedImage(state, duplicate);
            }
            CanvasLayerMutations.putCanvasImage(state, group, duplicate);
            state.canvas.canvasSelection.imageIds().add(id);
            state.canvas.canvasSelection.setPrimaryImageId(id);
            state.clipboard.canvasClipboard.recordPastedImage(id);
            pasted = true;
            index++;
        }
        for (CanvasTextLayer text : state.clipboard.canvasClipboard.textLayers()) {
            String id = uniqueLayerId("txt", existingTextIds);
            int x = TabletUiFactory.snapToGrid(state, anchorX + text.x() - state.clipboard.canvasClipboard.originX());
            int y = TabletUiFactory.snapToGrid(state, anchorY + text.y() - state.clipboard.canvasClipboard.originY());
            CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, text.w(), text.h(), text.w() / 2, text.h() / 2, text.rotation());
            CanvasTextLayer duplicate = new CanvasTextLayer(id, text.text(), clamped.x, clamped.y, text.w(), text.h(), text.rotation(), text.align(), text.style(), text.color(), text.fontSize(), text.spans());
            if (state.canvas.gridSnapLocked) {
                duplicate = CanvasGridFitController.fittedText(state, duplicate);
            }
            CanvasLayerMutations.putCanvasText(state, group, duplicate);
            state.canvas.canvasSelection.textIds().add(id);
            state.canvas.canvasSelection.setPrimaryTextId(id);
            state.clipboard.canvasClipboard.recordPastedText(id);
            pasted = true;
            index++;
        }
        for (CanvasExclusiveChoice ec : state.clipboard.canvasClipboard.exclusiveChoiceLayers()) {
            String id = uniqueLayerId("ec", existingEcIds);
            int x = TabletUiFactory.snapToGrid(state, anchorX + ec.x() - state.clipboard.canvasClipboard.originX());
            int y = TabletUiFactory.snapToGrid(state, anchorY + ec.y() - state.clipboard.canvasClipboard.originY());
            CanvasPoint clamped = CanvasGeometry.clampRotatedAnchorToCanvas(state, x, y, ec.w(), ec.h(), ec.pivotX(), ec.pivotY(), ec.rotation());
            CanvasExclusiveChoice duplicate = new CanvasExclusiveChoice(id, clamped.x, clamped.y, ec.w(), ec.h(), ec.rotation(), ec.connectionQuestIds(), ec.prerequisiteQuestIds(), ec.background(), ec.connectionColors(), ec.connectionModes(), ec.connectionTextures(), ec.connectionTextureSpacings(), ec.hiddenConnections());
            if (state.canvas.gridSnapLocked) {
                duplicate = CanvasGridFitController.fittedExclusiveChoice(state, duplicate);
            }
            CanvasLayerMutations.putCanvasExclusiveChoice(state, group, duplicate);
            state.canvas.canvasSelection.ecIds().add(id);
            state.canvas.canvasSelection.setPrimaryEcId(id);
            state.clipboard.canvasClipboard.recordPastedExclusiveChoice(id);
            pasted = true;
            index++;
        }
        return pasted || index > 0;
    }

    private static Set<String> existingImageIds(TabletUiState state, String group) {
        Set<String> ids = new LinkedHashSet<>();
        for (CanvasImageLayer image : state.canvas.canvasImagesByGroup.getOrDefault(group, List.of())) {
            ids.add(image.id());
        }
        return ids;
    }

    private static Set<String> existingTextIds(TabletUiState state, String group) {
        Set<String> ids = new LinkedHashSet<>();
        for (CanvasTextLayer text : state.canvas.canvasTextsByGroup.getOrDefault(group, List.of())) {
            ids.add(text.id());
        }
        return ids;
    }

    private static Set<String> existingExclusiveChoiceIds(TabletUiState state, String group) {
        Set<String> ids = new LinkedHashSet<>();
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of())) {
            ids.add(ec.id());
        }
        return ids;
    }

    private static String uniqueLayerId(String prefix, Set<String> existingIds) {
        String id = StableIdAllocator.nextId(prefix, existingIds);
        existingIds.add(id);
        return id;
    }

    private static CanvasPoint keyboardPasteAnchor(TabletUiState state, CanvasViewport canvasViewport) {
        int step = CanvasGeometry.gridSize(state);
        CanvasPoint selectionAnchor = selectedCanvasAnchor(state, canvasViewport, step);
        if (selectionAnchor != null) {
            return selectionAnchor;
        }
        CanvasPoint center = CanvasGeometry.anchorForScreenVisualCenter(
                state,
                Math.max(0, canvasViewport.getSizeWidth() / 2),
                Math.max(0, canvasViewport.getSizeHeight() / 2),
                1.0f
        );
        return new CanvasPoint(
                TabletUiFactory.snapToGrid(state, center.x),
                TabletUiFactory.snapToGrid(state, center.y)
        );
    }

    private static CanvasPoint selectedCanvasAnchor(TabletUiState state, CanvasViewport canvasViewport, int step) {
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        for (String questId : state.canvas.canvasSelection.questIds()) {
            QuestCardLayout card = canvasViewport.cardLookup().get(questId);
            if (card == null) {
                continue;
            }
            minX = Math.min(minX, card.logicalX());
            minY = Math.min(minY, card.logicalY());
        }
        String group = TabletStateQueries.selectedGroupName(state);
        Set<String> imageIds = CanvasSelectionActions.selectedImageIds(state);
        for (CanvasImageLayer image : state.canvas.canvasImagesByGroup.getOrDefault(group, List.of())) {
            if (imageIds.contains(image.id())) {
                minX = Math.min(minX, image.x());
                minY = Math.min(minY, image.y());
            }
        }
        Set<String> textIds = CanvasSelectionActions.selectedTextIds(state);
        for (CanvasTextLayer text : state.canvas.canvasTextsByGroup.getOrDefault(group, List.of())) {
            if (textIds.contains(text.id())) {
                minX = Math.min(minX, text.x());
                minY = Math.min(minY, text.y());
            }
        }
        Set<String> ecIds = CanvasSelectionActions.selectedEcIds(state);
        for (CanvasExclusiveChoice ec : state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(group, List.of())) {
            if (ecIds.contains(ec.id())) {
                minX = Math.min(minX, ec.x());
                minY = Math.min(minY, ec.y());
            }
        }
        if (minX == Integer.MAX_VALUE || minY == Integer.MAX_VALUE) {
            return null;
        }
        return new CanvasPoint(
                TabletUiFactory.snapToGrid(state, minX + step),
                TabletUiFactory.snapToGrid(state, minY + step)
        );
    }

    private record ClipboardSelection(Set<String> questIds, Set<String> imageIds, Set<String> textIds, Set<String> ecIds) {
        private static ClipboardSelection empty() {
            return new ClipboardSelection(Set.of(), Set.of(), Set.of(), Set.of());
        }

        private static ClipboardSelection ofQuest(String questId) {
            return new ClipboardSelection(single(questId), Set.of(), Set.of(), Set.of());
        }

        private static ClipboardSelection ofImage(String imageId) {
            return new ClipboardSelection(Set.of(), single(imageId), Set.of(), Set.of());
        }

        private static ClipboardSelection ofText(String textId) {
            return new ClipboardSelection(Set.of(), Set.of(), single(textId), Set.of());
        }

        private static ClipboardSelection ofExclusiveChoice(String ecId) {
            return new ClipboardSelection(Set.of(), Set.of(), Set.of(), single(ecId));
        }

        private boolean isEmpty() {
            return questIds.isEmpty() && imageIds.isEmpty() && textIds.isEmpty() && ecIds.isEmpty();
        }

        private static Set<String> single(String value) {
            String normalized = value == null ? "" : value.trim();
            return normalized.isBlank() ? Set.of() : Set.of(normalized);
        }
    }
}
