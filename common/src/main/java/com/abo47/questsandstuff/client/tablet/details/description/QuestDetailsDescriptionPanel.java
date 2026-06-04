package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.DragScrollBarWidget;
import com.abo47.questsandstuff.client.tablet.entity.motion.EntityMotionEditor;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class QuestDetailsDescriptionPanel {
    private QuestDetailsDescriptionPanel() {
    }

    public static void rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, String questId, CompoundTag quest, int x, int y, int w, int h) {
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(quest);
        QuestDetailsDescriptionMenus.keepTextStyleOpenForActiveEdit(state, model);
        int[] fit = QuestDetailsDescriptionLayout.gridFit(state, w, h);
        int viewportH = fit[1] - 1;
        state.questDetailsDescScroll = QuestDetailsDescriptionLayout.clampDescriptionScroll(state, model, viewportH, state.questDetailsDescScroll);
        QuestDetailsDescriptionCanvas canvas = new QuestDetailsDescriptionCanvas(x, y, fit[0], fit[1], state, player, refresh, questId);
        canvas.setBackground(Surfaces.transparentBorder(ModColors.BORDER_BASE));
        renderScrollbar(canvas, state, model, refresh, questId, fit[0], viewportH);
        modal.addWidget(canvas);
        QuestDetailsDescriptionMenus.renderStyleMenu(modal, state, player, refresh, questId, model, x, y, fit[0], fit[1]);
        QuestDetailsDescriptionMenus.renderContextMenu(modal, state, player, refresh, questId, model, x, y, fit[0], fit[1]);
        EntityMotionEditor.renderQuestDetails(modal, state, player, refresh);
    }

    public static void applyAssetPick(Player player, TabletUiState state, String asset) {
        QuestDetailsDescriptionPickActions.applyAssetPick(player, state, asset);
    }

    public static boolean applyIconPick(Player player, TabletUiState state, String entry) {
        return QuestDetailsDescriptionPickActions.applyIconPick(player, state, entry);
    }

    private static void renderScrollbar(WidgetGroup canvas, TabletUiState state, QuestDetailsDescriptionModel model, Runnable refresh, String questId, int canvasW, int viewportH) {
        int scrollMax = QuestDetailsDescriptionLayout.descriptionScrollMax(model, viewportH);
        if (scrollMax <= 0) {
            state.questDetailsDescScrollDragging = false;
            return;
        }
        int knobH = QuestDetailsDescriptionLayout.descriptionScrollKnobHeight(viewportH, scrollMax);
        canvas.addWidget(new DragScrollBarWidget(
                Math.max(0, canvasW - DragScrollBarWidget.RESERVED_WIDTH - 1),
                0,
                DragScrollBarWidget.RESERVED_WIDTH,
                Math.max(1, viewportH),
                () -> state.questDetailsDescScroll,
                () -> scrollMax,
                () -> knobH,
                value -> state.questDetailsDescScroll = QuestDetailsDescriptionLayout.clampDescriptionScroll(state, model, viewportH, value),
                () -> state.questDetailsDescScrollDragging,
                dragging -> {
                    if (state.questDetailsDescScrollDragging != dragging) {
                        QuestsAndStuffMod.debugLog("[QnS:UI] quest details description scrollbar {} quest={} scroll={}", dragging ? "start" : "finish", questId, state.questDetailsDescScroll);
                    }
                    state.questDetailsDescScrollDragging = dragging;
                },
                refresh,
                ModColors.scrollTrack(state.questDetailsDescScrollDragging),
                ModColors.scrollThumb(false),
                ModColors.scrollThumb(true),
                DragScrollBarWidget.WIDTH
        ));
    }

    public static boolean applyBlockPick(Player player, TabletUiState state, String block) {
        return QuestDetailsDescriptionPickActions.applyBlockPick(player, state, block);
    }

    public static boolean applyRecipePick(Player player, TabletUiState state, String recipe) {
        return QuestDetailsDescriptionPickActions.applyRecipePick(player, state, recipe);
    }

    public static String imageAsset(String questId, String imageId) {
        return QuestDetailsDescriptionPickActions.imageAsset(questId, imageId);
    }

    public static void applyEntityVariantPick(Player player, TabletUiState state, String questId, String imageId, String variantKey) {
        QuestDetailsDescriptionPickActions.applyEntityVariantPick(player, state, questId, imageId, variantKey);
    }

    public static void applyTextColor(Player player, TabletUiState state, String target, int color) {
        QuestDetailsDescriptionPickActions.applyTextColor(player, state, target, color);
    }

    public static void addTextAt(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, int panelX, int panelY) {
        QuestDetailsDescriptionEditActions.addTextAt(player, state, questId, model, panelX, panelY);
    }

    public static void addImageAt(TabletUiState state, String questId, int panelX, int panelY) {
        QuestDetailsDescriptionEditActions.addImageAt(state, questId, panelX, panelY);
    }

    public static void addEntityAt(TabletUiState state, String questId, int panelX, int panelY) {
        QuestDetailsDescriptionEditActions.addEntityAt(state, questId, panelX, panelY);
    }

    public static void addItemAt(TabletUiState state, String questId, int panelX, int panelY) {
        QuestDetailsDescriptionEditActions.addItemAt(state, questId, panelX, panelY);
    }

    public static void addBlockAt(TabletUiState state, String questId, int panelX, int panelY) {
        QuestDetailsDescriptionEditActions.addBlockAt(state, questId, panelX, panelY);
    }

    public static void addRecipeCardAt(TabletUiState state, String questId, int panelX, int panelY) {
        QuestDetailsDescriptionEditActions.addRecipeCardAt(state, questId, panelX, panelY);
    }

    public static void fitTextToGrid(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, String id) {
        QuestDetailsDescriptionEditActions.fitTextToGrid(player, state, questId, model, id);
    }

    public static void fitImageToGrid(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, String id) {
        QuestDetailsDescriptionEditActions.fitImageToGrid(player, state, questId, model, id);
    }

    public static void fitSelectionToGrid(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model) {
        QuestDetailsDescriptionEditActions.fitSelectionToGrid(player, state, questId, model);
    }

    public static void alignSelectionToCanvas(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model, int viewportW, int viewportH, boolean horizontal) {
        QuestDetailsDescriptionEditActions.alignSelectionToCanvas(player, state, questId, model, viewportW, viewportH, horizontal);
    }

    public static void moveSelectionLayers(TabletUiState state, QuestDetailsDescriptionModel model, boolean front) {
        QuestDetailsDescriptionEditActions.moveSelectionLayers(state, model, front);
    }

    public static void copyDescriptionSelection(TabletUiState state, QuestDetailsDescriptionModel model) {
        QuestDetailsDescriptionEditActions.copyDescriptionSelection(state, model);
    }

    public static boolean copySelectedDescriptionToClipboard(TabletUiState state, QuestDetailsDescriptionModel model) {
        return QuestDetailsDescriptionEditActions.copySelectedDescriptionToClipboard(state, model);
    }

    public static boolean selectAllDescription(TabletUiState state, QuestDetailsDescriptionModel model) {
        return QuestDetailsDescriptionEditActions.selectAllDescription(state, model);
    }

    public static boolean deleteSelectedDescription(Player player, TabletUiState state, String questId, QuestDetailsDescriptionModel model) {
        return QuestDetailsDescriptionEditActions.deleteSelectedDescription(player, state, questId, model);
    }

    public static void deleteDescriptionSelection(TabletUiState state, QuestDetailsDescriptionModel model) {
        QuestDetailsDescriptionEditActions.deleteDescriptionSelection(state, model);
    }
}
