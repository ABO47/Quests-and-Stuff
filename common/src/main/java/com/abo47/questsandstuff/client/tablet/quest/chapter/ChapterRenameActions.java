package com.abo47.questsandstuff.client.tablet.quest.chapter;

import com.abo47.questsandstuff.client.tablet.quest.editor.EditorChapterCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;

final class ChapterRenameActions {
    private ChapterRenameActions() {
    }

    static String sanitizeInlineTitle(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }

    static void commitRename(Player player, TabletUiState state, Runnable refresh, String group, String currentString) {
        String raw = EditorChapterCommandClient.sanitizeGroupName(currentString);
        if (raw.isBlank()) {
            raw = tr("ui.questsandstuff.chapter.default_name");
        }
        String renamed = EditorChapterCommandClient.uniqueGroupName(raw, group);
        if (!renamed.equals(group)) {
            EditorChapterCommandClient.runGroupAction(player, state, "rename", group, renamed, 0);
        } else if (TabletUiFactory.DRAFT_CHAPTER.equals(group)) {
            EditorChapterCommandClient.runGroupAction(player, state, "create", group, group, 0);
        }
        state.canvas.pendingChapterRename = "";
        state.root.selectedGroup = renamed;
        state.chapterPanel.groupDraft = renamed;
        state.chapterPanel.chapterDraftName = renamed;
        TabletUiFactory.persistUiState(state);
        refresh.run();
    }

    static void commitDraft(Player player, TabletUiState state, Runnable refresh, String currentString) {
        String typed = EditorChapterCommandClient.sanitizeGroupName(currentString);
        if (typed.isBlank()) {
            typed = tr("ui.questsandstuff.chapter.default_name");
        }
        String created = EditorChapterCommandClient.uniqueGroupName(typed, "");
        EditorChapterCommandClient.runGroupAction(player, state, "create", created, created, 0);
        state.root.selectedGroup = created;
        state.chapterPanel.groupDraft = created;
        state.chapterPanel.chapterDraftName = created;
        state.canvas.pendingChapterRename = "";
        TabletUiFactory.persistUiState(state);
        refresh.run();
    }

    static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
