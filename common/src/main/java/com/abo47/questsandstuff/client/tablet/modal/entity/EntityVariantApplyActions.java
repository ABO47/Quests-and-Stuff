package com.abo47.questsandstuff.client.tablet.modal.entity;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.quest.details.task.QuestTaskEditActions;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorChapterCommandClient;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import net.minecraft.world.entity.player.Player;

final class EntityVariantApplyActions {
    private EntityVariantApplyActions() {
    }

    static void apply(Player player, TabletUiState state, String target, String variantKey) {
        apply(player, state, ModalTargetParser.parse(target), variantKey);
    }

    static void apply(Player player, TabletUiState state, ModalTargetParser.Target parsed, String variantKey) {
        if (applyQuestIcon(player, parsed, variantKey)) {
            return;
        }
        if (applyChapterIcon(player, state, parsed, variantKey)) {
            return;
        }
        if (applyTaskIcon(player, parsed, variantKey)) {
            return;
        }
        if (!parsed.hasAtLeast(3)) {
            return;
        }
        if (parsed.isCanvasImage()) {
            CanvasImageLayer image = CanvasLayerMutations.findCanvasImage(state, parsed.questId(), parsed.entryId());
            if (image == null) {
                return;
            }
            CanvasLayerMutations.putCanvasImage(state, parsed.questId(), image.withAsset(EntityPreviewRenderer.withEntityVariant(image.asset(), variantKey)));
            state.canvas.canvasSelection.setPrimaryImageId(image.id());
            state.canvas.canvasSelection.imageIds().clear();
            state.canvas.canvasSelection.imageIds().add(image.id());
            QuestsAndStuffMod.debugLog("[QnS:UI] canvas entity variant picked chapter={} image={} variant={}", parsed.questId(), image.id(), variantKey);
            return;
        }
        if (parsed.isQuestDetailsImage()) {
            QuestDetailsWindow.applyEntityVariantPick(player, state, parsed.questId(), parsed.entryId(), variantKey);
        }
    }

    private static boolean applyQuestIcon(Player player, ModalTargetParser.Target parsed, String variantKey) {
        if (!parsed.hasAtLeast(2) || !parsed.isQuestIcon()) {
            return false;
        }
        var quest = ClientQuestStateFacade.quest(parsed.questId());
        String icon = quest == null ? "" : quest.getString("icon");
        if (!EntityPreviewRenderer.isEntityAsset(icon)) {
            return true;
        }
        String nextIcon = EntityPreviewRenderer.withEntityVariant(icon, variantKey);
        EditorQuestCommandClient.runQuestIconAction(player, parsed.questId(), nextIcon);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest icon entity variant picked quest={} variant={}", parsed.questId(), variantKey);
        return true;
    }

    private static boolean applyChapterIcon(Player player, TabletUiState state, ModalTargetParser.Target parsed, String variantKey) {
        if (!parsed.hasAtLeast(2) || !parsed.isChapterIcon()) {
            return false;
        }
        String icon = ClientQuestStateFacade.chapterIcon(parsed.questId());
        if (!EntityPreviewRenderer.isEntityAsset(icon)) {
            return true;
        }
        String nextIcon = EntityPreviewRenderer.withEntityVariant(icon, variantKey);
        EditorChapterCommandClient.runChapterAction(player, state, "set_icon", parsed.questId(), nextIcon, 0);
        QuestsAndStuffMod.debugLog("[QnS:UI] chapter icon entity variant picked chapter={} variant={}", parsed.questId(), variantKey);
        return true;
    }

    private static boolean applyTaskIcon(Player player, ModalTargetParser.Target parsed, String variantKey) {
        if (!parsed.hasAtLeast(3) || (!parsed.isTaskTask() && !parsed.isTaskReward())) {
            return false;
        }
        boolean task = parsed.isTaskTask();
        String icon = QuestTaskEditActions.taskIcon(parsed.questId(), parsed.entryId(), task);
        if (!EntityPreviewRenderer.isEntityAsset(icon)) {
            return true;
        }
        QuestTaskEditActions.putTaskIcon(player, parsed.questId(), parsed.entryId(), EntityPreviewRenderer.withEntityVariant(icon, variantKey), task);
        QuestsAndStuffMod.debugLog("[QnS:UI] task entity variant picked quest={} task={} task={} variant={}", parsed.questId(), parsed.entryId(), task, variantKey);
        return true;
    }
}
