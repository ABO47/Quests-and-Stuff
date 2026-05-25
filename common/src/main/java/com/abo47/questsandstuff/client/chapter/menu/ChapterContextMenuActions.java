package com.abo47.questsandstuff.client.chapter.menu;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;

public final class ChapterContextMenuActions {
    private ChapterContextMenuActions() {
    }

    public static void rename(TabletUiState state, String target, Runnable refresh) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        state.chapterMenuOpen = false;
        state.chapterMenuTarget = target;
        state.pendingChapterRename = target;
        state.chapterDraftName = target;
        state.selectedGroup = target;
        state.groupDraft = target;
        TabletUiFactory.persistUiState(state);
        refresh.run();
    }

    public static void addChapter(TabletUiState state, Runnable refresh) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        String drafted = EditorCommandClient.uniqueGroupName(tr("ui.questsandstuff.chapter.default_name"), "");
        state.pendingChapterRename = TabletUiFactory.DRAFT_CHAPTER;
        state.chapterDraftName = drafted;
        state.groupDraft = drafted;
        state.chapterMenuOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter add draft opened name={}", drafted);
        refresh.run();
    }

    public static void delete(Player player, TabletUiState state, String target, Runnable refresh) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        String deleteKey = ChapterContextMenuLayout.deleteKey(target);
        if (!TabletUiFactory.confirmDeleteClick(state, deleteKey)) {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter delete armed target={}", target);
            refresh.run();
            return;
        }
        EditorCommandClient.runGroupAction(player, state, "delete", target, "", 0);
        if (target.equals(state.selectedGroup)) {
            state.selectedGroup = ClientQuestCache.groupOrder().isEmpty() ? "" : ClientQuestCache.groupOrder().get(0);
            state.groupDraft = state.selectedGroup;
        }
        TabletUiFactory.persistUiState(state);
        state.chapterMenuOpen = false;
        refresh.run();
    }

    public static void changeIcon(TabletUiState state, String target, Runnable refresh) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        EntityIconControls.openIconPicker(state, EntityIconControls.IconPickerTarget.chapter(target));
        state.chapterMenuOpen = false;
        refresh.run();
    }

    public static void changeVariant(TabletUiState state, String target, Runnable refresh) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        EntityIconControls.openVariantPicker(state, ModalTargets.chapterIcon(target), ChapterContextMenuLayout.chapterIcon(target));
        state.chapterMenuOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter icon entity variant picker open chapter={}", target);
        refresh.run();
    }

    public static void editMotion(TabletUiState state, String target, Runnable refresh) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        EntityMotionEditor.openChapterIcon(state, target, state.chapterMenuX, state.chapterMenuY);
        state.chapterMenuOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter icon motion editor requested chapter={}", target);
        refresh.run();
    }

    public static void removeIcon(Player player, TabletUiState state, String target, Runnable refresh) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        if (!EntityIconControls.confirmRemoveIcon(state, ChapterContextMenuLayout.removeIconKey(target))) {
            refresh.run();
            return;
        }
        EditorCommandClient.runGroupAction(player, state, "set_icon", target, "", 0);
        state.chapterMenuOpen = false;
        refresh.run();
    }

    public static void changeBackground(TabletUiState state, String target, Runnable refresh) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        ModalOpenActions.openChapterBackgroundPicker(state, target, ClientQuestCache.groupBackground(target));
        state.chapterMenuOpen = false;
        refresh.run();
    }

    public static void removeBackground(Player player, TabletUiState state, String target, Runnable refresh) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        if (!TabletUiFactory.confirmDeleteClick(state, ChapterContextMenuLayout.removeBackgroundKey(target))) {
            refresh.run();
            return;
        }
        EditorCommandClient.runGroupAction(player, state, "set_background", target, "default", 0);
        state.chapterMenuOpen = false;
        refresh.run();
    }

    public static void setLockUntilUnlocked(Player player, TabletUiState state, String target, boolean enabled, Runnable refresh) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        EditorCommandClient.runGroupAction(player, state, "set_lock_until_unlocked", target, Boolean.toString(enabled), 0);
        state.chapterMenuOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter context action=lock_until_unlocked chapter={} enabled={}", target, enabled);
        refresh.run();
    }

    public static void setHideUntilUnlocked(Player player, TabletUiState state, String target, boolean enabled, Runnable refresh) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        EditorCommandClient.runGroupAction(player, state, "set_hide_until_unlocked", target, Boolean.toString(enabled), 0);
        state.chapterMenuOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter context action=hide_until_unlocked chapter={} enabled={}", target, enabled);
        refresh.run();
    }

    public static void textStyle(TabletUiState state, String target, Runnable refresh) {
        state.chapterTextMenuOpen = true;
        state.chapterTextMenuTarget = target;
        state.chapterMenuOpen = false;
        refresh.run();
    }

    public static void move(Player player, TabletUiState state, String target, int offset, Runnable refresh) {
        if (!EditorCommandClient.canManageGroups(state)) {
            return;
        }
        EditorCommandClient.runGroupAction(player, state, "move", target, "", offset);
        state.chapterMenuOpen = false;
        refresh.run();
    }

    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
