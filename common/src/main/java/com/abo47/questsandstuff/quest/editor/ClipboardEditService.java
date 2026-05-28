package com.abo47.questsandstuff.quest.editor;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardPasteRequest;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardPasteResult;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardSnapshot;
import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.util.StableIdAllocator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.deepCopyDefinition;
import static com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDebugFormatter.clipboardSourceSummary;
import static com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDebugFormatter.sortedConnectionColors;
import static com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDebugFormatter.sortedStringMap;
import static com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDebugFormatter.sortedStrings;
import static com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDefinitionCopier.duplicateDefinition;
import static com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDefinitionCopier.normalizeScale;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withSettings;

public final class ClipboardEditService {
    private final EditorSessionService owner;

    public ClipboardEditService(EditorSessionService owner) {
        this.owner = owner;
    }

    public void copyQuestsToClipboard(ServerPlayer player, Set<String> questIds) {
        copyQuestsToClipboard(player, player == null ? "" : owner.session(player).currentGroup, questIds);
    }

    public void copyQuestsToClipboard(ServerPlayer player, String groupName, Set<String> questIds) {
        if (player == null || questIds == null || questIds.isEmpty()) {
            return;
        }
        String group = EditorSessionService.normalizeGroup(groupName);
        String playerName = player.getGameProfile().getName();
        owner.clipboardDebug("COPY request player=" + playerName + " group=" + group + " requested=" + sortedStrings(questIds));
        EditorSessionService.EditorSession session = owner.session(player);
        List<ClipboardSnapshot.Entry> entries = new ArrayList<>();
        for (String questId : questIds) {
            String sourceId = EditorSessionService.normalizeQuestId(questId);
            if (sourceId.isBlank()) {
                continue;
            }
            QuestDefinition definition = owner.definitionStore().quests().get(sourceId);
            if (definition != null) {
                QuestDefinition snapshot = deepCopyDefinition(definition);
                ChapterDefinition view = group.isBlank() ? null : definition.display().groups().get(group);
                if (view == null) {
                    view = firstVisibleGroupView(definition);
                }
                String sourceGroup = group.isBlank() || !definition.display().groups().containsKey(group) ? firstVisibleGroup(definition) : group;
                entries.add(new ClipboardSnapshot.Entry(sourceId, sourceGroup, view == null ? 0 : view.x(), view == null ? 0 : view.y(), view == null ? 1.0f : view.scale(), snapshot));
                owner.clipboardDebug("COPY entry source=" + sourceId
                        + " sourceGroup=" + sourceGroup
                        + " pos=" + (view == null ? "<none>" : view.x() + "," + view.y())
                        + " scale=" + (view == null ? 1.0f : view.scale())
                        + " prerequisites=" + sortedStrings(definition.prerequisites())
                        + " connectionColors=" + sortedConnectionColors(definition.connectionColors())
                        + " connectionModes=" + sortedStringMap(definition.connectionModes())
                        + " hiddenConnections=" + sortedStrings(definition.hiddenConnections())
                        + " tasks=" + sortedStrings(definition.tasks().keySet())
                        + " rewards=" + sortedStrings(definition.rewards().keySet()));
            } else {
                owner.clipboardDebug("COPY missing source=" + sourceId);
            }
        }
        ClipboardSnapshot snapshot = new ClipboardSnapshot(entries.stream()
                .sorted(Comparator
                        .comparingInt(ClipboardSnapshot.Entry::sourceY)
                        .thenComparingInt(ClipboardSnapshot.Entry::sourceX)
                        .thenComparing(ClipboardSnapshot.Entry::sourceId))
                .toList());
        session.clipboardSnapshot = snapshot;
        owner.clipboardDebug("COPY stored player=" + playerName
                + " requested=" + questIds.size()
                + " sessionEntries=" + snapshot.entries().size()
                + " sourceIds=" + sortedStrings(snapshot.sourceIds())
                + " externalPrerequisiteEdges=" + snapshot.countExternalPrerequisiteEdges());
        QuestsAndStuffMod.debugLog("[QnS:Editor] copy_many player={} group={} requested={} stored={} external_prerequisite_edges={}", player.getGameProfile().getName(), group, questIds.size(), snapshot.entries().size(), snapshot.countExternalPrerequisiteEdges());
    }

    public void pasteClipboardInGroup(ServerPlayer player, String groupName, int anchorX, int anchorY) {
        ClipboardPasteRequest request = new ClipboardPasteRequest(EditorSessionService.normalizeGroup(groupName), anchorX, anchorY);
        if (player == null || request.targetChapter().isBlank()) {
            return;
        }
        EditorSessionService.EditorSession session = owner.session(player);
        String playerName = player.getGameProfile().getName();
        owner.clipboardDebug("PASTE request player=" + playerName + " group=" + request.targetChapter() + " anchor=" + request.anchorX() + "," + request.anchorY());
        ClipboardSnapshot snapshot = session.clipboardSnapshot;
        List<ClipboardSnapshot.Entry> entries = snapshot.sortedEntries();
        if (entries.isEmpty()) {
            owner.clipboardDebug("PASTE skipped empty session clipboard group=" + request.targetChapter());
            QuestsAndStuffMod.debugLog("[QnS:Editor] paste_clipboard skipped empty group={}", request.targetChapter());
            return;
        }
        owner.ensureGroupExists(request.targetChapter());
        int droppedExternalPrerequisiteEdges = snapshot.countExternalPrerequisiteEdges();
        owner.clipboardDebug("PASTE read entries=" + entries.size() + " sources=" + clipboardSourceSummary(entries) + " droppedExternalPrerequisiteEdges=" + droppedExternalPrerequisiteEdges);

        Map<String, String> allocatedIds = new LinkedHashMap<>();
        Set<String> reservedIds = new HashSet<>(owner.definitionStore().quests().keySet());
        reservedIds.addAll(snapshot.sourceIds());
        for (ClipboardSnapshot.Entry entry : entries) {
            if (entry == null || entry.sourceId().isBlank() || entry.definition() == null) {
                continue;
            }
            String newId = owner.nextQuestId(request.targetChapter(), reservedIds);
            allocatedIds.put(entry.sourceId(), newId);
            reservedIds.add(newId);
        }
        if (allocatedIds.isEmpty()) {
            owner.clipboardDebug("PASTE aborted no allocated IDs entries=" + entries.size());
            return;
        }
        owner.clipboardDebug("PASTE reservedSources=" + sortedStrings(snapshot.sourceIds()) + " allocated=" + allocatedIds);

        int minX = snapshot.minSourceX();
        int minY = snapshot.minSourceY();
        owner.captureUndo(session);
        List<QuestDefinition> pastedSnapshots = new ArrayList<>();
        for (ClipboardSnapshot.Entry entry : entries) {
            String newId = allocatedIds.get(entry.sourceId());
            if (newId == null || newId.isBlank()) {
                continue;
            }
            int x = request.anchorX() + (entry.sourceX() - minX);
            int y = request.anchorY() + (entry.sourceY() - minY);
            float scale = normalizeScale(entry.scale(), 1.0f);
            QuestDefinition duplicate = duplicateDefinition(entry.definition(), newId, request.targetChapter(), x, y, scale, allocatedIds);
            if (owner.definitionStore().groupLockUntilUnlocked(request.targetChapter())) {
                duplicate = withSettings(duplicate, withHiddenMode(duplicate.settings(), QuestVisibilityMode.LOCKED));
            }
            owner.clipboardDebug("PASTE stage source=" + entry.sourceId()
                    + " -> " + newId
                    + " pos=" + x + "," + y
                    + " prerequisitesBefore=" + sortedStrings(entry.definition().prerequisites())
                    + " prerequisitesMapped=" + sortedStrings(duplicate.prerequisites())
                    + " colorsBefore=" + sortedConnectionColors(entry.definition().connectionColors())
                    + " colorsMapped=" + sortedConnectionColors(duplicate.connectionColors())
                    + " modesBefore=" + sortedStringMap(entry.definition().connectionModes())
                    + " modesMapped=" + sortedStringMap(duplicate.connectionModes())
                    + " hiddenBefore=" + sortedStrings(entry.definition().hiddenConnections())
                    + " hiddenMapped=" + sortedStrings(duplicate.hiddenConnections()));
            pastedSnapshots.add(duplicate);
            session.currentQuest = newId;
        }
        if (pastedSnapshots.isEmpty()) {
            owner.clipboardDebug("PASTE aborted empty staged allocated=" + allocatedIds);
            return;
        }
        owner.definitionStore().upsertAll(pastedSnapshots);
        List<QuestDefinition> created = new ArrayList<>();
        for (QuestDefinition duplicate : pastedSnapshots) {
            QuestDefinition saved = owner.definitionStore().quests().getOrDefault(duplicate.id(), duplicate);
            created.add(saved);
            owner.clipboardDebug("PASTE finalSaved id=" + saved.id()
                    + " prerequisites=" + sortedStrings(saved.prerequisites())
                    + " connectionColors=" + sortedConnectionColors(saved.connectionColors())
                    + " connectionModes=" + sortedStringMap(saved.connectionModes())
                    + " hiddenConnections=" + sortedStrings(saved.hiddenConnections())
                    + " groups=" + sortedStrings(saved.display().groups().keySet()));
        }
        ClipboardPasteResult result = new ClipboardPasteResult(created, allocatedIds, droppedExternalPrerequisiteEdges);
        owner.definitionStore().saveAll();
        owner.clipboardDebug("PASTE flushed files created=" + result.selectionIds().stream().sorted().toList());
        session.currentGroup = request.targetChapter();
        owner.postMutation(player);
        for (QuestDefinition definition : result.createdQuests()) {
            QuestDefinition synced = owner.definitionStore().quests().getOrDefault(definition.id(), definition);
            owner.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "add", synced);
        }
        CompoundTag selection = new CompoundTag();
        ListTag ids = new ListTag();
        for (String questId : result.selectionIds()) {
            ids.add(StringTag.valueOf(questId));
        }
        selection.putString("group", request.targetChapter());
        selection.putInt("created_count", result.createdQuests().size());
        selection.putInt("dropped_external_edges", result.droppedExternalPrerequisiteEdges());
        selection.put("quests", ids);
        owner.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "paste_select", "__paste_select", selection);
        owner.clipboardDebug("PASTE complete player=" + playerName
                + " group=" + request.targetChapter()
                + " created=" + result.selectionIds().stream().sorted().toList()
                + " selectionCount=" + ids.size()
                + " droppedExternalPrerequisiteEdges=" + result.droppedExternalPrerequisiteEdges());
        QuestsAndStuffMod.debugLog("[QnS:Editor] paste_clipboard group={} copies={} anchor={},{} dropped_external_prerequisite_edges={}", request.targetChapter(), result.createdQuests().size(), request.anchorX(), request.anchorY(), result.droppedExternalPrerequisiteEdges());
    }

    public void pasteBlueprintInGroup(ServerPlayer player, String groupName, int anchorX, int anchorY, CanvasBlueprint blueprint) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (player == null || group.isBlank() || blueprint == null || blueprint.isEmpty()) {
            return;
        }
        EditorSessionService.EditorSession session = owner.session(player);
        owner.ensureGroupExists(group);

        Map<String, String> allocatedQuestIds = allocateQuestIds(group, blueprint);
        Map<String, String> allocatedImageIds = allocateImageIds(group, blueprint);
        Map<String, String> allocatedTextIds = allocateTextIds(group, blueprint);
        if (allocatedQuestIds.isEmpty() && allocatedImageIds.isEmpty() && allocatedTextIds.isEmpty()) {
            return;
        }

        owner.captureUndo(session);
        List<QuestDefinition> pastedQuests = pasteBlueprintQuests(group, anchorX, anchorY, blueprint, allocatedQuestIds);
        if (!pastedQuests.isEmpty()) {
            owner.definitionStore().upsertAll(pastedQuests);
            session.currentQuest = pastedQuests.get(pastedQuests.size() - 1).id();
        }
        List<CanvasImageLayer> pastedImages = pasteBlueprintImages(group, anchorX, anchorY, blueprint, allocatedImageIds);
        List<CanvasTextLayer> pastedTexts = pasteBlueprintTexts(group, anchorX, anchorY, blueprint, allocatedTextIds);
        owner.definitionStore().setCanvasLayerOrder(group, remappedLayerOrder(group, blueprint, allocatedQuestIds, allocatedImageIds, allocatedTextIds));
        owner.definitionStore().saveAll();
        session.currentGroup = group;
        owner.postMutation(player);
        for (QuestDefinition definition : pastedQuests) {
            QuestDefinition synced = owner.definitionStore().quests().getOrDefault(definition.id(), definition);
            owner.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "add", synced);
        }
        CompoundTag selection = selectionPayload(group, pastedQuests, pastedImages, pastedTexts);
        owner.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "paste_select", "__paste_select", selection);
        QuestsAndStuffMod.debugLog("[QnS:Editor] paste_blueprint group={} quests={} images={} texts={} anchor={},{}",
                group, pastedQuests.size(), pastedImages.size(), pastedTexts.size(), anchorX, anchorY);
    }

    private Map<String, String> allocateQuestIds(String group, CanvasBlueprint blueprint) {
        Map<String, String> allocatedIds = new LinkedHashMap<>();
        Set<String> reservedIds = new HashSet<>(owner.definitionStore().quests().keySet());
        for (CanvasBlueprint.QuestEntry entry : blueprint.quests()) {
            if (entry == null || entry.sourceId().isBlank() || entry.definition() == null) {
                continue;
            }
            String newId = owner.nextQuestId(group, reservedIds);
            allocatedIds.put(entry.sourceId(), newId);
            reservedIds.add(newId);
        }
        return allocatedIds;
    }

    private Map<String, String> allocateImageIds(String group, CanvasBlueprint blueprint) {
        Map<String, String> allocatedIds = new LinkedHashMap<>();
        Set<String> reservedIds = new HashSet<>();
        for (CanvasImageLayer image : owner.definitionStore().canvasImages(group)) {
            reservedIds.add(image.id());
        }
        for (CanvasImageLayer image : blueprint.images()) {
            if (image == null || image.id().isBlank()) {
                continue;
            }
            String newId = StableIdAllocator.nextId("img", reservedIds);
            allocatedIds.put(image.id(), newId);
            reservedIds.add(newId);
        }
        return allocatedIds;
    }

    private Map<String, String> allocateTextIds(String group, CanvasBlueprint blueprint) {
        Map<String, String> allocatedIds = new LinkedHashMap<>();
        Set<String> reservedIds = new HashSet<>();
        for (CanvasTextLayer text : owner.definitionStore().canvasTexts(group)) {
            reservedIds.add(text.id());
        }
        for (CanvasTextLayer text : blueprint.texts()) {
            if (text == null || text.id().isBlank()) {
                continue;
            }
            String newId = StableIdAllocator.nextId("txt", reservedIds);
            allocatedIds.put(text.id(), newId);
            reservedIds.add(newId);
        }
        return allocatedIds;
    }

    private List<QuestDefinition> pasteBlueprintQuests(String group, int anchorX, int anchorY, CanvasBlueprint blueprint, Map<String, String> allocatedQuestIds) {
        List<QuestDefinition> pasted = new ArrayList<>();
        for (CanvasBlueprint.QuestEntry entry : blueprint.quests()) {
            String newId = allocatedQuestIds.get(entry.sourceId());
            if (newId == null || newId.isBlank()) {
                continue;
            }
            int x = anchorX + (entry.sourceX() - blueprint.originX());
            int y = anchorY + (entry.sourceY() - blueprint.originY());
            float scale = normalizeScale(entry.scale(), 1.0f);
            QuestDefinition duplicate = duplicateDefinition(entry.definition(), newId, group, x, y, scale, allocatedQuestIds);
            if (owner.definitionStore().groupLockUntilUnlocked(group)) {
                duplicate = withSettings(duplicate, withHiddenMode(duplicate.settings(), QuestVisibilityMode.LOCKED));
            }
            pasted.add(duplicate);
        }
        return pasted;
    }

    private List<CanvasImageLayer> pasteBlueprintImages(String group, int anchorX, int anchorY, CanvasBlueprint blueprint, Map<String, String> allocatedImageIds) {
        List<CanvasImageLayer> pasted = new ArrayList<>();
        for (CanvasImageLayer image : blueprint.images()) {
            String newId = allocatedImageIds.get(image.id());
            if (newId == null || newId.isBlank()) {
                continue;
            }
            CanvasImageLayer duplicate = new CanvasImageLayer(
                    newId,
                    image.asset(),
                    anchorX + (image.x() - blueprint.originX()),
                    anchorY + (image.y() - blueprint.originY()),
                    image.w(),
                    image.h(),
                    image.rotation(),
                    image.entityYaw(),
                    image.entitySpinSpeed(),
                    image.modelPitch(),
                    image.pivotX(),
                    image.pivotY()
            );
            owner.definitionStore().putCanvasImage(group, duplicate);
            pasted.add(duplicate);
        }
        return pasted;
    }

    private List<CanvasTextLayer> pasteBlueprintTexts(String group, int anchorX, int anchorY, CanvasBlueprint blueprint, Map<String, String> allocatedTextIds) {
        List<CanvasTextLayer> pasted = new ArrayList<>();
        for (CanvasTextLayer text : blueprint.texts()) {
            String newId = allocatedTextIds.get(text.id());
            if (newId == null || newId.isBlank()) {
                continue;
            }
            CanvasTextLayer duplicate = new CanvasTextLayer(
                    newId,
                    text.text(),
                    anchorX + (text.x() - blueprint.originX()),
                    anchorY + (text.y() - blueprint.originY()),
                    text.w(),
                    text.h(),
                    text.rotation(),
                    text.align(),
                    text.style(),
                    text.color(),
                    text.fontSize(),
                    text.spans()
            );
            owner.definitionStore().putCanvasText(group, duplicate);
            pasted.add(duplicate);
        }
        return pasted;
    }

    private List<String> remappedLayerOrder(
            String group,
            CanvasBlueprint blueprint,
            Map<String, String> questIds,
            Map<String, String> imageIds,
            Map<String, String> textIds
    ) {
        List<String> existing = new ArrayList<>(owner.definitionStore().canvasLayerOrder(group));
        List<String> pasted = new ArrayList<>();
        for (String key : blueprint.layerOrder()) {
            String remapped = remapLayerKey(key, questIds, imageIds, textIds);
            if (!remapped.isBlank() && !pasted.contains(remapped)) {
                pasted.add(remapped);
            }
        }
        for (String id : imageIds.values()) {
            addLayerKey(pasted, imageKey(id));
        }
        for (String id : textIds.values()) {
            addLayerKey(pasted, textKey(id));
        }
        for (String id : questIds.values()) {
            addLayerKey(pasted, questKey(id));
        }
        existing.removeIf(pasted::contains);
        existing.addAll(pasted);
        return existing;
    }

    private static void addLayerKey(List<String> order, String key) {
        if (key != null && !key.isBlank() && !order.contains(key)) {
            order.add(key);
        }
    }

    private static String remapLayerKey(String key, Map<String, String> questIds, Map<String, String> imageIds, Map<String, String> textIds) {
        if (key == null || key.isBlank()) {
            return "";
        }
        if (key.startsWith("quest:")) {
            String id = questIds.get(key.substring("quest:".length()));
            return id == null || id.isBlank() ? "" : questKey(id);
        }
        if (key.startsWith("image:")) {
            String id = imageIds.get(key.substring("image:".length()));
            return id == null || id.isBlank() ? "" : imageKey(id);
        }
        if (key.startsWith("text:")) {
            String id = textIds.get(key.substring("text:".length()));
            return id == null || id.isBlank() ? "" : textKey(id);
        }
        return "";
    }

    private static String questKey(String id) {
        return "quest:" + id;
    }

    private static String imageKey(String id) {
        return "image:" + id;
    }

    private static String textKey(String id) {
        return "text:" + id;
    }

    private static CompoundTag selectionPayload(String group, List<QuestDefinition> quests, List<CanvasImageLayer> images, List<CanvasTextLayer> texts) {
        CompoundTag selection = new CompoundTag();
        selection.putString("group", group);
        selection.putInt("created_count", quests.size());
        selection.put("quests", questSelectionIds(quests));
        selection.put("images", imageSelectionIds(images));
        selection.put("texts", textSelectionIds(texts));
        return selection;
    }

    private static ListTag questSelectionIds(List<QuestDefinition> quests) {
        ListTag ids = new ListTag();
        for (QuestDefinition quest : quests) {
            ids.add(StringTag.valueOf(quest.id()));
        }
        return ids;
    }

    private static ListTag imageSelectionIds(List<CanvasImageLayer> images) {
        ListTag ids = new ListTag();
        for (CanvasImageLayer image : images) {
            ids.add(StringTag.valueOf(image.id()));
        }
        return ids;
    }

    private static ListTag textSelectionIds(List<CanvasTextLayer> texts) {
        ListTag ids = new ListTag();
        for (CanvasTextLayer text : texts) {
            ids.add(StringTag.valueOf(text.id()));
        }
        return ids;
    }

    private static String firstVisibleGroup(QuestDefinition definition) {
        if (definition == null || definition.display().groups().isEmpty()) {
            return "";
        }
        return definition.display().groups().keySet().stream().sorted().findFirst().orElse("");
    }

    private static ChapterDefinition firstVisibleGroupView(QuestDefinition definition) {
        String group = firstVisibleGroup(definition);
        return group.isBlank() ? null : definition.display().groups().get(group);
    }

    private static QuestSettings withHiddenMode(QuestSettings source, QuestVisibilityMode mode) {
        QuestSettings settings = source == null ? QuestSettings.DEFAULT : source;
        return new QuestSettings(
                settings.individualProgress(),
                mode,
                settings.repeatable(),
                settings.autoClaimRewards(),
                settings.unlockNotification(),
                settings.showPrerequisiteArrow()
        );
    }
}
