package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class TabletBiomePickerModal {
    private TabletBiomePickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        return ResourceListPickerModal.rebuild(modal, state, player, refresh, w, h,
                new ResourceListPickerModal.Options(
                        QuestVocabulary.text(QuestVocabulary.CHOOSE_BIOME),
                        QuestVocabulary.text(QuestVocabulary.NO_BIOMES),
                        "biome",
                        () -> state.biomeSearch,
                        value -> state.biomeSearch = value,
                        value -> state.biomeScroll = value,
                        focused -> state.biomeSearchFocused = focused,
                        ScrollState.bind(
                                () -> state.biomeScroll,
                                value -> state.biomeScroll = value,
                                () -> state.biomeScrollDragging,
                                dragging -> state.biomeScrollDragging = dragging
                        ),
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
        Map<String, String> displays = ClientQuestCache.biomeDisplays();
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
        return DisplayNameFormatter.biome(biome, ClientQuestCache.biomeDisplays());
    }

}
