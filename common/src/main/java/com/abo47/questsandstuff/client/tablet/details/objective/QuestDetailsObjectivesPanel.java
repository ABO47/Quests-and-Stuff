package com.abo47.questsandstuff.client.tablet.details.objective;

import com.abo47.questsandstuff.client.compat.recipeviewer.RecipeViewerIntegrations;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsEditState;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory;
import com.google.gson.JsonObject;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class QuestDetailsObjectivesPanel {
    static final int SECTION_GAP = 6;
    static final int TITLE_H = 18;
    static final int CARD_H = TabletUiFactory.CHAPTER_CARD_H;
    static final int CARD_GAP = TabletUiFactory.CHAPTER_CARD_GAP;
    static final int LIST_PAD = 6;
    static final int ICON = TabletUiFactory.CONTENT_ICON_SIZE;
    static final int HEADER_H = 14;

    private QuestDetailsObjectivesPanel() {
    }

    public static void rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        QuestObjectiveActionWidgets.renderProgress(modal, state, player, refresh, questId, quest, x, y, w, HEADER_H);
        int sectionsY = y + HEADER_H + SECTION_GAP;
        int sectionsH = Math.max(1, h - HEADER_H - SECTION_GAP);
        int sectionH = (sectionsH - SECTION_GAP) / 2;
        QuestObjectiveSectionWidget.renderRequirements(modal, state, player, refresh, questId, quest, x, sectionsY, w, sectionH);
        QuestObjectiveSectionWidget.renderRewards(modal, state, player, refresh, questId, quest, x, sectionsY + sectionH + SECTION_GAP, w, sectionsH - sectionH - SECTION_GAP);
    }

    public static boolean isCardHit(TabletUiState state, double mouseX, double mouseY) {
        if (state == null || !state.questDetailsOpen) {
            return false;
        }
        String questId = state.questDetailsQuestId == null ? "" : state.questDetailsQuestId.trim();
        if (questId.isBlank()) {
            return false;
        }
        CompoundTag quest = ClientQuestCache.quest(questId);
        if (quest.isEmpty()) {
            return false;
        }

        int leftW = QuestDetailsWindow.leftPanelWidth(state);
        int x = state.questDetailsScreenX + TabletUiFactory.CHAPTER_X + QuestDetailsWindow.CONTENT_INSET;
        int y = state.questDetailsScreenY + TabletUiFactory.CHAPTER_Y + QuestDetailsWindow.CONTENT_INSET;
        int w = Math.max(1, leftW - QuestDetailsWindow.CONTENT_INSET * 2);
        int h = Math.max(1, TabletUiFactory.CHAPTER_H - QuestDetailsWindow.CONTENT_INSET * 2);
        int sectionsY = y + HEADER_H + SECTION_GAP;
        int sectionsH = Math.max(1, h - HEADER_H - SECTION_GAP);
        int sectionH = (sectionsH - SECTION_GAP) / 2;

        List<QuestDetailsObjectiveEntry> tasks = QuestObjectiveEntries.entries(quest.getCompound("tasks"), quest.getList("tasks_order", Tag.TAG_STRING));
        if (isSectionCardHit(state, tasks, true, x, sectionsY, w, sectionH, mouseX, mouseY)) {
            return true;
        }
        List<QuestDetailsObjectiveEntry> rewards = QuestObjectiveEntries.entries(quest.getCompound("rewards"), quest.getList("rewards_order", Tag.TAG_STRING));
        List<QuestDetailsObjectiveEntry> displayRewards = QuestObjectiveSelectableRewards.displayEntries(rewards, QuestDetailsEditState.canEdit(state));
        return isSectionCardHit(state, displayRewards, false, x, sectionsY + sectionH + SECTION_GAP, w, sectionsH - sectionH - SECTION_GAP, mouseX, mouseY);
    }

    public static ItemStack hoveredViewerStack(TabletUiState state, double mouseX, double mouseY) {
        if (state == null || !state.questDetailsOpen) {
            return ItemStack.EMPTY;
        }
        String questId = state.questDetailsQuestId == null ? "" : state.questDetailsQuestId.trim();
        if (questId.isBlank()) {
            return ItemStack.EMPTY;
        }
        CompoundTag quest = ClientQuestCache.quest(questId);
        if (quest.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int x = state.questDetailsScreenX + TabletUiFactory.CHAPTER_X + QuestDetailsWindow.CONTENT_INSET;
        int y = state.questDetailsScreenY + TabletUiFactory.CHAPTER_Y + QuestDetailsWindow.CONTENT_INSET;
        int h = Math.max(1, TabletUiFactory.CHAPTER_H - QuestDetailsWindow.CONTENT_INSET * 2);
        int sectionsY = y + HEADER_H + SECTION_GAP;
        int sectionsH = Math.max(1, h - HEADER_H - SECTION_GAP);
        int sectionH = (sectionsH - SECTION_GAP) / 2;

        List<QuestDetailsObjectiveEntry> tasks = QuestObjectiveEntries.entries(quest.getCompound("tasks"), quest.getList("tasks_order", Tag.TAG_STRING));
        ItemStack taskStack = hoveredSectionViewerStack(state, tasks, true, x, sectionsY, sectionH, mouseX, mouseY);
        if (!taskStack.isEmpty()) {
            return taskStack;
        }
        List<QuestDetailsObjectiveEntry> rewards = QuestObjectiveEntries.entries(quest.getCompound("rewards"), quest.getList("rewards_order", Tag.TAG_STRING));
        List<QuestDetailsObjectiveEntry> displayRewards = QuestObjectiveSelectableRewards.displayEntries(rewards, QuestDetailsEditState.canEdit(state));
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
        return QuestObjectiveListInteractions.clearSelection(state, reason);
    }

    public static void renderTypePicker(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int modalW, int modalH) {
        QuestDetailsObjectiveMenus.renderTypePicker(modal, state, player, refresh, questId, quest, modalW, modalH);
    }

    public static void applyIconPick(Player player, TabletUiState state, String entry) {
        QuestObjectiveEditActions.applyIconPick(player, state, entry);
    }

    public static void applyBiomePick(Player player, TabletUiState state, String biome) {
        QuestObjectiveEditActions.applyBiomePick(player, state, biome);
    }

    public static void applyAdvancementPick(Player player, TabletUiState state, String advancement) {
        QuestObjectiveEditActions.applyAdvancementPick(player, state, advancement);
    }

    public static void applyRecipePick(Player player, TabletUiState state, String recipe) {
        QuestObjectiveEditActions.applyRecipePick(player, state, recipe);
    }

    public static void applyStructurePick(Player player, TabletUiState state, String structure) {
        QuestObjectiveEditActions.applyStructurePick(player, state, structure);
    }

    public static void applyBlockPick(Player player, TabletUiState state, String block) {
        QuestObjectiveEditActions.applyBlockPick(player, state, block);
    }

    public static void applyStatPick(Player player, TabletUiState state, String stat) {
        QuestObjectiveEditActions.applyStatPick(player, state, stat);
    }

    public static void applyDimensionPick(Player player, TabletUiState state, String dimension) {
        QuestObjectiveEditActions.applyDimensionPick(player, state, dimension);
    }

    public static void applyLootTablePick(Player player, TabletUiState state, String lootTable) {
        QuestObjectiveEditActions.applyLootTablePick(player, state, lootTable);
    }

    public static void applyInventoryItemPick(Player player, TabletUiState state, ItemStack stack) {
        QuestObjectiveEditActions.applyInventoryItemPick(player, state, stack);
    }

    public static void renderContextMenu(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId) {
        QuestDetailsObjectiveMenus.renderContextMenu(modal, state, player, refresh, questId);
    }

    public static boolean handleRenameKey(Player player, TabletUiState state, int keyCode, boolean draftUnchanged) {
        return QuestObjectiveInlineFields.handleRenameKey(player, state, keyCode, draftUnchanged);
    }

    public static boolean handleRenameChar(TabletUiState state, char c, boolean draftUnchanged) {
        return QuestObjectiveInlineFields.handleRenameChar(state, c, draftUnchanged);
    }

    public static void openObjectiveRenameEditor(TabletUiState state, String questId, String id, boolean task) {
        QuestObjectiveInlineFields.openObjectiveRenameEditor(state, questId, id, task);
    }

    public static boolean deleteSelectedObjective(Player player, TabletUiState state, String questId) {
        return QuestObjectiveListInteractions.deleteSelected(player, state, questId);
    }

    public static boolean moveSelectedObjective(Player player, TabletUiState state, String questId, int offset) {
        return QuestObjectiveListInteractions.moveSelected(player, state, questId, offset);
    }

    private static boolean isSectionCardHit(TabletUiState state, List<QuestDetailsObjectiveEntry> entries, boolean requirements, int x, int y, int w, int h, double mouseX, double mouseY) {
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
        int maxStart = QuestObjectiveSectionWidget.scrollMax(entries, visibleH);
        int cardW = Math.max(1, maxStart > 0 ? w - 12 - DragScrollBarWidget.RESERVED_WIDTH : w - 12);
        if (localX < LIST_PAD || localX >= LIST_PAD + cardW) {
            return false;
        }
        int scroll = requirements ? state.questDetailsReqScroll : state.questDetailsRewardScroll;
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

    private static ItemStack hoveredSectionViewerStack(TabletUiState state, List<QuestDetailsObjectiveEntry> entries, boolean requirements, int x, int y, int h, double mouseX, double mouseY) {
        if (entries.isEmpty() || h <= TITLE_H + 4) {
            return ItemStack.EMPTY;
        }
        int visibleH = Math.max(1, h - TITLE_H - 4);
        int maxStart = QuestObjectiveSectionWidget.scrollMax(entries, visibleH);
        int scroll = requirements ? state.questDetailsReqScroll : state.questDetailsRewardScroll;
        scroll = Math.max(0, Math.min(scroll, maxStart));
        int listTop = y + TITLE_H;
        int iconX = x + LIST_PAD + 8;
        if (mouseX < iconX || mouseX >= iconX + ICON || mouseY < listTop || mouseY >= listTop + visibleH) {
            return ItemStack.EMPTY;
        }
        int rowH = CARD_H + CARD_GAP;
        for (int index = 0; index < entries.size(); index++) {
            QuestDetailsObjectiveEntry entry = entries.get(index);
            int cardY = listTop + LIST_PAD - scroll + index * rowH;
            int iconY = cardY + 8;
            if (mouseY < iconY || mouseY >= iconY + ICON) {
                continue;
            }
            return QuestObjectiveItemStacks.viewerStack(viewerJson(entry));
        }
        return ItemStack.EMPTY;
    }

    private static JsonObject viewerJson(QuestDetailsObjectiveEntry entry) {
        return QuestObjectiveSelectableRewards.isSelectable(entry.json()) ? QuestObjectiveSelectableRewards.displayJson(entry.json()) : entry.json();
    }

}
