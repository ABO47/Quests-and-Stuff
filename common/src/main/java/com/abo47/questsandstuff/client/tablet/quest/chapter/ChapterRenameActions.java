package com.abo47.questsandstuff.client.tablet.quest.chapter;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.client.tablet.quest.editor.EditorChapterCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

final class ChapterRenameActions {
    private ChapterRenameActions() {
    }

    static String sanitizeInlineTitle(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }

    static void commitRename(Player player, TabletUiState state, Runnable refresh, String chapter, String currentString) {
        String raw = EditorChapterCommandClient.sanitizeChapterName(currentString);
        if (raw.isBlank()) {
            raw = tr("ui.questsandstuff.chapter.default_name");
        }
        String renamed = EditorChapterCommandClient.uniqueChapterName(raw, chapter);
        if (!renamed.equals(chapter)) {
            EditorChapterCommandClient.runChapterAction(player, state, "rename", chapter, renamed, 0);
        } else if (TabletUiFactory.DRAFT_CHAPTER.equals(chapter)) {
            EditorChapterCommandClient.runChapterAction(player, state, "create", chapter, chapter, 0);
        }
        state.canvas.pendingChapterRename = "";
        state.root.selectedChapter = renamed;
        state.chapterPanel.chapterDraft = renamed;
        state.chapterPanel.chapterDraftName = renamed;
        TabletUiFactory.persistUiState(state);
        refresh.run();
    }

    static void commitDraft(Player player, TabletUiState state, Runnable refresh, String currentString) {
        String typed = EditorChapterCommandClient.sanitizeChapterName(currentString);
        if (typed.isBlank()) {
            typed = tr("ui.questsandstuff.chapter.default_name");
        }
        String created = EditorChapterCommandClient.uniqueChapterName(typed, "");
        EditorChapterCommandClient.runChapterAction(player, state, "create", created, created, 0);
        state.root.selectedChapter = created;
        state.chapterPanel.chapterDraft = created;
        state.chapterPanel.chapterDraftName = created;
        state.canvas.pendingChapterRename = "";
        TabletUiFactory.persistUiState(state);
        refresh.run();
    }

    static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
