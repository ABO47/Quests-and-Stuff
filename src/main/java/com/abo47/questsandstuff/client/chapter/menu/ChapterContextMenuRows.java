package com.abo47.questsandstuff.client.chapter.menu;

import com.abo47.questsandstuff.client.tablet.context.ContextMenuAnimation;
import com.abo47.questsandstuff.client.tablet.entity.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;

public final class ChapterContextMenuRows {
    private ChapterContextMenuRows() {
    }

    public static void addRows(WidgetGroup menu, ChapterContextMenuLayout layout, TabletUiState state, Player player, Runnable refresh) {
        int rowY = ChapterContextMenuLayout.ROW_TOP_PAD;
        String target = layout.target();
        int rowW = layout.menuW() - 8;
        if (layout.hasTarget()) {
            TabletUiFactory.addWindowsContextRow(menu, rowY, rowW, tr("ui.questsandstuff.menu.rename"), "rename",
                    click -> runMenuAction(state, () -> ChapterContextMenuActions.rename(state, target, refresh)));
            rowY += ChapterContextMenuLayout.ROW_STEP;
        }
        TabletUiFactory.addWindowsContextRow(menu, rowY, rowW, tr("ui.questsandstuff.menu.new_chapter"), "add",
                click -> runMenuAction(state, () -> ChapterContextMenuActions.addChapter(state, refresh)));
        rowY += ChapterContextMenuLayout.ROW_STEP;
        if (!layout.hasTarget()) {
            return;
        }

        TabletUiFactory.addWindowsContextRow(menu, rowY, rowW,
                TabletUiFactory.pendingDeleteLabel(state, ChapterContextMenuLayout.deleteKey(target), tr("ui.questsandstuff.menu.delete")),
                "delete",
                click -> runMenuAction(state, () -> ChapterContextMenuActions.delete(player, state, target, refresh)));
        rowY += ChapterContextMenuLayout.ROW_STEP;

        TabletUiFactory.addWindowsContextRow(menu, rowY, rowW, tr("ui.questsandstuff.menu.change_icon"), "icon",
                click -> runMenuAction(state, () -> ChapterContextMenuActions.changeIcon(state, target, refresh)));
        rowY += ChapterContextMenuLayout.ROW_STEP;
        if (layout.entityVariants()) {
            TabletUiFactory.addWindowsContextRow(menu, rowY, rowW, "Change variant", "variant",
                    click -> runMenuAction(state, () -> ChapterContextMenuActions.changeVariant(state, target, refresh)));
            rowY += ChapterContextMenuLayout.ROW_STEP;
        }
        if (layout.entityIcon()) {
            TabletUiFactory.addWindowsContextRow(menu, rowY, rowW, "Edit motion", "motion",
                    click -> runMenuAction(state, () -> ChapterContextMenuActions.editMotion(state, target, refresh)));
            rowY += ChapterContextMenuLayout.ROW_STEP;
        }

        TabletUiFactory.addWindowsContextRow(menu, rowY, rowW,
                EntityIconControls.pendingRemoveIconLabel(state, ChapterContextMenuLayout.removeIconKey(target), tr("ui.questsandstuff.menu.remove_icon")),
                "delete",
                click -> runMenuAction(state, () -> ChapterContextMenuActions.removeIcon(player, state, target, refresh)));
        rowY += ChapterContextMenuLayout.ROW_STEP;
        TabletUiFactory.addWindowsContextRow(menu, rowY, rowW, tr("ui.questsandstuff.menu.change_card_bg"), "background",
                click -> runMenuAction(state, () -> ChapterContextMenuActions.changeBackground(state, target, refresh)));
        rowY += ChapterContextMenuLayout.ROW_STEP;
        TabletUiFactory.addWindowsContextRow(menu, rowY, rowW,
                TabletUiFactory.pendingDeleteLabel(state, ChapterContextMenuLayout.removeBackgroundKey(target), tr("ui.questsandstuff.menu.remove_card_bg")),
                "delete",
                click -> runMenuAction(state, () -> ChapterContextMenuActions.removeBackground(player, state, target, refresh)));
        rowY += ChapterContextMenuLayout.ROW_STEP;
        TabletUiFactory.addWindowsContextRow(menu, rowY, rowW, tr("ui.questsandstuff.menu.text_style"), "style",
                click -> runMenuAction(state, () -> ChapterContextMenuActions.textStyle(state, target, refresh)));
        rowY += ChapterContextMenuLayout.ROW_STEP;
        TabletUiFactory.addWindowsContextRow(menu, rowY, rowW, tr("ui.questsandstuff.menu.move_up"), "up",
                click -> runMenuAction(state, () -> ChapterContextMenuActions.move(player, state, target, -1, refresh)));
        rowY += ChapterContextMenuLayout.ROW_STEP;
        TabletUiFactory.addWindowsContextRow(menu, rowY, rowW, tr("ui.questsandstuff.menu.move_down"), "down",
                click -> runMenuAction(state, () -> ChapterContextMenuActions.move(player, state, target, 1, refresh)));
    }

    public static boolean click(ChapterContextMenuLayout layout, TabletUiState state, Player player, Runnable refresh, int x, int y) {
        if (!layout.contains(x, y)) {
            return false;
        }
        ContextMenuAnimation.finish(state, ContextMenuAnimation.CHAPTER_KEY);
        int relY = layout.relativeY(y);
        int rowY = ChapterContextMenuLayout.ROW_TOP_PAD;
        String target = layout.target();
        if (layout.hasTarget()) {
            if (ChapterContextMenuLayout.isContextRowHit(relY, rowY)) {
                ChapterContextMenuActions.rename(state, target, refresh);
                return true;
            }
            rowY += ChapterContextMenuLayout.ROW_STEP;
        }

        if (ChapterContextMenuLayout.isContextRowHit(relY, rowY)) {
            ChapterContextMenuActions.addChapter(state, refresh);
            return true;
        }
        rowY += ChapterContextMenuLayout.ROW_STEP;

        if (!layout.hasTarget()) {
            return true;
        }

        if (ChapterContextMenuLayout.isContextRowHit(relY, rowY)) {
            ChapterContextMenuActions.delete(player, state, target, refresh);
            return true;
        }
        rowY += ChapterContextMenuLayout.ROW_STEP;

        if (ChapterContextMenuLayout.isContextRowHit(relY, rowY)) {
            ChapterContextMenuActions.changeIcon(state, target, refresh);
            return true;
        }
        rowY += ChapterContextMenuLayout.ROW_STEP;

        if (layout.entityVariants() && ChapterContextMenuLayout.isContextRowHit(relY, rowY)) {
            ChapterContextMenuActions.changeVariant(state, target, refresh);
            return true;
        }
        if (layout.entityVariants()) {
            rowY += ChapterContextMenuLayout.ROW_STEP;
        }

        if (layout.entityIcon() && ChapterContextMenuLayout.isContextRowHit(relY, rowY)) {
            ChapterContextMenuActions.editMotion(state, target, refresh);
            return true;
        }
        if (layout.entityIcon()) {
            rowY += ChapterContextMenuLayout.ROW_STEP;
        }

        if (ChapterContextMenuLayout.isContextRowHit(relY, rowY)) {
            ChapterContextMenuActions.removeIcon(player, state, target, refresh);
            return true;
        }
        rowY += ChapterContextMenuLayout.ROW_STEP;

        if (ChapterContextMenuLayout.isContextRowHit(relY, rowY)) {
            ChapterContextMenuActions.changeBackground(state, target, refresh);
            return true;
        }
        rowY += ChapterContextMenuLayout.ROW_STEP;

        if (ChapterContextMenuLayout.isContextRowHit(relY, rowY)) {
            ChapterContextMenuActions.removeBackground(player, state, target, refresh);
            return true;
        }
        rowY += ChapterContextMenuLayout.ROW_STEP;

        if (ChapterContextMenuLayout.isContextRowHit(relY, rowY)) {
            ChapterContextMenuActions.textStyle(state, target, refresh);
            return true;
        }
        rowY += ChapterContextMenuLayout.ROW_STEP;

        if (ChapterContextMenuLayout.isContextRowHit(relY, rowY)) {
            ChapterContextMenuActions.move(player, state, target, -1, refresh);
            return true;
        }
        rowY += ChapterContextMenuLayout.ROW_STEP;

        if (ChapterContextMenuLayout.isContextRowHit(relY, rowY)) {
            ChapterContextMenuActions.move(player, state, target, 1, refresh);
            return true;
        }
        return true;
    }

    private static void runMenuAction(TabletUiState state, Runnable action) {
        ContextMenuAnimation.finish(state, ContextMenuAnimation.CHAPTER_KEY);
        action.run();
    }

    private static String tr(String key, Object... args) {
        return I18n.get(key, args);
    }
}
