package com.abo47.questsandstuff.client.tablet.quest.details.description;

import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasLayerMutations;
import com.abo47.questsandstuff.client.tablet.quest.canvas.CanvasTransformSessions;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.quest.model.canvas.CanvasImageLayer;
import com.abo47.questsandstuff.quest.model.canvas.CanvasTextLayer;
import net.minecraft.world.entity.player.Player;

final class QuestDetailsDescriptionTransformApply {
    private QuestDetailsDescriptionTransformApply() {
    }

    static void preview(TabletUiState state, QuestDetailsDescriptionModel model) {
        if ("desc_text".equals(state.questDetails.questDetailsTransformKind)) {
            CanvasTextLayer text = model.text(state.questDetails.questDetailsTransformId);
            if (text != null) {
                CanvasLayerMutations.putTransientQuestDetailsText(state, text);
            }
            return;
        }
        if ("desc_image".equals(state.questDetails.questDetailsTransformKind)) {
            CanvasImageLayer image = model.image(state.questDetails.questDetailsTransformId);
            if (image != null) {
                CanvasLayerMutations.putTransientQuestDetailsImage(state, image);
            }
            return;
        }
        if ("selection".equals(state.questDetails.questDetailsTransformKind)) {
            for (String textId : QuestDetailsDescriptionSelectionState.selectedTextIds(state)) {
                CanvasTextLayer text = model.text(textId);
                if (text != null) {
                    CanvasLayerMutations.putTransientQuestDetailsText(state, text);
                }
            }
            for (String imageId : QuestDetailsDescriptionSelectionState.selectedImageIds(state)) {
                CanvasImageLayer image = model.image(imageId);
                if (image != null) {
                    CanvasLayerMutations.putTransientQuestDetailsImage(state, image);
                }
            }
        }
    }

    static CommitInfo commit(Player player, TabletUiState state, String questId, QuestDetailsDescriptionTransform transforms, QuestDetailsDescriptionModel model, int pointerX, int pointerY) {
        CommitInfo info = new CommitInfo(state.questDetails.questDetailsTransformKind, state.questDetails.questDetailsTransformId, state.questDetails.questDetailsTransformMode);
        transforms.applyTransform(model, pointerX, pointerY);
        CanvasTransformSessions.clearQuestDetailsSession(state);
        QuestDetailsDescriptionModel.save(player, questId, model);
        return info;
    }

    static void clearEditDragState(TabletUiState state) {
        state.canvas.selectingCanvasTextRange = false;
        state.questDetails.questDetailsBoxSelecting = false;
        CanvasTransformSessions.clearQuestDetailsSession(state);
    }

    record CommitInfo(String kind, String id, String mode) {
    }
}
