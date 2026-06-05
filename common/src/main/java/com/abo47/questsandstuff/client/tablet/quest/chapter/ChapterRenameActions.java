package com.abo47.questsandstuff.client.tablet.quest.chapter;

import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
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
        String raw = EditorCommandClient.sanitizeGroupName(currentString);
        if (raw.isBlank()) {
            raw = tr("ui.questsandstuff.chapter.default_name");
        }
        String renamed = EditorCommandClient.uniqueGroupName(raw, group);
        if (!renamed.equals(group)) {
            EditorCommandClient.runGroupAction(player, state, "rename", group, renamed, 0);
        } else if (TabletUiFactory.DRAFT_CHAPTER.equals(group)) {
            EditorCommandClient.runGroupAction(player, state, "create", group, group, 0);
        }
        state.pendingChapterRename = "";
        state.selectedGroup = renamed;
        state.groupDraft = renamed;
        state.chapterDraftName = renamed;
        TabletUiFactory.persistUiState(state);
        refresh.run();
    }

    static void commitDraft(Player player, TabletUiState state, Runnable refresh, String currentString) {
        String typed = EditorCommandClient.sanitizeGroupName(currentString);
        if (typed.isBlank()) {
            typed = tr("ui.questsandstuff.chapter.default_name");
        }
        String created = EditorCommandClient.uniqueGroupName(typed, "");
        EditorCommandClient.runGroupAction(player, state, "create", created, created, 0);
        state.selectedGroup = created;
        state.groupDraft = created;
        state.chapterDraftName = created;
        state.pendingChapterRename = "";
        TabletUiFactory.persistUiState(state);
        refresh.run();
    }

    static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
