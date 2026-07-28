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

public final class TabletBiomePickerModal {
    private TabletBiomePickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        return ResourceListPickerModal.rebuild(modal, state, player, refresh, w, h,
                new ResourceListPickerModal.Options(
                        ModalWindowManager.ModalType.BIOME_PICKER,
                        TabletTranslationKeys.text(QuestTranslationKeys.CHOOSE_BIOME),
                        TabletTranslationKeys.text(QuestTranslationKeys.NO_BIOMES),
                        "biome",
                        TabletBiomePickerModal::biomes,
                        TabletBiomePickerModal::displayName,
                        QuestDetailsWindow::applyBiomePick,
                        "",
                        8,
                        36,
                        150,
                        34,
                        80
                ));
    }

    private static List<String> biomes(String query) {
        Map<String, String> displays = ClientQuestStateFacade.biomeDisplays();
        List<String> values = new ArrayList<>(displays.isEmpty()
                ? List.of("minecraft:plains", "minecraft:forest", "minecraft:desert", "minecraft:taiga", "minecraft:swamp")
                : displays.keySet());
        values.sort(Comparator.naturalOrder());
        if (SearchFilter.normalize(query).isBlank()) {
            return values;
        }
        return values.stream().filter(value -> SearchFilter.matches(query, value, displayName(value))).toList();
    }

    private static String displayName(String biome) {
        return DisplayNameFormatter.biome(biome, ClientQuestStateFacade.biomeDisplays());
    }

}
