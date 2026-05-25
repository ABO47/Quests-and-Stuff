package com.abo47.questsandstuff.quest.editor.chapter;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;


import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.quest.model.ChapterDefinition;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.QuestSettings;
import com.abo47.questsandstuff.quest.model.task.QuestVisibilityMode;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withGroups;
import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withSettings;

public final class ChapterEditService {
    private final EditorSessionService owner;

    public ChapterEditService(EditorSessionService owner) {
        this.owner = owner;
    }

    public void createGroup(ServerPlayer player, String groupName) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank()) {
            return;
        }
        owner.ensureGroupExists(group);
        owner.session(player).currentGroup = group;
        owner.postMutation(player);
    }

    public void deleteGroup(ServerPlayer player, String groupName) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank() || !owner.definitionStore().groupOrder().contains(group)) {
            return;
        }
        EditorSessionService.EditorSession session = owner.session(player);
        owner.captureUndo(session);

        List<String> groups = new ArrayList<>(owner.definitionStore().groupOrder());
        groups.remove(group);

        for (QuestDefinition quest : new ArrayList<>(owner.definitionStore().quests().values())) {
            if (!quest.display().groups().containsKey(group)) {
                continue;
            }
            Map<String, ChapterDefinition> map = new HashMap<>(quest.display().groups());
            map.remove(group);
            if (map.isEmpty()) {
                QuestsAndStuffMod.debugLog("[QnS:Editor] deleting quest without remaining chapters quest={} removedChapter={}", quest.id(), group);
                owner.definitionStore().remove(quest.id());
            } else {
                owner.definitionStore().upsert(withGroups(quest, map));
            }
        }
        owner.definitionStore().setGroupOrder(groups);
        session.currentGroup = groups.isEmpty() ? "" : groups.get(0);
        owner.normalizeQuestSelection(session);
        owner.postMutation(player);
    }

    public void moveGroup(ServerPlayer player, String groupName, int offset) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank() || offset == 0) {
            return;
        }
        List<String> groups = new ArrayList<>(owner.definitionStore().groupOrder());
        int index = groups.indexOf(group);
        if (index < 0) {
            return;
        }
        int next = Math.max(0, Math.min(groups.size() - 1, index + offset));
        if (next == index) {
            return;
        }
        groups.remove(index);
        groups.add(next, group);
        owner.definitionStore().setGroupOrder(groups);
        owner.session(player).currentGroup = group;
        owner.postMutation(player);
    }

    public void moveGroupToIndex(ServerPlayer player, String groupName, int targetIndex) {
        String group = EditorSessionService.normalizeGroup(groupName);
        if (group.isBlank()) {
            return;
        }
        List<String> groups = new ArrayList<>(owner.definitionStore().groupOrder());
        int index = groups.indexOf(group);
        if (index < 0) {
            return;
        }
        int next = Math.max(0, Math.min(groups.size() - 1, targetIndex));
        if (next == index) {
            return;
        }
        groups.remove(index);
        groups.add(next, group);
        owner.definitionStore().setGroupOrder(groups);
        owner.session(player).currentGroup = group;
        owner.postMutation(player);
    }

    public void renameGroup(ServerPlayer player, String fromName, String toName) {
        String from = EditorSessionService.normalizeGroup(fromName);
        String to = EditorSessionService.normalizeGroup(toName);
        if (from.isBlank() || to.isBlank() || from.equals(to)) {
            return;
        }
        List<String> groups = new ArrayList<>(owner.definitionStore().groupOrder());
        int index = groups.indexOf(from);
        if (index < 0 || groups.contains(to)) {
            return;
        }

        EditorSessionService.EditorSession session = owner.session(player);
        owner.captureUndo(session);

        groups.set(index, to);
        owner.definitionStore().setGroupOrder(groups);
        for (QuestDefinition quest : new ArrayList<>(owner.definitionStore().quests().values())) {
            if (!quest.display().groups().containsKey(from)) {
                continue;
            }
            Map<String, ChapterDefinition> map = new HashMap<>(quest.display().groups());
            ChapterDefinition view = map.remove(from);
            map.put(to, view == null ? ChapterDefinition.DEFAULT : view);
            owner.definitionStore().upsert(withGroups(quest, map));
        }

        if (from.equals(session.currentGroup)) {
            session.currentGroup = to;
        }
        owner.postMutation(player);
    }

    public void setGroupIcon(ServerPlayer player, String groupName, String iconId) {
        String group = validGroup(groupName);
        if (group.isBlank()) {
            return;
        }
        owner.definitionStore().setGroupIcon(group, iconId);
        owner.postMutation(player);
    }

    public void setGroupBackground(ServerPlayer player, String groupName, String backgroundId) {
        String group = validGroup(groupName);
        if (group.isBlank()) {
            return;
        }
        owner.definitionStore().setGroupBackground(group, backgroundId);
        owner.postMutation(player);
    }

    public void setGroupCanvasBackground(ServerPlayer player, String groupName, String backgroundId) {
        String group = validGroup(groupName);
        if (group.isBlank()) {
            return;
        }
        String value = backgroundId == null || backgroundId.isBlank() ? "default" : backgroundId.trim();
        QuestsAndStuffMod.debugLog("[QnS:Editor] chapter canvas background group={} background={}", group, value);
        owner.definitionStore().setGroupCanvasBackground(group, value);
        owner.postMutation(player);
    }

    public void setGroupTextAlign(ServerPlayer player, String groupName, String align) {
        String group = validGroup(groupName);
        if (group.isBlank()) {
            return;
        }
        owner.definitionStore().setGroupTextAlign(group, align);
        owner.postMutation(player);
    }

    public void setGroupTextColor(ServerPlayer player, String groupName, int color) {
        String group = validGroup(groupName);
        if (group.isBlank()) {
            return;
        }
        owner.definitionStore().setGroupTextColor(group, color);
        owner.postMutation(player);
    }

    public void setGroupTextStyle(ServerPlayer player, String groupName, String style) {
        String group = validGroup(groupName);
        if (group.isBlank()) {
            return;
        }
        owner.definitionStore().setGroupTextStyle(group, style);
        owner.postMutation(player);
    }

    public void setGroupTextSize(ServerPlayer player, String groupName, int size) {
        String group = validGroup(groupName);
        if (group.isBlank()) {
            return;
        }
        owner.definitionStore().setGroupTextSize(group, size);
        owner.postMutation(player);
    }

    public void setGroupLockUntilUnlocked(ServerPlayer player, String groupName, boolean lockUntilUnlocked) {
        String group = validGroup(groupName);
        if (group.isBlank() || owner.definitionStore().groupLockUntilUnlocked(group) == lockUntilUnlocked) {
            return;
        }
        EditorSessionService.EditorSession session = owner.session(player);
        owner.captureUndo(session);
        owner.definitionStore().setGroupLockUntilUnlocked(group, lockUntilUnlocked);

        QuestVisibilityMode mode = lockUntilUnlocked ? QuestVisibilityMode.LOCKED : QuestVisibilityMode.PREREQUISITES_VISIBLE;
        for (QuestDefinition quest : new ArrayList<>(owner.definitionStore().quests().values())) {
            if (!quest.display().groups().containsKey(group) || quest.settings().hiddenMode() == mode) {
                continue;
            }
            QuestSettings old = quest.settings();
            QuestSettings settings = new QuestSettings(
                    old.individualProgress(),
                    mode,
                    old.repeatable(),
                    old.autoClaimRewards(),
                    old.unlockNotification(),
                    old.showPrerequisiteArrow()
            );
            owner.definitionStore().upsert(withSettings(quest, settings));
        }
        owner.postMutation(player);
    }

    private String validGroup(String groupName) {
        String group = EditorSessionService.normalizeGroup(groupName);
        return group.isBlank() || !owner.definitionStore().groupOrder().contains(group) ? "" : group;
    }
}
