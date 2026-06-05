package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.quest.sound.QuestSoundPreview;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

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
        String target = state.modalQuestCompletionSoundTarget == null ? "" : state.modalQuestCompletionSoundTarget.trim();
        int volume = QuestDisplay.normalizeCompletionSoundVolume(state.soundVolumeDraft);
        state.soundVolumeDraft = volume;
        if (!state.modalQuestCompletionSoundTargets.isEmpty()) {
            EditorCommandClient.setQuestCompletionSoundVolume(player, state.modalQuestCompletionSoundTargets, volume);
            QuestSoundPreview.restartIfPlaying(soundId, volume);
            return;
        }
        if (!target.isBlank()) {
            EditorCommandClient.setQuestCompletionSoundVolume(player, target, volume);
        }
        QuestSoundPreview.restartIfPlaying(soundId, volume);
    }

}
