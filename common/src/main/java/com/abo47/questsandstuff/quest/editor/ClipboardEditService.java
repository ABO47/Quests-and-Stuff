package com.abo47.questsandstuff.quest.editor;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardPasteRequest;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardPasteResult;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardSnapshot;
import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
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
}
