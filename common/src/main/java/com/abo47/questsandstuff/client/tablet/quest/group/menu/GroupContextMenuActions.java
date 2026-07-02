package com.abo47.questsandstuff.client.tablet.quest.group.menu;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.quest.canvas.render.ConnectionRenderer;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorGroupCommandClient;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCanvasCommandClient;
import com.abo47.questsandstuff.client.tablet.controls.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.modal.ModalOpenActions;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargets;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class GroupContextMenuActions {
    private GroupContextMenuActions() {
    }

    public static void rename(TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        state.chapterPanel.chapterMenuOpen = false;
        state.chapterPanel.chapterMenuTarget = target;
        state.canvas.pendingChapterRename = target;
        state.chapterPanel.chapterDraftName = target;
        state.root.selectedGroup = target;
        state.chapterPanel.groupDraft = target;
        TabletUiFactory.persistUiState(state);
        refresh.run();
    }

    public static void addChapter(TabletUiState state, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        String drafted = EditorGroupCommandClient.uniqueGroupName(tr("ui.questsandstuff.chapter.default_name"), "");
        state.canvas.pendingChapterRename = TabletUiFactory.DRAFT_CHAPTER;
        state.chapterPanel.chapterDraftName = drafted;
        state.chapterPanel.groupDraft = drafted;
        state.chapterPanel.chapterMenuOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter add draft opened name={}", drafted);
        refresh.run();
    }

    public static void delete(Player player, TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        String deleteKey = GroupContextMenuLayout.deleteKey(target);
        if (!TabletUiFactory.confirmDeleteClick(state, deleteKey)) {
            QuestsAndStuffMod.debugLog("[QnS:UI] chapter delete armed target={}", target);
            refresh.run();
            return;
        }
        EditorGroupCommandClient.runGroupAction(player, state, "delete", target, "", 0);
        if (target.equals(state.root.selectedGroup)) {
            state.root.selectedGroup = ClientQuestCache.groupOrder().isEmpty() ? "" : ClientQuestCache.groupOrder().get(0);
            state.chapterPanel.groupDraft = state.root.selectedGroup;
        }
        TabletUiFactory.persistUiState(state);
        state.chapterPanel.chapterMenuOpen = false;
        refresh.run();
    }

    public static void changeIcon(TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        EntityIconControls.openIconPicker(state, EntityIconControls.IconPickerTarget.chapter(target));
        state.chapterPanel.chapterMenuOpen = false;
        refresh.run();
    }

    public static void changeVariant(TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        EntityIconControls.openVariantPicker(state, ModalTargets.chapterIcon(target), GroupContextMenuLayout.chapterIcon(target));
        state.chapterPanel.chapterMenuOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter icon entity variant picker open chapter={}", target);
        refresh.run();
    }

    public static void editMotion(TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        EntityMotionEditor.openChapterIcon(state, target, state.chapterPanel.chapterMenuX, state.chapterPanel.chapterMenuY);
        state.chapterPanel.chapterMenuOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter icon motion editor requested chapter={}", target);
        refresh.run();
    }

    public static void removeIcon(Player player, TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        if (!EntityIconControls.confirmRemoveIcon(state, GroupContextMenuLayout.removeIconKey(target))) {
            refresh.run();
            return;
        }
        EditorGroupCommandClient.runGroupAction(player, state, "set_icon", target, "", 0);
        state.chapterPanel.chapterMenuOpen = false;
        refresh.run();
    }

    public static void changeBackground(TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        ModalOpenActions.openChapterBackgroundPicker(state, target, ClientQuestCache.groupBackground(target));
        state.chapterPanel.chapterMenuOpen = false;
        refresh.run();
    }

    public static void removeBackground(Player player, TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        if (!TabletUiFactory.confirmDeleteClick(state, GroupContextMenuLayout.removeBackgroundKey(target))) {
            refresh.run();
            return;
        }
        EditorGroupCommandClient.runGroupAction(player, state, "set_background", target, "default", 0);
        state.chapterPanel.chapterMenuOpen = false;
        refresh.run();
    }

    public static void changeCompletionHudBackground(TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        List<String> questIds = ClientQuestCache.questIdsInGroup(target);
        if (questIds.isEmpty()) {
            return;
        }
        String currentBackground = firstQuestCompletionHud(questIds);
        ModalOpenActions.openBatchQuestCompletionHudBackgroundPicker(state, questIds, currentBackground);
        state.chapterPanel.chapterMenuOpen = false;
        refresh.run();
    }

    public static void changeCompletionSoundGame(TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        List<String> questIds = ClientQuestCache.questIdsInGroup(target);
        if (questIds.isEmpty()) {
            return;
        }
        String currentSound = firstQuestCompletionSound(questIds);
        ModalOpenActions.openBatchQuestGameSoundPicker(state, questIds, currentSound);
        state.chapterPanel.chapterMenuOpen = false;
        refresh.run();
    }

    public static void changeCompletionSoundCustom(TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        List<String> questIds = ClientQuestCache.questIdsInGroup(target);
        if (questIds.isEmpty()) {
            return;
        }
        String currentSound = firstQuestCompletionSound(questIds);
        ModalOpenActions.openBatchQuestCustomCompletionSoundPicker(state, questIds, currentSound);
        state.chapterPanel.chapterMenuOpen = false;
        refresh.run();
    }

    public static void changeConnectionTexture(TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        List<String> questIds = ClientQuestCache.questIdsInGroup(target);
        List<String> targets = new java.util.ArrayList<>();
        targets.addAll(questIds);
        for (var ec : state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(target, java.util.List.of())) {
            targets.add(ec.id());
        }
        if (targets.isEmpty()) {
            return;
        }
        ModalOpenActions.openChapterConnectionTexturePicker(state, target, targets);
        state.chapterPanel.chapterMenuOpen = false;
        refresh.run();
    }

    public static void removeConnectionTexture(Player player, TabletUiState state, String target, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        List<String> questIds = ClientQuestCache.questIdsInGroup(target);
        String group = target;
        for (String questId : questIds) {
            CompoundTag quest = ClientQuestCache.quest(questId);
            if (quest == null) continue;
            ListTag prereqs = quest.getList("prerequisites", Tag.TAG_STRING);
            for (int i = 0; i < prereqs.size(); i++) {
                String prereqId = prereqs.getString(i);
                EditorCanvasCommandClient.runConnectionTextureAction(player, questId, prereqId, "");
                ConnectionRenderer.setConnectionTexture(state, group, prereqId, questId, "");
            }
        }
        for (var ec : state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(target, java.util.List.of())) {
            for (String connectedId : ec.connectionQuestIds()) {
                EditorCanvasCommandClient.runEcConnectionTextureAction(state, ec.id(), connectedId, "");
            }
            for (String prereqId : ec.prerequisiteQuestIds()) {
                EditorCanvasCommandClient.runEcConnectionTextureAction(state, ec.id(), prereqId, "");
            }
        }
        state.chapterPanel.chapterMenuOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter remove connection textures target={} quests={}", target, questIds.size());
        refresh.run();
    }

    private static String firstQuestCompletionHud(List<String> questIds) {
        return firstQuestField(questIds, "completion_hud_background");
    }

    private static String firstQuestCompletionSound(List<String> questIds) {
        return firstQuestField(questIds, "completion_sound");
    }

    private static String firstQuestField(List<String> questIds, String field) {
        if (questIds == null || questIds.isEmpty()) {
            return "";
        }
        CompoundTag quest = ClientQuestCache.quest(questIds.get(0));
        return quest == null ? "" : quest.getString(field);
    }

    public static void setLockUntilUnlocked(Player player, TabletUiState state, String target, boolean enabled, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        EditorGroupCommandClient.runGroupAction(player, state, "set_lock_until_unlocked", target, Boolean.toString(enabled), 0);
        state.chapterPanel.chapterMenuOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter context action=lock_until_unlocked chapter={} enabled={}", target, enabled);
        refresh.run();
    }

    public static void setHideUntilUnlocked(Player player, TabletUiState state, String target, boolean enabled, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        EditorGroupCommandClient.runGroupAction(player, state, "set_hide_until_unlocked", target, Boolean.toString(enabled), 0);
        state.chapterPanel.chapterMenuOpen = false;
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter context action=hide_until_unlocked chapter={} enabled={}", target, enabled);
        refresh.run();
    }

    public static void textStyle(TabletUiState state, String target, Runnable refresh) {
        state.chapterPanel.chapterTextMenuOpen = true;
        state.chapterPanel.chapterTextMenuTarget = target;
        state.chapterPanel.chapterMenuOpen = false;
        refresh.run();
    }

    public static void move(Player player, TabletUiState state, String target, int offset, Runnable refresh) {
        if (!EditorGroupCommandClient.canManageGroups(state)) {
            return;
        }
        EditorGroupCommandClient.runGroupAction(player, state, "move", target, "", offset);
        state.chapterPanel.chapterMenuOpen = false;
        refresh.run();
    }

    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
