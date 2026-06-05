package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.DisplayNameFormatter;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class TabletStructurePickerModal {
    private TabletStructurePickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        return ResourceListPickerModal.rebuild(modal, state, player, refresh, w, h,
                new ResourceListPickerModal.Options(
                        TabletVocabulary.text(QuestVocabulary.CHOOSE_STRUCTURE),
                        TabletVocabulary.text(QuestVocabulary.NO_STRUCTURES),
                        "structure",
                        () -> state.structureSearch,
                        value -> state.structureSearch = value,
                        value -> state.structureScroll = value,
                        focused -> state.structureSearchFocused = focused,
                        ScrollState.bind(
                                () -> state.structureScroll,
                                value -> state.structureScroll = value,
                                () -> state.structureScrollDragging,
                                dragging -> state.structureScrollDragging = dragging
                        ),
                        TabletStructurePickerModal::structures,
                        TabletStructurePickerModal::displayName,
                        QuestDetailsWindow::applyStructurePick,
                        "minecraft:map",
                        24,
                        42,
                        178,
                        48,
                        96
                ));
    }

    private static List<String> structures(String query) {
        Set<String> found = new LinkedHashSet<>();
        if (Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().registryAccess().registry(Registries.STRUCTURE)
                    .ifPresent(registry -> registry.keySet().stream()
                            .map(Object::toString)
                            .forEach(found::add));
        }
        if (found.isEmpty()) {
            found.addAll(List.of(
                    "minecraft:village_desert",
                    "minecraft:village_plains",
                    "minecraft:village_savanna",
                    "minecraft:village_snowy",
                    "minecraft:village_taiga",
                    "minecraft:pillager_outpost",
                    "minecraft:mineshaft",
                    "minecraft:stronghold",
                    "minecraft:desert_pyramid",
                    "minecraft:jungle_pyramid",
                    "minecraft:woodland_mansion",
                    "minecraft:ocean_monument",
                    "minecraft:fortress",
                    "minecraft:bastion_remnant",
                    "minecraft:end_city",
                    "minecraft:ancient_city",
                    "minecraft:trail_ruins"
            ));
        }
        List<String> values = new ArrayList<>(found);
        values.sort(Comparator.naturalOrder());
        if (SearchFilter.normalize(query).isBlank()) {
            return values;
        }
        return values.stream()
                .filter(value -> SearchFilter.matches(query, value, displayName(value)))
                .toList();
    }

    private static String displayName(String structure) {
        return DisplayNameFormatter.resourceLeaf(structure);
    }
}
