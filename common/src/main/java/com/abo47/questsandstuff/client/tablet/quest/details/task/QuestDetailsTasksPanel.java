package com.abo47.questsandstuff.client.tablet.quest.details.task;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerIntegrations;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsEditController;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.factory.TabletUiFactory;

import com.google.gson.JsonObject;

public final class QuestDetailsTasksPanel {
    static final int HEADER_SECTION_GAP = TabletUiFactory.PANEL_INSET - 1;
    static final int SECTION_GAP = TabletUiFactory.PANEL_INSET + 1;
    static final int TITLE_H = 18;
    static final int CARD_H = TabletUiFactory.CHAPTER_CARD_H;
    static final int CARD_GAP = TabletUiFactory.CHAPTER_CARD_GAP;
    static final int LIST_PAD = TabletUiFactory.PANEL_INSET;
    static final int ICON = TabletUiFactory.CONTENT_ICON_SIZE;
    static final int HEADER_H = TabletUiFactory.HEADER_H;

    private QuestDetailsTasksPanel() {
    }

    public static void rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        QuestTaskActionWidgets.renderProgress(modal, state, player, refresh, questId, quest, x, y, w, HEADER_H);
        int sectionsY = y + HEADER_H + HEADER_SECTION_GAP;
        int sectionsH = Math.max(1, h - HEADER_H - HEADER_SECTION_GAP);
        int sectionH = (sectionsH - SECTION_GAP) / 2;
        QuestTaskSectionWidget.renderTasks(modal, state, player, refresh, questId, quest, x, sectionsY, w, sectionH);
        QuestTaskSectionWidget.renderRewards(modal, state, player, refresh, questId, quest, x, sectionsY + sectionH + SECTION_GAP, w, sectionsH - sectionH - SECTION_GAP);
    }

    public static boolean isCardHit(TabletUiState state, double mouseX, double mouseY) {
        if (state == null || !state.questDetails.questDetailsOpen) {
            return false;
        }
        String questId = state.questDetails.questDetailsQuestId == null ? "" : state.questDetails.questDetailsQuestId.trim();
        if (questId.isBlank()) {
            return false;
        }
        CompoundTag quest = ClientQuestStateFacade.quest(questId);
        if (quest.isEmpty()) {
            return false;
        }

        int leftW = QuestDetailsWindow.leftPanelWidth(state);
        int x = state.questDetails.questDetailsScreenX + TabletUiFactory.CHAPTER_X + leftPanelContentX();
        int y = state.questDetails.questDetailsScreenY + TabletUiFactory.CHAPTER_Y + leftPanelContentY();
        int w = leftPanelContentW(leftW);
        int h = leftPanelContentH();
        int sectionsY = y + HEADER_H + HEADER_SECTION_GAP;
        int sectionsH = Math.max(1, h - HEADER_H - HEADER_SECTION_GAP);
        int sectionH = (sectionsH - SECTION_GAP) / 2;

        List<QuestDetailsTaskEntry> tasks = QuestTaskEntries.entries(quest.getCompound("tasks"), quest.getList("tasks_order", Tag.TAG_STRING));
        if (isSectionCardHit(state, tasks, true, x, sectionsY, w, sectionH, mouseX, mouseY)) {
            return true;
        }
        List<QuestDetailsTaskEntry> rewards = QuestTaskEntries.entries(quest.getCompound("rewards"), quest.getList("rewards_order", Tag.TAG_STRING));
        List<QuestDetailsTaskEntry> displayRewards = QuestTaskSelectableRewards.displayEntries(rewards, QuestDetailsEditController.canEdit(state));
        return isSectionCardHit(state, displayRewards, false, x, sectionsY + sectionH + SECTION_GAP, w, sectionsH - sectionH - SECTION_GAP, mouseX, mouseY);
    }

    public static ItemStack hoveredViewerStack(TabletUiState state, double mouseX, double mouseY) {
        if (state == null || !state.questDetails.questDetailsOpen) {
            return ItemStack.EMPTY;
        }
        String questId = state.questDetails.questDetailsQuestId == null ? "" : state.questDetails.questDetailsQuestId.trim();
        if (questId.isBlank()) {
            return ItemStack.EMPTY;
        }
        CompoundTag quest = ClientQuestStateFacade.quest(questId);
        if (quest.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int x = state.questDetails.questDetailsScreenX + TabletUiFactory.CHAPTER_X + leftPanelContentX();
        int y = state.questDetails.questDetailsScreenY + TabletUiFactory.CHAPTER_Y + leftPanelContentY();
        int h = leftPanelContentH();
        int sectionsY = y + HEADER_H + HEADER_SECTION_GAP;
        int sectionsH = Math.max(1, h - HEADER_H - HEADER_SECTION_GAP);
        int sectionH = (sectionsH - SECTION_GAP) / 2;

        List<QuestDetailsTaskEntry> tasks = QuestTaskEntries.entries(quest.getCompound("tasks"), quest.getList("tasks_order", Tag.TAG_STRING));
        ItemStack taskStack = hoveredSectionViewerStack(state, tasks, true, x, sectionsY, sectionH, mouseX, mouseY);
        if (!taskStack.isEmpty()) {
            return taskStack;
        }
        List<QuestDetailsTaskEntry> rewards = QuestTaskEntries.entries(quest.getCompound("rewards"), quest.getList("rewards_order", Tag.TAG_STRING));
        List<QuestDetailsTaskEntry> displayRewards = QuestTaskSelectableRewards.displayEntries(rewards, QuestDetailsEditController.canEdit(state));
        return hoveredSectionViewerStack(state, displayRewards, false, x, sectionsY + sectionH + SECTION_GAP, sectionsH - sectionH - SECTION_GAP, mouseX, mouseY);
    }

    public static boolean handleRecipeViewerShortcut(TabletUiState state, int keyCode, int scanCode, double mouseX, double mouseY) {
        if (!RecipeViewerIntegrations.hasAvailableViewer()) {
            return false;
        }
        ItemStack stack = hoveredViewerStack(state, mouseX, mouseY);
        if (stack.isEmpty()) {
            return false;
        }
        return RecipeViewerIntegrations.handleKeybind(stack, keyCode, scanCode);
    }

    public static boolean clearSelection(TabletUiState state, String reason) {
        return QuestTaskListInteractions.clearSelection(state, reason);
    }

    public static void renderTypePicker(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int modalW, int modalH) {
        QuestDetailsTaskMenus.renderTypePicker(modal, state, player, refresh, questId, quest, modalW, modalH);
    }

    public static void applyIconPick(Player player, TabletUiState state, String entry) {
        QuestTaskEditActions.applyIconPick(player, state, entry);
    }

    public static void applyBiomePick(Player player, TabletUiState state, String biome) {
        QuestTaskEditActions.applyBiomePick(player, state, biome);
    }

    public static void applyAdvancementPick(Player player, TabletUiState state, String advancement) {
        QuestTaskEditActions.applyAdvancementPick(player, state, advancement);
    }

    public static void applyRecipePick(Player player, TabletUiState state, String recipe) {
        QuestTaskEditActions.applyRecipePick(player, state, recipe);
    }

    public static void applyStructurePick(Player player, TabletUiState state, String structure) {
        QuestTaskEditActions.applyStructurePick(player, state, structure);
    }

    public static void applyBlockPick(Player player, TabletUiState state, String block) {
        QuestTaskEditActions.applyBlockPick(player, state, block);
    }

    public static void applyStatPick(Player player, TabletUiState state, String stat) {
        QuestTaskEditActions.applyStatPick(player, state, stat);
    }

    public static void applyDimensionPick(Player player, TabletUiState state, String dimension) {
        QuestTaskEditActions.applyDimensionPick(player, state, dimension);
    }

    public static void applyLootTablePick(Player player, TabletUiState state, String lootTable) {
        QuestTaskEditActions.applyLootTablePick(player, state, lootTable);
    }

    public static void applyInventoryItemPick(Player player, TabletUiState state, ItemStack stack) {
        QuestTaskEditActions.applyInventoryItemPick(player, state, stack);
    }

    public static void renderContextMenu(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId) {
        QuestDetailsTaskMenus.renderContextMenu(modal, state, player, refresh, questId);
    }

    public static boolean handleRenameKey(Player player, TabletUiState state, int keyCode, boolean draftUnchanged) {
        return QuestTaskInlineFields.handleRenameKey(player, state, keyCode, draftUnchanged);
    }

    public static boolean handleRenameChar(TabletUiState state, char c, boolean draftUnchanged) {
        return QuestTaskInlineFields.handleRenameChar(state, c, draftUnchanged);
    }

    public static void openTaskRenameEditor(TabletUiState state, String questId, String id, boolean task) {
        QuestTaskInlineFields.openTaskRenameEditor(state, questId, id, task);
    }

    public static boolean deleteSelectedTask(Player player, TabletUiState state, String questId) {
        return QuestTaskListInteractions.deleteSelected(player, state, questId);
    }

    public static boolean moveSelectedTask(Player player, TabletUiState state, String questId, int offset) {
        return QuestTaskListInteractions.moveSelected(player, state, questId, offset);
    }

    private static boolean isSectionCardHit(TabletUiState state, List<QuestDetailsTaskEntry> entries, boolean tasks, int x, int y, int w, int h, double mouseX, double mouseY) {
        if (entries.isEmpty() || h <= TITLE_H + 4) {
            return false;
        }
        int localX = (int) Math.round(mouseX - x);
        int localY = (int) Math.round(mouseY - y);
        int listBottom = h - 4;
        if (localX < 0 || localX >= w || localY < TITLE_H || localY >= listBottom) {
            return false;
        }
        int visibleH = Math.max(1, h - TITLE_H - 4);
        int maxStart = QuestTaskSectionWidget.scrollMax(entries, visibleH);
        int cardW = Math.max(1, maxStart > 0 ? w - 12 - DragScrollBarWidget.RESERVED_WIDTH : w - 12);
        if (localX < LIST_PAD || localX >= LIST_PAD + cardW) {
            return false;
        }
        int scroll = tasks ? state.questDetails.questDetailsTaskScroll : state.questDetails.questDetailsRewardScroll;
        scroll = Math.max(0, Math.min(scroll, maxStart));
        int contentY = localY - TITLE_H + scroll - LIST_PAD;
        if (contentY < 0) {
            return false;
        }
        int rowH = CARD_H + CARD_GAP;
        int slot = contentY / rowH;
        int inSlot = contentY % rowH;
        return slot >= 0 && slot < entries.size() && inSlot < CARD_H;
    }

    static int leftPanelContentX() {
        return TabletUiFactory.CHAPTER_PANEL_GUTTER_X;
    }

    static int leftPanelContentY() {
        return QuestDetailsWindow.CONTENT_INSET;
    }

    static int leftPanelContentW(int leftW) {
        return Math.max(1, leftW - TabletUiFactory.CHAPTER_PANEL_GUTTER_X * 2);
    }

    static int leftPanelContentH() {
        return Math.max(1, TabletUiFactory.CHAPTER_H - QuestDetailsWindow.CONTENT_INSET - TabletUiFactory.CHAPTER_PANEL_GUTTER_BOTTOM);
    }

    private static ItemStack hoveredSectionViewerStack(TabletUiState state, List<QuestDetailsTaskEntry> entries, boolean tasks, int x, int y, int h, double mouseX, double mouseY) {
        if (entries.isEmpty() || h <= TITLE_H + 4) {
            return ItemStack.EMPTY;
        }
        int visibleH = Math.max(1, h - TITLE_H - 4);
        int maxStart = QuestTaskSectionWidget.scrollMax(entries, visibleH);
        int scroll = tasks ? state.questDetails.questDetailsTaskScroll : state.questDetails.questDetailsRewardScroll;
        scroll = Math.max(0, Math.min(scroll, maxStart));
        int listTop = y + TITLE_H;
        int iconX = x + LIST_PAD + 8;
        if (mouseX < iconX || mouseX >= iconX + ICON || mouseY < listTop || mouseY >= listTop + visibleH) {
            return ItemStack.EMPTY;
        }
        int rowH = CARD_H + CARD_GAP;
        for (int index = 0; index < entries.size(); index++) {
            QuestDetailsTaskEntry entry = entries.get(index);
            int cardY = listTop + LIST_PAD - scroll + index * rowH;
            int iconY = cardY + 8;
            if (mouseY < iconY || mouseY >= iconY + ICON) {
                continue;
            }
            return QuestTaskItemStacks.viewerStack(viewerJson(entry));
        }
        return ItemStack.EMPTY;
    }

    private static JsonObject viewerJson(QuestDetailsTaskEntry entry) {
        return QuestTaskSelectableRewards.isSelectable(entry.json()) ? QuestTaskSelectableRewards.displayJson(entry.json()) : entry.json();
    }

}
