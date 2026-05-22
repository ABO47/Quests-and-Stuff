package com.abo47.questsandstuff.quest.editor.session;

import com.abo47.questsandstuff.quest.editor.chapter.ChapterEditService;
import com.abo47.questsandstuff.quest.editor.ClipboardEditService;
import com.abo47.questsandstuff.quest.editor.canvas.CanvasEditService;
import com.abo47.questsandstuff.quest.editor.canvas.PrerequisiteEditService;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardSnapshot;
import com.abo47.questsandstuff.quest.editor.quest.QuestContentEditService;
import com.abo47.questsandstuff.quest.editor.quest.QuestDisplayEditService;
import com.abo47.questsandstuff.quest.editor.quest.QuestLifecycleEditService;
import com.abo47.questsandstuff.quest.editor.quest.QuestSettingsEditService;
import com.abo47.questsandstuff.quest.editor.session.actions.EditorCanvasSessionActions;
import com.abo47.questsandstuff.quest.editor.session.actions.EditorChapterSessionActions;
import com.abo47.questsandstuff.quest.editor.session.actions.EditorQuestSessionActions;
import com.abo47.questsandstuff.quest.editor.session.actions.EditorUndoRedoActions;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.runtime.QuestRuntimeEngine;
import com.abo47.questsandstuff.quest.sync.QuestSyncService;
import com.abo47.questsandstuff.util.QuestClipboardDebugLog;
import com.abo47.questsandstuff.util.QuestNaming;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class EditorSessionService {
    private final QuestDefinitionStore definitionStore;
    private final QuestRuntimeEngine runtimeEngine;
    private final QuestSyncService syncService;
    private final ChapterEditService chapterEdits;
    private final CanvasEditService canvasEdits;
    private final ClipboardEditService clipboardEdits;
    private final PrerequisiteEditService prerequisiteEdits;
    private final QuestContentEditService questContentEdits;
    private final QuestLifecycleEditService questLifecycleEdits;
    private final QuestDisplayEditService questDisplayEdits;
    private final QuestSettingsEditService questSettingsEdits;
    private final EditorQuestSessionActions questActions;
    private final EditorChapterSessionActions chapterActions;
    private final EditorCanvasSessionActions canvasActions;
    private final EditorUndoRedoActions undoRedoActions;

    private final Map<UUID, EditorSession> sessions = new HashMap<>();

    public EditorSessionService(QuestDefinitionStore definitionStore, QuestRuntimeEngine runtimeEngine, QuestSyncService syncService) {
        this.definitionStore = definitionStore;
        this.runtimeEngine = runtimeEngine;
        this.syncService = syncService;
        this.chapterEdits = new ChapterEditService(this);
        this.canvasEdits = new CanvasEditService(this);
        this.clipboardEdits = new ClipboardEditService(this);
        this.prerequisiteEdits = new PrerequisiteEditService(this);
        this.questContentEdits = new QuestContentEditService(this);
        this.questLifecycleEdits = new QuestLifecycleEditService(this);
        this.questDisplayEdits = new QuestDisplayEditService(this);
        this.questSettingsEdits = new QuestSettingsEditService(this);
        this.questActions = new EditorQuestSessionActions(this, questContentEdits, questLifecycleEdits, questDisplayEdits, questSettingsEdits, prerequisiteEdits);
        this.chapterActions = new EditorChapterSessionActions(chapterEdits);
        this.canvasActions = new EditorCanvasSessionActions(canvasEdits, clipboardEdits);
        this.undoRedoActions = new EditorUndoRedoActions(this, definitionStore, runtimeEngine, syncService);
    }

    public String groupLabel(ServerPlayer player) {
        return questActions.groupLabel(player);
    }

    public String questLabel(ServerPlayer player) {
        return questActions.questLabel(player);
    }

    public String modeLabel(ServerPlayer player) {
        return questActions.modeLabel(player);
    }

    public String settingsLabel(ServerPlayer player) {
        return questActions.settingsLabel(player);
    }

    public void nextGroup(ServerPlayer player) {
        questActions.nextGroup(player);
    }

    public void prevGroup(ServerPlayer player) {
        questActions.prevGroup(player);
    }

    public void nextQuest(ServerPlayer player) {
        questActions.nextQuest(player);
    }

    public void prevQuest(ServerPlayer player) {
        questActions.prevQuest(player);
    }

    public void cycleMode(ServerPlayer player) {
        questActions.cycleMode(player);
    }

    public void addQuest(ServerPlayer player) {
        questActions.addQuest(player);
    }

    public void addQuest(ServerPlayer player, String preferredGroup) {
        questActions.addQuest(player, preferredGroup);
    }

    public void addQuest(ServerPlayer player, String preferredGroup, String preferredQuestId, int x, int y, String preferredTitle) {
        questActions.addQuest(player, preferredGroup, preferredQuestId, x, y, preferredTitle);
    }

    public void updateQuestDisplay(ServerPlayer player, String questId, String title, String subtitle) {
        questActions.updateQuestDisplay(player, questId, title, subtitle);
    }

    public void updateQuestDescription(ServerPlayer player, String questId, List<String> description) {
        questActions.updateQuestDescription(player, questId, description);
    }

    public void setQuestIcon(ServerPlayer player, String questId, String icon) {
        questActions.setQuestIcon(player, questId, icon);
    }

    public void putQuestTask(ServerPlayer player, String questId, String taskJson) {
        questActions.putQuestTask(player, questId, taskJson);
    }

    public void removeQuestTask(ServerPlayer player, String questId, String taskId) {
        questActions.removeQuestTask(player, questId, taskId);
    }

    public void moveQuestTask(ServerPlayer player, String questId, String taskId, int offset) {
        questActions.moveQuestTask(player, questId, taskId, offset);
    }

    public void putQuestReward(ServerPlayer player, String questId, String rewardJson) {
        questActions.putQuestReward(player, questId, rewardJson);
    }

    public void removeQuestReward(ServerPlayer player, String questId, String rewardId) {
        questActions.removeQuestReward(player, questId, rewardId);
    }

    public void moveQuestReward(ServerPlayer player, String questId, String rewardId, int offset) {
        questActions.moveQuestReward(player, questId, rewardId, offset);
    }

    public void removeQuest(ServerPlayer player, String questId) {
        questActions.removeQuest(player, questId);
    }
    public void createGroup(ServerPlayer player, String groupName) {
        chapterActions.createGroup(player, groupName);
    }

    public void deleteGroup(ServerPlayer player, String groupName) {
        chapterActions.deleteGroup(player, groupName);
    }

    public void moveGroup(ServerPlayer player, String groupName, int offset) {
        chapterActions.moveGroup(player, groupName, offset);
    }

    public void moveGroupToIndex(ServerPlayer player, String groupName, int targetIndex) {
        chapterActions.moveGroupToIndex(player, groupName, targetIndex);
    }

    public void renameGroup(ServerPlayer player, String fromName, String toName) {
        chapterActions.renameGroup(player, fromName, toName);
    }

    public void setGroupIcon(ServerPlayer player, String groupName, String iconId) {
        chapterActions.setGroupIcon(player, groupName, iconId);
    }

    public void setGroupBackground(ServerPlayer player, String groupName, String backgroundId) {
        chapterActions.setGroupBackground(player, groupName, backgroundId);
    }

    public void setGroupCanvasBackground(ServerPlayer player, String groupName, String backgroundId) {
        chapterActions.setGroupCanvasBackground(player, groupName, backgroundId);
    }

    public void setGroupTextAlign(ServerPlayer player, String groupName, String align) {
        chapterActions.setGroupTextAlign(player, groupName, align);
    }

    public void setGroupTextColor(ServerPlayer player, String groupName, int color) {
        chapterActions.setGroupTextColor(player, groupName, color);
    }

    public void setGroupTextStyle(ServerPlayer player, String groupName, String style) {
        chapterActions.setGroupTextStyle(player, groupName, style);
    }

    public void setGroupTextSize(ServerPlayer player, String groupName, int size) {
        chapterActions.setGroupTextSize(player, groupName, size);
    }
    public void putCanvasImage(ServerPlayer player, String groupName, CanvasImageLayer image) {
        canvasActions.putCanvasImage(player, groupName, image);
    }

    public void removeCanvasImage(ServerPlayer player, String groupName, String imageId) {
        canvasActions.removeCanvasImage(player, groupName, imageId);
    }

    public void putCanvasText(ServerPlayer player, String groupName, CanvasTextLayer text) {
        canvasActions.putCanvasText(player, groupName, text);
    }

    public void removeCanvasText(ServerPlayer player, String groupName, String textId) {
        canvasActions.removeCanvasText(player, groupName, textId);
    }

    public void setCanvasLayerOrder(ServerPlayer player, String groupName, List<String> layerOrder) {
        canvasActions.setCanvasLayerOrder(player, groupName, layerOrder);
    }

    public void openGroup(ServerPlayer player, String groupName) {
        questActions.openGroup(player, groupName);
    }

    public void openQuest(ServerPlayer player, String questId) {
        questActions.openQuest(player, questId);
    }
    public void moveQuestsInGroup(ServerPlayer player, String groupName, Map<String, int[]> positions) {
        canvasActions.moveQuestsInGroup(player, groupName, positions);
    }

    public void scaleQuestsInGroup(ServerPlayer player, String groupName, Map<String, Float> scales) {
        canvasActions.scaleQuestsInGroup(player, groupName, scales);
    }
    public void copyQuestsToClipboard(ServerPlayer player, Set<String> questIds) {
        canvasActions.copyQuestsToClipboard(player, questIds);
    }

    public void copyQuestsToClipboard(ServerPlayer player, String groupName, Set<String> questIds) {
        canvasActions.copyQuestsToClipboard(player, groupName, questIds);
    }

    public void pasteClipboardInGroup(ServerPlayer player, String groupName, int anchorX, int anchorY) {
        canvasActions.pasteClipboardInGroup(player, groupName, anchorX, anchorY);
    }
    public void setQuestPrerequisite(ServerPlayer player, String questId, String prerequisiteId, boolean enabled) {
        questActions.setQuestPrerequisite(player, questId, prerequisiteId, enabled);
    }

    public void setConnectionMode(ServerPlayer player, String questId, String prerequisiteId, boolean gridMode) {
        questActions.setConnectionMode(player, questId, prerequisiteId, gridMode);
    }

    public void setConnectionHidden(ServerPlayer player, String questId, String prerequisiteId, boolean hidden) {
        questActions.setConnectionHidden(player, questId, prerequisiteId, hidden);
    }

    public void setConnectionColor(ServerPlayer player, String questId, String prerequisiteId, int color) {
        questActions.setConnectionColor(player, questId, prerequisiteId, color);
    }

    public void connectToNext(ServerPlayer player) {
        questActions.connectToNext(player);
    }

    public void disconnectFromNext(ServerPlayer player) {
        questActions.disconnectFromNext(player);
    }

    public void toggleRepeatable(ServerPlayer player) {
        questActions.toggleRepeatable(player);
    }

    public void toggleAutoClaim(ServerPlayer player) {
        questActions.toggleAutoClaim(player);
    }

    public void toggleIndividual(ServerPlayer player) {
        questActions.toggleIndividual(player);
    }

    public void setQuestAutoClaim(ServerPlayer player, String questId, boolean enabled) {
        questActions.setQuestAutoClaim(player, questId, enabled);
    }

    public void setQuestHiddenMode(ServerPlayer player, String questId, String mode) {
        questActions.setQuestHiddenMode(player, questId, mode);
    }

    public void setQuestVisualHidden(ServerPlayer player, String questId, boolean hidden) {
        questActions.setQuestVisualHidden(player, questId, hidden);
    }

    public void setQuestCompletionSound(ServerPlayer player, String questId, String sound) {
        questActions.setQuestCompletionSound(player, questId, sound);
    }

    public void undo(ServerPlayer player) {
        undoRedoActions.undo(player);
    }

    public void redo(ServerPlayer player) {
        undoRedoActions.redo(player);
    }

    public void saveAll(ServerPlayer player) {
        undoRedoActions.saveAll(player);
    }

    public QuestDefinition currentQuest(ServerPlayer player) {
        EditorSession session = session(player);
        return definitionStore.quests().get(session.currentQuest);
    }

    public QuestDefinitionStore definitionStore() {
        return definitionStore;
    }

    public QuestSyncService syncService() {
        return syncService;
    }

    public QuestRuntimeEngine runtimeEngine() {
        return runtimeEngine;
    }

    public EditorSession session(ServerPlayer player) {
        return sessions.computeIfAbsent(player.getUUID(), ignored -> createSession());
    }

    private EditorSession createSession() {
        EditorSession session = new EditorSession();
        List<String> groups = groups();
        session.currentGroup = groups.isEmpty() ? "" : groups.get(0);
        normalizeQuestSelection(session);
        return session;
    }

    public void normalizeQuestSelection(EditorSession session) {
        List<String> questIds = questIdsInGroup(session.currentGroup);
        if (questIds.isEmpty()) {
            session.currentQuest = "-";
            return;
        }
        if (!questIds.contains(session.currentQuest)) {
            session.currentQuest = questIds.get(0);
        }
    }

    public List<String> groups() {
        return new ArrayList<>(definitionStore.groupOrder());
    }

    public List<String> questIdsInGroup(String group) {
        return definitionStore.quests().values().stream()
                .filter(quest -> quest.display().groups().containsKey(group))
                .map(QuestDefinition::id)
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    public String nextQuestId(String group) {
        return nextQuestId(group, definitionStore.quests().keySet());
    }

    public String nextQuestId(String group, Set<String> reservedIds) {
        Set<String> reserved = new HashSet<>(definitionStore.quests().keySet());
        if (reservedIds != null) {
            reserved.addAll(reservedIds);
        }
        return QuestNaming.nextQuestId(group, reserved);
    }

    public void clipboardDebug(String message) {
        QuestClipboardDebugLog.append(definitionStore.clipboardDir(), message);
    }

    public void captureUndo(EditorSession session) {
        undoRedoActions.captureUndo(session);
    }

    public void postMutation(ServerPlayer player) {
        undoRedoActions.postMutation(player);
    }

    public void ensureGroupExists(String rawGroup) {
        String group = normalizeGroup(rawGroup);
        if (group.isBlank()) {
            return;
        }
        List<String> groups = new ArrayList<>(definitionStore.groupOrder());
        if (groups.contains(group)) {
            return;
        }
        groups.add(group);
        definitionStore.setGroupOrder(groups);
    }

    public static String normalizeGroup(String groupName) {
        return groupName == null ? "" : groupName.trim();
    }

    public static String normalizeQuestId(String questId) {
        return questId == null ? "" : questId.trim();
    }

    public enum EditorMode {
        MOVE,
        ADD,
        CONNECT
    }

    public static final class EditorSession {
        public String currentGroup;
        public String currentQuest;
        public EditorMode mode = EditorMode.MOVE;
        public ClipboardSnapshot clipboardSnapshot = ClipboardSnapshot.empty();
        public final Deque<Map<String, QuestDefinition>> undo = new ArrayDeque<>();
        public final Deque<Map<String, QuestDefinition>> redo = new ArrayDeque<>();
    }
}
