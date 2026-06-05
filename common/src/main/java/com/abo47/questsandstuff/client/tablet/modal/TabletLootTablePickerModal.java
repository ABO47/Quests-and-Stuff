package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.sync.cache.ClientQuestCache;
import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class TabletLootTablePickerModal {
    private TabletLootTablePickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        return ResourceListPickerModal.rebuild(modal, state, player, refresh, w, h,
                new ResourceListPickerModal.Options(
                        TabletVocabulary.text(QuestVocabulary.CHOOSE_LOOT_TABLE),
                        TabletVocabulary.text(QuestVocabulary.NO_LOOT_TABLES),
                        "loot table",
                        () -> state.lootTableSearch,
                        value -> state.lootTableSearch = value,
                        value -> state.lootTableScroll = value,
                        focused -> state.lootTableSearchFocused = focused,
                        ScrollState.bind(
                                () -> state.lootTableScroll,
                                value -> state.lootTableScroll = value,
                                () -> state.lootTableScrollDragging,
                                dragging -> state.lootTableScrollDragging = dragging
                        ),
                        TabletLootTablePickerModal::lootTables,
                        TabletLootTablePickerModal::displayName,
                        QuestDetailsWindow::applyLootTablePick,
                        "minecraft:chest",
                        24,
                        32,
                        170,
                        38,
                        96
                ));
    }

    private static List<String> lootTables(String query) {
        Map<String, String> displays = ClientQuestCache.lootTableDisplays();
        List<String> values = new ArrayList<>(displays.isEmpty()
                ? List.of("minecraft:chests/simple_dungeon", "minecraft:chests/village/village_armorer", "minecraft:empty")
                : displays.keySet());
        values.sort(Comparator.naturalOrder());
        if (SearchFilter.normalize(query).isBlank()) {
            return values;
        }
        return values.stream()
                .filter(value -> SearchFilter.matches(query, value, displayName(value)))
                .toList();
    }

    private static String displayName(String lootTable) {
        return DisplayNameFormatter.lootTable(lootTable, ClientQuestCache.lootTableDisplays());
    }

}
