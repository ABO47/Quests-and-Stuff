package com.abo47.questsandstuff.quest.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardPasteRequest;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardPasteResult;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardSnapshot;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.util.naming.QuestNaming;
import com.abo47.questsandstuff.util.naming.StableIdAllocator;

import static com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDebugFormatter.clipboardSourceSummary;
import static com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDebugFormatter.sortedConnectionColors;
import static com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDebugFormatter.sortedStringMap;
import static com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDebugFormatter.sortedStrings;
import static com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDefinitionCopier.duplicateDefinition;
import static com.abo47.questsandstuff.quest.editor.clipboard.ClipboardDefinitionCopier.normalizeScale;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.deepCopyDefinition;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withSettings;

public final class ClipboardEditService {
    private final EditorSessionService owner;

    public ClipboardEditService(EditorSessionService owner) {
        this.owner = owner;
    }

    public void copyQuestsToClipboard(ServerPlayer player, Set<String> questIds) {
        copyQuestsToClipboard(player, player == null ? "" : owner.session(player).currentChapter, questIds);
    }

    public void copyQuestsToClipboard(ServerPlayer player, String chapterName, Set<String> questIds) {
        if (player == null || questIds == null || questIds.isEmpty()) {
            return;
        }
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        String playerName = player.getGameProfile().getName();
        owner.clipboardDebug("COPY request player=" + playerName + " chapter=" + chapter + " requested=" + sortedStrings(questIds));
        EditorSessionService.EditorSession session = owner.session(player);
        List<ClipboardSnapshot.Entry> entries = new ArrayList<>();
        for (String questId : questIds) {
            String sourceId = EditorSessionService.normalizeQuestId(questId);
            if (sourceId.isBlank()) {
                continue;
            }
            QuestDefinition definition = owner.definitionStore().quest(sourceId);
            if (definition != null) {
                QuestDefinition snapshot = deepCopyDefinition(definition);
                ChapterDef view = chapter.isBlank() ? null : definition.display().chapters().get(chapter);
                if (view == null) {
                    view = firstVisibleChapterView(definition);
                }
                String sourceChapter = chapter.isBlank() || !definition.display().chapters().containsKey(chapter) ? firstVisibleChapter(definition) : chapter;
                entries.add(new ClipboardSnapshot.Entry(sourceId, sourceChapter, view == null ? 0 : view.x(), view == null ? 0 : view.y(), view == null ? 1.0f : view.scale(), snapshot));
                owner.clipboardDebug("COPY entry source=" + sourceId
                        + " sourceChapter=" + sourceChapter
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
                + " externalPrerequisiteEdges=" + snapshot.countExternalPrerequisiteConnections());
        QuestsAndStuffMod.debugLog("[QnS:Editor] copy_many player={} chapter={} requested={} stored={} external_prerequisite_connections={}", player.getGameProfile().getName(), chapter, questIds.size(), snapshot.entries().size(), snapshot.countExternalPrerequisiteConnections());
    }

    public void pasteClipboardInChapter(ServerPlayer player, String chapterName, int anchorX, int anchorY) {
        ClipboardPasteRequest request = new ClipboardPasteRequest(EditorSessionService.normalizeChapter(chapterName), anchorX, anchorY);
        if (player == null || request.targetChapter().isBlank()) {
            return;
        }
        EditorSessionService.EditorSession session = owner.session(player);
        String playerName = player.getGameProfile().getName();
        owner.clipboardDebug("PASTE request player=" + playerName + " chapter=" + request.targetChapter() + " anchor=" + request.anchorX() + "," + request.anchorY());
        ClipboardSnapshot snapshot = session.clipboardSnapshot;
        List<ClipboardSnapshot.Entry> entries = snapshot.sortedEntries();
        if (entries.isEmpty()) {
            owner.clipboardDebug("PASTE skipped empty session clipboard chapter=" + request.targetChapter());
            QuestsAndStuffMod.debugLog("[QnS:Editor] paste_clipboard skipped empty chapter={}", request.targetChapter());
            return;
        }
        owner.ensureChapterExists(request.targetChapter());
        int droppedExternalPrerequisiteConnections = snapshot.countExternalPrerequisiteConnections();
        owner.clipboardDebug("PASTE read entries=" + entries.size() + " sources=" + clipboardSourceSummary(entries) + " droppedExternalPrerequisiteConnections=" + droppedExternalPrerequisiteConnections);

        Map<String, String> allocatedIds = new LinkedHashMap<>();
        Set<String> reservedIds = new HashSet<>(owner.definitionStore().questIds());
        reservedIds.addAll(snapshot.sourceIds());
        for (ClipboardSnapshot.Entry entry : entries) {
            if (entry == null || entry.sourceId().isBlank() || entry.definition() == null) {
                continue;
            }
            String newId = QuestNaming.nextQuestId(request.targetChapter(), reservedIds);
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
        owner.capturePasteUndo(session, allocatedIds.values(), List.of(), List.of(), "");
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
            if (owner.definitionStore().chapterLockUntilUnlocked(request.targetChapter())) {
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
            QuestDefinition saved = owner.definitionStore().quest(duplicate.id());
            if (saved == null) {
                saved = duplicate;
            }
            created.add(saved);
            owner.clipboardDebug("PASTE finalSaved id=" + saved.id()
                    + " prerequisites=" + sortedStrings(saved.prerequisites())
                    + " connectionColors=" + sortedConnectionColors(saved.connectionColors())
                    + " connectionModes=" + sortedStringMap(saved.connectionModes())
                    + " hiddenConnections=" + sortedStrings(saved.hiddenConnections())
                    + " chapters=" + sortedStrings(saved.display().chapters().keySet()));
        }
        ClipboardPasteResult result = new ClipboardPasteResult(created, allocatedIds, droppedExternalPrerequisiteConnections);
        owner.definitionStore().saveNow(result.selectionIds());
        owner.clipboardDebug("PASTE flushed files created=" + result.selectionIds().stream().sorted().toList());
        session.currentChapter = request.targetChapter();
        owner.postMutationDelta(player, Set.copyOf(result.selectionIds()), Set.of(request.targetChapter()));
        for (QuestDefinition definition : result.createdQuests()) {
            QuestDefinition synced = owner.definitionStore().quest(definition.id());
            if (synced == null) {
                synced = definition;
            }
            owner.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "add", synced);
        }
        CompoundTag selection = new CompoundTag();
        ListTag ids = new ListTag();
        for (String questId : result.selectionIds()) {
            ids.add(StringTag.valueOf(questId));
        }
        selection.putString("chapter", request.targetChapter());
        selection.putInt("created_count", result.createdQuests().size());
        selection.putInt("dropped_external_connections", result.droppedExternalPrerequisiteConnections());
        selection.put("quests", ids);
        CompoundTag allocated = new CompoundTag();
        for (Map.Entry<String, String> a : allocatedIds.entrySet()) {
            allocated.putString(a.getKey(), a.getValue());
        }
        selection.put("allocated_ids", allocated);
        owner.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "paste_select", "__paste_select", selection);
        owner.clipboardDebug("PASTE complete player=" + playerName
                + " chapter=" + request.targetChapter()
                + " created=" + result.selectionIds().stream().sorted().toList()
                + " selectionCount=" + ids.size()
                + " droppedExternalPrerequisiteConnections=" + result.droppedExternalPrerequisiteConnections());
        QuestsAndStuffMod.debugLog("[QnS:Editor] paste_clipboard chapter={} copies={} anchor={},{} dropped_external_prerequisite_connections={}", request.targetChapter(), result.createdQuests().size(), request.anchorX(), request.anchorY(), result.droppedExternalPrerequisiteConnections());
    }

    public void pasteBlueprintInChapter(ServerPlayer player, String chapterName, int anchorX, int anchorY, CanvasBlueprint blueprint) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (player == null || chapter.isBlank() || blueprint == null || blueprint.isEmpty()) {
            return;
        }
        EditorSessionService.EditorSession session = owner.session(player);
        owner.ensureChapterExists(chapter);

        Map<String, String> allocatedQuestIds = allocateQuestIds(chapter, blueprint);
        Map<String, String> allocatedImageIds = allocateImageIds(chapter, blueprint);
        Map<String, String> allocatedTextIds = allocateTextIds(chapter, blueprint);
        Map<String, String> allocatedEcIds = allocateEcIds(chapter, blueprint);
        if (allocatedQuestIds.isEmpty() && allocatedImageIds.isEmpty() && allocatedTextIds.isEmpty() && allocatedEcIds.isEmpty()) {
            return;
        }

        owner.capturePasteUndo(session, allocatedQuestIds.values(), allocatedImageIds.values(), allocatedTextIds.values(), chapter);
        List<QuestDefinition> pastedQuests = pasteBlueprintQuests(chapter, anchorX, anchorY, blueprint, allocatedQuestIds);
        if (!pastedQuests.isEmpty()) {
            owner.definitionStore().upsertAll(pastedQuests);
            session.currentQuest = pastedQuests.get(pastedQuests.size() - 1).id();
        }
        List<CanvasImageLayer> pastedImages = pasteBlueprintImages(chapter, anchorX, anchorY, blueprint, allocatedImageIds);
        List<CanvasTextLayer> pastedTexts = pasteBlueprintTexts(chapter, anchorX, anchorY, blueprint, allocatedTextIds);
        List<CanvasExclusiveChoice> pastedEcs = pasteBlueprintEcs(chapter, anchorX, anchorY, blueprint, allocatedEcIds, allocatedQuestIds);
        for (CanvasExclusiveChoice ec : pastedEcs) {
            owner.definitionStore().putCanvasExclusiveChoice(chapter, ec);
        }
        owner.definitionStore().putCanvasLayers(chapter, pastedImages, pastedTexts, remappedLayerOrder(chapter, blueprint, allocatedQuestIds, allocatedImageIds, allocatedTextIds, allocatedEcIds));
        owner.definitionStore().saveNow(allocatedQuestIds.values());
        session.currentChapter = chapter;
        owner.postMutationDelta(player, Set.copyOf(allocatedQuestIds.values()), Set.of(chapter));
        for (QuestDefinition definition : pastedQuests) {
            QuestDefinition synced = owner.definitionStore().quest(definition.id());
            if (synced == null) {
                synced = definition;
            }
            owner.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "add", synced);
        }
        CompoundTag selection = selectionPayload(chapter, pastedQuests, pastedImages, pastedTexts, pastedEcs);
        owner.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "paste_select", "__paste_select", selection);
        QuestsAndStuffMod.debugLog("[QnS:Editor] paste_blueprint chapter={} quests={} images={} texts={} ecs={} anchor={},{}",
                chapter, pastedQuests.size(), pastedImages.size(), pastedTexts.size(), pastedEcs.size(), anchorX, anchorY);
    }

    private Map<String, String> allocateQuestIds(String chapter, CanvasBlueprint blueprint) {
        Map<String, String> allocatedIds = new LinkedHashMap<>();
        Set<String> reservedIds = new HashSet<>(owner.definitionStore().questIds());
        for (CanvasBlueprint.QuestEntry entry : blueprint.quests()) {
            if (entry == null || entry.sourceId().isBlank() || entry.definition() == null) {
                continue;
            }
            String newId = QuestNaming.nextQuestId(chapter, reservedIds);
            allocatedIds.put(entry.sourceId(), newId);
            reservedIds.add(newId);
        }
        return allocatedIds;
    }

    private Map<String, String> allocateImageIds(String chapter, CanvasBlueprint blueprint) {
        Map<String, String> allocatedIds = new LinkedHashMap<>();
        Set<String> reservedIds = new HashSet<>();
        for (CanvasImageLayer image : owner.definitionStore().canvasImages(chapter)) {
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

    private Map<String, String> allocateTextIds(String chapter, CanvasBlueprint blueprint) {
        Map<String, String> allocatedIds = new LinkedHashMap<>();
        Set<String> reservedIds = new HashSet<>();
        for (CanvasTextLayer text : owner.definitionStore().canvasTexts(chapter)) {
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

    private List<QuestDefinition> pasteBlueprintQuests(String chapter, int anchorX, int anchorY, CanvasBlueprint blueprint, Map<String, String> allocatedQuestIds) {
        List<QuestDefinition> pasted = new ArrayList<>();
        for (CanvasBlueprint.QuestEntry entry : blueprint.quests()) {
            String newId = allocatedQuestIds.get(entry.sourceId());
            if (newId == null || newId.isBlank()) {
                continue;
            }
            int x = anchorX + (entry.sourceX() - blueprint.originX());
            int y = anchorY + (entry.sourceY() - blueprint.originY());
            float scale = normalizeScale(entry.scale(), 1.0f);
            QuestDefinition duplicate = duplicateDefinition(entry.definition(), newId, chapter, x, y, scale, allocatedQuestIds);
            if (owner.definitionStore().chapterLockUntilUnlocked(chapter)) {
                duplicate = withSettings(duplicate, withHiddenMode(duplicate.settings(), QuestVisibilityMode.LOCKED));
            }
            pasted.add(duplicate);
        }
        return pasted;
    }

    private List<CanvasImageLayer> pasteBlueprintImages(String chapter, int anchorX, int anchorY, CanvasBlueprint blueprint, Map<String, String> allocatedImageIds) {
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
            pasted.add(duplicate);
        }
        return pasted;
    }

    private List<CanvasTextLayer> pasteBlueprintTexts(String chapter, int anchorX, int anchorY, CanvasBlueprint blueprint, Map<String, String> allocatedTextIds) {
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
            pasted.add(duplicate);
        }
        return pasted;
    }

    private List<String> remappedLayerOrder(
            String chapter,
            CanvasBlueprint blueprint,
            Map<String, String> questIds,
            Map<String, String> imageIds,
            Map<String, String> textIds,
            Map<String, String> ecIds
    ) {
        List<String> existing = new ArrayList<>(owner.definitionStore().canvasLayerOrder(chapter));
        List<String> pasted = new ArrayList<>();
        for (String key : blueprint.layerOrder()) {
            String remapped = remapLayerKey(key, questIds, imageIds, textIds, ecIds);
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
        for (String id : ecIds.values()) {
            addLayerKey(pasted, ecKey(id));
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

    private static String remapLayerKey(String key, Map<String, String> questIds, Map<String, String> imageIds, Map<String, String> textIds, Map<String, String> ecIds) {
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
        if (key.startsWith("exclusive_choice:")) {
            String id = ecIds.get(key.substring("exclusive_choice:".length()));
            return id == null || id.isBlank() ? "" : ecKey(id);
        }
        return "";
    }

    private static String ecKey(String id) {
        return "exclusive_choice:" + id;
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

    private Map<String, String> allocateEcIds(String chapter, CanvasBlueprint blueprint) {
        Map<String, String> allocatedIds = new LinkedHashMap<>();
        Set<String> reservedIds = new HashSet<>();
        for (CanvasExclusiveChoice ec : owner.definitionStore().canvasExclusiveChoices(chapter)) {
            reservedIds.add(ec.id());
        }
        for (CanvasBlueprint.ExclusiveChoiceEntry entry : blueprint.exclusiveChoices()) {
            if (entry == null || entry.sourceId().isBlank()) {
                continue;
            }
            String newId = StableIdAllocator.nextId("ec", reservedIds);
            allocatedIds.put(entry.sourceId(), newId);
            reservedIds.add(newId);
        }
        return allocatedIds;
    }

    private List<CanvasExclusiveChoice> pasteBlueprintEcs(String chapter, int anchorX, int anchorY, CanvasBlueprint blueprint, Map<String, String> allocatedEcIds, Map<String, String> allocatedQuestIds) {
        List<CanvasExclusiveChoice> pasted = new ArrayList<>();
        for (CanvasBlueprint.ExclusiveChoiceEntry entry : blueprint.exclusiveChoices()) {
            String newId = allocatedEcIds.get(entry.sourceId());
            if (newId == null || newId.isBlank()) {
                continue;
            }
            List<String> remappedConnections = new ArrayList<>();
            for (String conn : entry.connections()) {
                String mapped = allocatedQuestIds.get(conn);
                if (mapped != null) {
                    remappedConnections.add(mapped);
                }
            }
            List<String> remappedPrerequisites = new ArrayList<>();
            for (String prereq : entry.prerequisites()) {
                String mapped = allocatedQuestIds.get(prereq);
                if (mapped != null) {
                    remappedPrerequisites.add(mapped);
                }
            }
            Map<String, Integer> remappedColors = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> e : entry.connectionColors().entrySet()) {
                String mapped = allocatedQuestIds.get(e.getKey());
                if (mapped != null && e.getValue() != null) {
                    remappedColors.put(mapped, e.getValue());
                }
            }
            Map<String, String> remappedModes = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : entry.connectionModes().entrySet()) {
                String mapped = allocatedQuestIds.get(e.getKey());
                if (mapped != null) {
                    remappedModes.put(mapped, e.getValue());
                }
            }
            Map<String, String> remappedTextures = new LinkedHashMap<>();
            for (Map.Entry<String, String> e : entry.connectionTextures().entrySet()) {
                String mapped = allocatedQuestIds.get(e.getKey());
                String tex = e.getValue();
                if (mapped != null && tex != null && !tex.isBlank()) {
                    remappedTextures.put(mapped, tex);
                }
            }
            Map<String, Integer> remappedSpacings = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> e : entry.connectionTextureSpacings().entrySet()) {
                String mapped = allocatedQuestIds.get(e.getKey());
                if (mapped != null && e.getValue() != null) {
                    remappedSpacings.put(mapped, e.getValue());
                }
            }
            Set<String> remappedHidden = new LinkedHashSet<>();
            for (String h : entry.hiddenConnections()) {
                String mapped = allocatedQuestIds.get(h);
                if (mapped != null) {
                    remappedHidden.add(mapped);
                }
            }
            pasted.add(new CanvasExclusiveChoice(
                    newId,
                    anchorX + (entry.sourceX() - blueprint.originX()),
                    anchorY + (entry.sourceY() - blueprint.originY()),
                    entry.sourceW(),
                    entry.sourceH(),
                    entry.rotation(),
                    remappedConnections,
                    remappedPrerequisites,
                    entry.background(),
                    remappedColors,
                    remappedModes,
                    remappedTextures,
                    remappedSpacings,
                    remappedHidden
            ));
        }
        return pasted;
    }

    private static CompoundTag selectionPayload(String chapter, List<QuestDefinition> quests, List<CanvasImageLayer> images, List<CanvasTextLayer> texts, List<CanvasExclusiveChoice> ecs) {
        CompoundTag selection = new CompoundTag();
        selection.putString("chapter", chapter);
        selection.putInt("created_count", quests.size());
        selection.put("quests", questSelectionIds(quests));
        selection.put("images", imageSelectionIds(images));
        selection.put("texts", textSelectionIds(texts));
        selection.put("ecs", ecSelectionIds(ecs));
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

    private static ListTag ecSelectionIds(List<CanvasExclusiveChoice> ecs) {
        ListTag ids = new ListTag();
        for (CanvasExclusiveChoice ec : ecs) {
            ids.add(StringTag.valueOf(ec.id()));
        }
        return ids;
    }

    private static String firstVisibleChapter(QuestDefinition definition) {
        if (definition == null || definition.display().chapters().isEmpty()) {
            return "";
        }
        return definition.display().chapters().keySet().stream().sorted().findFirst().orElse("");
    }

    private static ChapterDef firstVisibleChapterView(QuestDefinition definition) {
        String chapter = firstVisibleChapter(definition);
        return chapter.isBlank() ? null : definition.display().chapters().get(chapter);
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
