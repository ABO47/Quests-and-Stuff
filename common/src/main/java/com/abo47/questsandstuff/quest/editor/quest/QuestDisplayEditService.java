package com.abo47.questsandstuff.quest.editor.quest;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class QuestDisplayEditService {
    private static final int MAX_DESCRIPTION_LINES = 256;
    private static final int MAX_DESCRIPTION_LINE_LENGTH = 16384;

    private final EditorSessionService service;

    public QuestDisplayEditService(EditorSessionService service) {
        this.service = service;
    }

    public void updateQuestDisplay(ServerPlayer player, String questId, String title, String subtitle) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition source = service.definitionStore().quests().get(normalizedQuestId);
        if (source == null) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestDisplay display = new QuestDisplay(
                title == null || title.isBlank() ? source.display().title() : title,
                subtitle == null ? source.display().subtitle() : subtitle,
                source.display().description(),
                source.display().groups(),
                source.display().icon(),
                source.display().iconBackground(),
                source.display().completionSound(),
                source.display().completionSoundVolume(),
                source.display().visualHidden(),
                source.display().questBackground(),
                source.display().questBackgroundGrayscale()
        );
        updateQuest(player, session, QuestDefinitionEdits.withDisplay(source, display));
    }

    public void updateQuestDescription(ServerPlayer player, String questId, List<String> description) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition source = service.definitionStore().quests().get(normalizedQuestId);
        if (source == null) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestDisplay display = new QuestDisplay(
                source.display().title(),
                source.display().subtitle(),
                description == null ? List.of() : description.stream()
                        .filter(line -> line != null)
                        .map(QuestDisplayEditService::limitDescriptionLine)
                        .limit(MAX_DESCRIPTION_LINES)
                        .toList(),
                source.display().groups(),
                source.display().icon(),
                source.display().iconBackground(),
                source.display().completionSound(),
                source.display().completionSoundVolume(),
                source.display().visualHidden(),
                source.display().questBackground(),
                source.display().questBackgroundGrayscale()
        );
        updateQuest(player, session, QuestDefinitionEdits.withDisplay(source, display));
    }

    public void setQuestIcon(ServerPlayer player, String questId, String icon) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition source = service.definitionStore().quests().get(normalizedQuestId);
        if (source == null) {
            return;
        }

        String normalizedIcon = icon == null || icon.isBlank() ? "minecraft:book" : icon.trim();
        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestDisplay display = new QuestDisplay(
                source.display().title(),
                source.display().subtitle(),
                source.display().description(),
                source.display().groups(),
                normalizedIcon,
                source.display().iconBackground(),
                source.display().completionSound(),
                source.display().completionSoundVolume(),
                source.display().visualHidden(),
                source.display().questBackground(),
                source.display().questBackgroundGrayscale()
        );
        QuestsAndStuffMod.debugLog("[QnS:Editor] quest icon quest={} icon={}", normalizedQuestId, normalizedIcon);
        updateQuest(player, session, QuestDefinitionEdits.withDisplay(source, display));
    }

    public void setQuestVisualHidden(ServerPlayer player, String questId, boolean hidden) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
        if (quest == null || quest.display().visualHidden() == hidden) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestDisplay old = quest.display();
        QuestDisplay display = new QuestDisplay(
                old.title(),
                old.subtitle(),
                old.description(),
                old.groups(),
                old.icon(),
                old.iconBackground(),
                old.completionSound(),
                old.completionSoundVolume(),
                hidden,
                old.questBackground(),
                old.questBackgroundGrayscale()
        );
        updateQuest(player, session, QuestDefinitionEdits.withDisplay(quest, display));
    }

    public void setQuestCompletionSound(ServerPlayer player, String questId, String sound) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
        if (quest == null) {
            return;
        }
        String normalizedSound = sound == null || sound.isBlank() ? QuestDisplay.DEFAULT_COMPLETION_SOUND : sound.trim();
        if (quest.display().completionSound().equals(normalizedSound)) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestDisplay old = quest.display();
        QuestDisplay display = new QuestDisplay(
                old.title(),
                old.subtitle(),
                old.description(),
                old.groups(),
                old.icon(),
                old.iconBackground(),
                normalizedSound,
                old.completionSoundVolume(),
                old.visualHidden(),
                old.questBackground(),
                old.questBackgroundGrayscale()
        );
        updateQuest(player, session, QuestDefinitionEdits.withDisplay(quest, display));
    }

    public void setQuestCompletionSound(ServerPlayer player, Set<String> questIds, String sound) {
        String normalizedSound = sound == null || sound.isBlank() ? QuestDisplay.DEFAULT_COMPLETION_SOUND : sound.trim();
        List<QuestDefinition> updated = new ArrayList<>();
        for (QuestDefinition quest : selectedQuests(questIds)) {
            if (quest.display().completionSound().equals(normalizedSound)) {
                continue;
            }
            QuestDisplay old = quest.display();
            QuestDisplay display = new QuestDisplay(
                    old.title(),
                    old.subtitle(),
                    old.description(),
                    old.groups(),
                    old.icon(),
                    old.iconBackground(),
                    normalizedSound,
                    old.completionSoundVolume(),
                    old.visualHidden(),
                    old.questBackground(),
                    old.questBackgroundGrayscale()
            );
            updated.add(QuestDefinitionEdits.withDisplay(quest, display));
        }
        updateQuests(player, updated);
    }

    public void setQuestCompletionSoundVolume(ServerPlayer player, String questId, int volume) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
        if (quest == null) {
            return;
        }
        int normalizedVolume = QuestDisplay.normalizeCompletionSoundVolume(volume);
        if (quest.display().completionSoundVolume() == normalizedVolume) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestDisplay old = quest.display();
        QuestDisplay display = new QuestDisplay(
                old.title(),
                old.subtitle(),
                old.description(),
                old.groups(),
                old.icon(),
                old.iconBackground(),
                old.completionSound(),
                normalizedVolume,
                old.visualHidden(),
                old.questBackground(),
                old.questBackgroundGrayscale()
        );
        updateQuest(player, session, QuestDefinitionEdits.withDisplay(quest, display));
    }

    public void setQuestCompletionSoundVolume(ServerPlayer player, Set<String> questIds, int volume) {
        int normalizedVolume = QuestDisplay.normalizeCompletionSoundVolume(volume);
        List<QuestDefinition> updated = new ArrayList<>();
        for (QuestDefinition quest : selectedQuests(questIds)) {
            if (quest.display().completionSoundVolume() == normalizedVolume) {
                continue;
            }
            QuestDisplay old = quest.display();
            QuestDisplay display = new QuestDisplay(
                    old.title(),
                    old.subtitle(),
                    old.description(),
                    old.groups(),
                    old.icon(),
                    old.iconBackground(),
                    old.completionSound(),
                    normalizedVolume,
                    old.visualHidden(),
                    old.questBackground(),
                    old.questBackgroundGrayscale()
            );
            updated.add(QuestDefinitionEdits.withDisplay(quest, display));
        }
        updateQuests(player, updated);
    }

    public void setQuestBackground(ServerPlayer player, String questId, String background, boolean grayscale) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
        if (quest == null) {
            return;
        }
        String normalizedBackground = QuestDisplay.normalizeQuestBackground(background);
        if (quest.display().questBackground().equals(normalizedBackground) && quest.display().questBackgroundGrayscale() == grayscale) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestDisplay old = quest.display();
        QuestDisplay display = new QuestDisplay(
                old.title(),
                old.subtitle(),
                old.description(),
                old.groups(),
                old.icon(),
                old.iconBackground(),
                old.completionSound(),
                old.completionSoundVolume(),
                old.visualHidden(),
                normalizedBackground,
                grayscale
        );
        updateQuest(player, session, QuestDefinitionEdits.withDisplay(quest, display));
    }

    public void setQuestBackground(ServerPlayer player, Set<String> questIds, String background, boolean grayscale) {
        String normalizedBackground = QuestDisplay.normalizeQuestBackground(background);
        List<QuestDefinition> updated = new ArrayList<>();
        for (QuestDefinition quest : selectedQuests(questIds)) {
            QuestDisplay old = quest.display();
            if (old.questBackground().equals(normalizedBackground) && old.questBackgroundGrayscale() == grayscale) {
                continue;
            }
            QuestDisplay display = new QuestDisplay(
                    old.title(),
                    old.subtitle(),
                    old.description(),
                    old.groups(),
                    old.icon(),
                    old.iconBackground(),
                    old.completionSound(),
                    old.completionSoundVolume(),
                    old.visualHidden(),
                    normalizedBackground,
                    grayscale
            );
            updated.add(QuestDefinitionEdits.withDisplay(quest, display));
        }
        updateQuests(player, updated);
    }

    private void updateQuest(ServerPlayer player, EditorSessionService.EditorSession session, QuestDefinition updated) {
        service.definitionStore().upsert(updated);
        service.definitionStore().saveNow(updated.id());
        session.currentQuest = updated.id();
        service.postMutation(player);
        service.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "update", updated);
    }

    private void updateQuests(ServerPlayer player, List<QuestDefinition> updated) {
        if (updated == null || updated.isEmpty()) {
            return;
        }
        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        for (QuestDefinition definition : updated) {
            service.definitionStore().upsert(definition);
            service.definitionStore().saveNow(definition.id());
            session.currentQuest = definition.id();
        }
        service.postMutation(player);
        for (QuestDefinition definition : updated) {
            service.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "update", definition);
        }
    }

    private List<QuestDefinition> selectedQuests(Set<String> questIds) {
        if (questIds == null || questIds.isEmpty()) {
            return List.of();
        }
        List<QuestDefinition> quests = new ArrayList<>();
        for (String questId : questIds) {
            String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
            QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
            if (quest != null) {
                quests.add(quest);
            }
        }
        return quests;
    }

    private static String limitDescriptionLine(String line) {
        return line.length() > MAX_DESCRIPTION_LINE_LENGTH ? line.substring(0, MAX_DESCRIPTION_LINE_LENGTH) : line;
    }
}
