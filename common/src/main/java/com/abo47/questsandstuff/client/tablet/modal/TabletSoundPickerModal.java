package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.QuestsAndStuffMod;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.controls.picker.PickerListPanel;
import com.abo47.questsandstuff.client.tablet.controls.picker.PickerCache;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSetSlot;
import com.abo47.questsandstuff.client.tablet.modal.ModalSession.TargetSlot;
import com.abo47.questsandstuff.client.tablet.quest.editor.EditorQuestCommandClient;
import com.abo47.questsandstuff.client.tablet.icons.DisplayIconWidget;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.theme.ModColors;
import com.abo47.questsandstuff.client.tablet.theme.Surfaces;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.abo47.questsandstuff.client.tablet.modal.ModalCloseActions.closeAll;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.flatHitButton;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.label;
import static com.abo47.questsandstuff.client.tablet.ui.TabletUiFactory.panel;
import static com.abo47.questsandstuff.client.tablet.theme.Surfaces.withAlpha;

public final class TabletSoundPickerModal {
    private static final int ROW_H = 16;
    private static final int HEADER_GAP = 3;
    private static final int HEADER_CLOSE_ANCHOR_RIGHT_PAD = 26;
    private static final int HEADER_CLOSE_RENDER_X_OFFSET = 1;

    private static final PickerCache<SoundOwner, List<SoundChoice>, String, List<SoundChoice>> CACHE = new PickerCache<>();

    private TabletSoundPickerModal() {
    }

    public static void prewarm() {
        CACHE.source(owner(), TabletSoundPickerModal::buildChoices);
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        ModalShell.addTitleAndClose(modal, TabletVocabulary.text(QuestVocabulary.CHOOSE_SOUND), w, state, refresh);
        ModalLibraryLayout.Metrics libraryLayout = ModalLibraryLayout.calculate(w, h);
        int rightX = libraryLayout.rightX();
        int rightW = libraryLayout.rightW();
        addPreviewPanel(modal, state, player, refresh, libraryLayout);
        int searchW = Math.max(40, headerCloseRenderX(w) - rightX - HEADER_GAP);
        TextFieldWidget search = ModalShell.addSearchField(modal, rightX, 2, searchW, 16, state.pickers.soundSearch, 120, value -> {
            String query = SearchFilter.normalizeUserInput(value);
            state.pickers.soundSearch = query;
            state.pickers.soundScroll = 0;
            QuestsAndStuffMod.debugLog("[QnS:UI] sound search query='{}'", query);
            refresh.run();
        }, focused -> state.pickers.soundSearchFocused = focused);

        int listX = rightX;
        int listY = libraryLayout.bodyY();
        int listW = rightW;
        int listH = libraryLayout.bodyH();
        List<SoundChoice> entries = sounds(state.pickers.soundSearch);
        PickerListPanel.add(modal, listX, listY, listW, listH, ROW_H, entries, TabletVocabulary.text(QuestVocabulary.NO_SOUNDS),
                ScrollState.bind(
                        () -> state.pickers.soundScroll,
                        value -> state.pickers.soundScroll = value,
                        () -> state.pickers.soundScrollDragging,
                        dragging -> state.pickers.soundScrollDragging = dragging
                ),
                3,
                refresh,
                (list, entry, index, rowY, rowW) -> renderRow(list, state, player, refresh, entry, rowY, rowW));
        return search;
    }

    private static void addPreviewPanel(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, ModalLibraryLayout.Metrics layout) {
        int previewW = layout.leftW();
        int previewH = layout.bodyH();
        WidgetGroup preview = panel(ModalLibraryLayout.PREVIEW_X, layout.bodyY(), previewW, previewH, withAlpha(ModColors.SURFACE_PANEL_ALT, 120), ModColors.BORDER_BASE);
        String selected = state.pickers.soundSelected == null ? "" : state.pickers.soundSelected.trim();
        SoundChoice choice = selected.isBlank() ? null : SoundChoice.of(selected);
        preview.addWidget(new DisplayIconWidget(8, 9, 14, 14, "audio-lines"));
        preview.addWidget(label(28, 12, choice == null ? TabletModalPanel.tr("ui.questsandstuff.sound.none_selected") : SearchFilter.crop(choice.name(), 19), ModColors.TEXT_SECONDARY));
        if (!selected.isBlank()) {
            int volumeY = Math.max(56, previewH - 24);
            int playY = 38;
            int playH = Math.max(34, volumeY - playY - 8);
            preview.addWidget(new SoundPreviewPlayerWidget(8, playY, previewW - 16, playH, selected, () -> state.pickers.soundVolumeDraft));
            SoundVolumeControls.add(preview, state, player, refresh, 8, volumeY, previewW - 16, selected);
        }
        modal.addWidget(preview);
    }

    private static int headerCloseRenderX(int modalW) {
        return modalW - HEADER_CLOSE_ANCHOR_RIGHT_PAD + HEADER_CLOSE_RENDER_X_OFFSET;
    }

    private static void renderRow(WidgetGroup list, TabletUiState state, Player player, Runnable refresh, SoundChoice entry, int rowY, int rowW) {
        boolean selected = entry.id().equals(state.pickers.soundSelected);
        if (selected) {
            WidgetGroup selectedFill = new WidgetGroup(4, rowY, rowW - 8, ROW_H);
            selectedFill.setBackground(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 64)));
            list.addWidget(selectedFill);
        }
        list.addWidget(new DisplayIconWidget(8, rowY + 1, 12, 12, "audio-lines"));
        list.addWidget(label(24, rowY + 4, SearchFilter.crop(entry.name(), Math.max(10, (rowW - 38) / 6)), ModColors.TEXT_PRIMARY));
        ButtonWidget hit = flatHitButton(4, rowY, rowW - 8, ROW_H, click -> {
            boolean doubleClick = click.button == 0 && TabletModalPanel.acceptPickerDoubleClick(state, ModalTargets.doubleClickKey("sound", entry.id()));
            state.pickers.soundSelected = entry.id();
            if (doubleClick) {
                applySound(state, player, entry.id());
                closeAll(state);
            }
            refresh.run();
        });
        hit.setHoverTexture(Surfaces.fill(withAlpha(ModColors.INTERACTIVE, 54)));
        hit.setHoverTooltips(PickerTooltips.nameOnly(entry.name()));
        list.addWidget(hit);
    }

    private static void applySound(TabletUiState state, Player player, String soundId) {
        Set<String> targets = ModalTargetState.targetSet(state, TargetSetSlot.QUEST_COMPLETION_SOUND, state.modal.modalQuestCompletionSoundTargets);
        if (!targets.isEmpty()) {
            EditorQuestCommandClient.setQuestCompletionSound(player, targets, soundId);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest batch completion sound picked quests={} sound={}", targets.size(), soundId);
            return;
        }
        String target = ModalTargetState.target(state, TargetSlot.QUEST_COMPLETION_SOUND, state.modal.modalQuestCompletionSoundTarget);
        if (!target.isBlank()) {
            EditorQuestCommandClient.setQuestCompletionSound(player, target, soundId);
            QuestsAndStuffMod.debugLog("[QnS:UI] quest completion sound picked quest={} sound={}", target, soundId);
        }
    }

    private static List<SoundChoice> sounds(String query) {
        String normalizedQuery = SearchFilter.normalize(query);
        return CACHE.query(owner(), normalizedQuery, TabletSoundPickerModal::buildChoices, choices -> normalizedQuery.isBlank()
                ? choices
                : choices.stream()
                .filter(choice -> choice.matches(normalizedQuery))
                .toList());
    }

    private static List<SoundChoice> buildChoices() {
        return BuiltInRegistries.SOUND_EVENT.stream()
                .map(BuiltInRegistries.SOUND_EVENT::getKey)
                .filter(id -> id != null)
                .map(ResourceLocation::toString)
                .distinct()
                .map(SoundChoice::of)
                .sorted(Comparator.comparing(SoundChoice::name, String.CASE_INSENSITIVE_ORDER).thenComparing(SoundChoice::id))
                .toList();
    }

    private static SoundOwner owner() {
        return new SoundOwner(RegistryFingerprint.of(BuiltInRegistries.SOUND_EVENT.keySet()));
    }

    private record SoundOwner(RegistryFingerprint sounds) {
    }

    private record RegistryFingerprint(int size, int keyHash) {
        static RegistryFingerprint of(Set<ResourceLocation> keys) {
            return new RegistryFingerprint(keys.size(), keys.hashCode());
        }
    }

    private record SoundChoice(String id, String name, String normalizedId, String normalizedName) {
        static SoundChoice of(String id) {
            String name = displayName(id);
            return new SoundChoice(id, name, SearchFilter.normalize(id), SearchFilter.normalize(name));
        }

        boolean matches(String query) {
            return normalizedId.contains(query) || normalizedName.contains(query);
        }
    }

    private static String displayName(String soundId) {
        String clean = soundId == null ? "" : soundId.trim();
        int colon = clean.indexOf(':');
        if (colon >= 0) {
            clean = clean.substring(colon + 1);
        }
        clean = clean.replace('.', ' ').replace('_', ' ').replace('-', ' ').replace('/', ' ');
        return DisplayNameFormatter.titleCase(clean);
    }
}
