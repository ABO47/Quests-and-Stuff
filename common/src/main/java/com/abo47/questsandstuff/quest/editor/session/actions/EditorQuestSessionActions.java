package com.abo47.questsandstuff.quest.editor.session.actions;

import com.abo47.questsandstuff.quest.editor.canvas.PrerequisiteEditService;
import com.abo47.questsandstuff.quest.editor.quest.QuestContentEditService;
import com.abo47.questsandstuff.quest.editor.quest.QuestDisplayEditService;
import com.abo47.questsandstuff.quest.editor.quest.QuestLifecycleEditService;
import com.abo47.questsandstuff.quest.editor.quest.QuestSettingsEditService;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService.EditorMode;
import com.abo47.questsandstuff.quest.editor.session.EditorSessionService.EditorSession;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class EditorQuestSessionActions {
    private final EditorSessionService service;
    private final QuestContentEditService contentEdits;
    private final QuestLifecycleEditService lifecycleEdits;
    private final QuestDisplayEditService displayEdits;
    private final QuestSettingsEditService settingsEdits;
    private final PrerequisiteEditService prerequisiteEdits;

    public EditorQuestSessionActions(
            EditorSessionService service,
            QuestContentEditService contentEdits,
            QuestLifecycleEditService lifecycleEdits,
            QuestDisplayEditService displayEdits,
            QuestSettingsEditService settingsEdits,
            PrerequisiteEditService prerequisiteEdits
    ) {
        this.service = service;
        this.contentEdits = contentEdits;
        this.lifecycleEdits = lifecycleEdits;
        this.displayEdits = displayEdits;
        this.settingsEdits = settingsEdits;
        this.prerequisiteEdits = prerequisiteEdits;
    }

    public String groupLabel(ServerPlayer player) {
        EditorSession session = service.session(player);
        return "Group: " + session.currentGroup;
    }

    public String questLabel(ServerPlayer player) {
        EditorSession session = service.session(player);
        return "Quest: " + session.currentQuest;
    }

    public String modeLabel(ServerPlayer player) {
        return "Mode: " + service.session(player).mode.name();
    }

    public String settingsLabel(ServerPlayer player) {
        QuestDefinition quest = service.currentQuest(player);
        if (quest == null) {
            return "Settings: -";
        }
        return "R=" + yesNo(quest.settings().repeatable())
                + " A=" + yesNo(quest.settings().autoClaimRewards())
                + " I=" + yesNo(quest.settings().individualProgress())
                + " H=" + quest.settings().hiddenMode().name();
    }

    public void nextGroup(ServerPlayer player) {
        EditorSession session = service.session(player);
        List<String> groups = service.groups();
        if (groups.isEmpty()) {
            return;
        }
        int idx = groups.indexOf(session.currentGroup);
        idx = (idx + 1) % groups.size();
        session.currentGroup = groups.get(idx);
        service.normalizeQuestSelection(session);
    }

    public void prevGroup(ServerPlayer player) {
        EditorSession session = service.session(player);
        List<String> groups = service.groups();
        if (groups.isEmpty()) {
            return;
        }
        int idx = groups.indexOf(session.currentGroup);
        if (idx < 0) {
            idx = 0;
        }
        idx = (idx - 1 + groups.size()) % groups.size();
        session.currentGroup = groups.get(idx);
        service.normalizeQuestSelection(session);
    }

    public void nextQuest(ServerPlayer player) {
        EditorSession session = service.session(player);
        List<String> questIds = service.questIdsInGroup(session.currentGroup);
        if (questIds.isEmpty()) {
            return;
        }
        int idx = questIds.indexOf(session.currentQuest);
        idx = (idx + 1) % questIds.size();
        session.currentQuest = questIds.get(idx);
    }

    public void prevQuest(ServerPlayer player) {
        EditorSession session = service.session(player);
        List<String> questIds = service.questIdsInGroup(session.currentGroup);
        if (questIds.isEmpty()) {
            return;
        }
        int idx = questIds.indexOf(session.currentQuest);
        if (idx < 0) {
            idx = 0;
        }
        idx = (idx - 1 + questIds.size()) % questIds.size();
        session.currentQuest = questIds.get(idx);
    }

    public void cycleMode(ServerPlayer player) {
        EditorSession session = service.session(player);
        session.mode = switch (session.mode) {
            case MOVE -> EditorMode.ADD;
            case ADD -> EditorMode.CONNECT;
            case CONNECT -> EditorMode.MOVE;
        };
    }

    public void addQuest(ServerPlayer player) {
        lifecycleEdits.addQuest(player);
    }

    public void addQuest(ServerPlayer player, String preferredGroup) {
        lifecycleEdits.addQuest(player, preferredGroup);
    }

    public void addQuest(ServerPlayer player, String preferredGroup, String preferredQuestId, int x, int y, String preferredTitle) {
        lifecycleEdits.addQuest(player, preferredGroup, preferredQuestId, x, y, preferredTitle);
    }

    public void removeQuest(ServerPlayer player, String questId) {
        lifecycleEdits.removeQuest(player, questId);
    }

    public void openGroup(ServerPlayer player, String groupName) {
        lifecycleEdits.openGroup(player, groupName);
    }

    public void openQuest(ServerPlayer player, String questId) {
        lifecycleEdits.openQuest(player, questId);
    }

    public void updateQuestDisplay(ServerPlayer player, String questId, String title, String subtitle) {
        displayEdits.updateQuestDisplay(player, questId, title, subtitle);
    }

    public void updateQuestDescription(ServerPlayer player, String questId, List<String> description) {
        displayEdits.updateQuestDescription(player, questId, description);
    }

    public void setQuestIcon(ServerPlayer player, String questId, String icon) {
        displayEdits.setQuestIcon(player, questId, icon);
    }

    public void setQuestVisualHidden(ServerPlayer player, String questId, boolean hidden) {
        displayEdits.setQuestVisualHidden(player, questId, hidden);
    }

    public void setQuestCompletionSound(ServerPlayer player, String questId, String sound) {
        displayEdits.setQuestCompletionSound(player, questId, sound);
    }

    public void putQuestTask(ServerPlayer player, String questId, String taskJson) {
        contentEdits.putQuestTask(player, questId, taskJson);
    }

    public void removeQuestTask(ServerPlayer player, String questId, String taskId) {
        contentEdits.removeQuestTask(player, questId, taskId);
    }

    public void moveQuestTask(ServerPlayer player, String questId, String taskId, int offset) {
        contentEdits.moveQuestTask(player, questId, taskId, offset);
    }

    public void putQuestReward(ServerPlayer player, String questId, String rewardJson) {
        contentEdits.putQuestReward(player, questId, rewardJson);
    }

    public void removeQuestReward(ServerPlayer player, String questId, String rewardId) {
        contentEdits.removeQuestReward(player, questId, rewardId);
    }

    public void moveQuestReward(ServerPlayer player, String questId, String rewardId, int offset) {
        contentEdits.moveQuestReward(player, questId, rewardId, offset);
    }

    public void toggleRepeatable(ServerPlayer player) {
        settingsEdits.toggleRepeatable(player);
    }

    public void toggleAutoClaim(ServerPlayer player) {
        settingsEdits.toggleAutoClaim(player);
    }

    public void toggleIndividual(ServerPlayer player) {
        settingsEdits.toggleIndividual(player);
    }

    public void setQuestAutoClaim(ServerPlayer player, String questId, boolean enabled) {
        settingsEdits.setQuestAutoClaim(player, questId, enabled);
    }

    public void setQuestHiddenMode(ServerPlayer player, String questId, String mode) {
        settingsEdits.setQuestHiddenMode(player, questId, mode);
    }

    public void setQuestPrerequisite(ServerPlayer player, String questId, String prerequisiteId, boolean enabled) {
        prerequisiteEdits.setQuestPrerequisite(player, questId, prerequisiteId, enabled);
    }

    public void setConnectionMode(ServerPlayer player, String questId, String prerequisiteId, boolean gridMode) {
        prerequisiteEdits.setConnectionMode(player, questId, prerequisiteId, gridMode);
    }

    public void setConnectionHidden(ServerPlayer player, String questId, String prerequisiteId, boolean hidden) {
        prerequisiteEdits.setConnectionHidden(player, questId, prerequisiteId, hidden);
    }

    public void setConnectionColor(ServerPlayer player, String questId, String prerequisiteId, int color) {
        prerequisiteEdits.setConnectionColor(player, questId, prerequisiteId, color);
    }

    public void connectToNext(ServerPlayer player) {
        prerequisiteEdits.connectToNext(player);
    }

    public void disconnectFromNext(ServerPlayer player) {
        prerequisiteEdits.disconnectFromNext(player);
    }

    private static String yesNo(boolean value) {
        return value ? "Y" : "N";
    }
}
