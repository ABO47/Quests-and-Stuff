package com.abo47.questsandstuff.quest.editor.quest;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;

import com.abo47.questsandstuff.quest.editor.canvas.EditorPlacementService;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import com.abo47.questsandstuff.util.naming.QuestNaming;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class QuestCrudHandler {
    private final EditorSessionService service;

    public QuestCrudHandler(EditorSessionService service) {
        this.service = service;
    }

    public void addQuest(ServerPlayer player) {
        addQuest(player, service.session(player).currentChapter);
    }

    public void addQuest(ServerPlayer player, String preferredGroup) {
        addQuest(player, preferredGroup, "", 0, 0, "");
    }

    public void addQuest(ServerPlayer player, String preferredGroup, String preferredQuestId, int x, int y, String preferredTitle) {
        EditorSessionService.EditorSession session = service.session(player);
        String group = preferredGroup == null || preferredGroup.isBlank() ? session.currentChapter : preferredGroup;
        if (group == null || group.isBlank()) {
            List<String> groups = service.chapters();
            if (!groups.isEmpty()) {
                group = groups.get(0);
            }
        }
        if (group == null || group.isBlank()) {
            QuestsAndStuffMod.debugLog("[QnS:Editor] add quest skipped: no chapter selected/available");
            return;
        }
        service.captureUndo(session);
        service.ensureChapterExists(group);
        session.currentChapter = group;
        String id = EditorSessionService.normalizeQuestId(preferredQuestId);
        if (id.isBlank() || service.definitionStore().quests().containsKey(id) || !QuestNaming.isAutoQuestId(id)) {
            id = service.nextQuestId(group);
        }
        String title = preferredTitle == null ? "" : preferredTitle.trim();
        int[] freePosition = EditorPlacementService.findNearestFreePosition(service.definitionStore().quests(), group, x, y, 16);
        int finalX = freePosition[0];
        int finalY = freePosition[1];
        QuestsAndStuffMod.debugLog("[QnS:Editor] add quest request group={} reqId={} assignedId={} reqPos={},{} finalPos={},{}", group, preferredQuestId, id, x, y, finalX, finalY);
        QuestSettings settings = service.definitionStore().chapterLockUntilUnlocked(group)
                ? questSettingsWithHiddenMode(QuestVisibilityMode.LOCKED)
                : QuestSettings.DEFAULT;

        QuestDefinition definition = new QuestDefinition(
                QuestDefinition.CURRENT_SCHEMA,
                id,
                QuestDisplay.forNewQuest(title, Map.of(group, new ChapterDef(true, finalX, finalY, 1.0f))),
                settings,
                Set.of(),
                Map.of(),
                Map.of()
        );

        service.definitionStore().upsert(definition);
        session.currentQuest = id;
        service.runtimeEngine().clearQuestProgress(id);
        service.postMutation(player);
        service.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "add", definition);
    }

    public void removeQuest(ServerPlayer player, String questId) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        if (!service.definitionStore().quests().containsKey(normalizedQuestId)) {
            return;
        }
        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        int removedReferences = removeQuestReferences(normalizedQuestId);
        service.definitionStore().remove(normalizedQuestId);
        service.normalizeQuestSelection(session);
        service.postMutation(player);
        QuestsAndStuffMod.debugLog("[QnS:Editor] remove quest id={} removed_prerequisite_references={}", normalizedQuestId, removedReferences);
        service.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "remove", normalizedQuestId, null);
    }

    private int removeQuestReferences(String removedQuestId) {
        int removedReferences = 0;
        for (QuestDefinition definition : new ArrayList<>(service.definitionStore().quests().values())) {
            if (definition == null || removedQuestId.equals(definition.id()) || !definition.prerequisites().contains(removedQuestId)) {
                continue;
            }
            QuestDefinition next = QuestDefinitionEdits.withoutPrerequisite(definition, removedQuestId);
            if (next == definition) {
                continue;
            }
            service.definitionStore().upsert(next);
            removedReferences++;
        }
        for (String group : service.definitionStore().chapterOrder()) {
            for (CanvasExclusiveChoice ec : new ArrayList<>(service.definitionStore().canvasExclusiveChoices(group))) {
                boolean changed = false;
                if (ec.connectionQuestIds().contains(removedQuestId)) {
                    ec = ec.removeConnection(removedQuestId);
                    changed = true;
                }
                if (ec.prerequisiteQuestIds().contains(removedQuestId)) {
                    ec = ec.removePrerequisite(removedQuestId);
                    changed = true;
                }
                if (changed) {
                    service.definitionStore().putCanvasExclusiveChoice(group, ec);
                    removedReferences++;
                }
            }
        }
        return removedReferences;
    }

    public void openChapter(ServerPlayer player, String chapterName) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank()) {
            return;
        }
        if (!service.definitionStore().chapterOrder().contains(chapter)) {
            return;
        }
        EditorSessionService.EditorSession session = service.session(player);
        session.currentChapter = chapter;
        service.normalizeQuestSelection(session);
    }

    public void openQuest(ServerPlayer player, String questId) {
        QuestDefinition definition = service.definitionStore().quests().get(questId);
        if (definition == null) {
            return;
        }
        EditorSessionService.EditorSession session = service.session(player);
        session.currentQuest = questId;
        if (!definition.display().chapters().isEmpty()) {
            session.currentChapter = definition.display().chapters().keySet().stream().sorted().findFirst().orElse(session.currentChapter);
        }
    }

    private static QuestSettings questSettingsWithHiddenMode(QuestVisibilityMode mode) {
        QuestSettings defaults = QuestSettings.DEFAULT;
        return new QuestSettings(
                defaults.individualProgress(),
                mode,
                defaults.repeatable(),
                defaults.autoClaimRewards(),
                defaults.unlockNotification(),
                defaults.showPrerequisiteArrow()
        );
    }
}
