package com.abo47.questsandstuff.client.tablet.details.description;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.canvas.CanvasRenderer;
import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.entity.EntityPreviewRenderer;
import com.abo47.questsandstuff.client.tablet.modal.ModalTargetParser;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.world.entity.player.Player;

final class QuestDetailsDescriptionPickActions {
    private QuestDetailsDescriptionPickActions() {
    }

    static void applyAssetPick(Player player, TabletUiState state, String asset) {
        String target = state.questDetailsAssetPickTarget == null ? "" : state.questDetailsAssetPickTarget;
        if (target.isBlank() || asset == null || asset.isBlank()) {
            return;
        }
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (!parsed.hasAtLeast(2)) {
            return;
        }
        String questId = parsed.questId();
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        if (parsed.isDescBackground()) {
            model.canvasBackground = asset;
            QuestDetailsDescriptionModel.save(player, questId, model);
        } else if (parsed.isDescImageNew() && parsed.hasAtLeast(5)) {
            String id = parsed.entryId();
            int x = parseInt(parsed.part(3), 0);
            int y = parseInt(parsed.part(4), 0);
            int[] size = QuestDetailsDescriptionLayout.imageSpawnSize(asset);
            model.putImage(new CanvasImageLayer(id, asset, x, y, size[0], size[1], 0));
            model.ensureOrder(QuestDetailsDescriptionModel.ORDER_IMAGE + id);
            QuestDetailsDescriptionModel.save(player, questId, model);
            QuestDetailsDescriptionSelectionState.selectOnlyImage(state, id);
        } else if (parsed.isDescImage()) {
            String id = parsed.entryId();
            CanvasImageLayer image = model.image(id);
            if (image != null) {
                model.putImage(image.withAsset(asset));
                QuestDetailsDescriptionModel.save(player, questId, model);
                QuestDetailsDescriptionSelectionState.selectOnlyImage(state, id);
            }
        }
        state.questDetailsAssetPickTarget = "";
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details asset picked quest={} target={} asset={}", questId, parsed.kind(), asset);
    }

    static boolean applyIconPick(Player player, TabletUiState state, String entry) {
        String target = state.questDetailsPickTarget == null ? "" : state.questDetailsPickTarget;
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if (target.isBlank() || entry == null || entry.isBlank() || (!parsed.isDescEntity() && !parsed.isDescEntityNew())) {
            return false;
        }
        if (!parsed.hasAtLeast(3)) {
            return false;
        }
        String entityId = EntityPreviewRenderer.entityIdFromSpawnEgg(entry);
        if (entityId.isBlank()) {
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details entity pick ignored target={} item={}", target, entry);
            return true;
        }
        String questId = parsed.questId();
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        if (parsed.isDescEntityNew() && parsed.hasAtLeast(5)) {
            String id = parsed.entryId();
            int x = parseInt(parsed.part(3), 0);
            int y = parseInt(parsed.part(4), 0);
            int size = 64;
            model.putImage(new CanvasImageLayer(id, EntityPreviewRenderer.entityAsset(entityId), x, y, size, size, 0));
            model.ensureOrder(QuestDetailsDescriptionModel.ORDER_IMAGE + id);
            QuestDetailsDescriptionModel.save(player, questId, model);
            QuestDetailsDescriptionSelectionState.selectOnlyImage(state, id);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest details add entity quest={} image={} entity={} pos={},{}", questId, id, entityId, x, y);
        } else if (parsed.isDescEntity()) {
            String id = parsed.entryId();
            CanvasImageLayer image = model.image(id);
            if (image != null) {
                model.putImage(image.withAsset(EntityPreviewRenderer.entityAsset(entityId)));
                QuestDetailsDescriptionModel.save(player, questId, model);
                QuestDetailsDescriptionSelectionState.selectOnlyImage(state, id);
                QuestsAndStuffMod.debugLog("[QnS:UI] quest details change entity quest={} image={} entity={}", questId, id, entityId);
            }
        }
        state.questDetailsPickTarget = "";
        return true;
    }

    static String imageAsset(String questId, String imageId) {
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        CanvasImageLayer image = model.image(imageId);
        return image == null ? "" : image.asset();
    }

    static void applyEntityVariantPick(Player player, TabletUiState state, String questId, String imageId, String variantKey) {
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        CanvasImageLayer image = model.image(imageId);
        if (image == null || !EntityPreviewRenderer.isEntityAsset(image.asset())) {
            return;
        }
        model.putImage(image.withAsset(EntityPreviewRenderer.withEntityVariant(image.asset(), variantKey)));
        QuestDetailsDescriptionModel.save(player, questId, model);
        QuestDetailsDescriptionSelectionState.selectOnlyImage(state, imageId);
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details entity variant picked quest={} image={} variant={}", questId, imageId, variantKey);
    }

    static void applyTextColor(Player player, TabletUiState state, String target, int color) {
        String questId = state.questDetailsTextColorQuestId;
        String textId = state.questDetailsTextColorTextId;
        ModalTargetParser.Target parsed = ModalTargetParser.parse(target);
        if ((questId == null || questId.isBlank() || textId == null || textId.isBlank()) && parsed.isQuestDescText()) {
            if (parsed.hasAtLeast(3)) {
                questId = parsed.questId();
                textId = parsed.entryId();
            }
        }
        if (!parsed.isQuestDescText()) {
            return;
        }
        if (questId == null || questId.isBlank() || textId == null || textId.isBlank()) {
            return;
        }
        QuestDetailsDescriptionModel model = QuestDetailsDescriptionModel.decode(ClientQuestCache.quest(questId));
        CanvasTextLayer text = model.text(textId);
        if (text == null) {
            return;
        }
        model.putText(CanvasRenderer.applyTextColorSelection(state, text, color));
        QuestDetailsDescriptionModel.save(player, questId, model);
        state.colorPickerTarget = "";
        state.questDetailsTextColorQuestId = "";
        state.questDetailsTextColorTextId = "";
        state.questDetailsTextStyleOpen = true;
        state.questDetailsTextStyleTarget = text.id();
        QuestsAndStuffMod.debugLog("[QnS:UI] quest details text color quest={} text={} color={}", questId, textId, color);
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }
}
