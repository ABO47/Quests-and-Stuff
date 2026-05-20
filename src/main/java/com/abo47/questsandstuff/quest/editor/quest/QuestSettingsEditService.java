package com.abo47.questsandstuff.quest.editor.quest;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import net.minecraft.server.level.ServerPlayer;

public final class QuestSettingsEditService {
    private final EditorSessionService service;

    public QuestSettingsEditService(EditorSessionService service) {
        this.service = service;
    }

    public void toggleRepeatable(ServerPlayer player) {
        toggleSetting(player, SettingSwitch.REPEATABLE);
    }

    public void toggleAutoClaim(ServerPlayer player) {
        toggleSetting(player, SettingSwitch.AUTO_CLAIM);
    }

    public void toggleIndividual(ServerPlayer player) {
        toggleSetting(player, SettingSwitch.INDIVIDUAL);
    }

    public void setQuestAutoClaim(ServerPlayer player, String questId, boolean enabled) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
        if (quest == null) {
            return;
        }
        QuestSettings old = quest.settings();
        if (old.autoClaimRewards() == enabled) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestSettings next = new QuestSettings(
                old.individualProgress(),
                old.hiddenMode(),
                old.repeatable(),
                enabled,
                old.unlockNotification(),
                old.showPrerequisiteArrow()
        );
        updateQuest(player, quest, next);
    }

    public void setQuestHiddenMode(ServerPlayer player, String questId, String mode) {
        String normalizedQuestId = EditorSessionService.normalizeQuestId(questId);
        QuestDefinition quest = service.definitionStore().quests().get(normalizedQuestId);
        if (quest == null) {
            return;
        }
        QuestVisibilityMode nextMode = QuestVisibilityMode.fromSerializedName(mode);
        QuestSettings old = quest.settings();
        if (old.hiddenMode() == nextMode) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);
        QuestSettings next = new QuestSettings(
                old.individualProgress(),
                nextMode,
                old.repeatable(),
                old.autoClaimRewards(),
                old.unlockNotification(),
                old.showPrerequisiteArrow()
        );
        updateQuest(player, quest, next);
    }

    private void toggleSetting(ServerPlayer player, SettingSwitch toggle) {
        QuestDefinition quest = service.currentQuest(player);
        if (quest == null) {
            return;
        }

        EditorSessionService.EditorSession session = service.session(player);
        service.captureUndo(session);

        QuestSettings old = quest.settings();
        QuestSettings next = switch (toggle) {
            case REPEATABLE -> new QuestSettings(old.individualProgress(), old.hiddenMode(), !old.repeatable(), old.autoClaimRewards(), old.unlockNotification(), old.showPrerequisiteArrow());
            case AUTO_CLAIM -> new QuestSettings(old.individualProgress(), old.hiddenMode(), old.repeatable(), !old.autoClaimRewards(), old.unlockNotification(), old.showPrerequisiteArrow());
            case INDIVIDUAL -> new QuestSettings(!old.individualProgress(), old.hiddenMode(), old.repeatable(), old.autoClaimRewards(), old.unlockNotification(), old.showPrerequisiteArrow());
        };

        updateQuest(player, quest, next);
    }

    private void updateQuest(ServerPlayer player, QuestDefinition quest, QuestSettings settings) {
        QuestDefinition updated = new QuestDefinition(
                quest.schema(),
                quest.id(),
                quest.display(),
                settings,
                quest.prerequisites(),
                quest.connectionColors(),
                quest.connectionModes(),
                quest.hiddenConnections(),
                quest.tasks(),
                quest.rewards()
        );
        service.definitionStore().upsert(updated);
        service.definitionStore().saveNow(updated.id());
        service.postMutation(player);
    }

    private enum SettingSwitch {
        REPEATABLE,
        AUTO_CLAIM,
        INDIVIDUAL
    }
}
