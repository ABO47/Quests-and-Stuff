package com.abo47.questsandstuff.client.tablet.quest.chapter.menu;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextAction;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuPanel;
import com.abo47.questsandstuff.client.tablet.contextmenu.ContextMenuRenderer;
import com.abo47.questsandstuff.client.tablet.controls.EntityIconControls;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorChapterCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

public record ChapterContextMenuLayout(
        String target,
        boolean hasTarget,
        boolean entityIcon,
        boolean entityVariants,
        int menuW,
        int menuH,
        int menuX,
        int menuY
) {
    public static final int ROW_TOP_PAD = 4;
    public static final int ROW_STEP = TabletUiFactory.CONTEXT_ROW_H;

    public static ChapterContextMenuLayout resolve(TabletUiState state, int availableW, int availableH) {
        String target = resolveTarget(state);
        boolean hasTarget = target != null && !target.isBlank();
        boolean entityIcon = hasTarget && isEntityChapterIcon(target);
        boolean entityVariants = entityIcon && hasEntityVariants(target);
        int menuW = width(state, availableW);
        int menuX = Math.max(4, Math.min(state.chapterPanel.chapterMenuX, availableW - menuW - 4));
        int menuY = Math.max(4, Math.min(state.chapterPanel.chapterMenuY, availableH - 4));
        return new ChapterContextMenuLayout(target, hasTarget, entityIcon, entityVariants, menuW, 0, menuX, menuY);
    }

    public static ChapterContextMenuLayout resolve(TabletUiState state, int availableW, int availableH, Player player, Runnable refresh) {
        ChapterContextMenuLayout bounds = resolve(state, availableW, availableH);
        List<ContextAction> actions = ChapterContextMenuRows.actions(bounds, state, player, refresh);
        int menuH = ContextMenuPanel.heightFor(actions, ContextMenuPanel.rowActionCount(actions));
        int menuY = Math.max(4, Math.min(state.chapterPanel.chapterMenuY, availableH - menuH - 4));
        return new ChapterContextMenuLayout(bounds.target(), bounds.hasTarget(), bounds.entityIcon(), bounds.entityVariants(), bounds.menuW(), menuH, bounds.menuX(), menuY);
    }

    public static int width(TabletUiState state, int maxAvailableWidth) {
        return ContextMenuRenderer.CONTEXT_MENU_WIDTH;
    }

    public boolean contains(int x, int y) {
        return x >= menuX && x <= menuX + menuW && y >= menuY && y <= menuY + menuH;
    }

    public int relativeY(int y) {
        return y - menuY;
    }

    public static boolean isContextRowHit(int relY, int rowY) {
        return relY >= rowY && relY < rowY + TabletUiFactory.CONTEXT_ROW_H;
    }

    public static String deleteKey(String target) {
        return "chapter:delete:" + target;
    }

    public static String removeIconKey(String target) {
        return "chapter:remove_icon:" + target;
    }

    public static String removeBackgroundKey(String target) {
        return "chapter:remove_bg:" + target;
    }

    public static boolean isEntityChapterIcon(String target) {
        return EntityIconControls.isEntityIcon(chapterIcon(target));
    }

    public static boolean hasEntityVariants(String target) {
        return EntityIconControls.hasVariants(chapterIcon(target));
    }

    public static String chapterIcon(String target) {
        String icon = ClientQuestStateFacade.chapterIcon(target);
        return icon == null ? "" : icon;
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

    private static String resolveTarget(TabletUiState state) {
        return state.chapterPanel.chapterMenuTarget.isBlank() ? EditorChapterCommandClient.selectedChapterName(state) : state.chapterPanel.chapterMenuTarget;
    }

}
