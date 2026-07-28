package com.abo47.questsandstuff.client.tablet.modal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import net.minecraft.world.entity.player.Player;

import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import com.abo47.questsandstuff.client.sync.state.ClientQuestStateFacade;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.TabletTranslationKeys;
import com.abo47.questsandstuff.client.tablet.text.format.DisplayNameFormatter;

public final class TabletAdvancementPickerModal {
    private TabletAdvancementPickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        return ResourceListPickerModal.rebuild(modal, state, player, refresh, w, h,
                new ResourceListPickerModal.Options(
                        ModalWindowManager.ModalType.ADVANCEMENT_PICKER,
                        TabletTranslationKeys.text(QuestTranslationKeys.CHOOSE_ADVANCEMENT),
                        TabletTranslationKeys.text(QuestTranslationKeys.NO_ADVANCEMENTS),
                        "advancement",
                        TabletAdvancementPickerModal::advancements,
                        TabletAdvancementPickerModal::displayName,
                        QuestDetailsWindow::applyAdvancementPick,
                        "minecraft:book",
                        24,
                        46,
                        180,
                        46,
                        96
                ));
    }

    private static List<String> advancements(String query) {
        Map<String, String> displays = ClientQuestStateFacade.advancementDisplays();
        List<String> values = new ArrayList<>(displays.isEmpty()
                ? List.of("minecraft:story/root", "minecraft:story/mine_stone", "minecraft:story/iron_tools", "minecraft:nether/root", "minecraft:end/root")
                : displays.keySet());
        values.sort(Comparator.naturalOrder());
        if (SearchFilter.normalize(query).isBlank()) {
            return values;
        }
        return values.stream()
                .filter(value -> SearchFilter.matches(query, value, displayName(value)))
                .toList();
    }

    private static String displayName(String advancement) {
        return DisplayNameFormatter.advancement(advancement, ClientQuestStateFacade.advancementDisplays());
    }
}
