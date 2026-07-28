package com.abo47.questsandstuff.quest.editor.canvas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.minecraft.server.level.ServerPlayer;

import com.abo47.questsandstuff.quest.editor.session.EditorSessionService;
import com.abo47.questsandstuff.quest.model.ChapterDef;
import com.abo47.questsandstuff.quest.model.QuestDefinition;
import com.abo47.questsandstuff.quest.model.canvas.CanvasExclusiveChoice;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;

import static com.abo47.questsandstuff.quest.editor.quest.QuestDefinitionEdits.withChapters;

public final class CanvasEditService {
    private final EditorSessionService owner;

    public CanvasEditService(EditorSessionService owner) {
        this.owner = owner;
    }

    public void putCanvasExclusiveChoice(ServerPlayer player, String chapterName, CanvasExclusiveChoice ec) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank() || ec == null || ec.id().isBlank()) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().putCanvasExclusiveChoice(chapter, ec);
        owner.postMutation(player);
    }

    public void putCanvasExclusiveChoices(ServerPlayer player, String chapterName, List<CanvasExclusiveChoice> ecs) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank() || ecs == null || ecs.isEmpty()) {
            return;
        }
        EditorSessionService.EditorSession session = owner.session(player);
        boolean changed = false;
        for (CanvasExclusiveChoice ec : ecs) {
            if (ec == null || ec.id().isBlank()) {
                continue;
            }
            if (!changed) {
                owner.captureUndo(session);
                changed = true;
            }
            owner.definitionStore().putCanvasExclusiveChoice(chapter, ec);
        }
        if (changed) {
            owner.postMutation(player);
        }
    }

    public void removeCanvasExclusiveChoice(ServerPlayer player, String chapterName, String ecId) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank() || ecId == null || ecId.isBlank()) {
            return;
        }
        CanvasExclusiveChoice removed = owner.definitionStore().canvasExclusiveChoices(chapter).stream()
                .filter(ec -> ec.id().equals(ecId))
                .findFirst()
                .orElse(null);
        owner.captureUndo(owner.session(player));
        if (owner.definitionStore().removeCanvasExclusiveChoice(chapter, ecId)) {
            if (removed != null && !removed.connectionQuestIds().isEmpty()) {
                owner.runtimeEngine().clearExclusiveChoiceDisabled(new HashSet<>(removed.connectionQuestIds()));
            }
            owner.postMutation(player);
        }
    }

    public void ecConnectionHidden(ServerPlayer player, String chapterName, String sourceId, String targetId, boolean hidden) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank() || sourceId.isBlank() || targetId.isBlank()) {
            return;
        }
        CanvasExclusiveChoice ec = owner.definitionStore().canvasExclusiveChoices(chapter).stream()
                .filter(e -> e.id().equals(sourceId))
                .findFirst()
                .orElse(null);
        if (ec == null) {
            ec = owner.definitionStore().canvasExclusiveChoices(chapter).stream()
                    .filter(e -> e.id().equals(targetId))
                    .findFirst()
                    .orElse(null);
            if (ec == null) return;
            owner.captureUndo(owner.session(player));
            owner.definitionStore().putCanvasExclusiveChoice(chapter, ec.withHiddenConnection(sourceId, hidden));
        } else {
            owner.captureUndo(owner.session(player));
            owner.definitionStore().putCanvasExclusiveChoice(chapter, ec.withHiddenConnection(targetId, hidden));
        }
        owner.postMutation(player);
    }

    public void putCanvasImage(ServerPlayer player, String chapterName, CanvasImageLayer image) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank() || image == null || image.id().isBlank()) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().putCanvasImage(chapter, image);
        owner.postMutation(player);
    }

    public void removeCanvasImage(ServerPlayer player, String chapterName, String imageId) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank() || imageId == null || imageId.isBlank()) {
            return;
        }
        owner.captureUndo(owner.session(player));
        if (owner.definitionStore().removeCanvasImage(chapter, imageId)) {
            owner.postMutation(player);
        }
    }

    public void putCanvasText(ServerPlayer player, String chapterName, CanvasTextLayer text) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank() || text == null || text.id().isBlank()) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().putCanvasText(chapter, text);
        owner.postMutation(player);
    }

    public void removeCanvasText(ServerPlayer player, String chapterName, String textId) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank() || textId == null || textId.isBlank()) {
            return;
        }
        owner.captureUndo(owner.session(player));
        if (owner.definitionStore().removeCanvasText(chapter, textId)) {
            owner.postMutation(player);
        }
    }

    public void setCanvasLayerOrder(ServerPlayer player, String chapterName, List<String> layerOrder) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank()) {
            return;
        }
        owner.captureUndo(owner.session(player));
        owner.definitionStore().setCanvasLayerOrder(chapter, layerOrder);
        owner.postMutation(player);
    }

    public void moveQuestsInChapter(ServerPlayer player, String chapterName, Map<String, int[]> positions) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank() || positions == null || positions.isEmpty()) {
            return;
        }

        EditorSessionService.EditorSession session = owner.session(player);
        boolean changed = false;
        for (Map.Entry<String, int[]> entry : positions.entrySet()) {
            String questId = entry.getKey();
            int[] coords = entry.getValue();
            if (questId == null || questId.isBlank() || coords == null || coords.length < 2) {
                continue;
            }
            QuestDefinition source = owner.definitionStore().quests().get(questId);
            if (source == null) {
                continue;
            }
            ChapterDef existingView = source.display().chapters().get(chapter);
            if (existingView == null) {
                continue;
            }
            int targetX = coords[0];
            int targetY = coords[1];
            if (existingView.x() == targetX && existingView.y() == targetY) {
                continue;
            }

            if (!changed) {
                owner.captureUndo(session);
                changed = true;
            }

            Map<String, ChapterDef> chapters = new HashMap<>(source.display().chapters());
            chapters.put(chapter, new ChapterDef(existingView.visible(), targetX, targetY, existingView.scale()));
            owner.definitionStore().upsert(withChapters(source, chapters));
        }

        if (changed) {
            owner.postMutation(player);
        }
    }

    public void scaleQuestsInChapter(ServerPlayer player, String chapterName, Map<String, Float> scales) {
        String chapter = EditorSessionService.normalizeChapter(chapterName);
        if (chapter.isBlank() || scales == null || scales.isEmpty()) {
            return;
        }

        EditorSessionService.EditorSession session = owner.session(player);
        boolean changed = false;
        for (Map.Entry<String, Float> entry : scales.entrySet()) {
            String questId = entry.getKey();
            Float scaleValue = entry.getValue();
            if (questId == null || questId.isBlank() || scaleValue == null) {
                continue;
            }
            QuestDefinition source = owner.definitionStore().quests().get(questId);
            if (source == null) {
                continue;
            }
            ChapterDef existingView = source.display().chapters().get(chapter);
            if (existingView == null) {
                continue;
            }
            float targetScale = scaleValue;
            if (Float.isNaN(targetScale) || Float.isInfinite(targetScale)) {
                continue;
            }
            targetScale = Math.max(0.5f, targetScale);
            if (existingView.scale() == targetScale) {
                continue;
            }

            if (!changed) {
                owner.captureUndo(session);
                changed = true;
            }

            Map<String, ChapterDef> chapters = new HashMap<>(source.display().chapters());
            chapters.put(chapter, new ChapterDef(existingView.visible(), existingView.x(), existingView.y(), targetScale));
            owner.definitionStore().upsert(withChapters(source, chapters));
        }

        if (changed) {
            owner.postMutation(player);
        }
    }
}
