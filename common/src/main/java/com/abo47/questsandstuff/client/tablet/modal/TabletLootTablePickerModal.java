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

public final class TabletLootTablePickerModal {
    private TabletLootTablePickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        return ResourceListPickerModal.rebuild(modal, state, player, refresh, w, h,
                new ResourceListPickerModal.Options(
                        ModalWindowManager.ModalType.LOOT_TABLE_PICKER,
                        TabletTranslationKeys.text(QuestTranslationKeys.CHOOSE_LOOT_TABLE),
                        TabletTranslationKeys.text(QuestTranslationKeys.NO_LOOT_TABLES),
                        "loot table",
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
        Map<String, String> displays = ClientQuestStateFacade.lootTableDisplays();
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
        return DisplayNameFormatter.lootTable(lootTable, ClientQuestStateFacade.lootTableDisplays());
    }

}
