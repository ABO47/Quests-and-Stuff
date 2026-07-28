package com.abo47.questsandstuff.quest.editor.session;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.editor.ClipboardEditService;
import com.abo47.questsandstuff.quest.editor.blueprint.CanvasBlueprint;
import com.abo47.questsandstuff.quest.editor.canvas.CanvasEditService;
import com.abo47.questsandstuff.quest.editor.canvas.PrerequisiteEditService;
import com.abo47.questsandstuff.quest.editor.chapter.ChapterEditService;
import com.abo47.questsandstuff.quest.editor.clipboard.ClipboardSnapshot;
import com.abo47.questsandstuff.quest.editor.clipboard.QuestClipboardDebugLog;
import com.abo47.questsandstuff.quest.editor.quest.QuestContentEditService;
import com.abo47.questsandstuff.quest.editor.quest.QuestCrudHandler;
import com.abo47.questsandstuff.quest.editor.quest.QuestDisplayEditService;
import com.abo47.questsandstuff.quest.editor.quest.QuestSettingsEditService;
import com.abo47.questsandstuff.quest.editor.session.actions.EditorCanvasSessionActions;
import com.abo47.questsandstuff.quest.editor.session.actions.EditorChapterSessionActions;
import com.abo47.questsandstuff.quest.editor.session.actions.EditorQuestSessionActions;
import com.abo47.questsandstuff.quest.editor.session.actions.EditorUndoRedoActions;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import com.abo47.questsandstuff.quest.persistence.quest.QuestDefinitionStore;
import com.abo47.questsandstuff.quest.runtime.RuntimeEngine;
import com.abo47.questsandstuff.quest.sync.SyncService;

public final class EditorSessionService {
    private final QuestDefinitionStore definitionStore;
    private final RuntimeEngine runtimeEngine;
    private final SyncService syncService;
    private final ChapterEditService chapterEdits;
    private final CanvasEditService canvasEdits;
    private final ClipboardEditService clipboardEdits;
    private final PrerequisiteEditService prerequisiteEdits;
    private final QuestContentEditService questContentEdits;
    private final QuestCrudHandler questLifecycleEdits;
    private final QuestDisplayEditService questDisplayEdits;
    private final QuestSettingsEditService questSettingsEdits;
    private final EditorQuestSessionActions questActions;
    private final EditorChapterSessionActions chapterActions;
    private final EditorCanvasSessionActions canvasActions;
    private final EditorUndoRedoActions undoRedoActions;
    private final EditorSessionState sessionState;

    private final Map<UUID, EditorSession> sessions = new HashMap<>();

    public EditorSessionService(QuestDefinitionStore definitionStore, RuntimeEngine runtimeEngine, SyncService syncService) {
        this.definitionStore = definitionStore;
        this.runtimeEngine = runtimeEngine;
        this.syncService = syncService;
        this.chapterEdits = new ChapterEditService(this);
        this.canvasEdits = new CanvasEditService(this);
        this.clipboardEdits = new ClipboardEditService(this);
        this.prerequisiteEdits = new PrerequisiteEditService(this);
        this.questContentEdits = new QuestContentEditService(this);
        this.questLifecycleEdits = new QuestCrudHandler(this);
        this.questDisplayEdits = new QuestDisplayEditService(this);
        this.questSettingsEdits = new QuestSettingsEditService(this);
        this.questActions = new EditorQuestSessionActions(this, questContentEdits, questLifecycleEdits, questDisplayEdits, questSettingsEdits, prerequisiteEdits);
        this.chapterActions = new EditorChapterSessionActions(chapterEdits);
        this.canvasActions = new EditorCanvasSessionActions(canvasEdits, clipboardEdits);
        this.undoRedoActions = new EditorUndoRedoActions(this, definitionStore, runtimeEngine, syncService);
        this.sessionState = new EditorSessionState(definitionStore);
    }

    public String chapterLabel(ServerPlayer player) {
        return questActions.chapterLabel(player);
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

    public void nextChapter(ServerPlayer player) {
        questActions.nextChapter(player);
    }

    public void prevChapter(ServerPlayer player) {
        questActions.prevChapter(player);
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

    public void addQuest(ServerPlayer player, String preferredChapter) {
        questActions.addQuest(player, preferredChapter);
    }

    public void addQuest(ServerPlayer player, String preferredChapter, String preferredQuestId, int x, int y, String preferredTitle) {
        questActions.addQuest(player, preferredChapter, preferredQuestId, x, y, preferredTitle);
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
    public void createChapter(ServerPlayer player, String chapterName) {
        chapterActions.createChapter(player, chapterName);
    }

    public void deleteChapter(ServerPlayer player, String chapterName) {
        chapterActions.deleteChapter(player, chapterName);
    }

    public void moveChapter(ServerPlayer player, String chapterName, int offset) {
        chapterActions.moveChapter(player, chapterName, offset);
    }

    public void moveChapterToIndex(ServerPlayer player, String chapterName, int targetIndex) {
        chapterActions.moveChapterToIndex(player, chapterName, targetIndex);
    }

    public void renameChapter(ServerPlayer player, String fromName, String toName) {
        chapterActions.renameChapter(player, fromName, toName);
    }

    public void setChapterIcon(ServerPlayer player, String chapterName, String iconId) {
        chapterActions.setChapterIcon(player, chapterName, iconId);
    }

    public void setChapterBackground(ServerPlayer player, String chapterName, String backgroundId) {
        chapterActions.setChapterBackground(player, chapterName, backgroundId);
    }

    public void setChapterCanvasBackground(ServerPlayer player, String chapterName, String backgroundId) {
        chapterActions.setChapterCanvasBackground(player, chapterName, backgroundId);
    }

    public void setChapterTextAlign(ServerPlayer player, String chapterName, String align) {
        chapterActions.setChapterTextAlign(player, chapterName, align);
    }

    public void setChapterTextColor(ServerPlayer player, String chapterName, int color) {
        chapterActions.setChapterTextColor(player, chapterName, color);
    }

    public void setChapterTextStyle(ServerPlayer player, String chapterName, String style) {
        chapterActions.setChapterTextStyle(player, chapterName, style);
    }

    public void setChapterTextSize(ServerPlayer player, String chapterName, int size) {
        chapterActions.setChapterTextSize(player, chapterName, size);
    }

    public void setChapterLockUntilUnlocked(ServerPlayer player, String chapterName, boolean lockUntilUnlocked) {
        chapterActions.setChapterLockUntilUnlocked(player, chapterName, lockUntilUnlocked);
    }

    public void setChapterHideUntilUnlocked(ServerPlayer player, String chapterName, boolean hideUntilUnlocked) {
        chapterActions.setChapterHideUntilUnlocked(player, chapterName, hideUntilUnlocked);
    }

    public void putCanvasExclusiveChoice(ServerPlayer player, String chapterName, CanvasExclusiveChoice ec) {
        canvasActions.putCanvasExclusiveChoice(player, chapterName, ec);
    }

    public void putCanvasExclusiveChoices(ServerPlayer player, String chapterName, List<CanvasExclusiveChoice> ecs) {
        canvasActions.putCanvasExclusiveChoices(player, chapterName, ecs);
    }

    public void removeCanvasExclusiveChoice(ServerPlayer player, String chapterName, String ecId) {
        canvasActions.removeCanvasExclusiveChoice(player, chapterName, ecId);
    }

    public void ecConnectionHidden(ServerPlayer player, String chapterName, String sourceId, String targetId, boolean hidden) {
        canvasActions.ecConnectionHidden(player, chapterName, sourceId, targetId, hidden);
    }

    public void putCanvasImage(ServerPlayer player, String chapterName, CanvasImageLayer image) {
        canvasActions.putCanvasImage(player, chapterName, image);
    }

    public void removeCanvasImage(ServerPlayer player, String chapterName, String imageId) {
        canvasActions.removeCanvasImage(player, chapterName, imageId);
    }

    public void putCanvasText(ServerPlayer player, String chapterName, CanvasTextLayer text) {
        canvasActions.putCanvasText(player, chapterName, text);
    }

    public void removeCanvasText(ServerPlayer player, String chapterName, String textId) {
        canvasActions.removeCanvasText(player, chapterName, textId);
    }

    public void setCanvasLayerOrder(ServerPlayer player, String chapterName, List<String> layerOrder) {
        canvasActions.setCanvasLayerOrder(player, chapterName, layerOrder);
    }

    public void openChapter(ServerPlayer player, String chapterName) {
        questActions.openChapter(player, chapterName);
    }

    public void openQuest(ServerPlayer player, String questId) {
        questActions.openQuest(player, questId);
    }
    public void moveQuestsInChapter(ServerPlayer player, String chapterName, Map<String, int[]> positions) {
        canvasActions.moveQuestsInChapter(player, chapterName, positions);
    }

    public void scaleQuestsInChapter(ServerPlayer player, String chapterName, Map<String, Float> scales) {
        canvasActions.scaleQuestsInChapter(player, chapterName, scales);
    }
    public void copyQuestsToClipboard(ServerPlayer player, Set<String> questIds) {
        canvasActions.copyQuestsToClipboard(player, questIds);
    }

    public void copyQuestsToClipboard(ServerPlayer player, String chapterName, Set<String> questIds) {
        canvasActions.copyQuestsToClipboard(player, chapterName, questIds);
    }

    public void pasteClipboardInChapter(ServerPlayer player, String chapterName, int anchorX, int anchorY) {
        canvasActions.pasteClipboardInChapter(player, chapterName, anchorX, anchorY);
    }

    public void pasteBlueprintInChapter(ServerPlayer player, String chapterName, int anchorX, int anchorY, CanvasBlueprint blueprint) {
        canvasActions.pasteBlueprintInChapter(player, chapterName, anchorX, anchorY, blueprint);
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

    public void setConnectionTexture(ServerPlayer player, String questId, String prerequisiteId, String texture) {
        questActions.setConnectionTexture(player, questId, prerequisiteId, texture);
    }

    public void setConnectionTextures(ServerPlayer player, Map<String, Map<String, String>> questTextures) {
        questActions.setConnectionTextures(player, questTextures);
    }

    public void setConnectionTextureSpacing(ServerPlayer player, String questId, String prerequisiteId, int spacing) {
        questActions.setConnectionTextureSpacing(player, questId, prerequisiteId, spacing);
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

    public void toggleIndividual(ServerPlayer player) {
        questActions.toggleIndividual(player);
    }

    public void setQuestRepeatable(ServerPlayer player, String questId, boolean enabled) {
        questActions.setQuestRepeatable(player, questId, enabled);
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

    public void setQuestCompletionSound(ServerPlayer player, Set<String> questIds, String sound) {
        questActions.setQuestCompletionSound(player, questIds, sound);
    }

    public void setQuestCompletionSoundVolume(ServerPlayer player, String questId, int volume) {
        questActions.setQuestCompletionSoundVolume(player, questId, volume);
    }

    public void setQuestCompletionSoundVolume(ServerPlayer player, Set<String> questIds, int volume) {
        questActions.setQuestCompletionSoundVolume(player, questIds, volume);
    }

    public void setQuestCompletionHudBackground(ServerPlayer player, String questId, String background) {
        questActions.setQuestCompletionHudBackground(player, questId, background);
    }

    public void setQuestCompletionHudBackground(ServerPlayer player, Set<String> questIds, String background) {
        questActions.setQuestCompletionHudBackground(player, questIds, background);
    }

    public void setQuestBackground(ServerPlayer player, String questId, String background, boolean grayscale) {
        questActions.setQuestBackground(player, questId, background, grayscale);
    }

    public void setQuestBackground(ServerPlayer player, Set<String> questIds, String background, boolean grayscale) {
        questActions.setQuestBackground(player, questIds, background, grayscale);
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
        return definitionStore.quest(session.currentQuest);
    }

    public QuestDefinitionStore definitionStore() {
        return definitionStore;
    }

    public SyncService syncService() {
        return syncService;
    }

    public RuntimeEngine runtimeEngine() {
        return runtimeEngine;
    }

    public EditorSession session(ServerPlayer player) {
        return sessions.computeIfAbsent(player.getUUID(), ignored -> sessionState.createSession());
    }

    public void normalizeQuestSelection(EditorSession session) {
        sessionState.normalizeQuestSelection(session);
    }

    public List<String> chapters() {
        return sessionState.chapters();
    }

    public List<String> questIdsInChapter(String chapter) {
        return sessionState.questIdsInChapter(chapter);
    }

    public String nextQuestId(String chapter) {
        return sessionState.nextQuestId(chapter);
    }

    public String nextQuestId(String chapter, Set<String> reservedIds) {
        return sessionState.nextQuestId(chapter, reservedIds);
    }

    public void clipboardDebug(String message) {
        QuestClipboardDebugLog.append(definitionStore.clipboardDir(), message);
    }

    public void captureUndo(EditorSession session) {
        undoRedoActions.captureUndo(session);
    }

    public void capturePasteUndo(
            EditorSession session,
            Collection<String> questIds,
            Collection<String> imageIds,
            Collection<String> textIds,
            String chapter
    ) {
        undoRedoActions.capturePasteUndo(session, questIds, imageIds, textIds, chapter);
    }

    public void postMutation(ServerPlayer player) {
        undoRedoActions.postMutation(player);
    }

    public void postMutationDelta(ServerPlayer player, Set<String> changedQuestIds, Set<String> changedChapters) {
        undoRedoActions.postMutationDelta(player, changedQuestIds, changedChapters);
    }

    public void ensureChapterExists(String rawChapter) {
        sessionState.ensureChapterExists(rawChapter);
    }

    public static String normalizeChapter(String chapterName) {
        return chapterName.trim().replace('\\', '/').replaceAll("/{2,}", "/");
    }

    public static String normalizeQuestId(String questId) {
        return questId.trim().replace('\\', '/').replaceAll("/{2,}", "/");
    }

    public enum EditorMode {
        MOVE,
        ADD,
        CONNECT
    }

    public static final class EditorSession {
        public String currentChapter;
        public String currentQuest;
        public EditorMode mode = EditorMode.MOVE;
        public ClipboardSnapshot clipboardSnapshot = ClipboardSnapshot.empty();
        public final Deque<EditorUndoRedoActions.EditorHistoryEntry> undo = new ArrayDeque<>();
        public final Deque<EditorUndoRedoActions.EditorHistoryEntry> redo = new ArrayDeque<>();
    }
}
