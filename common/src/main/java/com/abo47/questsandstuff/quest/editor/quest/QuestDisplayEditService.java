package com.abo47.questsandstuff.quest.editor.quest;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

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
                source.display().visualHidden()
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
                source.display().visualHidden()
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
                source.display().visualHidden()
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
                hidden
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
                old.visualHidden()
        );
        updateQuest(player, session, QuestDefinitionEdits.withDisplay(quest, display));
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
                old.visualHidden()
        );
        updateQuest(player, session, QuestDefinitionEdits.withDisplay(quest, display));
    }

    private void updateQuest(ServerPlayer player, EditorSessionService.EditorSession session, QuestDefinition updated) {
        service.definitionStore().upsert(updated);
        service.definitionStore().saveNow(updated.id());
        session.currentQuest = updated.id();
        service.postMutation(player);
        service.syncService().broadcastEditorMutation(player.server.getPlayerList().getPlayers(), "update", updated);
    }

    private static String limitDescriptionLine(String line) {
        return line.length() > MAX_DESCRIPTION_LINE_LENGTH ? line.substring(0, MAX_DESCRIPTION_LINE_LENGTH) : line;
    }
}
