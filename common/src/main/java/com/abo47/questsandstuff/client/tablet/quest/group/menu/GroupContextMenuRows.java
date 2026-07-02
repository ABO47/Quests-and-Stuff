package com.abo47.questsandstuff.client.tablet.quest.group.menu;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ActionTone;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextActionFactory;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuAnimationBridge;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.theme.tokens.TabletColors;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class GroupContextMenuRows {
    private GroupContextMenuRows() {
    }

    public static List<ContextAction> actions(GroupContextMenuLayout layout, TabletUiState state, Player player, Runnable refresh) {
        List<ContextAction> actions = new ArrayList<>();
        String target = layout.target();
        if (layout.hasTarget()) {
            actions.add(ContextActions.promoted(tr("ui.questsandstuff.menu.new_chapter"), "add", TabletColors.SUCCESS, () -> GroupContextMenuActions.addChapter(state, refresh)));
            actions.add(ContextActions.promotedRename(tr("ui.questsandstuff.menu.rename"), () -> GroupContextMenuActions.rename(state, target, refresh)));
            actions.add(ContextActions.changeIcon(() -> GroupContextMenuActions.changeIcon(state, target, refresh)));
            actions.add(ContextActions.promoted(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_BACKGROUND), "background", ActionTone.PRIMARY, () -> GroupContextMenuActions.changeBackground(state, target, refresh)));
        } else {
            actions.add(ContextActions.promoted(tr("ui.questsandstuff.menu.new_chapter"), "add", TabletColors.SUCCESS, () -> GroupContextMenuActions.addChapter(state, refresh)));
            return actions;
        }

        actions.add(ContextActions.action(tr("ui.questsandstuff.menu.text_style"), "style", TabletColors.INTERACTIVE, () -> GroupContextMenuActions.textStyle(state, target, refresh)));
        actions.add(ContextActions.action(tr("ui.questsandstuff.context.change_completion_hud_background"), "completion_hud_background", TabletColors.INTERACTIVE, () -> GroupContextMenuActions.changeCompletionHudBackground(state, target, refresh)));
        actions.add(ContextActions.action(tr("ui.questsandstuff.context.change_connection_texture"), "connect", TabletColors.INTERACTIVE, () -> GroupContextMenuActions.changeConnectionTexture(state, target, refresh)));
        if (chapterHasConnectionTexture(state, target)) {
            actions.add(ContextActions.action(tr("ui.questsandstuff.context.remove_connection_texture"), "delete", TabletColors.WARNING, () -> GroupContextMenuActions.removeConnectionTexture(player, state, target, refresh)));
        }
        actions.add(ContextActions.submenu(tr("ui.questsandstuff.context.change_completion_sound"), "audio-lines", TabletColors.INTERACTIVE, List.of(
                ContextActions.action(tr("ui.questsandstuff.context.use_game_sound"), "audio-lines", TabletColors.INTERACTIVE, () -> GroupContextMenuActions.changeCompletionSoundGame(state, target, refresh)),
                ContextActions.action(tr("ui.questsandstuff.context.use_custom_sound"), "audio-lines", TabletColors.INTERACTIVE, () -> GroupContextMenuActions.changeCompletionSoundCustom(state, target, refresh))
        )));
        boolean locked = ClientQuestCache.groupLockUntilUnlocked(target);
        boolean hidden = ClientQuestCache.groupHideUntilUnlocked(target);
        actions.add(ContextActions.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_VISIBILITY), "eye", TabletColors.INTERACTIVE, List.of(
                ContextActions.action(
                        tr(locked ? QuestTranslationKeys.CONTEXT_SHOW_CHAPTER_BEFORE_UNLOCKED : QuestTranslationKeys.CONTEXT_LOCK_CHAPTER_UNTIL_UNLOCKED),
                        locked ? "unlock_chapter" : "lock_chapter",
                        locked ? TabletColors.SUCCESS : TabletColors.INTERACTIVE,
                        () -> GroupContextMenuActions.setLockUntilUnlocked(player, state, target, !locked, refresh)
                ),
                ContextActions.action(
                        tr(hidden ? QuestTranslationKeys.CONTEXT_REVEAL_CHAPTER : QuestTranslationKeys.CONTEXT_HIDE_CHAPTER_UNTIL_UNLOCKED),
                        hidden ? "eye" : "eye-off",
                        hidden ? TabletColors.SUCCESS : TabletColors.WARNING,
                        () -> GroupContextMenuActions.setHideUntilUnlocked(player, state, target, !hidden, refresh)
                )
        )));

        if (layout.entityVariants()) {
            actions.add(ContextActions.changeVariant(() -> GroupContextMenuActions.changeVariant(state, target, refresh)));
        }
        if (layout.entityIcon()) {
            actions.add(ContextActions.editMotion(() -> GroupContextMenuActions.editMotion(state, target, refresh)));
        }
        actions.add(ContextActions.moveUp(() -> GroupContextMenuActions.move(player, state, target, -1, refresh)));
        actions.add(ContextActions.moveDown(() -> GroupContextMenuActions.move(player, state, target, 1, refresh)));
        String deleteLabel = TabletUiFactory.pendingDeleteLabel(state, GroupContextMenuLayout.deleteKey(target), tr("ui.questsandstuff.menu.delete"));
        actions.add(new ContextAction(deleteLabel, "delete", TabletColors.ERROR, false, true, () -> GroupContextMenuActions.delete(player, state, target, refresh)));
        if (!ClientQuestCache.groupIcon(target).isBlank()) {
            String removeIconLabel = TabletUiFactory.pendingDeleteLabel(state, GroupContextMenuLayout.removeIconKey(target), tr("ui.questsandstuff.menu.remove_icon"));
            actions.add(new ContextAction(removeIconLabel, "delete", TabletColors.WARNING, false, false, () -> GroupContextMenuActions.removeIcon(player, state, target, refresh)));
        }
        if (!"default".equals(ClientQuestCache.groupBackground(target))) {
            String removeCardBgLabel = TabletUiFactory.pendingDeleteLabel(state, GroupContextMenuLayout.removeBackgroundKey(target), tr("ui.questsandstuff.menu.remove_card_bg"));
            actions.add(new ContextAction(removeCardBgLabel, "delete", TabletColors.WARNING, false, false, () -> GroupContextMenuActions.removeBackground(player, state, target, refresh)));
        }
        return actions;
    }

    public static boolean click(GroupContextMenuLayout layout, TabletUiState state, Player player, Runnable refresh, int x, int y) {
        List<ContextAction> actions = actions(layout, state, player, refresh);
        return ContextMenuPanel.click(
                actions,
                0,
                ContextMenuPanel.rowActionCount(actions),
                layout.menuX(),
                layout.menuY(),
                layout.menuW(),
                x,
                y,
                state,
                null,
                ContextMenuAnimation.CHAPTER_KEY
        );
    }

    private static boolean chapterHasConnectionTexture(TabletUiState state, String target) {
        for (String questId : ClientQuestCache.questIdsInGroup(target)) {
            CompoundTag quest = ClientQuestCache.quest(questId);
            if (quest != null && quest.contains("connection_textures", Tag.TAG_COMPOUND)) {
                CompoundTag textures = quest.getCompound("connection_textures");
                if (!textures.isEmpty()) return true;
            }
        }
        for (var ec : state.canvas.canvasExclusiveChoicesByGroup.getOrDefault(target, List.of())) {
            if (!ec.connectionTextures().isEmpty()) return true;
        }
        return false;
    }

    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
