package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.ScrollState;
import com.abo47.questsandstuff.client.tablet.quest.details.QuestDetailsWindow;
import com.abo47.questsandstuff.client.tablet.state.TabletUiState;
import com.abo47.questsandstuff.client.tablet.text.QuestVocabulary;
import com.abo47.questsandstuff.client.tablet.text.TabletVocabulary;
import com.abo47.questsandstuff.client.tablet.text.StatTargetFormatter;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TabletStatPickerModal {
    private static StatChoices cachedChoices;
    private static String cachedQuery = null;
    private static List<String> cachedValues = List.of();

    private TabletStatPickerModal() {
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        return ResourceListPickerModal.rebuild(modal, state, player, refresh, w, h,
                new ResourceListPickerModal.Options(
                        TabletVocabulary.text(QuestVocabulary.CHOOSE_STAT),
                        TabletVocabulary.text(QuestVocabulary.NO_STATS),
                        "stat",
                        () -> state.statSearch,
                        value -> state.statSearch = value,
                        value -> state.statScroll = value,
                        focused -> state.statSearchFocused = focused,
                        ScrollState.bind(
                                () -> state.statScroll,
                                value -> state.statScroll = value,
                                () -> state.statScrollDragging,
                                dragging -> state.statScrollDragging = dragging
                        ),
                        TabletStatPickerModal::stats,
                        TabletStatPickerModal::displayName,
                        QuestDetailsWindow::applyStatPick,
                        "stat",
                        24,
                        50,
                        192,
                        52,
                        120
                ));
    }

    private static List<String> stats(String query) {
        String normalizedQuery = SearchFilter.normalize(query);
        synchronized (TabletStatPickerModal.class) {
            StatChoices choices = choices();
            if (normalizedQuery.equals(cachedQuery)) {
                return cachedValues;
            }
            List<String> values;
            if (normalizedQuery.isBlank()) {
                values = choices.values();
            } else {
                String compactQuery = SearchFilter.normalizeKey(normalizedQuery);
                values = choices.rows().stream()
                        .filter(choice -> choice.matches(normalizedQuery, compactQuery))
                        .map(StatChoice::value)
                        .toList();
            }
            cachedQuery = normalizedQuery;
            cachedValues = values;
            return values;
        }
    }

    private static StatChoices choices() {
        if (cachedChoices != null) {
            return cachedChoices;
        }
        Set<String> found = new LinkedHashSet<>();
        BuiltInRegistries.CUSTOM_STAT.stream()
                .map(ResourceLocation::toString)
                .forEach(found::add);
        addBlockStats(found);
        addItemStats(found, "crafted");
        addItemStats(found, "used");
        addItemStats(found, "broken");
        addItemStats(found, "picked_up");
        addItemStats(found, "dropped");
        addEntityStats(found, "killed");
        addEntityStats(found, "killed_by");
        if (found.isEmpty()) {
            found.addAll(List.of(
                    "minecraft:jump",
                    "minecraft:walk_one_cm",
                    "mined:minecraft:stone",
                    "crafted:minecraft:crafting_table",
                    "used:minecraft:bow",
                    "killed:minecraft:zombie"
            ));
        }

        List<StatChoice> rows = found.stream()
                .map(StatChoice::of)
                .sorted(Comparator.comparing(StatChoice::displayName, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(StatChoice::value))
                .toList();
        List<String> values = rows.stream()
                .map(StatChoice::value)
                .toList();
        Map<String, String> displayNames = new HashMap<>();
        for (StatChoice row : rows) {
            displayNames.put(row.value(), row.displayName());
        }
        cachedChoices = new StatChoices(rows, values, displayNames);
        return cachedChoices;
    }

    private static void addBlockStats(Set<String> found) {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block == Blocks.AIR || block == Blocks.CAVE_AIR || block == Blocks.VOID_AIR) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (id != null) {
                found.add("mined:" + id);
            }
        }
    }

    private static void addItemStats(Set<String> found, String category) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (item == Items.AIR) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            if (id != null) {
                found.add(category + ":" + id);
            }
        }
    }

    private static void addEntityStats(Set<String> found, String category) {
        for (EntityType<?> entity : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(entity);
            if (id != null) {
                found.add(category + ":" + id);
            }
        }
    }

    private static String displayName(String stat) {
        synchronized (TabletStatPickerModal.class) {
            StatChoices choices = cachedChoices;
            if (choices != null) {
                String displayName = choices.displayNames().get(stat);
                if (displayName != null) {
                    return displayName;
                }
            }
        }
        return StatTargetFormatter.displayName(stat);
    }

    private record StatChoices(List<StatChoice> rows, List<String> values, Map<String, String> displayNames) {
    }

    private record StatChoice(
            String value,
            String displayName,
            String normalizedValue,
            String normalizedDisplayName,
            String compactValue,
            String compactDisplayName
    ) {
        static StatChoice of(String value) {
            String displayName = TabletStatPickerModal.displayName(value);
            String normalizedValue = SearchFilter.normalize(value);
            String normalizedDisplayName = SearchFilter.normalize(displayName);
            return new StatChoice(
                    value,
                    displayName,
                    normalizedValue,
                    normalizedDisplayName,
                    SearchFilter.normalizeKey(normalizedValue),
                    SearchFilter.normalizeKey(normalizedDisplayName)
            );
        }

        boolean matches(String query, String compactQuery) {
            return normalizedValue.contains(query)
                    || normalizedDisplayName.contains(query)
                    || (!compactQuery.isBlank()
                    && (compactValue.contains(compactQuery) || compactDisplayName.contains(compactQuery)));
        }
    }
}
