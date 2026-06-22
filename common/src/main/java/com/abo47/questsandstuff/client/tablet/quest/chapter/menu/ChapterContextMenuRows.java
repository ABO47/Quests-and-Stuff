package com.abo47.questsandstuff.client.tablet.quest.chapter.menu;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.context.ActionTone;
import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public final class ChapterContextMenuRows {
    private ChapterContextMenuRows() {
    }

    public static List<ContextAction> actions(ChapterContextMenuLayout layout, TabletUiState state, Player player, Runnable refresh) {
        List<ContextAction> actions = new ArrayList<>();
        String target = layout.target();
        if (layout.hasTarget()) {
            actions.add(ContextActions.promotedRename(tr("ui.questsandstuff.menu.rename"), () -> ChapterContextMenuActions.rename(state, target, refresh)));
            actions.add(ContextActions.changeIcon(() -> ChapterContextMenuActions.changeIcon(state, target, refresh)));
            actions.add(ContextActions.promoted(TabletVocabulary.text(QuestVocabulary.CONTEXT_CHANGE_BACKGROUND), "background", ActionTone.PRIMARY, () -> ChapterContextMenuActions.changeBackground(state, target, refresh)));
        }
        actions.add(ContextActions.add(tr("ui.questsandstuff.menu.new_chapter"), () -> ChapterContextMenuActions.addChapter(state, refresh)));
        if (!layout.hasTarget()) {
            return actions;
        }

        actions.add(ContextActions.action(tr("ui.questsandstuff.menu.text_style"), "style", ModColors.INTERACTIVE, () -> ChapterContextMenuActions.textStyle(state, target, refresh)));
        actions.add(ContextActions.action(tr("ui.questsandstuff.context.batch_completion_hud_background"), "completion_hud_background", ModColors.INTERACTIVE, () -> ChapterContextMenuActions.changeCompletionHudBackground(state, target, refresh)));
        actions.add(ContextActions.submenu(tr("ui.questsandstuff.context.batch_completion_sound"), "audio-lines", ModColors.INTERACTIVE, List.of(
                ContextActions.action(tr("ui.questsandstuff.context.use_game_sound"), "audio-lines", ModColors.INTERACTIVE, () -> ChapterContextMenuActions.changeCompletionSoundGame(state, target, refresh)),
                ContextActions.action(tr("ui.questsandstuff.context.use_custom_sound"), "audio-lines", ModColors.INTERACTIVE, () -> ChapterContextMenuActions.changeCompletionSoundCustom(state, target, refresh))
        )));
        boolean locked = ClientQuestCache.groupLockUntilUnlocked(target);
        boolean hidden = ClientQuestCache.groupHideUntilUnlocked(target);
        actions.add(ContextActions.submenu(TabletVocabulary.text(QuestVocabulary.CONTEXT_VISIBILITY), "eye", ModColors.INTERACTIVE, List.of(
                ContextActions.action(
                        tr(locked ? QuestVocabulary.CONTEXT_SHOW_CHAPTER_BEFORE_UNLOCKED : QuestVocabulary.CONTEXT_LOCK_CHAPTER_UNTIL_UNLOCKED),
                        locked ? "unlock_chapter" : "lock_chapter",
                        locked ? ModColors.SUCCESS : ModColors.INTERACTIVE,
                        () -> ChapterContextMenuActions.setLockUntilUnlocked(player, state, target, !locked, refresh)
                ),
                ContextActions.action(
                        tr(hidden ? QuestVocabulary.CONTEXT_REVEAL_CHAPTER : QuestVocabulary.CONTEXT_HIDE_CHAPTER_UNTIL_UNLOCKED),
                        hidden ? "eye" : "eye-off",
                        hidden ? ModColors.SUCCESS : ModColors.WARNING,
                        () -> ChapterContextMenuActions.setHideUntilUnlocked(player, state, target, !hidden, refresh)
                )
        )));

        if (layout.entityVariants()) {
            actions.add(ContextActions.changeVariant(() -> ChapterContextMenuActions.changeVariant(state, target, refresh)));
        }
        if (layout.entityIcon()) {
            actions.add(ContextActions.editMotion(() -> ChapterContextMenuActions.editMotion(state, target, refresh)));
        }
        actions.add(ContextActions.moveUp(() -> ChapterContextMenuActions.move(player, state, target, -1, refresh)));
        actions.add(ContextActions.moveDown(() -> ChapterContextMenuActions.move(player, state, target, 1, refresh)));
        String deleteLabel = TabletUiFactory.pendingDeleteLabel(state, ChapterContextMenuLayout.deleteKey(target), tr("ui.questsandstuff.menu.delete"));
        actions.add(new ContextAction(deleteLabel, "delete", ModColors.ERROR, false, true, () -> ChapterContextMenuActions.delete(player, state, target, refresh)));
        if (!ClientQuestCache.groupIcon(target).isBlank()) {
            String removeIconLabel = TabletUiFactory.pendingDeleteLabel(state, ChapterContextMenuLayout.removeIconKey(target), tr("ui.questsandstuff.menu.remove_icon"));
            actions.add(new ContextAction(removeIconLabel, "delete", ModColors.WARNING, false, false, () -> ChapterContextMenuActions.removeIcon(player, state, target, refresh)));
        }
        if (!"default".equals(ClientQuestCache.groupBackground(target))) {
            String removeCardBgLabel = TabletUiFactory.pendingDeleteLabel(state, ChapterContextMenuLayout.removeBackgroundKey(target), tr("ui.questsandstuff.menu.remove_card_bg"));
            actions.add(new ContextAction(removeCardBgLabel, "delete", ModColors.WARNING, false, false, () -> ChapterContextMenuActions.removeBackground(player, state, target, refresh)));
        }
        return actions;
    }

    public static boolean click(ChapterContextMenuLayout layout, TabletUiState state, Player player, Runnable refresh, int x, int y) {
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

    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
