package com.abo47.questsandstuff.client.tablet.modal;

import com.abo47.questsandstuff.client.tablet.controls.SearchFilter;
import com.abo47.questsandstuff.client.tablet.controls.picker.PickerCache;
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
    private static final PickerCache<StatOwner, StatChoices, String, List<String>> CACHE = new PickerCache<>();

    private TabletStatPickerModal() {
    }

    public static void prewarm() {
        CACHE.source(owner(), TabletStatPickerModal::buildChoices);
    }

    public static TextFieldWidget rebuild(WidgetGroup modal, TabletUiState state, Player player, Runnable refresh, int w, int h) {
        return ResourceListPickerModal.rebuild(modal, state, player, refresh, w, h,
                new ResourceListPickerModal.Options(
                        ModalWindowManager.ModalType.STAT_PICKER,
                        TabletVocabulary.text(QuestVocabulary.CHOOSE_STAT),
                        TabletVocabulary.text(QuestVocabulary.NO_STATS),
                        "stat",
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
        return CACHE.query(owner(), normalizedQuery, TabletStatPickerModal::buildChoices, choices -> {
            if (normalizedQuery.isBlank()) {
                return choices.values();
            }
            String compactQuery = SearchFilter.normalizeKey(normalizedQuery);
            return choices.rows().stream()
                    .filter(choice -> choice.matches(normalizedQuery, compactQuery))
                    .map(StatChoice::value)
                    .toList();
        });
    }

    private static StatChoices choices() {
        return CACHE.source(owner(), TabletStatPickerModal::buildChoices);
    }

    private static StatChoices buildChoices() {
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
        return new StatChoices(rows, values, displayNames);
    }

    private static StatOwner owner() {
        return new StatOwner(
                RegistryFingerprint.of(BuiltInRegistries.CUSTOM_STAT.keySet()),
                RegistryFingerprint.of(BuiltInRegistries.BLOCK.keySet()),
                RegistryFingerprint.of(BuiltInRegistries.ITEM.keySet()),
                RegistryFingerprint.of(BuiltInRegistries.ENTITY_TYPE.keySet())
        );
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
        String displayName = choices().displayNames().get(stat);
        if (displayName != null) {
            return displayName;
        }
        return StatTargetFormatter.displayName(stat);
    }

    private record StatOwner(
            RegistryFingerprint customStats,
            RegistryFingerprint blocks,
            RegistryFingerprint items,
            RegistryFingerprint entities
    ) {
    }

    private record RegistryFingerprint(int size, int keyHash) {
        static RegistryFingerprint of(Set<ResourceLocation> keys) {
            return new RegistryFingerprint(keys.size(), keys.hashCode());
        }
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
            String displayName = StatTargetFormatter.displayName(value);
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
