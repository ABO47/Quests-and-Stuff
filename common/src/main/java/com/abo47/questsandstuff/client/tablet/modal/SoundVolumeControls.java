package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.sound.QuestSoundPreview;
import com.abo47.questsandstuff.client.tablet.controls.StyledTextFields;
import com.abo47.questsandstuff.client.tablet.editor.EditorCommandClient;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.quest.model.QuestDisplay;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

final class SoundVolumeControls {
    private static final int FIELD_W = 34;
    private static final int GAP = 6;

    private SoundVolumeControls() {
    }

    static TextFieldWidget add(WidgetGroup parent, TabletUiState state, Player player, Runnable refresh, int x, int y, int width, String soundId) {
        int sliderW = Math.max(24, width - FIELD_W - GAP);
        parent.addWidget(new SoundVolumeSliderWidget(
                x,
                y,
                sliderW,
                16,
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
                dragging -> state.soundVolumeDragging = dragging
        ));

        TextFieldWidget field = StyledTextFields.numberField(
                x + sliderW + GAP,
                y + 1,
                FIELD_W,
                14,
                state.soundVolumeDraft,
                0,
                100,
                3,
                value -> state.soundVolumeDraft = parseVolume(value, state.soundVolumeDraft),
                () -> {
                    commit(player, state, soundId);
                    refresh.run();
                },
                () -> refresh.run(),
                () -> {
                    commit(player, state, soundId);
                    refresh.run();
                }
        );
        StyledTextFields.applyStandardStyle(field, ModColors.SURFACE_BASE, ModColors.BORDER_BASE);
        field.setHoverTooltips(new Component[]{QuestVocabulary.component(QuestVocabulary.SOUND_LEVEL)});
        parent.addWidget(field);
        return field;
    }

    private static void commit(Player player, TabletUiState state, String soundId) {
        String target = state.modalQuestCompletionSoundTarget == null ? "" : state.modalQuestCompletionSoundTarget.trim();
        int volume = QuestDisplay.normalizeCompletionSoundVolume(state.soundVolumeDraft);
        state.soundVolumeDraft = volume;
        if (!target.isBlank()) {
            EditorCommandClient.setQuestCompletionSoundVolume(player, target, volume);
        }
        QuestSoundPreview.restartIfPlaying(soundId, volume);
    }

    private static int parseVolume(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return QuestDisplay.normalizeCompletionSoundVolume(fallback);
        }
        try {
            return QuestDisplay.normalizeCompletionSoundVolume(Integer.parseInt(value.trim()));
        } catch (NumberFormatException ignored) {
            return QuestDisplay.normalizeCompletionSoundVolume(fallback);
        }
    }
}
