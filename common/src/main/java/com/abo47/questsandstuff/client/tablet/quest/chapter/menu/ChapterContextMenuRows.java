package com.abo47.questsandstuff.client.tablet.quest.chapter.menu;

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

public final class ChapterContextMenuRows {
    private ChapterContextMenuRows() {
    }

    public static List<ContextAction> actions(ChapterContextMenuLayout layout, TabletUiState state, Player player, Runnable refresh) {
        List<ContextAction> actions = new ArrayList<>();
        String target = layout.target();
        if (layout.hasTarget()) {
            actions.add(ContextActionFactory.promoted(tr("ui.questsandstuff.menu.new_chapter"), "add", TabletColors.SUCCESS, () -> ChapterContextMenuActions.addChapter(state, refresh)));
            actions.add(ContextActionFactory.promotedRename(tr("ui.questsandstuff.menu.rename"), () -> ChapterContextMenuActions.rename(state, target, refresh)));
            actions.add(ContextActionFactory.changeIcon(() -> ChapterContextMenuActions.changeIcon(state, target, refresh)));
            actions.add(ContextActionFactory.promoted(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_CHANGE_BACKGROUND), "background", ActionTone.PRIMARY, () -> ChapterContextMenuActions.changeBackground(state, target, refresh)));
        } else {
            actions.add(ContextActionFactory.promoted(tr("ui.questsandstuff.menu.new_chapter"), "add", TabletColors.SUCCESS, () -> ChapterContextMenuActions.addChapter(state, refresh)));
            return actions;
        }

        actions.add(ContextActionFactory.action(tr("ui.questsandstuff.menu.text_style"), "style", TabletColors.INTERACTIVE, () -> ChapterContextMenuActions.textStyle(state, target, refresh)));
        actions.add(ContextActionFactory.action(tr("ui.questsandstuff.context.change_completion_hud_background"), "completion_hud_background", TabletColors.INTERACTIVE, () -> ChapterContextMenuActions.changeCompletionHudBackground(state, target, refresh)));
        actions.add(ContextActionFactory.action(tr("ui.questsandstuff.context.change_connection_texture"), "connect", TabletColors.INTERACTIVE, () -> ChapterContextMenuActions.changeConnectionTexture(state, target, refresh)));
        if (chapterHasConnectionTexture(state, target)) {
            actions.add(ContextActionFactory.action(tr("ui.questsandstuff.context.remove_connection_texture"), "delete", TabletColors.WARNING, () -> ChapterContextMenuActions.removeConnectionTexture(player, state, target, refresh)));
        }
        actions.add(ContextActionFactory.submenu(tr("ui.questsandstuff.context.change_completion_sound"), "audio-lines", TabletColors.INTERACTIVE, List.of(
                ContextActionFactory.action(tr("ui.questsandstuff.context.use_game_sound"), "audio-lines", TabletColors.INTERACTIVE, () -> ChapterContextMenuActions.changeCompletionSoundGame(state, target, refresh)),
                ContextActionFactory.action(tr("ui.questsandstuff.context.use_custom_sound"), "audio-lines", TabletColors.INTERACTIVE, () -> ChapterContextMenuActions.changeCompletionSoundCustom(state, target, refresh))
        )));
        boolean locked = ClientQuestStateFacade.chapterLockUntilUnlocked(target);
        boolean hidden = ClientQuestStateFacade.chapterHideUntilUnlocked(target);
        actions.add(ContextActionFactory.submenu(TabletTranslationKeys.text(QuestTranslationKeys.CONTEXT_VISIBILITY), "eye", TabletColors.INTERACTIVE, List.of(
                ContextActionFactory.action(
                        tr(locked ? QuestTranslationKeys.CONTEXT_SHOW_CHAPTER_BEFORE_UNLOCKED : QuestTranslationKeys.CONTEXT_LOCK_CHAPTER_UNTIL_UNLOCKED),
                        locked ? "unlock_chapter" : "lock_chapter",
                        locked ? TabletColors.SUCCESS : TabletColors.INTERACTIVE,
                        () -> ChapterContextMenuActions.setLockUntilUnlocked(player, state, target, !locked, refresh)
                ),
                ContextActionFactory.action(
                        tr(hidden ? QuestTranslationKeys.CONTEXT_REVEAL_CHAPTER : QuestTranslationKeys.CONTEXT_HIDE_CHAPTER_UNTIL_UNLOCKED),
                        hidden ? "eye" : "eye-off",
                        hidden ? TabletColors.SUCCESS : TabletColors.WARNING,
                        () -> ChapterContextMenuActions.setHideUntilUnlocked(player, state, target, !hidden, refresh)
                )
        )));

        if (layout.entityVariants()) {
            actions.add(ContextActionFactory.changeVariant(() -> ChapterContextMenuActions.changeVariant(state, target, refresh)));
        }
        if (layout.entityIcon()) {
            actions.add(ContextActionFactory.editMotion(() -> ChapterContextMenuActions.editMotion(state, target, refresh)));
        }
        actions.add(ContextActionFactory.moveUp(() -> ChapterContextMenuActions.move(player, state, target, -1, refresh)));
        actions.add(ContextActionFactory.moveDown(() -> ChapterContextMenuActions.move(player, state, target, 1, refresh)));
        String deleteLabel = TabletUiFactory.pendingDeleteLabel(state, ChapterContextMenuLayout.deleteKey(target), tr("ui.questsandstuff.menu.delete"));
        actions.add(new ContextAction(deleteLabel, "delete", TabletColors.ERROR, false, true, () -> ChapterContextMenuActions.delete(player, state, target, refresh)));
        if (!ClientQuestStateFacade.chapterIcon(target).isBlank()) {
            String removeIconLabel = TabletUiFactory.pendingDeleteLabel(state, ChapterContextMenuLayout.removeIconKey(target), tr("ui.questsandstuff.menu.remove_icon"));
            actions.add(new ContextAction(removeIconLabel, "delete", TabletColors.WARNING, false, false, () -> ChapterContextMenuActions.removeIcon(player, state, target, refresh)));
        }
        if (!"default".equals(ClientQuestStateFacade.chapterBackground(target))) {
            String removeCardBgLabel = TabletUiFactory.pendingDeleteLabel(state, ChapterContextMenuLayout.removeBackgroundKey(target), tr("ui.questsandstuff.menu.remove_card_bg"));
            actions.add(new ContextAction(removeCardBgLabel, "delete", TabletColors.WARNING, false, false, () -> ChapterContextMenuActions.removeBackground(player, state, target, refresh)));
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
                ContextMenuAnimationBridge.CHAPTER_KEY
        );
    }

    private static boolean chapterHasConnectionTexture(TabletUiState state, String target) {
        for (String questId : ClientQuestStateFacade.questIdsInChapter(target)) {
            CompoundTag quest = ClientQuestStateFacade.quest(questId);
            if (quest != null && quest.contains("connection_textures", Tag.TAG_COMPOUND)) {
                CompoundTag textures = quest.getCompound("connection_textures");
                if (!textures.isEmpty()) return true;
            }
        }
        for (var ec : state.canvas.canvasExclusiveChoicesByChapter.getOrDefault(target, List.of())) {
            if (!ec.connectionTextures().isEmpty()) return true;
        }
        return false;
    }

    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
