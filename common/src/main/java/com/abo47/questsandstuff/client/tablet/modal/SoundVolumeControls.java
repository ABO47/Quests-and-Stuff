package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.quest.sound.QuestSoundPreview;
import com.abo47.questsandstuff.client.tablet.controls.PercentSliderControls;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSetSlot;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

final class SoundVolumeControls {
    private SoundVolumeControls() {
    }

    static TextFieldWidget add(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, int x, int y, int width, String soundId) {
        return PercentSliderControls.add(
                parent,
                x,
                y,
                width,
                state.soundVolumeDraft,
                next -> {
                    state.soundVolumeDraft = QuestDisplay.normalizeCompletionSoundVolume(next);
                    refresh.run();
                },
                () -> {
                    commit(player, state, soundId);
                    refresh.run();
                },
                () -> state.soundVolumeDragging,
                dragging -> state.soundVolumeDragging = dragging,
                new Component[]{TabletVocabulary.component(QuestVocabulary.SOUND_LEVEL)}
        );
    }

    private static void commit(Player player, TabletUiState state, String soundId) {
        String target = ModalTargetState.target(state, TargetSlot.QUEST_COMPLETION_SOUND, state.modalQuestCompletionSoundTarget);
        Set<String> targets = ModalTargetState.targetSet(state, TargetSetSlot.QUEST_COMPLETION_SOUND, state.modalQuestCompletionSoundTargets);
        int volume = QuestDisplay.normalizeCompletionSoundVolume(state.soundVolumeDraft);
        state.soundVolumeDraft = volume;
        if (!targets.isEmpty()) {
            EditorCommandClient.setQuestCompletionSoundVolume(player, targets, volume);
            QuestSoundPreview.restartIfPlaying(soundId, volume);
            return;
        }
        if (!target.isBlank()) {
            EditorCommandClient.setQuestCompletionSoundVolume(player, target, volume);
        }
        QuestSoundPreview.restartIfPlaying(soundId, volume);
    }

}
