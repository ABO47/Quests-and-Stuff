package com.abo47.questsandstuff.client.chapter.menu;

import com.abo47.questsandstuff.client.tablet.context.ContextAction;
import com.abo47.questsandstuff.client.tablet.context.ContextActions;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.context.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
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
        }
        actions.add(ContextActions.add(tr("ui.questsandstuff.menu.new_chapter"), () -> ChapterContextMenuActions.addChapter(state, refresh)));
        if (!layout.hasTarget()) {
            return actions;
        }

        String deleteLabel = TabletUiFactory.pendingDeleteLabel(state, ChapterContextMenuLayout.deleteKey(target), tr("ui.questsandstuff.menu.delete"));
        actions.add(new ContextAction(deleteLabel, "delete", ModColors.ERROR, false, true, () -> ChapterContextMenuActions.delete(player, state, target, refresh)));
        actions.add(ContextActions.changeIcon(() -> ChapterContextMenuActions.changeIcon(state, target, refresh)));
        if (layout.entityVariants()) {
            actions.add(ContextActions.changeVariant(() -> ChapterContextMenuActions.changeVariant(state, target, refresh)));
        }
        if (layout.entityIcon()) {
            actions.add(ContextActions.editMotion(() -> ChapterContextMenuActions.editMotion(state, target, refresh)));
        }

        actions.add(new ContextAction(
                EntityIconControls.pendingRemoveIconLabel(state, ChapterContextMenuLayout.removeIconKey(target), tr("ui.questsandstuff.menu.remove_icon")),
                "delete",
                ModColors.WARNING,
                false,
                () -> ChapterContextMenuActions.removeIcon(player, state, target, refresh)
        ));
        actions.add(ContextActions.action(tr("ui.questsandstuff.menu.change_card_bg"), "background", ModColors.INTERACTIVE, () -> ChapterContextMenuActions.changeBackground(state, target, refresh)));
        actions.add(new ContextAction(
                TabletUiFactory.pendingDeleteLabel(state, ChapterContextMenuLayout.removeBackgroundKey(target), tr("ui.questsandstuff.menu.remove_card_bg")),
                "delete",
                ModColors.WARNING,
                false,
                () -> ChapterContextMenuActions.removeBackground(player, state, target, refresh)
        ));
        actions.add(ContextActions.action(tr("ui.questsandstuff.menu.text_style"), "style", ModColors.INTERACTIVE, () -> ChapterContextMenuActions.textStyle(state, target, refresh)));
        actions.add(ContextActions.moveUp(() -> ChapterContextMenuActions.move(player, state, target, -1, refresh)));
        actions.add(ContextActions.moveDown(() -> ChapterContextMenuActions.move(player, state, target, 1, refresh)));
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
